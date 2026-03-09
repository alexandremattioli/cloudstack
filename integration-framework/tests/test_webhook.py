"""Tests for the webhook dispatcher."""

import pytest

from csintegration.bridge.webhook import WebhookDispatcher, WebhookTarget
from csintegration.core.events import Event


@pytest.fixture
def dispatcher():
    return WebhookDispatcher()


def test_add_target(dispatcher):
    target = WebhookTarget(url="http://example.com/hook", name="test")
    dispatcher.add_target(target)
    targets = dispatcher.list_targets()
    assert len(targets) == 1
    assert targets[0]["name"] == "test"


def test_remove_target(dispatcher):
    target = WebhookTarget(url="http://example.com/hook", name="test")
    dispatcher.add_target(target)
    dispatcher.remove_target("test")
    assert len(dispatcher.list_targets()) == 0


def test_stats_initialized(dispatcher):
    target = WebhookTarget(url="http://example.com/hook", name="test")
    dispatcher.add_target(target)
    stats = dispatcher.stats
    assert stats["test"]["sent"] == 0
    assert stats["test"]["failed"] == 0
