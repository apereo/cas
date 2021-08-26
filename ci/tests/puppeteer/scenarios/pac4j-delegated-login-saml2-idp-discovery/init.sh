#!/bin/bash
echo -e "Removing previous SAML metadata directory, if any, for ${SCENARIO}"
rm -Rf "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-md"
echo -e "Creating SAML metadata directory for ${SCENARIO}"
mkdir "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-md"

