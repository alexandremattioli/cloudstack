"""
Entry point for the CloudStack Integration Framework.

Usage:
    python -m csintegration.main [--config config.yaml] [--host 0.0.0.0] [--port 8600]

Or via the installed CLI:
    csintegration [--config config.yaml] [--host 0.0.0.0] [--port 8600]
"""

from __future__ import annotations

import argparse
import sys


def parse_args(argv=None):
    parser = argparse.ArgumentParser(
        description="CloudStack Integration Framework"
    )
    parser.add_argument(
        "--config", "-c",
        default=None,
        help="Path to YAML configuration file",
    )
    parser.add_argument(
        "--host",
        default=None,
        help="Host to bind to (default: 0.0.0.0)",
    )
    parser.add_argument(
        "--port", "-p",
        type=int,
        default=None,
        help="Port to bind to (default: 8600)",
    )
    parser.add_argument(
        "--log-level",
        default=None,
        choices=["DEBUG", "INFO", "WARNING", "ERROR"],
        help="Logging level",
    )
    parser.add_argument(
        "--generate-key",
        action="store_true",
        help="Generate a new API key and exit",
    )
    return parser.parse_args(argv)


def main(argv=None):
    args = parse_args(argv)

    if args.generate_key:
        from csintegration.utils.crypto import generate_api_key
        print(f"Generated API Key: {generate_api_key()}")
        return

    from csintegration.config import load_config
    from csintegration.utils.logging import setup_logging

    config = load_config(args.config)

    if args.host:
        config["host"] = args.host
    if args.port:
        config["port"] = args.port
    if args.log_level:
        config["log_level"] = args.log_level

    setup_logging(config.get("log_level", "INFO"))

    from csintegration.api.app import create_app

    app = create_app(config)

    import uvicorn
    uvicorn.run(
        app,
        host=config.get("host", "0.0.0.0"),
        port=config.get("port", 8600),
        log_level=config.get("log_level", "info").lower(),
    )


if __name__ == "__main__":
    main()
