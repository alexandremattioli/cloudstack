"""Tests for the REST API endpoints."""

import pytest
from httpx import ASGITransport, AsyncClient

from csintegration.api.app import create_app


@pytest.fixture
def app():
    return create_app(config={"api_key": "", "plugins": {}})


@pytest.fixture
async def client(app):
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as c:
        yield c


@pytest.mark.asyncio
async def test_health_endpoint(client):
    resp = await client.get("/health")
    assert resp.status_code == 200
    data = resp.json()
    assert "framework_running" in data


@pytest.mark.asyncio
async def test_status_endpoint(client):
    resp = await client.get("/status")
    assert resp.status_code == 200
    data = resp.json()
    assert "running" in data


@pytest.mark.asyncio
async def test_list_plugins(client):
    resp = await client.get("/plugins/")
    assert resp.status_code == 200
    data = resp.json()
    assert "registered" in data
    assert "active" in data


@pytest.mark.asyncio
async def test_event_stats(client):
    resp = await client.get("/events/stats")
    assert resp.status_code == 200
    data = resp.json()
    assert "stats" in data


@pytest.mark.asyncio
async def test_event_history(client):
    resp = await client.get("/events/history")
    assert resp.status_code == 200
    data = resp.json()
    assert "events" in data


@pytest.mark.asyncio
async def test_publish_event(client):
    resp = await client.post(
        "/events/publish",
        json={
            "event_type": "TEST.API",
            "payload": {"test": True},
        },
    )
    assert resp.status_code == 200
    data = resp.json()
    assert data["status"] == "ok"


@pytest.mark.asyncio
async def test_list_webhooks(client):
    resp = await client.get("/integrations/webhooks")
    assert resp.status_code == 200
    data = resp.json()
    assert "webhooks" in data


@pytest.mark.asyncio
async def test_add_webhook(client):
    resp = await client.post(
        "/integrations/webhooks",
        json={
            "name": "test-hook",
            "url": "http://example.com/hook",
            "event_filter": "VM.*",
        },
    )
    assert resp.status_code == 200
    assert resp.json()["status"] == "ok"


@pytest.mark.asyncio
async def test_cloudstack_proxy_not_configured(client):
    resp = await client.post(
        "/integrations/cloudstack/proxy",
        json={"command": "listZones"},
    )
    assert resp.status_code == 503


@pytest.mark.asyncio
async def test_enable_unknown_plugin(client):
    resp = await client.post(
        "/plugins/nonexistent/enable",
        json={},
    )
    assert resp.status_code == 404


@pytest.mark.asyncio
async def test_api_key_auth():
    app = create_app(config={"api_key": "secret-key-123"})
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as c:
        resp = await c.get("/status")
        assert resp.status_code == 401

        resp = await c.get("/status", headers={"X-API-Key": "secret-key-123"})
        assert resp.status_code == 200

        resp = await c.get("/health")
        assert resp.status_code == 200
