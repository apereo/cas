#!/bin/bash
echo -e "Removing previous SAML metadata directory"
rm -Rf "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-md"
mkdir "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-md"
echo -e "Removing previous SAML metadata backups"
rm -Rf "${PWD}/ci/tests/puppeteer/overlay/${SCENARIO}"
mkdir -p "${PWD}/ci/tests/puppeteer/overlay/${SCENARIO}"
