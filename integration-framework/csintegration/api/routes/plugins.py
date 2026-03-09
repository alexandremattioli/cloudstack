"""Plugin management endpoints."""

from __future__ import annotations

from typing import Any, Dict, List, Optional

from fastapi import APIRouter, HTTPException, Request
from pydantic import BaseModel

router = APIRouter(prefix="/plugins", tags=["Plugins"])


class PluginActionRequest(BaseModel):
    config: Optional[Dict[str, Any]] = None
    auto_start: bool = True


@router.get("/")
async def list_plugins(request: Request) -> Dict[str, Any]:
    """List all registered plugins and their status."""
    manager = request.app.state.manager
    registered = [
        {
            "name": m.name,
            "version": m.version,
            "description": m.description,
            "author": m.author,
            "tags": m.tags,
        }
        for m in manager.registry.list_plugins()
    ]
    active = manager.lifecycle.list_instances()
    return {"registered": registered, "active": active}


@router.post("/{plugin_name}/enable")
async def enable_plugin(
    plugin_name: str, body: PluginActionRequest, request: Request
) -> Dict[str, Any]:
    """Instantiate and start a plugin."""
    manager = request.app.state.manager
    try:
        result = await manager.add_plugin(
            plugin_name, config=body.config, auto_start=body.auto_start
        )
        return {"status": "ok", "plugin": result}
    except KeyError as exc:
        raise HTTPException(status_code=404, detail=str(exc))
    except ValueError as exc:
        raise HTTPException(status_code=409, detail=str(exc))
    except Exception as exc:
        raise HTTPException(status_code=500, detail=str(exc))


@router.post("/{plugin_name}/disable")
async def disable_plugin(plugin_name: str, request: Request) -> Dict[str, Any]:
    """Stop and unload a plugin."""
    manager = request.app.state.manager
    try:
        await manager.remove_plugin(plugin_name)
        return {"status": "ok", "plugin": plugin_name}
    except KeyError as exc:
        raise HTTPException(status_code=404, detail=str(exc))
    except Exception as exc:
        raise HTTPException(status_code=500, detail=str(exc))


@router.post("/{plugin_name}/restart")
async def restart_plugin(plugin_name: str, request: Request) -> Dict[str, Any]:
    """Restart a running plugin."""
    manager = request.app.state.manager
    try:
        await manager.lifecycle.restart(plugin_name)
        inst = manager.lifecycle.instances.get(plugin_name)
        return {"status": "ok", "plugin": inst.to_dict() if inst else plugin_name}
    except KeyError as exc:
        raise HTTPException(status_code=404, detail=str(exc))
    except Exception as exc:
        raise HTTPException(status_code=500, detail=str(exc))


@router.get("/{plugin_name}/health")
async def plugin_health(plugin_name: str, request: Request) -> Dict[str, Any]:
    """Check health of a specific plugin."""
    manager = request.app.state.manager
    try:
        health = await manager.lifecycle.health_check(plugin_name)
        return {
            "plugin": plugin_name,
            "healthy": health.healthy,
            "message": health.message,
            "details": health.details,
        }
    except KeyError as exc:
        raise HTTPException(status_code=404, detail=str(exc))
