#!/bin/bash
echo -e "Removing previous SAML metadata directory"
rm -Rf "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-md"

chmod +x ${PWD}/ci/tests/aws/run-aws-server.sh
${PWD}/ci/tests/aws/run-aws-server.sh
