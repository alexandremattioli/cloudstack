"""Tests for the integration manager."""

import pytest

from csintegration.core.manager import IntegrationManager


@pytest.fixture
def manager():
    return IntegrationManager(config={"plugins": {}})


@pytest.mark.asyncio
async def test_manager_start_stop(manager):
    await manager.start()
    assert manager.is_running
    assert manager.uptime > 0

    status = manager.status()
    assert status["running"] is True
    assert isinstance(status["registered_plugins"], list)

    await manager.stop()
    assert not manager.is_running


@pytest.mark.asyncio
async def test_manager_health(manager):
    await manager.start()
    health = await manager.health()
    assert "framework_healthy" in health
    assert "framework_running" in health
    assert health["framework_running"] is True
    await manager.stop()


@pytest.mark.asyncio
async def test_publish_event(manager):
    await manager.start()
    results = await manager.publish_event(
        event_type="CUSTOM.TEST",
        payload={"key": "value"},
        source="test",
    )
    assert isinstance(results, list)
    await manager.stop()


@pytest.mark.asyncio
async def test_event_history(manager):
    await manager.start()
    await manager.publish_event("TEST.1", {"a": 1})
    await manager.publish_event("TEST.2", {"b": 2})

    history = manager.event_bus.get_history()
    event_types = [e["event_type"] for e in history]
    assert "TEST.1" in event_types
    assert "TEST.2" in event_types
    await manager.stop()


@pytest.mark.asyncio
async def test_double_start(manager):
    await manager.start()
    await manager.start()
    assert manager.is_running
    await manager.stop()


@pytest.mark.asyncio
async def test_stop_when_not_started(manager):
    await manager.stop()
    assert not manager.is_running
