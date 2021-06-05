#!/bin/bash
echo -e "Removing previous SAML metadata directory, if any"
rm -Rf "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-md"
echo -e "Creating SAML metadata directory"
mkdir "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-md"

