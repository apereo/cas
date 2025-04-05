#!/bin/bash

echo -e "Removing previous SAML metadata directory from ${SCENARIO_FOLDER}"
rm -Rf "${SCENARIO_FOLDER}/saml-md"
rm -Rf "${SCENARIO_FOLDER}/saml-sp"

echo -e "Fetching InCommon signing certificate..."
mkdir ${SCENARIO_FOLDER}/saml-md
curl --retry 5 --retry-delay 2 --retry-connrefused --max-time 30 -L https://md.incommon.org/certs/inc-md-cert-mdq.pem -o "${SCENARIO_FOLDER}/saml-md/inc-md-cert-mdq.pem"
