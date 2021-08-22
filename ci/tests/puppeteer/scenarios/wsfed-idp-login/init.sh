#!/bin/bash
rm -Rf ${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/wsfed-sp
echo -e "Installing WSFED SP..."
wget https://github.com/apereo/wsfed-sample-java-webapp/archive/refs/heads/master.zip \
  -P "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}" &> /dev/null \
  && unzip "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/master.zip" -d "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}" &> /dev/null \
  && mv "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/wsfed-sample-java-webapp-master" "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/wsfed-sp" \
  && chmod +x "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/wsfed-sp/gradlew" \
  && [ -f ./gradlew ] \
  && echo "Cloned WSFED SP project." \
  && rm "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/master.zip"
