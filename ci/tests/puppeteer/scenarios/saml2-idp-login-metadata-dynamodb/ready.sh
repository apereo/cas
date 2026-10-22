#!/bin/bash
metadataDirectory="${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-md"
mkdir -p "${metadataDirectory}"

echo "Fetching CAS SAML2 identity provider metadata, kept in DynamoDb"
curl -k -fsS --connect-timeout 10 https://localhost:8443/cas/idp/metadata > "${metadataDirectory}/idp-metadata.xml" || exit 1
curl -k -fsS --connect-timeout 10 https://localhost:8443/cas/idp/metadata/signingCertificate > "${metadataDirectory}/idp-signing.crt" || exit 1
curl -k -fsS --connect-timeout 10 https://localhost:8443/cas/idp/metadata/encryptionCertificate > "${metadataDirectory}/idp-encryption.crt" || exit 1

export IDP_SIGNING_CERTIFICATE=$(sed '/-----/d' "${metadataDirectory}/idp-signing.crt")
export IDP_ENCRYPTION_CERTIFICATE=$(sed '/-----/d' "${metadataDirectory}/idp-encryption.crt")
"${PWD}/ci/tests/saml2/run-saml-server.sh" || exit 1

METADATA_URL="http://localhost:9443/simplesaml/module.php/saml/sp/metadata.php/default-sp"
MANAGER_URL="https://localhost:8443/cas/actuator/samlIdPRegisteredServiceMetadata/managers/DynamoDbSamlRegisteredServiceMetadataResolver"

echo "Uploading SAML2 service provider metadata from ${METADATA_URL} to ${MANAGER_URL}"
xml="$(curl -k -fsS "${METADATA_URL}")" || exit 1
payload="$(jq -n --arg name "SimpleSAMLphp" --arg value "${xml}" '{name:$name, value:$value}')"
curl -k -fsS -X POST "${MANAGER_URL}" -H "Content-Type: application/json" -d "${payload}" >/dev/null || exit 1
echo "Uploaded SAML2 service provider metadata"
