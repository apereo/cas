#!/bin/bash

echo -e "Removing previous SAML metadata directory"
SCENARIO_FOLDER=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
rm -Rf "${SCENARIO_FOLDER}/saml-md"
rm -Rf ${SCENARIO_FOLDER}/saml-sp
