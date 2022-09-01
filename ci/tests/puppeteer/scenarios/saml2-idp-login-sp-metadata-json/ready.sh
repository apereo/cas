#!/bin/bash

metadataDirectory="${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-md"
jsonMetadata="${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-sp-metadata.json"

echo "Copying $jsonMetadata to $metadataDirectory"
cp "$jsonMetadata" "$metadataDirectory"
