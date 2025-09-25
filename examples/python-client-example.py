#!/usr/bin/env python3
"""
OAuth2 Client Registration Example in Python

This script demonstrates how to:
1. Register a new OAuth2 client
2. Generate access tokens
3. Validate tokens
4. Manage client lifecycle

Requirements:
    pip install requests

Usage:
    python python-client-example.py
"""

import requests
import json
import time
import sys
from typing import Dict, Optional


class OAuth2ClientManager:
    """OAuth2 Client Registration and Management"""
    
    def __init__(self, auth_server_url: str = "http://localhost:9000/auth"):
        self.auth_server_url = auth_server_url.rstrip('/')
        self.session = requests.Session()
    
    def check_server_health(self) -> bool:
        """Check if the authorization server is running"""
        try:
            response = self.session.get(f"{self.auth_server_url}/api/health", timeout=5)
            return response.status_code == 200
        except requests.RequestException:
            return False
    
    def register_client(self, client_data: Dict) -> Optional[Dict]:
        """Register a new OAuth2 client"""
        url = f"{self.auth_server_url}/api/v1/clients"
        headers = {'Content-Type': 'application/json'}
        
        try:
            response = self.session.post(url, json=client_data, headers=headers)
            response.raise_for_status()
            return response.json()
        except requests.RequestException as e:
            print(f"❌ Failed to register client: {e}")
            if hasattr(e, 'response') and e.response is not None:
                print(f"Response: {e.response.text}")
            return None
    
    def get_access_token(self, client_id: str, client_secret: str, scope: str) -> Optional[Dict]:
        """Get access token using client credentials grant"""
        url = f"{self.auth_server_url}/oauth2/token"
        
        data = {
            'grant_type': 'client_credentials',
            'client_id': client_id,
            'client_secret': client_secret,
            'scope': scope
        }
        
        try:
            response = self.session.post(url, data=data)
            response.raise_for_status()
            return response.json()
        except requests.RequestException as e:
            print(f"❌ Failed to get access token: {e}")
            if hasattr(e, 'response') and e.response is not None:
                print(f"Response: {e.response.text}")
            return None
    
    def introspect_token(self, access_token: str) -> Optional[Dict]:
        """Introspect an access token"""
        url = f"{self.auth_server_url}/oauth2/introspect"
        
        data = {'token': access_token}
        
        try:
            response = self.session.post(url, data=data)
            response.raise_for_status()
            return response.json()
        except requests.RequestException as e:
            print(f"❌ Failed to introspect token: {e}")
            return None
    
    def list_clients(self) -> Optional[Dict]:
        """List all registered clients"""
        url = f"{self.auth_server_url}/api/clients"
        
        try:
            response = self.session.get(url)
            response.raise_for_status()
            return response.json()
        except requests.RequestException as e:
            print(f"❌ Failed to list clients: {e}")
            return None
    
    def get_jwks(self) -> Optional[Dict]:
        """Get JSON Web Key Set"""
        url = f"{self.auth_server_url}/.well-known/jwks.json"
        
        try:
            response = self.session.get(url)
            response.raise_for_status()
            return response.json()
        except requests.RequestException as e:
            print(f"❌ Failed to get JWKS: {e}")
            return None


def print_section(title: str):
    """Print a formatted section header"""
    print(f"\n{'='*50}")
    print(f"🔧 {title}")
    print('='*50)


def print_success(message: str):
    """Print a success message"""
    print(f"✅ {message}")


def print_error(message: str):
    """Print an error message"""
    print(f"❌ {message}")


def print_info(message: str):
    """Print an info message"""
    print(f"ℹ️  {message}")


