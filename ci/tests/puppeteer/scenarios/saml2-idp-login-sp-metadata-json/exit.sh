#!/bin/bash

echo -e "Removing previous SAML metadata directory"
rm -Rf "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-md/metadata-backups"
rm -Rf "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}"/saml-md/idp-*
