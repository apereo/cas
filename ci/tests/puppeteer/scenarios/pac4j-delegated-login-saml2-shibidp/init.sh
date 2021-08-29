#!/bin/bash
echo -e "Removing previous running containers, if any, for ${SCENARIO}"
docker stop $(docker container ls -aq) >/dev/null 2>&1 || true
docker rm $(docker container ls -aq) >/dev/null 2>&1 || true

VERSION="4.0.0"
BASE_URL="https://github.com/Unicon/shib-cas-authn/releases/download/${VERSION}"
SHIBCAS_AUTHN_DIR="${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/shibcasauthn"

echo -e "Removing previous SAML metadata directory, if any, for ${SCENARIO}"
rm -Rf "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-md"
echo -e "Creating SAML metadata directory for ${SCENARIO}"
mkdir "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-md"
rm -Rf ${SHIBCAS_AUTHN_DIR} >/dev/null 2>&1
mkdir ${SHIBCAS_AUTHN_DIR} >/dev/null 2>&1

echo -e "Fetching shib-cas-authn release artifacts for v${VERSION}"
wget "${BASE_URL}/cas-client-core-3.6.0.jar" -P ${SHIBCAS_AUTHN_DIR}  >/dev/null 2>&1
wget "${BASE_URL}/no-conversation-state.jsp" -P ${SHIBCAS_AUTHN_DIR}  >/dev/null 2>&1
wget "${BASE_URL}/shib-cas-authenticator-${VERSION}.jar" -P ${SHIBCAS_AUTHN_DIR}  >/dev/null 2>&1

echo -e "Building Shibboleth IdP..."
docker build \
  --progress="auto" \
  --file="${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/Dockerfile" \
  -t cas/shibidp:latest \
  "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}"

echo -e "Running Shibboleth IdP..."
docker run --name shibidp -d --rm -p 9443:443 cas/shibidp:latest
clear
docker logs -f shibidp &
echo -e "Waiting for Shibboleth IdP..."
until curl -k -L --output /dev/null --silent --fail https://localhost:9443/idp/shibboleth; do
    echo -n '.'
    sleep 1
done
echo -e "\n\nReady!"
echo "Fetching Shibboleth IdP metadata..."
docker cp shibidp:/opt/shibboleth-idp/metadata/idp-metadata.xml \
  "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-md/idp-metadata.xml"
