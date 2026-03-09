"""
Integration Manager — top-level orchestrator for the framework.

Ties together the plugin registry, lifecycle manager, event bus, and the
CloudStack bridge into a single cohesive service that can be started,
stopped, and queried.
"""

from __future__ import annotations

import asyncio
import logging
import time
from typing import Any, Dict, List, Optional

from csintegration.core.events import Event, EventBus
from csintegration.core.lifecycle import LifecycleManager
from csintegration.core.registry import PluginRegistry

logger = logging.getLogger("csintegration.manager")


class IntegrationManager:
    """
    Central coordinator for the CloudStack Integration Framework.

    Provides a unified interface for:
      - Plugin discovery, registration, and lifecycle management
      - Event publishing and subscription
      - CloudStack bridge management
      - Health and status reporting
    """

    def __init__(self, config: Dict[str, Any] | None = None) -> None:
        self.config = config or {}
        self.registry = PluginRegistry()
        self.event_bus = EventBus(
            max_history=self.config.get("event_history_size", 1000)
        )
        self.lifecycle = LifecycleManager(self.registry, self.event_bus)
        self._started = False
        self._start_time: Optional[float] = None
        self._bridge_task: Optional[asyncio.Task] = None

    @property
    def is_running(self) -> bool:
        return self._started

    @property
    def uptime(self) -> float:
        if self._start_time is None:
            return 0.0
        return time.time() - self._start_time

    async def initialize(self) -> None:
        """Discover plugins and prepare the framework."""
        self.registry.discover_builtin()
        self.registry.discover_entrypoints()

        extra_paths = self.config.get("plugin_paths", [])
        for path in extra_paths:
            self.registry.discover_path(path)

        logger.info(
            "Initialized with %d registered plugins",
            len(self.registry.plugin_names),
        )

    async def start(self) -> None:
        """Start the framework and all configured plugins."""
        if self._started:
            logger.warning("Integration manager is already running")
            return

        await self.initialize()

        plugin_configs = self.config.get("plugins", {})
        await self.lifecycle.start_all(plugin_configs)

        self._started = True
        self._start_time = time.time()

        await self.event_bus.publish(
            Event(event_type="FRAMEWORK.STARTED", payload={"plugins": self.registry.plugin_names})
        )

        logger.info("Integration framework started")

    async def stop(self) -> None:
        """Stop the framework and all running plugins."""
        if not self._started:
            return

        await self.event_bus.publish(
            Event(event_type="FRAMEWORK.STOPPING", payload={})
        )

        if self._bridge_task and not self._bridge_task.done():
            self._bridge_task.cancel()
            try:
                await self._bridge_task
            except asyncio.CancelledError:
                pass

        await self.lifecycle.unload_all()
        self._started = False
        self._start_time = None
        logger.info("Integration framework stopped")

    async def publish_event(
        self,
        event_type: str,
        payload: Dict[str, Any],
        source: str = "external",
        metadata: Optional[Dict[str, Any]] = None,
    ) -> List[Dict[str, Any]]:
        """Publish an event into the framework event bus."""
        event = Event(
            event_type=event_type,
            payload=payload,
            source=source,
            metadata=metadata or {},
        )
        return await self.event_bus.publish(event)

    async def add_plugin(
        self,
        plugin_name: str,
        config: Optional[Dict[str, Any]] = None,
        auto_start: bool = True,
    ) -> Dict[str, Any]:
        """Add and optionally start a plugin at runtime."""
        inst = await self.lifecycle.instantiate(plugin_name, config)
        if auto_start:
            await self.lifecycle.start(plugin_name)
        return inst.to_dict()

    async def remove_plugin(self, plugin_name: str) -> None:
        """Stop and remove a plugin at runtime."""
        await self.lifecycle.unload(plugin_name)

    def status(self) -> Dict[str, Any]:
        return {
            "running": self._started,
            "uptime_seconds": round(self.uptime, 2),
            "registered_plugins": self.registry.plugin_names,
            "active_instances": self.lifecycle.list_instances(),
            "event_bus": {
                "subscriptions": self.event_bus.subscription_count,
                "patterns": list(self.event_bus.patterns),
                "stats": self.event_bus.stats,
            },
        }

    async def health(self) -> Dict[str, Any]:
        plugin_health = await self.lifecycle.health_check_all()
        all_healthy = all(h.get("healthy", False) for h in plugin_health.values())
        return {
            "framework_healthy": self._started and all_healthy,
            "framework_running": self._started,
            "uptime_seconds": round(self.uptime, 2),
            "plugins": plugin_health,
        }
