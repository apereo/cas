#!/bin/bash
echo "Running SAML server..."

metadataDirectory="${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-md"

echo "Accessing CAS SAML2 identity provider metadata"
curl -k -I https://localhost:8443/cas/idp/metadata
