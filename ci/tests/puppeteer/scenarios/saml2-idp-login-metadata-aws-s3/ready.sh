#!/bin/bash
echo "Running SAML server..."

metadataDirectory="${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-md"
mkdir -p "${metadataDirectory}"

echo "Accessing CAS SAML2 identity provider metadata"
curl -k --connect-timeout 10 https://localhost:8443/cas/idp/metadata > "${metadataDirectory}/idp-metadata.xml"
curl -k --connect-timeout 10 https://localhost:8443/cas/idp/metadata/signingCertificate > "${metadataDirectory}/idp-signing.crt"
curl -k --connect-timeout 10 https://localhost:8443/cas/idp/metadata/encryptionCertificate > "${metadataDirectory}/idp-encryption.crt"

cert=$(cat "${metadataDirectory}"/idp-signing.crt | sed 's/-----BEGIN CERTIFICATE-----//g' | sed 's/-----END CERTIFICATE-----//g')
export IDP_SIGNING_CERTIFICATE=$cert
echo -e "Using signing certificate:\n$IDP_SIGNING_CERTIFICATE"

cert=$(cat "${metadataDirectory}"/idp-encryption.crt | sed 's/-----BEGIN CERTIFICATE-----//g' | sed 's/-----END CERTIFICATE-----//g')
export IDP_ENCRYPTION_CERTIFICATE=$cert
echo -e "Using encryption certificate:\n$IDP_ENCRYPTION_CERTIFICATE"

chmod +x "${PWD}/ci/tests/saml2/run-saml-server.sh"
"${PWD}/ci/tests/saml2/run-saml-server.sh"
