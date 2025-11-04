# VNF Broker Service

A secure proxy service for CloudStack that enables the management server to communicate with Virtual Network Function (VNF) appliances through HTTP/HTTPS REST APIs and SSH/CLI commands.

## Overview

The VNF Broker runs on CloudStack Virtual Routers and acts as a secure intermediary between:
- **CloudStack Management Server** → Broker (JWT authenticated, mTLS encrypted)
- **Broker** → VNF Appliances (HTTP/HTTPS or SSH)

## Features

- ✅ **JWT Authentication** - Secure token-based auth for management server requests
- ✅ **mTLS Support** - Encrypted communication with auto-generated self-signed certs
- ✅ **HTTP/HTTPS Proxy** - Forward REST API calls to VNF devices
- ✅ **SSH/CLI Execution** - Execute commands via SSH on VNF appliances
- ✅ **Audit Logging** - Comprehensive logging of all operations
- ✅ **IP Allowlisting** - Restrict access by source and target IPs
- ✅ **Systemd Integration** - Auto-start and monitoring via systemd
- ✅ **Health Checks** - Built-in health endpoint for monitoring

## Requirements

- Python 3.8+
- Linux with systemd (Ubuntu 20.04+, Debian 10+, RHEL 8+)
- Network access to VNF appliances
- Root access for installation

## Installation

### Quick Install

```bash
cd tools/vnf-broker
sudo bash install.sh
```

The installer will:
1. Check Python version (3.8+ required)
2. Install Python dependencies
3. Create necessary directories
4. Generate JWT secret
5. Create default configuration
6. Install and enable systemd service

### Manual Installation

```bash
# Create directories
sudo mkdir -p /opt/vnfbroker /etc/vnf-broker /var/log/vnf-broker

# Copy broker files
sudo cp vnf_broker.py /opt/vnfbroker/
sudo chmod +x /opt/vnfbroker/vnf_broker.py

# Install dependencies
sudo pip3 install -r requirements.txt

# Generate JWT secret
openssl rand -base64 32 | sudo tee /etc/vnf-broker/jwt_secret
sudo chmod 600 /etc/vnf-broker/jwt_secret

# Copy and enable service
sudo cp vnfbroker.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable vnfbroker
```

## Configuration

Edit `/etc/vnf-broker/config.json`:

```json
{
  "BROKER_PORT": 8443,
  "BROKER_HOST": "0.0.0.0",
  "JWT_ALGORITHM": "HS256",
  "ALLOWED_MANAGEMENT_IPS": ["10.1.1.5"],
  "ALLOWED_VNF_IPS": ["192.168.1.10", "192.168.1.11"],
  "TLS_CERT_PATH": "/etc/vnf-broker/server.crt",
  "TLS_KEY_PATH": "/etc/vnf-broker/server.key",
  "LOG_FILE": "/var/log/vnf-broker/broker.log",
  "REQUEST_TIMEOUT": 30,
  "DEBUG": false
}
```

### Configuration Options

| Option | Description | Default |
|--------|-------------|---------|
| `BROKER_PORT` | HTTPS port to listen on | `8443` |
| `BROKER_HOST` | Interface to bind to | `0.0.0.0` |
| `JWT_ALGORITHM` | JWT signing algorithm | `HS256` |
| `ALLOWED_MANAGEMENT_IPS` | Allowed CloudStack management IPs (empty = all) | `[]` |
| `ALLOWED_VNF_IPS` | Allowed VNF target IPs (empty = all) | `[]` |
| `REQUEST_TIMEOUT` | Timeout for VNF requests (seconds) | `30` |
| `DEBUG` | Enable debug logging | `false` |

## Usage

### Start the Service

```bash
sudo systemctl start vnfbroker
```

### Check Status

```bash
sudo systemctl status vnfbroker
```

### View Logs

```bash
# Live logs
sudo journalctl -u vnfbroker -f

# Recent logs
sudo journalctl -u vnfbroker -n 100
```

### Stop the Service

```bash
sudo systemctl stop vnfbroker
```

## API Reference

### Health Check

```bash
curl -k https://localhost:8443/health
```

