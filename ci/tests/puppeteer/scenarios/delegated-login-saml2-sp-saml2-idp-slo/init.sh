#!/bin/bash

${PWD}/ci/tests/httpbin/run-httpbin-server.sh

${PWD}/ci/tests/keycloak/run-keycloak-server.sh

echo -e "Removing previous SAML metadata directory, if any, for ${SCENARIO}"
rm -Rf "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-md"
echo -e "Creating SAML metadata directory for ${SCENARIO}"
mkdir "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-md"
chmod +x "${PWD}/ci/tests/saml2/run-saml-server.sh"
"${PWD}/ci/tests/saml2/run-saml-server.sh"