def main():
    """Main demonstration function"""
    print("🚀 OAuth2 Client Registration Demo")
    print("==================================")
    
    # Initialize client manager
    client_manager = OAuth2ClientManager()
    
    # Check server health
    print_section("Server Health Check")
    if not client_manager.check_server_health():
        print_error("Authorization server is not running!")
        print_info("Please start the server first: ./quick-start.sh")
        sys.exit(1)
    
    print_success("Authorization server is running")
    
    # Example client registrations
    clients_to_register = [
        {
            "clientName": "Python Data Processing Service",
            "tenantId": "platform",
            "scopes": ["read:data", "write:reports", "read:analytics"],
            "description": "Python-based data processing and analytics service",
            "contactEmail": "data-team@company.com",
            "grantTypes": ["client_credentials"]
        },
        {
            "clientName": "Python Payment Gateway",
            "tenantId": "retail-banking",
            "scopes": ["read:accounts", "write:transactions", "read:customer-data"],
            "description": "Python payment gateway integration service",
            "contactEmail": "payments-team@company.com",
            "grantTypes": ["client_credentials"]
        },
        {
            "clientName": "Python Monitoring Service",
            "tenantId": "platform",
            "scopes": ["read:metrics", "write:alerts", "read:logs"],
            "description": "System monitoring and alerting service",
            "contactEmail": "ops-team@company.com",
            "grantTypes": ["client_credentials"],
            "accessTokenValiditySeconds": 7200
        }
    ]
    
    registered_clients = []
    
    # Register clients
    print_section("Client Registration")
    for i, client_data in enumerate(clients_to_register, 1):
        print(f"\n📝 Registering client {i}: {client_data['clientName']}")
        
        result = client_manager.register_client(client_data)
        if result:
            print_success(f"Client registered successfully")
            print(f"   Client ID: {result['clientId']}")
            print(f"   Client Secret: {result['clientSecret'][:20]}...")
            print(f"   Tenant: {result['tenantId']}")
            print(f"   Scopes: {', '.join(result['scopes'])}")
            
            registered_clients.append(result)
        else:
            print_error(f"Failed to register client: {client_data['clientName']}")
    
    if not registered_clients:
        print_error("No clients were registered successfully")
        sys.exit(1)
    
    # Test token generation
    print_section("Token Generation Tests")
    for client in registered_clients:
        print(f"\n🎫 Testing token generation for: {client['clientName']}")
        
        # Use the first scope for testing
        test_scope = client['scopes'][0] if client['scopes'] else ""
        
        token_response = client_manager.get_access_token(
            client['clientId'],
            client['clientSecret'],
            test_scope
        )
        
        if token_response:
            print_success("Token generated successfully")
            print(f"   Token Type: {token_response['token_type']}")
            print(f"   Expires In: {token_response['expires_in']} seconds")
            print(f"   Scope: {token_response.get('scope', 'N/A')}")
            print(f"   Token: {token_response['access_token'][:50]}...")
            
            # Test token introspection
            print(f"\n🔍 Testing token introspection...")
            introspection = client_manager.introspect_token(token_response['access_token'])
            
            if introspection:
                if introspection.get('active'):
                    print_success("Token is active and valid")
                    print(f"   Client ID: {introspection.get('client_id')}")
                    print(f"   Scope: {introspection.get('scope')}")
                    print(f"   Tenant: {introspection.get('tenant_id', 'N/A')}")
                else:
                    print_error("Token is inactive")
            else:
                print_error("Failed to introspect token")
        else:
            print_error(f"Failed to generate token for {client['clientName']}")
    
    # List all clients
    print_section("Client Listing")
    clients_list = client_manager.list_clients()
    if clients_list:
        print_success("Retrieved client list")
        if isinstance(clients_list, list):
            for client in clients_list:
                print(f"   • {client.get('clientId', 'N/A')} ({client.get('tenantId', 'N/A')})")
        else:
            print(f"   Total clients: {len(clients_list)}")
    else:
        print_error("Failed to retrieve client list")
    
    # Get JWKS
    print_section("JWKS Retrieval")
    jwks = client_manager.get_jwks()
    if jwks:
        print_success("Retrieved JWKS")
        print(f"   Keys count: {len(jwks.get('keys', []))}")
        for i, key in enumerate(jwks.get('keys', []), 1):
            print(f"   Key {i}: {key.get('kty')} - {key.get('alg')} - {key.get('use')}")
    else:
        print_error("Failed to retrieve JWKS")
    
    # Summary
    print_section("Demo Summary")
    print_success(f"Successfully registered {len(registered_clients)} clients")
    print_info("Registered clients:")
    for client in registered_clients:
        print(f"   • {client['clientId']} ({client['tenantId']})")
    
    print(f"\n🔗 Useful endpoints:")
    print(f"   Health: {client_manager.auth_server_url}/api/health")
    print(f"   Clients: {client_manager.auth_server_url}/api/clients")
    print(f"   Token: {client_manager.auth_server_url}/oauth2/token")
    print(f"   JWKS: {client_manager.auth_server_url}/.well-known/jwks.json")
    
    print(f"\n📝 Example token request:")
    if registered_clients:
        example_client = registered_clients[0]
        print(f"curl -X POST {client_manager.auth_server_url}/oauth2/token \\")
        print(f"  -H 'Content-Type: application/x-www-form-urlencoded' \\")
        print(f"  -d 'grant_type=client_credentials&client_id={example_client['clientId']}&client_secret={example_client['clientSecret']}&scope={example_client['scopes'][0] if example_client['scopes'] else ''}'")


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\n👋 Demo interrupted by user")
        sys.exit(0)
    except Exception as e:
        print(f"\n❌ Unexpected error: {e}")
        sys.exit(1)

