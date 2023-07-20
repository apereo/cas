#!/bin/bash

echo "Accessing CAS SAML2 identity provider metadata"
curl -k -I https://localhost:8443/cas/idp/metadata

metadataDirectory="${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-md"
jsonMetadata="${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-sp-metadata.json"

echo "Copying $jsonMetadata to $metadataDirectory"
cp "$jsonMetadata" "$metadataDirectory"
