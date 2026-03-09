"""Cryptographic utilities for webhook signatures and API key generation."""

from __future__ import annotations

import hashlib
import hmac
import secrets
from typing import Optional


def generate_api_key(length: int = 48) -> str:
    """Generate a secure random API key."""
    return secrets.token_urlsafe(length)


def verify_webhook_signature(
    payload: bytes,
    signature: str,
    secret: str,
    algorithm: str = "sha256",
) -> bool:
    """Verify an HMAC signature on a webhook payload."""
    if "=" in signature:
        _, _, signature = signature.partition("=")

    hash_func = getattr(hashlib, algorithm, None)
    if hash_func is None:
        raise ValueError(f"Unsupported algorithm: {algorithm}")

    expected = hmac.new(
        secret.encode("utf-8"),
        payload,
        hash_func,
    ).hexdigest()

    return hmac.compare_digest(expected, signature)
