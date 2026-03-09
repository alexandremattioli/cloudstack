"""Tests for crypto utilities."""

import hashlib
import hmac

from csintegration.utils.crypto import generate_api_key, verify_webhook_signature


def test_generate_api_key():
    key = generate_api_key()
    assert len(key) > 20
    key2 = generate_api_key()
    assert key != key2


def test_verify_webhook_signature():
    payload = b'{"event": "test"}'
    secret = "my-secret"
    sig = hmac.new(secret.encode(), payload, hashlib.sha256).hexdigest()

    assert verify_webhook_signature(payload, sig, secret)
    assert verify_webhook_signature(payload, f"sha256={sig}", secret)
    assert not verify_webhook_signature(payload, "invalid", secret)
