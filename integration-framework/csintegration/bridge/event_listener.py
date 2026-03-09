"""
Event listeners that bridge CloudStack events into the framework event bus.

Supports multiple transport modes:
  - Webhook: Receives events via HTTP POST from CloudStack's webhook event bus plugin
  - Polling: Polls CloudStack's listEvents API periodically
  - Kafka: Consumes from a Kafka topic (CloudStack kafka event bus plugin)
  - RabbitMQ: Consumes from a RabbitMQ exchange (CloudStack rabbitmq event bus plugin)
"""

from __future__ import annotations

import asyncio
import json
import logging
import time
from typing import Any, Callable, Coroutine, Dict, Optional

from csintegration.bridge.cloudstack_client import CloudStackClient
from csintegration.core.events import Event, EventBus

logger = logging.getLogger("csintegration.bridge.listener")


class EventListenerBase:
    """Base class for CloudStack event listeners."""

    def __init__(self, event_bus: EventBus) -> None:
        self.event_bus = event_bus
        self._running = False

    def _normalize_event(self, raw: Dict[str, Any]) -> Event:
        event_type = raw.get("event", raw.get("event_type", raw.get("eventType", "UNKNOWN")))
        event_type = event_type.replace(".", "_").replace("-", "_").upper()
        if "." not in event_type:
            parts = event_type.split("_", 1)
            if len(parts) == 2:
                event_type = f"{parts[0]}.{parts[1]}"
            else:
                event_type = f"CS.{event_type}"

        return Event(
            event_type=event_type,
            payload=raw,
            source="cloudstack",
            metadata={
                "original_event_type": raw.get("event", ""),
                "entity_uuid": raw.get("entityuuid", ""),
                "status": raw.get("status", ""),
            },
        )

    async def start(self) -> None:
        self._running = True

    async def stop(self) -> None:
        self._running = False


class PollingEventListener(EventListenerBase):
    """
    Polls CloudStack's listEvents API at regular intervals and publishes
    new events to the framework event bus.
    """

    def __init__(
        self,
        event_bus: EventBus,
        cs_client: CloudStackClient,
        poll_interval: float = 10.0,
    ) -> None:
        super().__init__(event_bus)
        self.cs_client = cs_client
        self.poll_interval = poll_interval
        self._last_event_id: Optional[str] = None
        self._task: Optional[asyncio.Task] = None

    async def start(self) -> None:
        await super().start()
        self._task = asyncio.create_task(self._poll_loop())
        logger.info("Polling event listener started (interval=%.1fs)", self.poll_interval)

    async def stop(self) -> None:
        await super().stop()
        if self._task and not self._task.done():
            self._task.cancel()
            try:
                await self._task
            except asyncio.CancelledError:
                pass
        logger.info("Polling event listener stopped")

    async def _poll_loop(self) -> None:
        while self._running:
            try:
                await self._poll_events()
            except asyncio.CancelledError:
                break
            except Exception:
                logger.exception("Error polling CloudStack events")
            await asyncio.sleep(self.poll_interval)

    async def _poll_events(self) -> None:
        params: Dict[str, Any] = {"listall": "true", "pagesize": "100"}
        if self._last_event_id:
            params["startid"] = self._last_event_id

        try:
            result = await self.cs_client.request("listEvents", **params)
        except Exception:
            logger.debug("Failed to poll events from CloudStack")
            return

        events = result.get("event", [])
        for raw_event in events:
            event_id = raw_event.get("id", "")
            if event_id == self._last_event_id:
                continue
            event = self._normalize_event(raw_event)
            await self.event_bus.publish(event)
            self._last_event_id = event_id


class WebhookEventReceiver(EventListenerBase):
    """
    Receives CloudStack events via webhook HTTP POST.

    This doesn't run its own HTTP server — it provides a handler
    that the FastAPI app mounts at a configured path.
    """

    def __init__(self, event_bus: EventBus) -> None:
        super().__init__(event_bus)

    async def handle_webhook(self, body: Dict[str, Any]) -> Dict[str, Any]:
        """Process an incoming webhook payload from CloudStack."""
        if not self._running:
            return {"status": "listener_not_running"}

        if isinstance(body, list):
            events_processed = 0
            for item in body:
                event = self._normalize_event(item)
                await self.event_bus.publish(event)
                events_processed += 1
            return {"status": "ok", "events_processed": events_processed}
        else:
            event = self._normalize_event(body)
            await self.event_bus.publish(event)
            return {"status": "ok", "event_id": event.event_id}


