"""Event publishing and history endpoints."""

from __future__ import annotations

from typing import Any, Dict, List, Optional

from fastapi import APIRouter, Request
from pydantic import BaseModel

router = APIRouter(prefix="/events", tags=["Events"])


class PublishEventRequest(BaseModel):
    event_type: str
    payload: Dict[str, Any]
    source: str = "api"
    metadata: Optional[Dict[str, Any]] = None


@router.post("/publish")
async def publish_event(body: PublishEventRequest, request: Request) -> Dict[str, Any]:
    """Publish a custom event into the framework event bus."""
    manager = request.app.state.manager
    results = await manager.publish_event(
        event_type=body.event_type,
        payload=body.payload,
        source=body.source,
        metadata=body.metadata,
    )
    return {"status": "ok", "responses": results}


@router.get("/history")
async def event_history(
    request: Request,
    event_type: Optional[str] = None,
    limit: int = 50,
) -> Dict[str, Any]:
    """Retrieve recent event history."""
    manager = request.app.state.manager
    history = manager.event_bus.get_history(event_type=event_type, limit=limit)
    return {"events": history, "count": len(history)}


@router.get("/stats")
async def event_stats(request: Request) -> Dict[str, Any]:
    """Get event bus statistics."""
    manager = request.app.state.manager
    return {
        "stats": manager.event_bus.stats,
        "subscription_count": manager.event_bus.subscription_count,
        "patterns": list(manager.event_bus.patterns),
    }
