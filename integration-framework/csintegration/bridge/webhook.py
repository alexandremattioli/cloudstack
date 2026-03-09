"""
Outbound webhook dispatcher.

Sends framework events to external HTTP endpoints. Supports configurable
retry logic, timeout, authentication, and payload transformation.
"""

from __future__ import annotations

import asyncio
import hashlib
import hmac
import json
import logging
import time
from dataclasses import dataclass, field
from typing import Any, Callable, Dict, List, Optional

import httpx

from csintegration.core.events import Event

logger = logging.getLogger("csintegration.bridge.webhook")


@dataclass
class WebhookTarget:
    url: str
    name: str = ""
    secret: str = ""
    headers: Dict[str, str] = field(default_factory=dict)
    event_filter: str = "*"
    max_retries: int = 3
    timeout: float = 30.0
    enabled: bool = True


class WebhookDispatcher:
    """
    Dispatches events to configured webhook targets.

    Each target can filter which events it receives via glob patterns
    and optionally verify payloads using HMAC signatures.
    """

    def __init__(self) -> None:
        self._targets: Dict[str, WebhookTarget] = {}
        self._client: Optional[httpx.AsyncClient] = None
        self._stats: Dict[str, Dict[str, int]] = {}

    async def _get_client(self) -> httpx.AsyncClient:
        if self._client is None or self._client.is_closed:
            self._client = httpx.AsyncClient(timeout=30.0)
        return self._client

    async def close(self) -> None:
        if self._client and not self._client.is_closed:
            await self._client.aclose()

    def add_target(self, target: WebhookTarget) -> None:
        key = target.name or target.url
        self._targets[key] = target
        self._stats[key] = {"sent": 0, "failed": 0, "retries": 0}
        logger.info("Added webhook target: %s -> %s", key, target.url)

    def remove_target(self, name: str) -> None:
        self._targets.pop(name, None)
        self._stats.pop(name, None)

    def list_targets(self) -> List[Dict[str, Any]]:
        result = []
        for key, target in self._targets.items():
            info = {
                "name": key,
                "url": target.url,
                "event_filter": target.event_filter,
                "enabled": target.enabled,
                "stats": self._stats.get(key, {}),
            }
            result.append(info)
        return result

    async def dispatch(self, event: Event) -> None:
        """Send an event to all matching webhook targets."""
        import fnmatch

        tasks = []
        for key, target in self._targets.items():
            if not target.enabled:
                continue
            if not fnmatch.fnmatch(event.event_type, target.event_filter):
                continue
            tasks.append(self._send_with_retry(key, target, event))

        if tasks:
            await asyncio.gather(*tasks)

    async def _send_with_retry(
        self, key: str, target: WebhookTarget, event: Event
    ) -> None:
        payload = json.dumps(event.to_dict(), default=str)
        headers = {
            "Content-Type": "application/json",
            "X-CSIntegration-Event": event.event_type,
            "X-CSIntegration-EventID": event.event_id,
            **target.headers,
        }

        if target.secret:
            signature = hmac.new(
                target.secret.encode("utf-8"),
                payload.encode("utf-8"),
                hashlib.sha256,
            ).hexdigest()
            headers["X-CSIntegration-Signature"] = f"sha256={signature}"

        client = await self._get_client()
        last_error: Optional[Exception] = None

        for attempt in range(target.max_retries + 1):
            try:
                response = await client.post(
                    target.url,
                    content=payload,
                    headers=headers,
                    timeout=target.timeout,
                )
                response.raise_for_status()
                self._stats[key]["sent"] += 1
                return
            except Exception as exc:
                last_error = exc
                if attempt < target.max_retries:
                    self._stats[key]["retries"] += 1
                    backoff = 2 ** attempt
                    logger.warning(
                        "Webhook %s attempt %d failed, retrying in %ds: %s",
                        key, attempt + 1, backoff, exc,
                    )
                    await asyncio.sleep(backoff)

        self._stats[key]["failed"] += 1
        logger.error("Webhook %s failed after %d attempts: %s", key, target.max_retries + 1, last_error)

    @property
    def stats(self) -> Dict[str, Dict[str, int]]:
        return dict(self._stats)
