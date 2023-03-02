#!/bin/bash

echo -e "Removing previous SAML metadata directory from ${SCENARIO_FOLDER}"
rm -Rf "${SCENARIO_FOLDER}/saml-md"
rm -Rf "${SCENARIO_FOLDER}/saml-sp"

echo -e "Fetching InCommon signing certificate..."
mkdir ${SCENARIO_FOLDER}/saml-md
curl -L http://md.incommon.org/certs/inc-md-cert-mdq.pem -o "${SCENARIO_FOLDER}/saml-md/inc-md-cert-mdq.pem"
