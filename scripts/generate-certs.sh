#!/bin/bash
set -e

# Define directories
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
CERT_DIR_ORDER="$PROJECT_ROOT/order-service/src/main/resources/certificates"
CERT_DIR_INVENTORY="$PROJECT_ROOT/inventory-service/src/main/resources/certificates"

# Ensure directories exist
mkdir -p "$CERT_DIR_ORDER"
mkdir -p "$CERT_DIR_INVENTORY"

# Clean up existing certificates if they exist
rm -f "$CERT_DIR_ORDER/keystore.p12"
rm -f "$CERT_DIR_ORDER/truststore.p12"
rm -f "$CERT_DIR_INVENTORY/keystore.p12"
rm -f "$CERT_DIR_INVENTORY/truststore.p12"

echo "Generating self-signed certificate and keystore..."
keytool -genkeypair \
  -alias wiremock-demo \
  -keyalg RSA \
  -keysize 2048 \
  -storetype PKCS12 \
  -keystore "$CERT_DIR_ORDER/keystore.p12" \
  -storepass changeit \
  -keypass changeit \
  -dname "CN=localhost, OU=Test, O=MinicDesign, L=Melbourne, ST=VIC, C=AU" \
  -validity 3650 \
  -ext "SAN=dns:localhost,ip:127.0.0.1" \
  -noprompt

echo "Exporting public certificate..."
keytool -exportcert \
  -alias wiremock-demo \
  -keystore "$CERT_DIR_ORDER/keystore.p12" \
  -storepass changeit \
  -file "$SCRIPT_DIR/wiremock-demo.crt" \
  -noprompt

echo "Importing public certificate into truststore..."
keytool -importcert \
  -alias wiremock-demo \
  -keystore "$CERT_DIR_ORDER/truststore.p12" \
  -storepass changeit \
  -file "$SCRIPT_DIR/wiremock-demo.crt" \
  -noprompt

# Clean up temp cert file
rm -f "$SCRIPT_DIR/wiremock-demo.crt"

echo "Copying keystore and truststore to inventory-service..."
cp "$CERT_DIR_ORDER/keystore.p12" "$CERT_DIR_INVENTORY/keystore.p12"
cp "$CERT_DIR_ORDER/truststore.p12" "$CERT_DIR_INVENTORY/truststore.p12"

echo "Certificates successfully generated and synchronized!"
