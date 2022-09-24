#!/bin/bash

echo "Launching SAML2 service provider..."
SCENARIO_FOLDER=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
docker stop saml2-sp || true && docker rm saml2-sp || true
docker run -p 9876:9876 -p 8076:8076 \
  -d -it --rm --name=saml2-sp \
  -e METADATA_TYPE=metadataFile \
  -e METADATA_LOCATION=/sp-webapp/src/main/resources/metadata/sp-metadata.xml \
  -e IDP_METADATA_TYPE=idpMetadataFile \
  -e AUTHN_CONTEXT=https://refeds.org/profile/mfa \
  -e IDP_METADATA=/sp-webapp/idp-metadata.xml \
  -v ${SCENARIO_FOLDER}/saml-md/idp-metadata.xml:/sp-webapp/idp-metadata.xml \
  -v ${CAS_KEYSTORE}:/etc/cas/thekeystore apereo/saml2-sp:latest

docker logs -f saml2-sp &
