#!/bin/bash

set -e
echo Starting Spring Cloud Config Server

docker stop configserver || true && docker rm configserver || true

CONFIG_SOURCE="${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/configserver"
echo "Spring Cloud configuration directory: ${CONFIG_SOURCE}"

rm -f "${CONFIG_SOURCE}/thekeystore"
cp "$CAS_KEYSTORE" "${CONFIG_SOURCE}"
CONFIG_SERVER_KEYSTORE="file:///etc/cas/configserver/thekeystore"
TARGET_DIR="/etc/cas/configserver"

#  -e DEBUG=true \
#  -e JVM_DEBUG=true \
#  -p 5000:5000 \

docker run --rm -d \
  --name=configserver \
  --mount type=bind,source="${CONFIG_SOURCE}",target="${TARGET_DIR}" \
  -e SERVER_SSL_ENABLED=false \
  -e ENCRYPT_KEY-STORE_LOCATION="${CONFIG_SERVER_KEYSTORE}" \
  -e SPRING_CLOUD_CONFIG_SERVER_NATIVE_SEARCH-LOCATIONS="file://${TARGET_DIR}" \
  -e SPRING_SECURITY_USER_NAME=casuser \
  -e SPRING_SECURITY_USER_PASSWORD=Mellon \
  -e ENTRYPOINT_DEBUG=true \
  -p 8888:8888 \
  apereo/cas-config-server:6.4.4.2
echo "Waiting for Spring Cloud Config Server..."
sleep 5

"${PWD}"/ci/tests/httpbin/run-httpbin-server.sh

echo -e "\n\nReady!"
