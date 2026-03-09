"""Tests for the plugin registry."""

import pytest

from csintegration.core.registry import PluginRegistry
from csintegration.plugins.base import IntegrationPlugin, PluginMetadata


class MockPlugin(IntegrationPlugin):
    def metadata(self):
        return PluginMetadata(
            name="mock-plugin",
            version="0.1.0",
            description="A mock plugin for testing",
        )

    async def on_start(self):
        pass

    async def on_stop(self):
        pass

    async def handle_event(self, event):
        return {"mock": True}


class AnotherPlugin(IntegrationPlugin):
    def metadata(self):
        return PluginMetadata(
            name="another-plugin",
            version="0.2.0",
            description="Another mock plugin",
        )

    async def on_start(self):
        pass

    async def on_stop(self):
        pass

    async def handle_event(self, event):
        return None


@pytest.fixture
def registry():
    return PluginRegistry()


def test_register_plugin(registry):
    registry.register(MockPlugin)
    assert "mock-plugin" in registry.plugin_names
    meta = registry.get_metadata("mock-plugin")
    assert meta is not None
    assert meta.version == "0.1.0"


def test_register_multiple(registry):
    registry.register(MockPlugin)
    registry.register(AnotherPlugin)
    assert len(registry.plugin_names) == 2


def test_unregister(registry):
    registry.register(MockPlugin)
    registry.unregister("mock-plugin")
    assert "mock-plugin" not in registry.plugin_names
    assert registry.get_class("mock-plugin") is None


def test_get_class(registry):
    registry.register(MockPlugin)
    cls = registry.get_class("mock-plugin")
    assert cls is MockPlugin


def test_list_plugins(registry):
    registry.register(MockPlugin)
    registry.register(AnotherPlugin)
    plugins = registry.list_plugins()
    assert len(plugins) == 2
    names = {p.name for p in plugins}
    assert names == {"mock-plugin", "another-plugin"}


def test_discover_builtin(registry):
    count = registry.discover_builtin()
    assert count >= 0


def test_get_nonexistent(registry):
    assert registry.get_class("nonexistent") is None
    assert registry.get_metadata("nonexistent") is None
