"""Tests for configuration loading."""

import os
import pytest

from csintegration.config import load_config


def test_default_config():
    config = load_config()
    assert config["port"] == 8600
    assert config["host"] == "0.0.0.0"
    assert config["cloudstack"]["verify_ssl"] is True


def test_env_override(monkeypatch):
    monkeypatch.setenv("CSINT_PORT", "9000")
    monkeypatch.setenv("CSINT_LOG_LEVEL", "DEBUG")
    config = load_config()
    assert config["port"] == 9000
    assert config["log_level"] == "DEBUG"


def test_nested_env_override(monkeypatch):
    monkeypatch.setenv("CSINT_CLOUDSTACK__ENDPOINT", "http://cs.example.com:8080")
    monkeypatch.setenv("CSINT_CLOUDSTACK__VERIFY_SSL", "false")
    config = load_config()
    assert config["cloudstack"]["endpoint"] == "http://cs.example.com:8080"
    assert config["cloudstack"]["verify_ssl"] is False


def test_nonexistent_config_file():
    config = load_config("/nonexistent/path.yaml")
    assert config["port"] == 8600


def test_event_listener_defaults():
    config = load_config()
    assert config["event_listener"]["type"] == "webhook"
    assert config["event_listener"]["poll_interval"] == 10.0