class KafkaEventListener(EventListenerBase):
    """
    Consumes events from a Kafka topic (requires aiokafka).

    Configure CloudStack to publish events to Kafka using the
    kafka event bus plugin, then point this listener at the same
    topic/broker.
    """

    def __init__(
        self,
        event_bus: EventBus,
        bootstrap_servers: str = "localhost:9092",
        topic: str = "cloudstack-events",
        group_id: str = "csintegration",
    ) -> None:
        super().__init__(event_bus)
        self.bootstrap_servers = bootstrap_servers
        self.topic = topic
        self.group_id = group_id
        self._consumer = None
        self._task: Optional[asyncio.Task] = None

    async def start(self) -> None:
        await super().start()
        try:
            from aiokafka import AIOKafkaConsumer

            self._consumer = AIOKafkaConsumer(
                self.topic,
                bootstrap_servers=self.bootstrap_servers,
                group_id=self.group_id,
                value_deserializer=lambda m: json.loads(m.decode("utf-8")),
            )
            await self._consumer.start()
            self._task = asyncio.create_task(self._consume_loop())
            logger.info("Kafka event listener started on topic '%s'", self.topic)
        except ImportError:
            logger.error("aiokafka is not installed — Kafka listener unavailable")
            self._running = False

    async def stop(self) -> None:
        await super().stop()
        if self._task and not self._task.done():
            self._task.cancel()
            try:
                await self._task
            except asyncio.CancelledError:
                pass
        if self._consumer:
            await self._consumer.stop()
        logger.info("Kafka event listener stopped")

    async def _consume_loop(self) -> None:
        while self._running and self._consumer:
            try:
                async for msg in self._consumer:
                    if not self._running:
                        break
                    event = self._normalize_event(msg.value)
                    await self.event_bus.publish(event)
            except asyncio.CancelledError:
                break
            except Exception:
                logger.exception("Error consuming Kafka message")
                await asyncio.sleep(5)


class RabbitMQEventListener(EventListenerBase):
    """
    Consumes events from RabbitMQ (requires aio-pika).

    Configure CloudStack to publish events to RabbitMQ using the
    rabbitmq event bus plugin.
    """

    def __init__(
        self,
        event_bus: EventBus,
        amqp_url: str = "amqp://guest:guest@localhost/",
        exchange: str = "cloudstack-events",
        queue: str = "csintegration",
    ) -> None:
        super().__init__(event_bus)
        self.amqp_url = amqp_url
        self.exchange_name = exchange
        self.queue_name = queue
        self._connection = None
        self._channel = None

    async def start(self) -> None:
        await super().start()
        try:
            import aio_pika

            self._connection = await aio_pika.connect_robust(self.amqp_url)
            self._channel = await self._connection.channel()
            exchange = await self._channel.declare_exchange(
                self.exchange_name, aio_pika.ExchangeType.FANOUT, durable=True
            )
            queue = await self._channel.declare_queue(self.queue_name, durable=True)
            await queue.bind(exchange)
            await queue.consume(self._on_message)
            logger.info("RabbitMQ event listener started on exchange '%s'", self.exchange_name)
        except ImportError:
            logger.error("aio-pika is not installed — RabbitMQ listener unavailable")
            self._running = False

    async def stop(self) -> None:
        await super().stop()
        if self._connection and not self._connection.is_closed:
            await self._connection.close()
        logger.info("RabbitMQ event listener stopped")

    async def _on_message(self, message) -> None:
        async with message.process():
            try:
                body = json.loads(message.body.decode("utf-8"))
                event = self._normalize_event(body)
                await self.event_bus.publish(event)
            except Exception:
                logger.exception("Error processing RabbitMQ message")
