#!/bin/bash
echo -e "Removing previous SAML metadata directory"
rm -Rf "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-md"
rm -Rf ${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-sp
wget https://github.com/apereo/saml2-sample-java-webapp/archive/refs/heads/master.zip \
  -P "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}" &> /dev/null \
  && unzip "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/master.zip" -d "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}" &> /dev/null \
  && mv "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml2-sample-java-webapp-master" "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-sp" \
  && chmod +x "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-sp/gradlew" \
  && [ -f ./gradlew ] \
  && echo "Cloned SAML2 SP project." \
  && rm "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/master.zip"
