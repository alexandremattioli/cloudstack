#!/bin/bash
###############################################################################
# VNF Broker Installation Script
# 
# This script installs the VNF Broker service on a CloudStack Virtual Router
# or any Linux system with Python 3.8+
#
# Usage: sudo bash install.sh
###############################################################################

set -e

INSTALL_DIR="/opt/vnfbroker"
CONFIG_DIR="/etc/vnf-broker"
LOG_DIR="/var/log/vnf-broker"
SYSTEMD_DIR="/etc/systemd/system"

echo "==============================================="
echo " VNF Broker Installation"
echo "==============================================="
echo ""

# Check if running as root
if [ "$EUID" -ne 0 ]; then
   echo "Error: Please run as root or with sudo"
   exit 1
fi

# Check Python version
echo "[1/8] Checking Python version..."
if ! command -v python3 &> /dev/null; then
    echo "Error: Python 3 is not installed"
    exit 1
fi

PYTHON_VERSION=$(python3 -c 'import sys; print(".".join(map(str, sys.version_info[:2])))')
echo "Found Python $PYTHON_VERSION"

if [ "$(python3 -c 'import sys; print(int(sys.version_info[0] >= 3 and sys.version_info[1] >= 8))')" != "1" ]; then
    echo "Error: Python 3.8+ is required"
    exit 1
fi

# Install pip if not present
echo "[2/8] Checking pip..."
if ! command -v pip3 &> /dev/null; then
    echo "Installing pip..."
    apt-get update -qq
    apt-get install -y python3-pip
fi

# Create directories
echo "[3/8] Creating directories..."
mkdir -p "$INSTALL_DIR"
mkdir -p "$CONFIG_DIR"
mkdir -p "$LOG_DIR"

# Copy broker files
echo "[4/8] Installing broker files..."
cp vnf_broker.py "$INSTALL_DIR/"
chmod +x "$INSTALL_DIR/vnf_broker.py"

# Install Python dependencies
echo "[5/8] Installing Python dependencies..."
pip3 install -r requirements.txt --quiet

# Generate JWT secret if not exists
echo "[6/8] Configuring JWT authentication..."
JWT_SECRET_FILE="$CONFIG_DIR/jwt_secret"
if [ ! -f "$JWT_SECRET_FILE" ]; then
    openssl rand -base64 32 > "$JWT_SECRET_FILE"
    chmod 600 "$JWT_SECRET_FILE"
    echo "Generated new JWT secret"
else
    echo "Using existing JWT secret"
fi

# Create default config
echo "[7/8] Creating configuration..."
cat > "$CONFIG_DIR/config.json" << 'EOFconfig'
{
  "BROKER_PORT": 8443,
  "BROKER_HOST": "0.0.0.0",
  "JWT_ALGORITHM": "HS256",
  "ALLOWED_MANAGEMENT_IPS": [],
  "ALLOWED_VNF_IPS": [],
  "TLS_CERT_PATH": "/etc/vnf-broker/server.crt",
  "TLS_KEY_PATH": "/etc/vnf-broker/server.key",
  "LOG_FILE": "/var/log/vnf-broker/broker.log",
  "REQUEST_TIMEOUT": 30,
  "DEBUG": false
}
EOFconfig

# Install systemd service
echo "[8/8] Installing systemd service..."
cp vnfbroker.service "$SYSTEMD_DIR/"
systemctl daemon-reload
systemctl enable vnfbroker.service

echo ""
echo "==============================================="
echo " Installation Complete!"
echo "==============================================="
echo ""
echo "Configuration:"
echo "  Config file: $CONFIG_DIR/config.json"
echo "  JWT secret:  $JWT_SECRET_FILE"
echo "  Log file:    $LOG_DIR/broker.log"
echo ""
echo "JWT Secret (save this for CloudStack configuration):"
cat "$JWT_SECRET_FILE"
echo ""
echo ""
echo "To start the broker:"
echo "  systemctl start vnfbroker"
echo ""
echo "To check status:"
echo "  systemctl status vnfbroker"
echo ""
echo "To view logs:"
echo "  journalctl -u vnfbroker -f"
echo ""
echo "Note: TLS certificates will be auto-generated on first start"
echo ""
