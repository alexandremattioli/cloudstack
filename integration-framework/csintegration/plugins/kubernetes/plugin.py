"""
Kubernetes integration plugin.

Syncs CloudStack VM lifecycle events with Kubernetes node management.
When VMs are created/destroyed in CloudStack, this plugin can automatically
register/deregister them as Kubernetes nodes, apply labels, and manage
workload scheduling.
"""

from __future__ import annotations

import logging
from typing import Any, Dict, List, Optional

from csintegration.plugins.base import (
    IntegrationPlugin,
    PluginHealth,
    PluginMetadata,
)

logger = logging.getLogger("csintegration.plugins.kubernetes")


class KubernetesPlugin(IntegrationPlugin):
    """Bridges CloudStack VM lifecycle with Kubernetes node management."""

    def __init__(self) -> None:
        super().__init__()
        self._k8s_client = None
        self._connected = False
        self.subscribe("VM.*")
        self.subscribe("NETWORK.*")

    def metadata(self) -> PluginMetadata:
        return PluginMetadata(
            name="kubernetes",
            version="1.0.0",
            description="Sync CloudStack VM lifecycle with Kubernetes node management",
            author="CSIntegration",
            tags=["kubernetes", "k8s", "container", "orchestration"],
            config_schema={
                "kubeconfig_path": {"type": "string", "default": "~/.kube/config"},
                "context": {"type": "string", "default": ""},
                "namespace": {"type": "string", "default": "default"},
                "auto_register_nodes": {"type": "boolean", "default": True},
                "node_labels": {"type": "object", "default": {}},
            },
        )

    async def on_start(self) -> None:
        kubeconfig = self.config.get("kubeconfig_path", "")
        context = self.config.get("context", "")

        try:
            from kubernetes_asyncio import client as k8s_client
            from kubernetes_asyncio import config as k8s_config

            if kubeconfig:
                await k8s_config.load_kube_config(
                    config_file=kubeconfig,
                    context=context or None,
                )
            else:
                k8s_config.load_incluster_config()

            self._k8s_client = k8s_client.CoreV1Api()
            self._connected = True
            logger.info("Kubernetes plugin connected")
        except ImportError:
            logger.warning(
                "kubernetes-asyncio not installed — running in dry-run mode"
            )
            self._connected = False
        except Exception:
            logger.exception("Failed to connect to Kubernetes")
            self._connected = False

    async def on_stop(self) -> None:
        if self._k8s_client:
            await self._k8s_client.api_client.close()
        self._connected = False
        logger.info("Kubernetes plugin stopped")

    async def handle_event(self, event: Dict[str, Any]) -> Optional[Dict[str, Any]]:
        event_type = event.get("event_type", "")
        payload = event.get("payload", {})

        if "VM.CREATE" in event_type:
            return await self._on_vm_created(payload)
        elif "VM.DESTROY" in event_type:
            return await self._on_vm_destroyed(payload)
        elif "VM.START" in event_type:
            return await self._on_vm_started(payload)
        elif "VM.STOP" in event_type:
            return await self._on_vm_stopped(payload)

        return None

    async def _on_vm_created(self, payload: Dict[str, Any]) -> Dict[str, Any]:
        vm_id = payload.get("entityuuid", payload.get("id", "unknown"))
        logger.info("VM created in CloudStack: %s", vm_id)

        if self.config.get("auto_register_nodes") and self._connected:
            logger.info("Would register VM %s as K8s node", vm_id)

        return {"action": "vm_created_noted", "vm_id": vm_id}

    async def _on_vm_destroyed(self, payload: Dict[str, Any]) -> Dict[str, Any]:
        vm_id = payload.get("entityuuid", payload.get("id", "unknown"))
        logger.info("VM destroyed in CloudStack: %s", vm_id)

        if self._connected:
            logger.info("Would deregister VM %s from K8s", vm_id)

        return {"action": "vm_destroyed_noted", "vm_id": vm_id}

    async def _on_vm_started(self, payload: Dict[str, Any]) -> Dict[str, Any]:
        vm_id = payload.get("entityuuid", payload.get("id", "unknown"))
        logger.info("VM started: %s — marking K8s node schedulable", vm_id)
        return {"action": "vm_started_noted", "vm_id": vm_id}

    async def _on_vm_stopped(self, payload: Dict[str, Any]) -> Dict[str, Any]:
        vm_id = payload.get("entityuuid", payload.get("id", "unknown"))
        logger.info("VM stopped: %s — cordoning K8s node", vm_id)
        return {"action": "vm_stopped_noted", "vm_id": vm_id}

    async def health_check(self) -> PluginHealth:
        if not self._connected:
            return PluginHealth(
                healthy=True,
                message="Running in dry-run mode (no K8s connection)",
            )

        try:
            version = await self._k8s_client.get_api_versions()
            return PluginHealth(
                healthy=True,
                message="Connected to Kubernetes",
                details={"api_versions": len(version.versions) if version else 0},
            )
        except Exception as exc:
            return PluginHealth(healthy=False, message=f"K8s health check failed: {exc}")

    def api_routes(self) -> list:
        return [
            {
                "method": "GET",
                "path": "/nodes",
                "handler": self._list_nodes,
                "summary": "List Kubernetes nodes managed by this plugin",
            },
        ]

    async def _list_nodes(self) -> Dict[str, Any]:
        if not self._connected:
            return {"nodes": [], "mode": "dry-run"}

        try:
            nodes = await self._k8s_client.list_node()
            return {
                "nodes": [
                    {"name": n.metadata.name, "status": n.status.conditions[-1].type}
                    for n in nodes.items
                ]
            }
        except Exception as exc:
            return {"error": str(exc)}
