"""
Event bus for the CloudStack Integration Framework.

Provides an in-process async event bus that routes events from CloudStack
(or internal sources) to subscribed plugins. Supports glob-style pattern
matching on event types (e.g. "VM.*", "NETWORK.CREATE.*").
"""

from __future__ import annotations

import asyncio
import fnmatch
import logging
import time
import uuid
from dataclasses import dataclass, field
from typing import Any, Callable, Coroutine, Dict, List, Optional, Set

logger = logging.getLogger("csintegration.events")


@dataclass
class Event:
    event_type: str
    payload: Dict[str, Any]
    source: str = "cloudstack"
    event_id: str = field(default_factory=lambda: str(uuid.uuid4()))
    timestamp: float = field(default_factory=time.time)
    metadata: Dict[str, Any] = field(default_factory=dict)

    def to_dict(self) -> Dict[str, Any]:
        return {
            "event_id": self.event_id,
            "event_type": self.event_type,
            "source": self.source,
            "timestamp": self.timestamp,
            "payload": self.payload,
            "metadata": self.metadata,
        }

    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> "Event":
        return cls(
            event_id=data.get("event_id", str(uuid.uuid4())),
            event_type=data["event_type"],
            source=data.get("source", "unknown"),
            timestamp=data.get("timestamp", time.time()),
            payload=data.get("payload", {}),
            metadata=data.get("metadata", {}),
        )


EventHandler = Callable[[Event], Coroutine[Any, Any, Optional[Dict[str, Any]]]]


class EventBus:
    """
    Asynchronous event bus with pattern-based subscriptions.

    Handlers are matched against event types using fnmatch glob patterns.
    Events are dispatched concurrently to all matching handlers.
    """

    def __init__(self, max_history: int = 1000) -> None:
        self._handlers: Dict[str, List[EventHandler]] = {}
        self._history: List[Event] = []
        self._max_history = max_history
        self._lock = asyncio.Lock()
        self._stats: Dict[str, int] = {
            "events_published": 0,
            "events_delivered": 0,
            "errors": 0,
        }

    def subscribe(self, pattern: str, handler: EventHandler) -> None:
        if pattern not in self._handlers:
            self._handlers[pattern] = []
        self._handlers[pattern].append(handler)
        logger.debug("Subscribed handler to pattern '%s'", pattern)

    def unsubscribe(self, pattern: str, handler: EventHandler) -> None:
        if pattern in self._handlers:
            self._handlers[pattern] = [
                h for h in self._handlers[pattern] if h is not handler
            ]
            if not self._handlers[pattern]:
                del self._handlers[pattern]

    def unsubscribe_all(self, handler: EventHandler) -> None:
        for pattern in list(self._handlers.keys()):
            self.unsubscribe(pattern, handler)

    async def publish(self, event: Event) -> List[Dict[str, Any]]:
        """
        Publish an event to all matching handlers.

        Returns a list of non-None responses from handlers.
        """
        self._stats["events_published"] += 1
        async with self._lock:
            self._history.append(event)
            if len(self._history) > self._max_history:
                self._history = self._history[-self._max_history:]

        matching_handlers = self._find_handlers(event.event_type)
        if not matching_handlers:
            logger.debug("No handlers for event type '%s'", event.event_type)
            return []

        results: List[Dict[str, Any]] = []
        tasks = [self._safe_dispatch(handler, event) for handler in matching_handlers]
        responses = await asyncio.gather(*tasks)
        for resp in responses:
            if resp is not None:
                results.append(resp)

        return results

    def _find_handlers(self, event_type: str) -> List[EventHandler]:
        matched: List[EventHandler] = []
        for pattern, handlers in self._handlers.items():
            if fnmatch.fnmatch(event_type, pattern):
                matched.extend(handlers)
        return matched

    async def _safe_dispatch(
        self, handler: EventHandler, event: Event
    ) -> Optional[Dict[str, Any]]:
        try:
            result = await handler(event)
            self._stats["events_delivered"] += 1
            return result
        except Exception:
            self._stats["errors"] += 1
            logger.exception(
                "Error dispatching event '%s' to handler %s",
                event.event_type,
                handler,
            )
            return None

    def get_history(
        self,
        event_type: Optional[str] = None,
        limit: int = 50,
    ) -> List[Dict[str, Any]]:
        events = self._history
        if event_type:
            events = [e for e in events if fnmatch.fnmatch(e.event_type, event_type)]
        return [e.to_dict() for e in events[-limit:]]

    @property
    def stats(self) -> Dict[str, int]:
        return dict(self._stats)

    @property
    def subscription_count(self) -> int:
        return sum(len(h) for h in self._handlers.values())

    @property
    def patterns(self) -> Set[str]:
        return set(self._handlers.keys())
