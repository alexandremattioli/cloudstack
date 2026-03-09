"""
Webhook relay plugin.

Forwards CloudStack events to external systems via configurable
webhook endpoints. Supports event filtering, payload transformation,
and delivery tracking.
"""

from __future__ import annotations

import logging
from typing import Any, Dict, List, Optional

from csintegration.plugins.base import (
    IntegrationPlugin,
    PluginHealth,
    PluginMetadata,
)

logger = logging.getLogger("csintegration.plugins.webhook_relay")


class WebhookRelayPlugin(IntegrationPlugin):
    """
    Relays CloudStack events to external webhook endpoints.

    This plugin works with the framework's WebhookDispatcher to forward
    events to registered targets. Configure targets via the REST API
    or through plugin config.
    """

    def __init__(self) -> None:
        super().__init__()
        self._events_relayed: int = 0
        self._relay_targets: List[Dict[str, Any]] = []
        self.subscribe("*")

    def metadata(self) -> PluginMetadata:
        return PluginMetadata(
            name="webhook-relay",
            version="1.0.0",
            description="Relay CloudStack events to external webhook endpoints",
            author="CSIntegration",
            tags=["webhook", "relay", "notifications", "integration"],
            config_schema={
                "targets": {
                    "type": "array",
                    "items": {
                        "type": "object",
                        "properties": {
                            "url": {"type": "string"},
                            "name": {"type": "string"},
                            "event_filter": {"type": "string", "default": "*"},
                            "secret": {"type": "string", "default": ""},
                        },
                    },
                    "default": [],
                },
                "include_raw_payload": {"type": "boolean", "default": True},
            },
        )

    async def on_start(self) -> None:
        self._events_relayed = 0
        self._relay_targets = self.config.get("targets", [])
        logger.info(
            "Webhook relay started with %d targets", len(self._relay_targets)
        )

    async def on_stop(self) -> None:
        logger.info("Webhook relay stopped — relayed %d events", self._events_relayed)

    async def handle_event(self, event: Dict[str, Any]) -> Optional[Dict[str, Any]]:
        self._events_relayed += 1
        logger.debug(
            "Event queued for relay: %s", event.get("event_type", "unknown")
        )
        return {"relayed": True, "relay_count": self._events_relayed}

    async def health_check(self) -> PluginHealth:
        return PluginHealth(
            healthy=True,
            message=f"Relayed {self._events_relayed} events to {len(self._relay_targets)} targets",
            details={
                "events_relayed": self._events_relayed,
                "targets": len(self._relay_targets),
            },
        )

    def api_routes(self) -> list:
        return [
            {
                "method": "GET",
                "path": "/stats",
                "handler": self._get_stats,
                "summary": "Webhook relay statistics",
            },
        ]

    async def _get_stats(self) -> Dict[str, Any]:
        return {
            "events_relayed": self._events_relayed,
            "targets": self._relay_targets,
        }
