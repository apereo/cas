#!/bin/bash
metadataDirectory="${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-md"

echo "Fetching CAS SAML2 identity provider metadata"
curl -k -fsS --connect-timeout 10 https://localhost:8443/cas/idp/metadata > /dev/null || exit 1
export IDP_SIGNING_CERTIFICATE=$(sed '/-----/d' "${metadataDirectory}/idp-signing.crt")
export IDP_ENCRYPTION_CERTIFICATE=$(sed '/-----/d' "${metadataDirectory}/idp-encryption.crt")
"${PWD}/ci/tests/saml2/run-saml-server.sh" || exit 1
