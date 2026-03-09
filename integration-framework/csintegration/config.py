"""
Configuration management for the integration framework.

Loads configuration from YAML files and environment variables.
Environment variables override file-based config using the prefix
CSINT_ with double-underscore nesting (e.g. CSINT_CLOUDSTACK__ENDPOINT).
"""

from __future__ import annotations

import logging
import os
from pathlib import Path
from typing import Any, Dict, Optional

logger = logging.getLogger("csintegration.config")

_DEFAULT_CONFIG: Dict[str, Any] = {
    "host": "0.0.0.0",
    "port": 8600,
    "api_key": "",
    "log_level": "INFO",
    "cors_origins": ["*"],
    "event_history_size": 1000,
    "plugin_paths": [],
    "cloudstack": {
        "endpoint": "",
        "api_key": "",
        "secret_key": "",
        "verify_ssl": True,
        "timeout": 60.0,
    },
    "event_listener": {
        "type": "webhook",
        "poll_interval": 10.0,
        "kafka": {
            "bootstrap_servers": "localhost:9092",
            "topic": "cloudstack-events",
            "group_id": "csintegration",
        },
        "rabbitmq": {
            "amqp_url": "amqp://guest:guest@localhost/",
            "exchange": "cloudstack-events",
            "queue": "csintegration",
        },
    },
    "plugins": {},
}


def load_config(config_path: Optional[str] = None) -> Dict[str, Any]:
    """
    Load configuration from a YAML file, falling back to defaults.

    Environment variables with prefix CSINT_ override values.
    """
    config = _deep_copy(_DEFAULT_CONFIG)

    if config_path:
        file_config = _load_yaml(config_path)
        if file_config:
            _deep_merge(config, file_config)
    else:
        for candidate in ["config/config.yaml", "config/default.yaml", "config.yaml"]:
            if Path(candidate).exists():
                file_config = _load_yaml(candidate)
                if file_config:
                    _deep_merge(config, file_config)
                break

    _apply_env_overrides(config)
    return config


def _load_yaml(path: str) -> Optional[Dict[str, Any]]:
    try:
        import yaml

        with open(path, "r") as f:
            data = yaml.safe_load(f)
        if isinstance(data, dict):
            logger.info("Loaded config from %s", path)
            return data
    except ImportError:
        logger.debug("PyYAML not installed — skipping YAML config")
    except FileNotFoundError:
        logger.debug("Config file not found: %s", path)
    except Exception:
        logger.exception("Failed to load config from %s", path)
    return None


def _apply_env_overrides(config: Dict[str, Any], prefix: str = "CSINT_") -> None:
    """Override config values from environment variables."""
    for key, value in os.environ.items():
        if not key.startswith(prefix):
            continue
        config_path = key[len(prefix):].lower().split("__")
        _set_nested(config, config_path, _parse_value(value))


def _parse_value(value: str) -> Any:
    if value.lower() in ("true", "yes", "1"):
        return True
    if value.lower() in ("false", "no", "0"):
        return False
    try:
        return int(value)
    except ValueError:
        pass
    try:
        return float(value)
    except ValueError:
        pass
    return value


def _set_nested(d: Dict[str, Any], keys: list, value: Any) -> None:
    for key in keys[:-1]:
        d = d.setdefault(key, {})
    d[keys[-1]] = value


def _deep_merge(base: Dict[str, Any], override: Dict[str, Any]) -> None:
    for key, value in override.items():
        if key in base and isinstance(base[key], dict) and isinstance(value, dict):
            _deep_merge(base[key], value)
        else:
            base[key] = value


def _deep_copy(d: Dict[str, Any]) -> Dict[str, Any]:
    import copy
    return copy.deepcopy(d)
