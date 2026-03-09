"""Health and status endpoints."""

from __future__ import annotations

from typing import Any, Dict

from fastapi import APIRouter, Request

router = APIRouter(tags=["Health"])


@router.get("/health")
async def health_check(request: Request) -> Dict[str, Any]:
    manager = request.app.state.manager
    return await manager.health()


@router.get("/status")
async def framework_status(request: Request) -> Dict[str, Any]:
    manager = request.app.state.manager
    return manager.status()
