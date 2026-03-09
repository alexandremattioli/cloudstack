"""
CloudStack API client for the Integration Framework.

Provides both synchronous-style and async methods for communicating with
a CloudStack management server. Handles request signing, pagination,
and error handling transparently.
"""

from __future__ import annotations

import hashlib
import hmac
import base64
import json
import logging
import urllib.parse
from typing import Any, Dict, List, Optional

import httpx

logger = logging.getLogger("csintegration.bridge.client")


class CloudStackClientError(Exception):
    def __init__(self, message: str, error_code: int = 0, cs_error_code: int = 0):
        super().__init__(message)
        self.error_code = error_code
        self.cs_error_code = cs_error_code


class CloudStackClient:
    """
    Async client for the Apache CloudStack API.

    Supports both API-key and session-based authentication.
    """

    def __init__(
        self,
        endpoint: str,
        api_key: str,
        secret_key: str,
        verify_ssl: bool = True,
        timeout: float = 60.0,
    ) -> None:
        self.endpoint = endpoint.rstrip("/")
        self.api_key = api_key
        self.secret_key = secret_key
        self.verify_ssl = verify_ssl
        self.timeout = timeout
        self._client: Optional[httpx.AsyncClient] = None

    async def _get_client(self) -> httpx.AsyncClient:
        if self._client is None or self._client.is_closed:
            self._client = httpx.AsyncClient(
                verify=self.verify_ssl,
                timeout=self.timeout,
            )
        return self._client

    async def close(self) -> None:
        if self._client and not self._client.is_closed:
            await self._client.aclose()

    def _sign_request(self, params: Dict[str, str]) -> str:
        params["apiKey"] = self.api_key
        params["response"] = "json"

        sorted_params = sorted(params.items(), key=lambda x: x[0].lower())
        query_string = "&".join(
            f"{k.lower()}={urllib.parse.quote_plus(v).lower()}"
            for k, v in sorted_params
        )

        signature = hmac.new(
            self.secret_key.encode("utf-8"),
            query_string.encode("utf-8"),
            hashlib.sha512,
        ).digest()
        return base64.b64encode(signature).decode("utf-8")

    async def request(
        self, command: str, **params: Any
    ) -> Dict[str, Any]:
        """
        Execute a CloudStack API command.

        Args:
            command: The API command name (e.g. "listVirtualMachines").
            **params: Command parameters.

        Returns:
            Parsed JSON response dict.

        Raises:
            CloudStackClientError: On API-level errors.
        """
        request_params: Dict[str, str] = {"command": command}
        for k, v in params.items():
            if v is not None:
                request_params[k] = str(v)

        signature = self._sign_request(request_params)
        request_params["signature"] = signature
        request_params["response"] = "json"
        request_params["apiKey"] = self.api_key

        client = await self._get_client()
        url = f"{self.endpoint}/api"

        try:
            response = await client.get(url, params=request_params)
            response.raise_for_status()
        except httpx.HTTPStatusError as exc:
            raise CloudStackClientError(
                f"HTTP {exc.response.status_code}: {exc.response.text}",
                error_code=exc.response.status_code,
            ) from exc
        except httpx.RequestError as exc:
            raise CloudStackClientError(f"Request failed: {exc}") from exc

        data = response.json()

        response_key = f"{command.lower()}response"
        if response_key not in data:
            for key in data:
                if key.endswith("response"):
                    response_key = key
                    break

        result = data.get(response_key, data)
        if "errorcode" in result:
            raise CloudStackClientError(
                result.get("errortext", "Unknown error"),
                error_code=result.get("errorcode", 0),
                cs_error_code=result.get("cserrorcode", 0),
            )

        return result

    async def list_all(
        self, command: str, result_key: str, page_size: int = 500, **params: Any
    ) -> List[Dict[str, Any]]:
        """
        Auto-paginate a list* command and return all results.

        Args:
            command: List command (e.g. "listVirtualMachines").
            result_key: Key in the response containing the list (e.g. "virtualmachine").
            page_size: Number of items per page.
            **params: Additional command parameters.

        Returns:
            Complete list of result items.
        """
        all_items: List[Dict[str, Any]] = []
        page = 1

        while True:
            result = await self.request(
                command, pagesize=page_size, page=page, **params
            )
            items = result.get(result_key, [])
            if not items:
                break
            all_items.extend(items)
            if len(items) < page_size:
                break
            page += 1

        return all_items

    # ── Convenience methods ──

    async def list_vms(self, **params: Any) -> List[Dict[str, Any]]:
        return await self.list_all("listVirtualMachines", "virtualmachine", **params)

    async def list_networks(self, **params: Any) -> List[Dict[str, Any]]:
        return await self.list_all("listNetworks", "network", **params)

    async def list_volumes(self, **params: Any) -> List[Dict[str, Any]]:
        return await self.list_all("listVolumes", "volume", **params)

    async def list_zones(self, **params: Any) -> List[Dict[str, Any]]:
        return await self.list_all("listZones", "zone", **params)

    async def list_hosts(self, **params: Any) -> List[Dict[str, Any]]:
        return await self.list_all("listHosts", "host", **params)

    async def list_events(self, **params: Any) -> List[Dict[str, Any]]:
        return await self.list_all("listEvents", "event", **params)

    async def deploy_vm(self, **params: Any) -> Dict[str, Any]:
        return await self.request("deployVirtualMachine", **params)

    async def destroy_vm(self, vm_id: str, expunge: bool = False) -> Dict[str, Any]:
        return await self.request(
            "destroyVirtualMachine", id=vm_id, expunge=str(expunge).lower()
        )

    async def start_vm(self, vm_id: str) -> Dict[str, Any]:
        return await self.request("startVirtualMachine", id=vm_id)

    async def stop_vm(self, vm_id: str, forced: bool = False) -> Dict[str, Any]:
        return await self.request(
            "stopVirtualMachine", id=vm_id, forced=str(forced).lower()
        )
