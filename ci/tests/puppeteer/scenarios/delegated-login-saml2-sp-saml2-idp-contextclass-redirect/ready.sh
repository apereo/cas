#!/bin/bash
echo "Running SAML server..."

metadataDirectory="${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-md"

cert=$(cat "${metadataDirectory}"/idp-signing.crt | sed 's/-----BEGIN CERTIFICATE-----//g' | sed 's/-----END CERTIFICATE-----//g')
export IDP_SIGNING_CERTIFICATE=$cert
echo -e "Using signing certificate:\n$IDP_SIGNING_CERTIFICATE"

cert=$(cat "${metadataDirectory}"/idp-encryption.crt | sed 's/-----BEGIN CERTIFICATE-----//g' | sed 's/-----END CERTIFICATE-----//g')
export IDP_ENCRYPTION_CERTIFICATE=$cert
echo -e "Using encryption certificate:\n$IDP_ENCRYPTION_CERTIFICATE"

chmod +x "${PWD}/ci/tests/saml2/run-saml-server.sh"
"${PWD}/ci/tests/saml2/run-saml-server.sh"

echo "Launching SAML2 service provider..."
SCENARIO_FOLDER=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
docker run -p 9876:9876 -p 8076:8076 \
  -d -it --rm --name=saml2-sp \
  -e METADATA_TYPE=metadataFile \
  -e METADATA_LOCATION=/sp-webapp/src/main/resources/metadata/sp-metadata.xml \
  -e IDP_METADATA_TYPE=idpMetadataFile \
  -e AUTHN_CONTEXT=https://refeds.org/profile/mfa \
  -e IDP_METADATA=/sp-webapp/idp-metadata.xml \
  -v ${SCENARIO_FOLDER}/saml-md/idp-metadata.xml:/sp-webapp/idp-metadata.xml \
  -v ${CAS_KEYSTORE}:/etc/cas/thekeystore apereo/saml2-sp:latest

