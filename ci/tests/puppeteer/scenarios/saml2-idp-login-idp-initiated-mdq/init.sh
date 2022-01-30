#!/bin/bash
SCENARIO_FOLDER=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
echo -e "Removing previous SAML metadata directory"
rm -Rf "${SCENARIO_FOLDER}/saml-md"

if [[ -z $TMPDIR ]]; then
  TMPDIR=${TEMP:-/tmp}
fi
echo -e "Fetching InCommon MDQ signing certificate..."
cd $TMPDIR && pwd
mkdir ${SCENARIO_FOLDER}/saml-md
curl -o inc-md-cert-mdq.pem http://md.incommon.org/certs/inc-md-cert-mdq.pem
