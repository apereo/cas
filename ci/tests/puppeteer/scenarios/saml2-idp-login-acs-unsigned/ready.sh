#!/bin/bash

echo "Launching SAML2 service provider..."
SCENARIO_FOLDER=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
docker run -p 9876:9876 -p 8076:8076 \
  -d -it --rm --name=saml2-sp \
  -e METADATA_TYPE=metadataFile \
  -e METADATA_LOCATION=/sp-webapp/src/main/resources/metadata/sp-metadata.xml \
  -e SIGN_AUTHN_REQUESTS=false \
  -e IDP_METADATA_TYPE=idpMetadataFile \
  -e IDP_METADATA=/sp-webapp/idp-metadata.xml \
  -e ACS_URL=https://httpbin.org/post \
  -v ${SCENARIO_FOLDER}/saml-md/idp-metadata.xml:/sp-webapp/idp-metadata.xml \
  -v ${CAS_KEYSTORE}:/etc/cas/thekeystore apereo/saml2-sp:latest
