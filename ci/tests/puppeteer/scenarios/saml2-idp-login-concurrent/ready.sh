#!/bin/bash
echo "Running SAML2 server..."

echo "Accessing CAS SAML2 identity provider metadata"
curl -k -I --connect-timeout 10 https://localhost:8443/cas/idp/metadata

metadataDirectory="${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-md"
echo "Metadata directory: ${metadataDirectory}" && ls ${metadataDirectory}

cert=$(cat "${metadataDirectory}"/idp-signing.crt | sed 's/-----BEGIN CERTIFICATE-----//g' | sed 's/-----END CERTIFICATE-----//g')
export IDP_SIGNING_CERTIFICATE=$cert
echo -e "Using signing certificate:\n$IDP_SIGNING_CERTIFICATE"

if [[ -z "${IDP_SIGNING_CERTIFICATE}" ]]; then
  echo "Unable to find SAML2 signing certificate at ${metadataDirectory}/idp-signing.crt"
  exit 1
fi
cert=$(cat "${metadataDirectory}"/idp-encryption.crt | sed 's/-----BEGIN CERTIFICATE-----//g' | sed 's/-----END CERTIFICATE-----//g')
export IDP_ENCRYPTION_CERTIFICATE=$cert
echo -e "Using encryption certificate:\n$IDP_ENCRYPTION_CERTIFICATE"
if [[ -z "${IDP_ENCRYPTION_CERTIFICATE}" ]]; then
  echo "Unable to find SAML2 signing certificate at ${metadataDirectory}/idp-encryption.crt"
  exit 1
fi

chmod +x "${PWD}/ci/tests/saml2/run-saml-server.sh"
"${PWD}/ci/tests/saml2/run-saml-server.sh"