Response:
```json
{
  "status": "healthy",
  "service": "vnf-broker",
  "timestamp": "2025-11-04T15:00:00"
}
```

### VNF Proxy Endpoint

**Endpoint:** `POST /vnfproxy`

**Authentication:** Bearer token (JWT)

**HTTP/HTTPS Request Example:**

```bash
curl -k -X POST https://localhost:8443/vnfproxy \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "target": "192.168.1.10",
    "protocol": "HTTPS",
    "method": "POST",
    "uri": "/api/firewall/rules",
    "headers": {
      "X-API-Key": "vnf-api-key",
      "Content-Type": "application/json"
    },
    "body": "{\"action\":\"allow\",\"source\":\"10.0.0.0/24\"}"
  }'
```

**SSH Command Example:**

```bash
curl -k -X POST https://localhost:8443/vnfproxy \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "target": "192.168.1.10",
    "protocol": "SSH",
    "command": "show running-config",
    "ssh_username": "admin",
    "ssh_password": "password",
    "ssh_port": 22
  }'
```

## Testing

Run the test suite:

```bash
# Get JWT secret
JWT_SECRET=$(sudo cat /etc/vnf-broker/jwt_secret)

# Run tests
python3 test_broker.py https://localhost:8443 "$JWT_SECRET" 192.168.1.10
```

## CloudStack Integration

### Configuration in CloudStack

Add to `/etc/cloudstack/management/server.properties` or global settings:

```properties
vnf.broker.enabled=true
vnf.broker.url=https://<VR_IP>:8443
vnf.broker.jwt.secret=<JWT_SECRET_FROM_BROKER>
vnf.broker.timeout=30
```

### JWT Token Generation (Java)

```java
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

String token = Jwts.builder()
    .setSubject("cloudstack")
    .setExpiration(new Date(System.currentTimeMillis() + 3600000))
    .claim("allowed_target", "192.168.1.10")
    .signWith(SignatureAlgorithm.HS256, jwtSecret)
    .compact();
```

## Security Considerations

1. **JWT Secret** - Keep the JWT secret secure and synchronized between CloudStack and broker
2. **TLS Certificates** - Replace self-signed certs with proper CA-signed certs in production
3. **IP Allowlisting** - Configure `ALLOWED_MANAGEMENT_IPS` and `ALLOWED_VNF_IPS` restrictively
4. **Firewall** - Only allow port 8443 from CloudStack management server
5. **SSH Keys** - Use SSH key authentication instead of passwords when possible

## Troubleshooting

### Broker won't start

Check logs:
```bash
sudo journalctl -u vnfbroker -n 50
```

Common issues:
- Port 8443 already in use: Check with `sudo netstat -tlnp | grep 8443`
- Missing Python dependencies: Run `pip3 install -r requirements.txt`
- Permission issues: Ensure `/etc/vnf-broker` and `/var/log/vnf-broker` exist

### JWT authentication fails

- Verify JWT secret matches between CloudStack and broker
- Check token expiration (JWT tokens expire after 1 hour by default)
- Ensure token is sent as `Authorization: Bearer <token>` header

### Cannot connect to VNF

- Verify VNF IP is reachable: `ping <VNF_IP>`
- Check firewall rules on VNF appliance
- Verify VNF credentials (SSH username/password or API key)
- Check `ALLOWED_VNF_IPS` configuration

### High latency

- Check `REQUEST_TIMEOUT` setting (increase if needed)
- Monitor network between broker and VNF
- Check VNF appliance performance

## Development

### Running in Development Mode

```bash
# Enable debug logging
sudo nano /etc/vnf-broker/config.json
# Set "DEBUG": true

# Restart service
sudo systemctl restart vnfbroker

# Follow logs
sudo journalctl -u vnfbroker -f
```

### Testing without systemd

```bash
cd /opt/vnfbroker
sudo python3 vnf_broker.py
```

## License

Apache License 2.0 - Part of Apache CloudStack

## Support

- Documentation: https://cloudstack.apache.org/
- Mailing List: dev@cloudstack.apache.org
- Issue Tracker: https://github.com/apache/cloudstack/issues
