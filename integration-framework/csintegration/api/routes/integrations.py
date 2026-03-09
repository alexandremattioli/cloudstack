"""
Integration management endpoints.

Provides CRUD operations for webhook targets and CloudStack proxy calls.
"""

from __future__ import annotations

from typing import Any, Dict, List, Optional

from fastapi import APIRouter, HTTPException, Request
from pydantic import BaseModel

router = APIRouter(prefix="/integrations", tags=["Integrations"])


class WebhookTargetRequest(BaseModel):
    name: str
    url: str
    secret: str = ""
    headers: Dict[str, str] = {}
    event_filter: str = "*"
    max_retries: int = 3
    timeout: float = 30.0
    enabled: bool = True


class CloudStackProxyRequest(BaseModel):
    command: str
    params: Dict[str, Any] = {}


@router.get("/webhooks")
async def list_webhooks(request: Request) -> Dict[str, Any]:
    """List all configured webhook targets."""
    dispatcher = request.app.state.webhook_dispatcher
    return {"webhooks": dispatcher.list_targets()}


@router.post("/webhooks")
async def add_webhook(body: WebhookTargetRequest, request: Request) -> Dict[str, Any]:
    """Add a new webhook target."""
    from csintegration.bridge.webhook import WebhookTarget

    target = WebhookTarget(
        url=body.url,
        name=body.name,
        secret=body.secret,
        headers=body.headers,
        event_filter=body.event_filter,
        max_retries=body.max_retries,
        timeout=body.timeout,
        enabled=body.enabled,
    )
    dispatcher = request.app.state.webhook_dispatcher
    dispatcher.add_target(target)
    return {"status": "ok", "webhook": body.name}


@router.delete("/webhooks/{name}")
async def remove_webhook(name: str, request: Request) -> Dict[str, Any]:
    """Remove a webhook target."""
    dispatcher = request.app.state.webhook_dispatcher
    dispatcher.remove_target(name)
    return {"status": "ok", "removed": name}


@router.post("/cloudstack/proxy")
async def cloudstack_proxy(
    body: CloudStackProxyRequest, request: Request
) -> Dict[str, Any]:
    """
    Proxy a command to the CloudStack API.

    Useful for plugins and external systems that need to call CloudStack
    through the framework rather than directly.
    """
    cs_client = request.app.state.cs_client
    if cs_client is None:
        raise HTTPException(
            status_code=503,
            detail="CloudStack client is not configured",
        )
    try:
        result = await cs_client.request(body.command, **body.params)
        return {"status": "ok", "result": result}
    except Exception as exc:
        raise HTTPException(status_code=502, detail=str(exc))


@router.post("/cloudstack/events")
async def receive_cloudstack_webhook(request: Request) -> Dict[str, Any]:
    """
    Receive CloudStack webhook events.

    Point CloudStack's webhook event bus plugin at this endpoint.
    """
    body = await request.json()
    receiver = request.app.state.webhook_receiver
    if receiver is None:
        raise HTTPException(
            status_code=503,
            detail="Webhook receiver is not configured",
        )
    return await receiver.handle_webhook(body)
