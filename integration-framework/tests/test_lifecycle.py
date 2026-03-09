"""Tests for the plugin lifecycle manager."""

import pytest

from csintegration.core.events import EventBus
from csintegration.core.lifecycle import LifecycleManager
from csintegration.core.registry import PluginRegistry
from csintegration.plugins.base import (
    IntegrationPlugin,
    PluginHealth,
    PluginMetadata,
    PluginState,
)


class SamplePlugin(IntegrationPlugin):
    started = False
    stopped = False
    loaded = False

    def metadata(self):
        return PluginMetadata(name="test-plugin", version="1.0.0", description="Test")

    async def on_load(self):
        self.__class__.loaded = True

    async def on_start(self):
        self.__class__.started = True
        self.subscribe("TEST.*")

    async def on_stop(self):
        self.__class__.stopped = True

    async def handle_event(self, event):
        return {"test_handled": True}

    async def health_check(self):
        return PluginHealth(healthy=True, message="OK")


class FailingPlugin(IntegrationPlugin):
    def metadata(self):
        return PluginMetadata(name="failing-plugin", version="1.0.0", description="Fails")

    async def on_start(self):
        raise RuntimeError("Startup failure")

    async def on_stop(self):
        pass

    async def handle_event(self, event):
        return None


@pytest.fixture
def setup():
    SamplePlugin.started = False
    SamplePlugin.stopped = False
    SamplePlugin.loaded = False
    registry = PluginRegistry()
    event_bus = EventBus()
    registry.register(SamplePlugin)
    registry.register(FailingPlugin)
    lifecycle = LifecycleManager(registry, event_bus)
    return registry, event_bus, lifecycle


@pytest.mark.asyncio
async def test_instantiate(setup):
    _, _, lifecycle = setup
    inst = await lifecycle.instantiate("test-plugin")
    assert inst.name == "test-plugin"
    assert inst.state == PluginState.LOADED
    assert SamplePlugin.loaded


@pytest.mark.asyncio
async def test_start_and_stop(setup):
    _, _, lifecycle = setup
    await lifecycle.instantiate("test-plugin")
    await lifecycle.start("test-plugin")
    assert SamplePlugin.started

    instances = lifecycle.list_instances()
    assert len(instances) == 1
    assert instances[0]["state"] == "running"

    await lifecycle.stop("test-plugin")
    assert SamplePlugin.stopped


@pytest.mark.asyncio
async def test_unload(setup):
    _, _, lifecycle = setup
    await lifecycle.instantiate("test-plugin")
    await lifecycle.start("test-plugin")
    await lifecycle.unload("test-plugin")
    assert "test-plugin" not in lifecycle.instances


@pytest.mark.asyncio
async def test_failing_plugin_start(setup):
    _, _, lifecycle = setup
    await lifecycle.instantiate("failing-plugin")
    with pytest.raises(RuntimeError, match="Startup failure"):
        await lifecycle.start("failing-plugin")

    inst = lifecycle.instances["failing-plugin"]
    assert inst.state == PluginState.ERROR


@pytest.mark.asyncio
async def test_health_check(setup):
    _, _, lifecycle = setup
    await lifecycle.instantiate("test-plugin")
    await lifecycle.start("test-plugin")
    health = await lifecycle.health_check("test-plugin")
    assert health.healthy


@pytest.mark.asyncio
async def test_health_check_all(setup):
    _, _, lifecycle = setup
    await lifecycle.instantiate("test-plugin")
    await lifecycle.start("test-plugin")
    results = await lifecycle.health_check_all()
    assert "test-plugin" in results
    assert results["test-plugin"]["healthy"]


@pytest.mark.asyncio
async def test_duplicate_instantiate(setup):
    _, _, lifecycle = setup
    await lifecycle.instantiate("test-plugin")
    with pytest.raises(ValueError, match="already instantiated"):
        await lifecycle.instantiate("test-plugin")


@pytest.mark.asyncio
async def test_unknown_plugin(setup):
    _, _, lifecycle = setup
    with pytest.raises(KeyError, match="Unknown plugin"):
        await lifecycle.instantiate("nonexistent")


@pytest.mark.asyncio
async def test_restart(setup):
    _, _, lifecycle = setup
    await lifecycle.instantiate("test-plugin")
    await lifecycle.start("test-plugin")
    await lifecycle.restart("test-plugin")
    assert lifecycle.instances["test-plugin"].state == PluginState.RUNNING


@pytest.mark.asyncio
async def test_event_delivery_to_plugin(setup):
    _, event_bus, lifecycle = setup
    await lifecycle.instantiate("test-plugin")
    await lifecycle.start("test-plugin")

    from csintegration.core.events import Event
    results = await event_bus.publish(
        Event(event_type="TEST.SOMETHING", payload={"data": "hello"})
    )
    assert any(r.get("test_handled") for r in results)


@pytest.mark.asyncio
async def test_start_all(setup):
    registry, _, lifecycle = setup
    await lifecycle.start_all()
    test_inst = lifecycle.instances.get("test-plugin")
    assert test_inst is not None
    assert test_inst.state == PluginState.RUNNING
    failing_inst = lifecycle.instances.get("failing-plugin")
    assert failing_inst is not None
    assert failing_inst.state == PluginState.ERROR
