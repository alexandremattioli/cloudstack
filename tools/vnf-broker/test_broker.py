#!/usr/bin/env python3
"""
VNF Broker Test Script
=======================
Tests the VNF Broker service functionality

Usage:
    python3 test_broker.py [broker_url] [jwt_secret]
    
Example:
    python3 test_broker.py https://192.168.1.1:8443 your_jwt_secret_here
"""

import sys
import json
import requests
import jwt
from datetime import datetime, timedelta

# Disable SSL warnings for self-signed certs
requests.packages.urllib3.disable_warnings()

def generate_jwt(secret):
    """Generate a JWT token for testing"""
    payload = {
        'sub': 'test-client',
        'exp': datetime.utcnow() + timedelta(hours=1),
        'iat': datetime.utcnow()
    }
    return jwt.encode(payload, secret, algorithm='HS256')

def test_health(broker_url):
    """Test health check endpoint"""
    print("\n[TEST] Health Check")
    try:
        response = requests.get(
            f"{broker_url}/health",
            verify=False,
            timeout=5
        )
        print(f"  Status: {response.status_code}")
        print(f"  Response: {response.json()}")
        return response.status_code == 200
    except Exception as e:
        print(f"  Error: {e}")
        return False

def test_http_proxy(broker_url, token, target_ip):
    """Test HTTP proxy functionality"""
    print("\n[TEST] HTTP Proxy")
    headers = {
        'Authorization': f'Bearer {token}',
        'Content-Type': 'application/json'
    }
    
    payload = {
        'target': target_ip,
        'protocol': 'HTTPS',
        'method': 'GET',
        'uri': '/api/v1/status',
        'headers': {
            'Accept': 'application/json'
        }
    }
    
    try:
        response = requests.post(
            f"{broker_url}/vnfproxy",
            headers=headers,
            json=payload,
            verify=False,
            timeout=10
        )
        print(f"  Status: {response.status_code}")
        print(f"  Response: {json.dumps(response.json(), indent=2)}")
        return response.status_code == 200
    except Exception as e:
        print(f"  Error: {e}")
        return False

def test_ssh_execution(broker_url, token, target_ip):
    """Test SSH execution functionality"""
    print("\n[TEST] SSH Execution")
    headers = {
        'Authorization': f'Bearer {token}',
        'Content-Type': 'application/json'
    }
    
    payload = {
        'target': target_ip,
        'protocol': 'SSH',
        'command': 'uname -a',
        'ssh_username': 'root',
        'ssh_password': 'password',  # Replace with actual credentials
        'ssh_port': 22
    }
    
    try:
        response = requests.post(
            f"{broker_url}/vnfproxy",
            headers=headers,
            json=payload,
            verify=False,
            timeout=15
        )
        print(f"  Status: {response.status_code}")
        print(f"  Response: {json.dumps(response.json(), indent=2)}")
        return response.status_code == 200
    except Exception as e:
        print(f"  Error: {e}")
        return False

def test_unauthorized_access(broker_url):
    """Test that unauthorized requests are rejected"""
    print("\n[TEST] Unauthorized Access (should fail)")
    try:
        response = requests.post(
            f"{broker_url}/vnfproxy",
            json={'target': '127.0.0.1'},
            verify=False,
            timeout=5
        )
        print(f"  Status: {response.status_code}")
        success = response.status_code == 401
        print(f"  Result: {'PASS' if success else 'FAIL'}")
        return success
    except Exception as e:
        print(f"  Error: {e}")
        return False

def main():
    if len(sys.argv) < 3:
        print("Usage: python3 test_broker.py <broker_url> <jwt_secret> [target_vnf_ip]")
        print("Example: python3 test_broker.py https://192.168.1.1:8443 mysecret 192.168.1.10")
        sys.exit(1)
    
    broker_url = sys.argv[1].rstrip('/')
    jwt_secret = sys.argv[2]
    target_vnf = sys.argv[3] if len(sys.argv) > 3 else "192.168.1.10"
    
    print("="*60)
    print("VNF Broker Test Suite")
    print("="*60)
    print(f"Broker URL: {broker_url}")
    print(f"Target VNF: {target_vnf}")
    
    # Generate JWT token
    print("\n[SETUP] Generating JWT token...")
    token = generate_jwt(jwt_secret)
    print(f"  Token: {token[:20]}...")
    
    # Run tests
    results = []
    results.append(("Health Check", test_health(broker_url)))
    results.append(("Unauthorized Access", test_unauthorized_access(broker_url)))
    results.append(("HTTP Proxy", test_http_proxy(broker_url, token, target_vnf)))
    results.append(("SSH Execution", test_ssh_execution(broker_url, token, target_vnf)))
    
    # Summary
    print("\n" + "="*60)
    print("Test Summary")
    print("="*60)
    passed = sum(1 for _, result in results if result)
    total = len(results)
    
    for test_name, result in results:
        status = "✓ PASS" if result else "✗ FAIL"
        print(f"  {status} - {test_name}")
    
    print(f"\nTotal: {passed}/{total} tests passed")
    print("="*60)
    
    sys.exit(0 if passed == total else 1)

if __name__ == '__main__':
    main()
