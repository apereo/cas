#!/bin/bash

export DOCKER_IMAGE="apereo/saml2-sp:latest"

if [[ -n "${AUTHN_CONTEXT}" ]]; then
  echo -e "Found requested authn context: ${AUTHN_CONTEXT}"
fi

if [[ -n "${SIGN_AUTHN_REQUESTS}" ]]; then
  echo -e "Found requested sign authn request flag: ${SIGN_AUTHN_REQUESTS}"
fi

if [[ -n "${ACS_URL}" ]]; then
  echo -e "Found requested ACS url: ${ACS_URL}"
fi

echo "Launching SAML2 service provider from scenario ${SCENARIO_FOLDER}"

echo "Accessing CAS SAML2 identity provider metadata"
curl -k -I https://localhost:8443/cas/idp/metadata

chmod +x ${PWD}/ci/tests/httpbin/run-httpbin-server.sh
${PWD}/ci/tests/httpbin/run-httpbin-server.sh

echo "Launching SAML2 service provider..."
docker stop saml2-sp || true && docker rm saml2-sp || true
docker run -p 9876:9876 -p 8076:8076 \
  -d -it --rm --name=saml2-sp \
  -e METADATA_TYPE=metadataFile \
  -e METADATA_LOCATION=/sp-webapp/src/main/resources/metadata/sp-metadata.xml \
  -e IDP_METADATA_TYPE=idpMetadataFile \
  -e IDP_METADATA=/sp-webapp/idp-metadata.xml \
  -e AUTHN_CONTEXT="${AUTHN_CONTEXT}" \
  -e SIGN_AUTHN_REQUESTS=${SIGN_AUTHN_REQUESTS} \
  -e ACS_URL="${ACS_URL}" \
  -v "${SCENARIO_FOLDER}"/saml-md/idp-metadata.xml:/sp-webapp/idp-metadata.xml \
  -v "${CAS_KEYSTORE}":/etc/cas/thekeystore \
  ${DOCKER_IMAGE}
docker logs -f saml2-sp &
sleep 30
docker ps | grep "saml2-sp"
retVal=$?
if [ $retVal == 0 ]; then
    echo "SAML2 SP docker container is running."
else
    echo "SAML2 SP docker container failed to start."
    exit $retVal
fi

docker logs -f saml2-sp &


