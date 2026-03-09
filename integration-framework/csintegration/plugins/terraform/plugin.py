"""
Terraform state synchronization plugin.

Tracks CloudStack resource lifecycle events and maintains a mapping that
can be consumed by Terraform's external data source or used to generate
Terraform import blocks. Also exposes an API for triggering Terraform
plan/apply against CloudStack-managed infrastructure.
"""

from __future__ import annotations

import json
import logging
import time
from typing import Any, Dict, List, Optional

from csintegration.plugins.base import (
    IntegrationPlugin,
    PluginHealth,
    PluginMetadata,
)

logger = logging.getLogger("csintegration.plugins.terraform")


class TerraformPlugin(IntegrationPlugin):
    """Synchronizes CloudStack resources with Terraform state awareness."""

    def __init__(self) -> None:
        super().__init__()
        self._resource_map: Dict[str, Dict[str, Any]] = {}
        self._last_sync: float = 0
        self.subscribe("VM.*")
        self.subscribe("NETWORK.*")
        self.subscribe("VOLUME.*")
        self.subscribe("VPC.*")

    def metadata(self) -> PluginMetadata:
        return PluginMetadata(
            name="terraform",
            version="1.0.0",
            description="Terraform state synchronization and resource mapping for CloudStack",
            author="CSIntegration",
            tags=["terraform", "iac", "infrastructure-as-code"],
            config_schema={
                "state_file": {"type": "string", "default": ""},
                "workspace": {"type": "string", "default": "default"},
                "auto_import": {"type": "boolean", "default": False},
                "resource_prefix": {"type": "string", "default": "cloudstack_"},
            },
        )

    async def on_start(self) -> None:
        self._resource_map = {}
        self._last_sync = time.time()
        logger.info("Terraform sync plugin started")

    async def on_stop(self) -> None:
        logger.info(
            "Terraform sync plugin stopped — tracked %d resources",
            len(self._resource_map),
        )

    async def handle_event(self, event: Dict[str, Any]) -> Optional[Dict[str, Any]]:
        event_type = event.get("event_type", "")
        payload = event.get("payload", {})
        entity_uuid = payload.get("entityuuid", payload.get("id", ""))

        if not entity_uuid:
            return None

        resource_type = self._event_to_resource_type(event_type)
        if not resource_type:
            return None

        if "CREATE" in event_type or "DEPLOY" in event_type:
            self._resource_map[entity_uuid] = {
                "type": resource_type,
                "cs_id": entity_uuid,
                "terraform_resource": f"{self.config.get('resource_prefix', 'cloudstack_')}{resource_type}",
                "created_at": time.time(),
                "last_event": event_type,
                "payload": payload,
            }
            logger.info("Tracking new resource: %s (%s)", entity_uuid, resource_type)

        elif "DESTROY" in event_type or "DELETE" in event_type:
            removed = self._resource_map.pop(entity_uuid, None)
            if removed:
                logger.info("Removed resource from tracking: %s", entity_uuid)

        elif entity_uuid in self._resource_map:
            self._resource_map[entity_uuid]["last_event"] = event_type
            self._resource_map[entity_uuid]["updated_at"] = time.time()

        self._last_sync = time.time()
        return {"tracked_resources": len(self._resource_map)}

    def _event_to_resource_type(self, event_type: str) -> str:
        mapping = {
            "VM": "instance",
            "NETWORK": "network",
            "VOLUME": "volume",
            "VPC": "vpc",
        }
        for prefix, tf_type in mapping.items():
            if event_type.startswith(prefix):
                return tf_type
        return ""

    async def health_check(self) -> PluginHealth:
        return PluginHealth(
            healthy=True,
            message=f"Tracking {len(self._resource_map)} resources",
            details={
                "tracked_resources": len(self._resource_map),
                "last_sync": self._last_sync,
            },
        )

    def api_routes(self) -> list:
        return [
            {
                "method": "GET",
                "path": "/resources",
                "handler": self._get_resources,
                "summary": "List tracked CloudStack resources for Terraform",
            },
            {
                "method": "GET",
                "path": "/import-blocks",
                "handler": self._generate_import_blocks,
                "summary": "Generate Terraform import blocks for tracked resources",
            },
        ]

    async def _get_resources(self) -> Dict[str, Any]:
        return {
            "resources": list(self._resource_map.values()),
            "count": len(self._resource_map),
            "last_sync": self._last_sync,
        }

    async def _generate_import_blocks(self) -> Dict[str, Any]:
        blocks = []
        for uuid, res in self._resource_map.items():
            tf_resource = res["terraform_resource"]
            block = f'import {{\n  to = {tf_resource}.{uuid[:8]}\n  id = "{uuid}"\n}}'
            blocks.append(block)
        return {
            "import_blocks": "\n\n".join(blocks),
            "count": len(blocks),
        }
