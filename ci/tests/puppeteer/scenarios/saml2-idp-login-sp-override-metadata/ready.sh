#!/bin/bash
echo "Running SAML server..."

metadataDirectory="${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-md/Sample-1"

echo "Accessing CAS SAML2 identity provider metadata"
curl -k -I https://localhost:8443/cas/idp/metadata

cert=$(cat "${metadataDirectory}"/idp-signing.crt | sed 's/-----BEGIN CERTIFICATE-----//g' | sed 's/-----END CERTIFICATE-----//g')
export IDP_SIGNING_CERTIFICATE=$cert
echo -e "Using signing certificate:\n$IDP_SIGNING_CERTIFICATE"

cert=$(cat "${metadataDirectory}"/idp-encryption.crt | sed 's/-----BEGIN CERTIFICATE-----//g' | sed 's/-----END CERTIFICATE-----//g')
export IDP_ENCRYPTION_CERTIFICATE=$cert
echo -e "Using encryption certificate:\n$IDP_ENCRYPTION_CERTIFICATE"

export IDP_ENTITYID="https://cas.apereo.org/custom/idp/21c826665039536e"
echo -e "Using entity id:\n$IDP_ENTITYID"

chmod +x "${PWD}/ci/tests/saml2/run-saml-server.sh"
"${PWD}/ci/tests/saml2/run-saml-server.sh"
