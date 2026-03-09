"""Tests for the event bus."""

import asyncio
import pytest

from csintegration.core.events import Event, EventBus


@pytest.fixture
def event_bus():
    return EventBus(max_history=100)


def make_event(event_type="VM.CREATE", payload=None):
    return Event(event_type=event_type, payload=payload or {"id": "test-123"})


@pytest.mark.asyncio
async def test_publish_with_no_handlers(event_bus):
    results = await event_bus.publish(make_event())
    assert results == []
    assert event_bus.stats["events_published"] == 1


@pytest.mark.asyncio
async def test_subscribe_and_receive(event_bus):
    received = []

    async def handler(event):
        received.append(event)
        return {"handled": True}

    event_bus.subscribe("VM.*", handler)
    results = await event_bus.publish(make_event("VM.CREATE"))

    assert len(received) == 1
    assert received[0].event_type == "VM.CREATE"
    assert results == [{"handled": True}]


@pytest.mark.asyncio
async def test_pattern_matching(event_bus):
    vm_events = []
    net_events = []

    async def vm_handler(event):
        vm_events.append(event)

    async def net_handler(event):
        net_events.append(event)

    event_bus.subscribe("VM.*", vm_handler)
    event_bus.subscribe("NETWORK.*", net_handler)

    await event_bus.publish(make_event("VM.CREATE"))
    await event_bus.publish(make_event("NETWORK.CREATE"))
    await event_bus.publish(make_event("VOLUME.CREATE"))

    assert len(vm_events) == 1
    assert len(net_events) == 1


@pytest.mark.asyncio
async def test_wildcard_subscription(event_bus):
    all_events = []

    async def handler(event):
        all_events.append(event)

    event_bus.subscribe("*", handler)

    await event_bus.publish(make_event("VM.CREATE"))
    await event_bus.publish(make_event("NETWORK.DELETE"))
    await event_bus.publish(make_event("CUSTOM.EVENT"))

    assert len(all_events) == 3


@pytest.mark.asyncio
async def test_unsubscribe(event_bus):
    received = []

    async def handler(event):
        received.append(event)

    event_bus.subscribe("VM.*", handler)
    await event_bus.publish(make_event("VM.CREATE"))
    assert len(received) == 1

    event_bus.unsubscribe("VM.*", handler)
    await event_bus.publish(make_event("VM.CREATE"))
    assert len(received) == 1


@pytest.mark.asyncio
async def test_handler_error_is_isolated(event_bus):
    good_results = []

    async def bad_handler(event):
        raise RuntimeError("boom")

    async def good_handler(event):
        good_results.append(event)
        return {"ok": True}

    event_bus.subscribe("VM.*", bad_handler)
    event_bus.subscribe("VM.*", good_handler)

    results = await event_bus.publish(make_event("VM.CREATE"))
    assert len(good_results) == 1
    assert results == [{"ok": True}]
    assert event_bus.stats["errors"] == 1


@pytest.mark.asyncio
async def test_event_history(event_bus):
    async def noop(event):
        pass

    event_bus.subscribe("*", noop)

    for i in range(5):
        await event_bus.publish(make_event(f"TEST.EVENT_{i}"))

    history = event_bus.get_history()
    assert len(history) == 5
    assert history[0]["event_type"] == "TEST.EVENT_0"

    filtered = event_bus.get_history(event_type="TEST.EVENT_2")
    assert len(filtered) == 1


@pytest.mark.asyncio
async def test_history_limit(event_bus):
    bus = EventBus(max_history=3)
    for i in range(10):
        await bus.publish(make_event(f"E.{i}"))

    history = bus.get_history()
    assert len(history) == 3


def test_event_serialization():
    event = Event(event_type="VM.CREATE", payload={"id": "123"}, source="test")
    d = event.to_dict()
    restored = Event.from_dict(d)
    assert restored.event_type == event.event_type
    assert restored.payload == event.payload
    assert restored.source == event.source


def test_subscription_count(event_bus):
    async def h1(e):
        pass

    async def h2(e):
        pass

    event_bus.subscribe("A.*", h1)
    event_bus.subscribe("B.*", h2)
    assert event_bus.subscription_count == 2
