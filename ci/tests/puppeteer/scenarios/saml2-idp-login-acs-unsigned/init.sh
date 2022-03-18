#!/bin/bash
SCENARIO_FOLDER=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
BRANCH=${SAMLSP_BRANCH:-master}
GROUP=${SAMLSP_GROUP:-apereo}
echo -e "Removing previous SAML metadata directory"
rm -Rf "${SCENARIO_FOLDER}/saml-md"
rm -Rf ${SCENARIO_FOLDER}/saml-sp
echo -e "Installing SAML2 SP..."
echo "${SCENARIO_FOLDER}/${BRANCH}.zip"
curl -L https://github.com/$GROUP/saml2-sample-java-webapp/archive/refs/heads/${BRANCH}.zip \
  -o "${SCENARIO_FOLDER}/${BRANCH}.zip" \
  && unzip "${SCENARIO_FOLDER}/${BRANCH}.zip" -d "${SCENARIO_FOLDER}" \
  && mv "${SCENARIO_FOLDER}/saml2-sample-java-webapp-${BRANCH}" "${SCENARIO_FOLDER}/saml-sp" \
  && chmod +x "${SCENARIO_FOLDER}/saml-sp/gradlew" \
  && [ -f ./gradlew ] \
  && echo "Cloned SAML2 SP project." \
  && rm "${SCENARIO_FOLDER}/${BRANCH}.zip"
