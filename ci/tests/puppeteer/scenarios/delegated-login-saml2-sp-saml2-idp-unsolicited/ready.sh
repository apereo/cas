#!/bin/bash
echo "Running SAML server..."

metadataDirectory="${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-md"

cert=$(cat "${metadataDirectory}"/idp-signing.crt | sed 's/-----BEGIN CERTIFICATE-----//g' | sed 's/-----END CERTIFICATE-----//g')
export IDP_SIGNING_CERTIFICATE=$cert
echo -e "Using signing certificate:\n$IDP_SIGNING_CERTIFICATE"

cert=$(cat "${metadataDirectory}"/idp-encryption.crt | sed 's/-----BEGIN CERTIFICATE-----//g' | sed 's/-----END CERTIFICATE-----//g')
export IDP_ENCRYPTION_CERTIFICATE=$cert
echo -e "Using encryption certificate:\n$IDP_ENCRYPTION_CERTIFICATE"

export SP_ACS_SERVICE="https://localhost:8443/cas/login?service=https://localhost:9859/anything/cas&client_name=SAML2Client"

chmod +x "${PWD}/ci/tests/saml2/run-saml-server.sh"
"${PWD}/ci/tests/saml2/run-saml-server.sh"
