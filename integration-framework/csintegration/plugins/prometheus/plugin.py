"""
Prometheus metrics exporter plugin.

Collects CloudStack events and resource metrics, then exposes them in
Prometheus exposition format at a plugin-specific endpoint. This
complements CloudStack's built-in Prometheus plugin by adding
integration-framework-level observability.
"""

from __future__ import annotations

import logging
import time
from collections import defaultdict
from typing import Any, Dict, List, Optional

from csintegration.plugins.base import (
    IntegrationPlugin,
    PluginHealth,
    PluginMetadata,
)

logger = logging.getLogger("csintegration.plugins.prometheus")


class PrometheusPlugin(IntegrationPlugin):
    """Exports CloudStack and framework events as Prometheus metrics."""

    def __init__(self) -> None:
        super().__init__()
        self._event_counters: Dict[str, int] = defaultdict(int)
        self._resource_gauges: Dict[str, int] = defaultdict(int)
        self._start_time: float = 0
        self._total_events: int = 0
        self.subscribe("*")

    def metadata(self) -> PluginMetadata:
        return PluginMetadata(
            name="prometheus",
            version="1.0.0",
            description="Export CloudStack events and metrics in Prometheus format",
            author="CSIntegration",
            tags=["prometheus", "monitoring", "metrics", "observability"],
            config_schema={
                "metric_prefix": {"type": "string", "default": "csintegration"},
                "include_event_details": {"type": "boolean", "default": False},
            },
        )

    async def on_start(self) -> None:
        self._event_counters.clear()
        self._resource_gauges.clear()
        self._start_time = time.time()
        self._total_events = 0
        logger.info("Prometheus metrics plugin started")

    async def on_stop(self) -> None:
        logger.info(
            "Prometheus metrics plugin stopped — recorded %d events",
            self._total_events,
        )

    async def handle_event(self, event: Dict[str, Any]) -> Optional[Dict[str, Any]]:
        event_type = event.get("event_type", "UNKNOWN")
        self._event_counters[event_type] += 1
        self._total_events += 1

        category = event_type.split(".")[0] if "." in event_type else event_type

        payload = event.get("payload", {})
        if "CREATE" in event_type or "DEPLOY" in event_type:
            self._resource_gauges[category] = self._resource_gauges.get(category, 0) + 1
        elif "DESTROY" in event_type or "DELETE" in event_type:
            self._resource_gauges[category] = max(
                0, self._resource_gauges.get(category, 0) - 1
            )

        return None

    async def health_check(self) -> PluginHealth:
        return PluginHealth(
            healthy=True,
            message=f"Tracking {self._total_events} events across {len(self._event_counters)} types",
            details={
                "total_events": self._total_events,
                "event_types": len(self._event_counters),
                "uptime": time.time() - self._start_time,
            },
        )

    def api_routes(self) -> list:
        return [
            {
                "method": "GET",
                "path": "/metrics",
                "handler": self._get_metrics,
                "summary": "Prometheus metrics endpoint",
            },
            {
                "method": "GET",
                "path": "/metrics/json",
                "handler": self._get_metrics_json,
                "summary": "Metrics in JSON format",
            },
        ]

    async def _get_metrics(self) -> str:
        """Generate Prometheus exposition format metrics."""
        prefix = self.config.get("metric_prefix", "csintegration")
        lines: List[str] = []

        lines.append(f"# HELP {prefix}_events_total Total events processed")
        lines.append(f"# TYPE {prefix}_events_total counter")
        lines.append(f"{prefix}_events_total {self._total_events}")

        lines.append(f"# HELP {prefix}_events_by_type_total Events by type")
        lines.append(f"# TYPE {prefix}_events_by_type_total counter")
        for event_type, count in sorted(self._event_counters.items()):
            safe_type = event_type.replace(".", "_").lower()
            lines.append(f'{prefix}_events_by_type_total{{type="{safe_type}"}} {count}')

        lines.append(f"# HELP {prefix}_resources_active Active resources by category")
        lines.append(f"# TYPE {prefix}_resources_active gauge")
        for category, count in sorted(self._resource_gauges.items()):
            safe_cat = category.replace(".", "_").lower()
            lines.append(f'{prefix}_resources_active{{category="{safe_cat}"}} {count}')

        uptime = time.time() - self._start_time
        lines.append(f"# HELP {prefix}_uptime_seconds Framework uptime in seconds")
        lines.append(f"# TYPE {prefix}_uptime_seconds gauge")
        lines.append(f"{prefix}_uptime_seconds {uptime:.1f}")

        return "\n".join(lines) + "\n"

    async def _get_metrics_json(self) -> Dict[str, Any]:
        return {
            "total_events": self._total_events,
            "event_counters": dict(self._event_counters),
            "resource_gauges": dict(self._resource_gauges),
            "uptime": time.time() - self._start_time,
        }
