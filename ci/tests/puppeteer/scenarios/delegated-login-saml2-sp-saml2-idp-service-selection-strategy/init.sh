#!/bin/bash
SCENARIO_FOLDER=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
echo -e "Removing previous SAML metadata directory"
rm -Rf "${SCENARIO_FOLDER}/saml-md"
rm -Rf ${SCENARIO_FOLDER}/saml-sp
