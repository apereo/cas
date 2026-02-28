#!/bin/bash

RED="\e[31m"
GREEN="\e[32m"
ENDCOLOR="\e[0m"

function printred() {
  printf "🔥 ${RED}$1${ENDCOLOR}\n"
}
function printgreen() {
  printf "🍀 ${GREEN}$1${ENDCOLOR}\n"
}
printgreen "Running SAML server..."

printgreen "Accessing CAS SAML2 identity provider metadata"
curl -k -I --connect-timeout 10 https://localhost:8443/cas/idp/metadata

metadataDirectory="${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-md"

cert=$(cat "${metadataDirectory}"/idp-signing.crt | sed 's/-----BEGIN CERTIFICATE-----//g' | sed 's/-----END CERTIFICATE-----//g')
export IDP_SIGNING_CERTIFICATE=$cert
printgreen "Using signing certificate:\n$IDP_SIGNING_CERTIFICATE"

cert=$(cat "${metadataDirectory}"/idp-encryption.crt | sed 's/-----BEGIN CERTIFICATE-----//g' | sed 's/-----END CERTIFICATE-----//g')
export IDP_ENCRYPTION_CERTIFICATE=$cert
printgreen "Using encryption certificate:\n$IDP_ENCRYPTION_CERTIFICATE"

"${PWD}/ci/tests/saml2/run-saml-server.sh"

METADATA_URL="http://localhost:9443/simplesaml/module.php/saml/sp/metadata.php/default-sp"

BASE_URL="https://localhost:8443/cas/actuator/samlIdPRegisteredServiceMetadata/managers"

printgreen "Downloading SAML service provider metadata from ${METADATA_URL}"
xml="$(curl -k -fsS "$METADATA_URL")"
payload="$(jq -n --arg name "MyProvider" --arg value "$xml" '{name:$name, value:$value}')"

curl -k ${BASE_URL} -H "Content-Type: application/x-www-form-urlencoded" | jq

printgreen "Uploading SAML service provider metadata to ${BASE_URL}/MongoDbSamlRegisteredServiceMetadataResolver"
curl -k -fsS -X POST "${BASE_URL}/MongoDbSamlRegisteredServiceMetadataResolver" \
  -H 'Content-Type: application/json' -d "$payload" >/dev/null 2>&1
if [ $? -ne 0 ]; then
  printred "Failed to upload SAML service provider metadata"
  exit 1
fi
printgreen "Uploaded SAML service provider metadata"
