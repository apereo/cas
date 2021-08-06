#!/bin/bash

rm -Rf ${PWD}/saml2-sp
git clone --depth 1 --branch master --single-branch \
  git@github.com:apereo/saml2-sample-java-webapp.git \
  ${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml2-sp

cd ${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml2-sp
echo "Launching SAML2 SP with keystore ${CAS_KEYSTORE}"
./gradlew -q --no-daemon -DidpMetadataType=idpMetadataUrl \
  -DidpMetadata=http://localhost:8888/cas/idp/metadata \
  -Dsp.sslKeystorePath="${CAS_KEYSTORE}" &

echo -e "\nWaiting for SAML2 SP..."
until curl -k -L --output /dev/null --silent --fail https://localhost:9876/sp/saml/status; do
    echo -n '.'
    sleep 1
done
