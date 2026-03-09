"""
Plugin lifecycle manager.

Handles instantiation, configuration, starting, stopping, and health-checking
of plugin instances. Ensures plugins transition through valid states and
wires up event subscriptions with the event bus.
"""

from __future__ import annotations

import asyncio
import logging
from typing import Any, Dict, List, Optional

from csintegration.core.events import Event, EventBus
from csintegration.core.registry import PluginRegistry
from csintegration.plugins.base import (
    IntegrationPlugin,
    PluginHealth,
    PluginState,
)

logger = logging.getLogger("csintegration.lifecycle")


class PluginInstance:
    """Wrapper around a live plugin providing management metadata."""

    def __init__(self, plugin: IntegrationPlugin, config: Dict[str, Any]) -> None:
        self.plugin = plugin
        self.config = config
        self.error: Optional[str] = None

    @property
    def name(self) -> str:
        return self.plugin.metadata().name

    @property
    def state(self) -> PluginState:
        return self.plugin.state

    def to_dict(self) -> Dict[str, Any]:
        meta = self.plugin.metadata()
        return {
            "name": meta.name,
            "version": meta.version,
            "description": meta.description,
            "state": self.plugin.state.value,
            "subscriptions": list(self.plugin.subscriptions),
            "error": self.error,
        }


class LifecycleManager:
    """Manages the full lifecycle of plugin instances."""

    def __init__(self, registry: PluginRegistry, event_bus: EventBus) -> None:
        self._registry = registry
        self._event_bus = event_bus
        self._instances: Dict[str, PluginInstance] = {}

    @property
    def instances(self) -> Dict[str, PluginInstance]:
        return dict(self._instances)

    async def instantiate(
        self, plugin_name: str, config: Optional[Dict[str, Any]] = None
    ) -> PluginInstance:
        """Create and load a plugin instance."""
        if plugin_name in self._instances:
            raise ValueError(f"Plugin '{plugin_name}' is already instantiated")

        cls = self._registry.get_class(plugin_name)
        if cls is None:
            raise KeyError(f"Unknown plugin: {plugin_name}")

        plugin = cls()
        plugin.config = config or {}
        inst = PluginInstance(plugin, plugin.config)

        try:
            await plugin.on_load()
            plugin.state = PluginState.LOADED
        except Exception as exc:
            plugin.state = PluginState.ERROR
            inst.error = str(exc)
            logger.exception("Failed to load plugin '%s'", plugin_name)
            raise

        self._instances[plugin_name] = inst
        logger.info("Instantiated plugin: %s", plugin_name)
        return inst

    async def start(self, plugin_name: str) -> None:
        inst = self._get_instance(plugin_name)
        plugin = inst.plugin

        if plugin.state not in (PluginState.LOADED, PluginState.STOPPED):
            raise RuntimeError(
                f"Cannot start plugin in state '{plugin.state.value}'"
            )

        plugin.state = PluginState.STARTING
        try:
            await plugin.on_start()
            plugin.state = PluginState.RUNNING
            self._wire_subscriptions(plugin)
            logger.info("Started plugin: %s", plugin_name)
        except Exception as exc:
            plugin.state = PluginState.ERROR
            inst.error = str(exc)
            logger.exception("Failed to start plugin '%s'", plugin_name)
            raise

    async def stop(self, plugin_name: str) -> None:
        inst = self._get_instance(plugin_name)
        plugin = inst.plugin

        if plugin.state != PluginState.RUNNING:
            raise RuntimeError(
                f"Cannot stop plugin in state '{plugin.state.value}'"
            )

        plugin.state = PluginState.STOPPING
        self._unwire_subscriptions(plugin)
        try:
            await plugin.on_stop()
            plugin.state = PluginState.STOPPED
            logger.info("Stopped plugin: %s", plugin_name)
        except Exception as exc:
            plugin.state = PluginState.ERROR
            inst.error = str(exc)
            logger.exception("Error stopping plugin '%s'", plugin_name)
            raise

    async def unload(self, plugin_name: str) -> None:
        inst = self._get_instance(plugin_name)
        plugin = inst.plugin

        if plugin.state == PluginState.RUNNING:
            await self.stop(plugin_name)

        try:
            await plugin.on_unload()
        except Exception:
            logger.exception("Error during unload of '%s'", plugin_name)

        plugin.state = PluginState.UNLOADED
        del self._instances[plugin_name]
        logger.info("Unloaded plugin: %s", plugin_name)

    async def restart(self, plugin_name: str) -> None:
        inst = self._get_instance(plugin_name)
        if inst.plugin.state == PluginState.RUNNING:
            await self.stop(plugin_name)
        await self.start(plugin_name)

    async def health_check(self, plugin_name: str) -> PluginHealth:
        inst = self._get_instance(plugin_name)
        return await inst.plugin.health_check()

    async def health_check_all(self) -> Dict[str, Dict[str, Any]]:
        results: Dict[str, Dict[str, Any]] = {}
        for name, inst in self._instances.items():
            try:
                health = await inst.plugin.health_check()
                results[name] = {
                    "healthy": health.healthy,
                    "message": health.message,
                    "details": health.details,
                    "state": inst.plugin.state.value,
                }
            except Exception as exc:
                results[name] = {
                    "healthy": False,
                    "message": str(exc),
                    "state": inst.plugin.state.value,
                }
        return results

    async def start_all(self, configs: Optional[Dict[str, Dict[str, Any]]] = None) -> None:
        """Instantiate and start all registered plugins."""
        configs = configs or {}
        for name in self._registry.plugin_names:
            if name in self._instances:
                if self._instances[name].state == PluginState.RUNNING:
                    continue
            try:
                if name not in self._instances:
                    await self.instantiate(name, configs.get(name, {}))
                await self.start(name)
            except Exception:
                logger.exception("Failed to start plugin '%s'", name)

    async def stop_all(self) -> None:
        for name in list(self._instances.keys()):
            try:
                if self._instances[name].state == PluginState.RUNNING:
                    await self.stop(name)
            except Exception:
                logger.exception("Failed to stop plugin '%s'", name)

    async def unload_all(self) -> None:
        await self.stop_all()
        for name in list(self._instances.keys()):
            try:
                await self.unload(name)
            except Exception:
                logger.exception("Failed to unload plugin '%s'", name)

    def list_instances(self) -> List[Dict[str, Any]]:
        return [inst.to_dict() for inst in self._instances.values()]

    def _get_instance(self, name: str) -> PluginInstance:
        if name not in self._instances:
            raise KeyError(f"No instance for plugin: {name}")
        return self._instances[name]

    def _wire_subscriptions(self, plugin: IntegrationPlugin) -> None:
        async def _handler(event: Event) -> Optional[Dict[str, Any]]:
            return await plugin.handle_event(event.to_dict())

        plugin._event_handler_ref = _handler  # type: ignore[attr-defined]
        for pattern in plugin.subscriptions:
            self._event_bus.subscribe(pattern, _handler)

    def _unwire_subscriptions(self, plugin: IntegrationPlugin) -> None:
        handler = getattr(plugin, "_event_handler_ref", None)
        if handler:
            self._event_bus.unsubscribe_all(handler)
