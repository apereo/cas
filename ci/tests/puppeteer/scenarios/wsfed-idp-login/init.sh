#!/bin/bash
echo "Running Fediz sample client web application using keystore ${CAS_KEYSTORE}"
docker run --rm -d -p9876:9876 -p8976:8076 \
  -v "${CAS_KEYSTORE}":/etc/cas/thekeystore \
  -e SP_SSL_KEYSTORE_PATH="/etc/cas/thekeystore"\
  --name "fediz" apereo/fediz-client-webapp
docker logs -f fediz &
sleep 15
until curl -k -L --output /dev/null --silent --fail https://localhost:9876/fediz; do
    printf '.'
    sleep 1
done
