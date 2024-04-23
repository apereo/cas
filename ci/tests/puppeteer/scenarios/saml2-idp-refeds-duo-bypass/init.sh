#!/bin/bash

echo -e "Removing previous SAML metadata directory from ${SCENARIO_FOLDER}"
rm -Rf "${SCENARIO_FOLDER}/saml-md"
rm -Rf "${SCENARIO_FOLDER}/saml-sp"

