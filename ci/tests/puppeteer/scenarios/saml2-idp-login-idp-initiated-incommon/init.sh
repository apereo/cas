#!/bin/bash
SCENARIO_FOLDER=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
echo -e "Removing previous SAML metadata directory"
rm -Rf "${SCENARIO_FOLDER}/saml-md"

echo -e "Fetching InCommon signing certificate..."
mkdir ${SCENARIO_FOLDER}/saml-md
curl -L http://md.incommon.org/certs/inc-md-cert-mdq.pem -o "${SCENARIO_FOLDER}/saml-md/inc-md-cert-mdq.pem"
