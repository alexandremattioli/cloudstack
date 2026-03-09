# CloudStack Integration Framework

A plugin-based integration framework that runs as a sidecar alongside Apache CloudStack, enabling seamless integration of external platforms, services, and custom features through a unified event-driven architecture.

## Architecture

```
                    ┌──────────────────────────────────────────────────┐
                    │       CloudStack Integration Framework           │
                    │                                                  │
┌──────────┐       │  ┌─────────┐  ┌──────────┐  ┌────────────────┐  │
│CloudStack│──────►│  │  Event  │──│  Plugin   │──│   Kubernetes   │  │
│Management│ events│  │  Bridge │  │ Lifecycle │  │   Terraform    │  │
│  Server  │◄──────│  │         │  │  Manager  │  │   Prometheus   │  │
└──────────┘  API  │  └─────────┘  └──────────┘  │   Webhook Relay│  │
                    │       │           │          │   Custom ...   │  │
                    │  ┌────▼───────────▼────┐    └────────────────┘  │
                    │  │     Event Bus       │                        │
                    │  │  (pattern matching) │                        │
                    │  └────────────────────┘                        │
                    │       │                                         │
                    │  ┌────▼──────────────┐                         │
                    │  │    REST API       │◄── External Systems      │
                    │  │  (FastAPI :8600)  │                          │
                    │  └──────────────────┘                          │
                    └──────────────────────────────────────────────────┘
```

## Key Features

- **Plugin Architecture** — Add integrations by implementing a simple Python interface. Plugins are discovered, loaded, and managed automatically.
- **Event-Driven** — CloudStack events flow through the framework's event bus with glob-pattern subscriptions (`VM.*`, `NETWORK.CREATE`, `*`).
- **Multiple Event Transports** — Receive CloudStack events via Webhook, Kafka, RabbitMQ, or API polling.
- **REST API** — Manage plugins, publish events, configure webhooks, and proxy CloudStack API calls.
- **Outbound Webhooks** — Forward events to external HTTP endpoints with HMAC signatures and automatic retry.
- **CloudStack API Client** — Built-in async client with request signing, pagination, and convenience methods.
- **Custom Plugin Routes** — Plugins can expose their own REST API endpoints.
- **Health Monitoring** — Per-plugin and framework-level health checks.

## Quick Start

### Install

```bash
cd integration-framework
pip install -e ".[dev]"
```

### Run

```bash
# Start with defaults
csintegration

# Or with a config file
csintegration --config config/default.yaml --port 8600

# Or with Docker
docker compose up
```

### Environment Variables

Configure via `CSINT_` prefixed environment variables:

```bash
export CSINT_PORT=8600
export CSINT_LOG_LEVEL=DEBUG
export CSINT_CLOUDSTACK__ENDPOINT=http://cloudstack:8080/client
export CSINT_CLOUDSTACK__API_KEY=your-api-key
export CSINT_CLOUDSTACK__SECRET_KEY=your-secret-key
```

## API Usage

### Health Check

```bash
curl http://localhost:8600/health
```

### List Plugins

```bash
curl http://localhost:8600/plugins/
```

### Enable a Plugin

```bash
curl -X POST http://localhost:8600/plugins/kubernetes/enable \
  -H "Content-Type: application/json" \
  -d '{"config": {"kubeconfig_path": "~/.kube/config"}}'
```

### Publish an Event

```bash
curl -X POST http://localhost:8600/events/publish \
  -H "Content-Type: application/json" \
  -d '{"event_type": "CUSTOM.DEPLOY", "payload": {"app": "myapp"}}'
```

### Add a Webhook Target

```bash
curl -X POST http://localhost:8600/integrations/webhooks \
  -H "Content-Type: application/json" \
  -d '{"name": "slack", "url": "https://hooks.slack.com/...", "event_filter": "VM.*"}'
```

### Proxy a CloudStack API Call

```bash
curl -X POST http://localhost:8600/integrations/cloudstack/proxy \
  -H "Content-Type: application/json" \
  -d '{"command": "listVirtualMachines", "params": {"state": "Running"}}'
```

## Writing a Plugin

Create a Python package under `csintegration/plugins/` (or anywhere on the plugin path) with a `plugin.py` module:

```python
from csintegration.plugins.base import IntegrationPlugin, PluginMetadata

class MyPlugin(IntegrationPlugin):
    def __init__(self):
        super().__init__()
        self.subscribe("VM.*")  # Subscribe to VM events

    def metadata(self):
        return PluginMetadata(
            name="my-plugin",
            version="1.0.0",
            description="My custom integration",
        )

    async def on_start(self):
        # Initialize connections, resources, etc.
        pass

    async def on_stop(self):
        # Cleanup
        pass

    async def handle_event(self, event):
        event_type = event["event_type"]
        payload = event["payload"]
        # React to CloudStack events
        return {"processed": True}

    def api_routes(self):
        # Optionally expose custom REST endpoints
        return [
            {
                "method": "GET",
                "path": "/status",
                "handler": self.get_status,
                "summary": "My plugin status",
            }
        ]

    async def get_status(self):
        return {"status": "operational"}
```

### Plugin Lifecycle

```
on_load() → on_start() → [handle_event() ...] → on_stop() → on_unload()
```

### External Plugins via Entry Points

Register plugins in your package's `setup.py`:

```python
setup(
    ...
    entry_points={
        "csintegration.plugins": [
            "my-plugin = my_package.plugin:MyPlugin",
        ],
    },
)
```

## Included Plugins

| Plugin | Description |
|--------|-------------|
| **kubernetes** | Syncs CloudStack VM lifecycle with Kubernetes node management |
| **terraform** | Tracks CloudStack resources and generates Terraform import blocks |
| **prometheus** | Exports events and metrics in Prometheus exposition format |
| **webhook-relay** | Forwards CloudStack events to external webhook endpoints |

## Event Transport Configuration

### Webhook (default)

Point CloudStack's webhook event bus plugin at:

```
POST http://<framework-host>:8600/integrations/cloudstack/events
```

### Kafka

```yaml
event_listener:
  type: kafka
  kafka:
    bootstrap_servers: "kafka:9092"
    topic: "cloudstack-events"
    group_id: "csintegration"
```

Requires: `pip install csintegration[kafka]`

### RabbitMQ

```yaml
event_listener:
  type: rabbitmq
  rabbitmq:
    amqp_url: "amqp://guest:guest@rabbitmq/"
    exchange: "cloudstack-events"
    queue: "csintegration"
```

Requires: `pip install csintegration[rabbitmq]`

## Testing

```bash
pip install -e ".[dev]"
pytest
```

## License

Apache License 2.0
