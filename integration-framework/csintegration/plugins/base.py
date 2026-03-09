"""
Base plugin interface for the CloudStack Integration Framework.

All integration plugins must inherit from IntegrationPlugin and implement
the required lifecycle methods. Plugins declare which CloudStack events
they subscribe to and receive them through the handle_event callback.
"""

from __future__ import annotations

import abc
import enum
from dataclasses import dataclass, field
from typing import Any, Dict, List, Optional, Set


class PluginState(str, enum.Enum):
    UNLOADED = "unloaded"
    LOADED = "loaded"
    STARTING = "starting"
    RUNNING = "running"
    STOPPING = "stopping"
    STOPPED = "stopped"
    ERROR = "error"


@dataclass
class PluginMetadata:
    name: str
    version: str
    description: str
    author: str = ""
    url: str = ""
    tags: List[str] = field(default_factory=list)
    dependencies: List[str] = field(default_factory=list)
    config_schema: Dict[str, Any] = field(default_factory=dict)


@dataclass
class PluginHealth:
    healthy: bool
    message: str = ""
    details: Dict[str, Any] = field(default_factory=dict)


class IntegrationPlugin(abc.ABC):
    """
    Abstract base class for all integration plugins.

    Lifecycle:
        load() -> start() -> [handle_event() ...] -> stop() -> unload()

    Plugins declare subscribed event patterns (e.g. "VM.*", "NETWORK.CREATE")
    and receive matching CloudStack events via handle_event().
    """

    def __init__(self) -> None:
        self._state: PluginState = PluginState.UNLOADED
        self._config: Dict[str, Any] = {}
        self._subscriptions: Set[str] = set()

    @property
    def state(self) -> PluginState:
        return self._state

    @state.setter
    def state(self, value: PluginState) -> None:
        self._state = value

    @property
    def config(self) -> Dict[str, Any]:
        return self._config

    @config.setter
    def config(self, value: Dict[str, Any]) -> None:
        self._config = value

    @property
    def subscriptions(self) -> Set[str]:
        return self._subscriptions

    # ── Abstract methods every plugin must implement ──

    @abc.abstractmethod
    def metadata(self) -> PluginMetadata:
        """Return plugin metadata describing this integration."""
        ...

    @abc.abstractmethod
    async def on_start(self) -> None:
        """Called when the plugin is started. Initialize connections/resources here."""
        ...

    @abc.abstractmethod
    async def on_stop(self) -> None:
        """Called when the plugin is stopped. Clean up resources here."""
        ...

    @abc.abstractmethod
    async def handle_event(self, event: Dict[str, Any]) -> Optional[Dict[str, Any]]:
        """
        Handle a CloudStack event or framework event.

        Args:
            event: Event dict with at least 'event_type' and 'payload' keys.

        Returns:
            Optional response dict, or None if the event is fire-and-forget.
        """
        ...

    # ── Optional hooks ──

    async def on_load(self) -> None:
        """Called when the plugin is first loaded into the registry."""
        pass

    async def on_unload(self) -> None:
        """Called when the plugin is removed from the registry."""
        pass

    async def health_check(self) -> PluginHealth:
        """Return the current health status of this plugin."""
        return PluginHealth(healthy=self._state == PluginState.RUNNING)

    def subscribe(self, *patterns: str) -> None:
        """Subscribe to one or more event patterns (supports fnmatch globs)."""
        self._subscriptions.update(patterns)

    def unsubscribe(self, *patterns: str) -> None:
        """Unsubscribe from event patterns."""
        self._subscriptions.difference_update(patterns)

    # ── Custom API routes ──

    def api_routes(self) -> List[Dict[str, Any]]:
        """
        Return custom API route definitions this plugin wants to expose.

        Each dict should have: method, path, handler, summary.
        Example:
            [{"method": "GET", "path": "/status", "handler": self.get_status, "summary": "Plugin status"}]
        """
        return []
