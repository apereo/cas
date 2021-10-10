#!/bin/bash
echo -e "Removing previous SAML metadata directory, if any, for ${SCENARIO}"
rm -Rf "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-md"
echo -e "Creating SAML metadata directory for ${SCENARIO}"
mkdir "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-md"

echo -e "Mapping CAS keystore to ${CAS_KEYSTORE}"
docker run -d \
  --mount type=bind,source="${CAS_KEYSTORE}",target=/etc/cas/thekeystore \
  -e SPRING_APPLICATION_JSON='{"cas": {"service-registry": {"core": {"init-from-json": true} } } }' \
  -p 8444:8443 --name casserver apereo/cas:6.4.0
clear
docker logs -f casserver &
echo -e "Waiting for CAS..."
until curl -k -L --output /dev/null --silent --fail https://localhost:8444/cas/login; do
    echo -n '.'
    sleep 1
done
echo -e "\n\nReady!"
