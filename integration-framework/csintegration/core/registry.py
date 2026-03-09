"""
Plugin registry for discovering, loading, and tracking integration plugins.

Supports loading plugins from:
  - Built-in plugin packages under csintegration.plugins.*
  - External Python packages via entry points
  - Arbitrary file paths
"""

from __future__ import annotations

import importlib
import inspect
import logging
import pkgutil
from pathlib import Path
from typing import Any, Dict, List, Optional, Type

from csintegration.plugins.base import IntegrationPlugin, PluginMetadata

logger = logging.getLogger("csintegration.registry")


class PluginRegistry:
    """Central registry for all discovered and loaded plugin classes."""

    def __init__(self) -> None:
        self._plugin_classes: Dict[str, Type[IntegrationPlugin]] = {}
        self._metadata_cache: Dict[str, PluginMetadata] = {}

    def register(self, plugin_cls: Type[IntegrationPlugin]) -> None:
        instance = plugin_cls()
        meta = instance.metadata()
        if meta.name in self._plugin_classes:
            logger.warning("Overwriting existing plugin registration: %s", meta.name)
        self._plugin_classes[meta.name] = plugin_cls
        self._metadata_cache[meta.name] = meta
        logger.info("Registered plugin: %s v%s", meta.name, meta.version)

    def unregister(self, name: str) -> None:
        self._plugin_classes.pop(name, None)
        self._metadata_cache.pop(name, None)

    def get_class(self, name: str) -> Optional[Type[IntegrationPlugin]]:
        return self._plugin_classes.get(name)

    def get_metadata(self, name: str) -> Optional[PluginMetadata]:
        return self._metadata_cache.get(name)

    def list_plugins(self) -> List[PluginMetadata]:
        return list(self._metadata_cache.values())

    @property
    def plugin_names(self) -> List[str]:
        return list(self._plugin_classes.keys())

    def discover_builtin(self, package_path: str = "csintegration.plugins") -> int:
        """
        Discover plugins in built-in sub-packages.

        Each sub-package should contain a module named 'plugin' with a class
        that inherits from IntegrationPlugin.
        """
        count = 0
        try:
            package = importlib.import_module(package_path)
        except ImportError:
            logger.error("Could not import package: %s", package_path)
            return 0

        for importer, modname, ispkg in pkgutil.iter_modules(
            package.__path__, prefix=package.__name__ + "."
        ):
            if not ispkg:
                continue
            plugin_module_name = f"{modname}.plugin"
            try:
                mod = importlib.import_module(plugin_module_name)
            except ImportError:
                logger.debug("No plugin module in %s", modname)
                continue

            for _, obj in inspect.getmembers(mod, inspect.isclass):
                if (
                    issubclass(obj, IntegrationPlugin)
                    and obj is not IntegrationPlugin
                    and not inspect.isabstract(obj)
                ):
                    self.register(obj)
                    count += 1

        logger.info("Discovered %d built-in plugins", count)
        return count

    def discover_entrypoints(
        self, group: str = "csintegration.plugins"
    ) -> int:
        """Discover plugins registered via setuptools entry points."""
        count = 0
        try:
            from importlib.metadata import entry_points

            eps = entry_points()
            plugin_eps = eps.get(group, []) if isinstance(eps, dict) else [
                ep for ep in eps if ep.group == group
            ]
        except Exception:
            logger.debug("No entry points found for group: %s", group)
            return 0

        for ep in plugin_eps:
            try:
                cls = ep.load()
                if issubclass(cls, IntegrationPlugin):
                    self.register(cls)
                    count += 1
            except Exception:
                logger.exception("Failed to load entry point: %s", ep.name)

        logger.info("Discovered %d entry-point plugins", count)
        return count

    def discover_path(self, path: str) -> int:
        """Discover plugins from a directory of Python files."""
        count = 0
        plugin_dir = Path(path)
        if not plugin_dir.is_dir():
            logger.warning("Plugin path is not a directory: %s", path)
            return 0

        import sys
        if str(plugin_dir) not in sys.path:
            sys.path.insert(0, str(plugin_dir))

        for py_file in plugin_dir.glob("*.py"):
            mod_name = py_file.stem
            try:
                mod = importlib.import_module(mod_name)
                for _, obj in inspect.getmembers(mod, inspect.isclass):
                    if (
                        issubclass(obj, IntegrationPlugin)
                        and obj is not IntegrationPlugin
                        and not inspect.isabstract(obj)
                    ):
                        self.register(obj)
                        count += 1
            except Exception:
                logger.exception("Failed to load plugin file: %s", py_file)

        logger.info("Discovered %d plugins from path: %s", count, path)
        return count
