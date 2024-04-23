#!/bin/bash

echo -e "Removing previous SAML metadata directory from ${SCENARIO_FOLDER}"
rm -Rf "${SCENARIO_FOLDER}/saml-md"
rm -Rf "${SCENARIO_FOLDER}/saml-sp"

if [[ -z $TMPDIR ]]; then
  TMPDIR=${TEMP:-/tmp}
fi
echo -e "Fetching InCommon MDQ signing certificate..."
cd $TMPDIR && pwd
mkdir ${SCENARIO_FOLDER}/saml-md
curl -o inc-md-cert-mdq.pem http://md.incommon.org/certs/inc-md-cert-mdq.pem
