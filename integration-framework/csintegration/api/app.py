"""
FastAPI application for the CloudStack Integration Framework.

Mounts all route modules and middleware, initialises the integration
manager, and exposes plugin-defined custom routes dynamically.
"""

from __future__ import annotations

import logging
from contextlib import asynccontextmanager
from typing import Any, Dict, Optional

from fastapi import APIRouter, FastAPI
from fastapi.middleware.cors import CORSMiddleware

from csintegration.api.middleware import APIKeyAuthMiddleware, RequestLoggingMiddleware
from csintegration.api.routes import events, health, integrations, plugins
from csintegration.bridge.event_listener import WebhookEventReceiver
from csintegration.bridge.webhook import WebhookDispatcher
from csintegration.core.manager import IntegrationManager

logger = logging.getLogger("csintegration.api")


def create_app(config: Dict[str, Any] | None = None) -> FastAPI:
    """Build and return the fully-configured FastAPI application."""
    config = config or {}

    manager = IntegrationManager(config)
    webhook_dispatcher = WebhookDispatcher()
    webhook_receiver = WebhookEventReceiver(manager.event_bus)

    cs_client = _build_cs_client(config.get("cloudstack", {}))

    @asynccontextmanager
    async def lifespan(app: FastAPI):
        await manager.start()
        await webhook_receiver.start()

        if cs_client:
            _setup_polling_listener(config, manager)

        _mount_plugin_routes(app, manager)

        yield

        await manager.stop()
        await webhook_receiver.stop()
        await webhook_dispatcher.close()
        if cs_client:
            await cs_client.close()

    app = FastAPI(
        title="CloudStack Integration Framework",
        description=(
            "A plugin-based integration framework that runs alongside "
            "Apache CloudStack, enabling seamless integration of external "
            "platforms, services, and custom features."
        ),
        version="1.0.0",
        lifespan=lifespan,
    )

    app.state.manager = manager
    app.state.webhook_dispatcher = webhook_dispatcher
    app.state.webhook_receiver = webhook_receiver
    app.state.cs_client = cs_client

    app.add_middleware(RequestLoggingMiddleware)
    app.add_middleware(
        APIKeyAuthMiddleware,
        api_key=config.get("api_key", ""),
    )
    app.add_middleware(
        CORSMiddleware,
        allow_origins=config.get("cors_origins", ["*"]),
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    app.include_router(health.router)
    app.include_router(plugins.router)
    app.include_router(events.router)
    app.include_router(integrations.router)

    return app


def _build_cs_client(cs_config: Dict[str, Any]):
    endpoint = cs_config.get("endpoint", "")
    api_key = cs_config.get("api_key", "")
    secret_key = cs_config.get("secret_key", "")

    if not all([endpoint, api_key, secret_key]):
        logger.warning(
            "CloudStack client not configured — proxy and polling disabled"
        )
        return None

    from csintegration.bridge.cloudstack_client import CloudStackClient

    return CloudStackClient(
        endpoint=endpoint,
        api_key=api_key,
        secret_key=secret_key,
        verify_ssl=cs_config.get("verify_ssl", True),
        timeout=cs_config.get("timeout", 60.0),
    )


def _setup_polling_listener(config: Dict[str, Any], manager: IntegrationManager):
    listener_config = config.get("event_listener", {})
    if listener_config.get("type") == "polling":
        from csintegration.bridge.event_listener import PollingEventListener

        import asyncio

        listener = PollingEventListener(
            event_bus=manager.event_bus,
            cs_client=manager.lifecycle,  # type: ignore
            poll_interval=listener_config.get("poll_interval", 10.0),
        )
        asyncio.create_task(listener.start())


def _mount_plugin_routes(app: FastAPI, manager: IntegrationManager):
    """Mount custom API routes declared by plugins."""
    for name, inst in manager.lifecycle.instances.items():
        routes = inst.plugin.api_routes()
        if not routes:
            continue

        plugin_router = APIRouter(
            prefix=f"/plugins/{name}",
            tags=[f"Plugin: {name}"],
        )
        for route_def in routes:
            method = route_def.get("method", "GET").upper()
            path = route_def.get("path", "/")
            handler = route_def["handler"]
            summary = route_def.get("summary", "")
            if method == "GET":
                plugin_router.add_api_route(path, handler, methods=["GET"], summary=summary)
            elif method == "POST":
                plugin_router.add_api_route(path, handler, methods=["POST"], summary=summary)
            elif method == "PUT":
                plugin_router.add_api_route(path, handler, methods=["PUT"], summary=summary)
            elif method == "DELETE":
                plugin_router.add_api_route(path, handler, methods=["DELETE"], summary=summary)

        app.include_router(plugin_router)
        logger.info("Mounted %d custom routes for plugin '%s'", len(routes), name)
