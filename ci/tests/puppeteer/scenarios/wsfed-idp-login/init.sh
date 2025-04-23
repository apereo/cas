#!/bin/bash
echo "Running Fediz sample client web application using keystore ${CAS_KEYSTORE}"
docker run --rm -d -p9876:9876 -p5008:5008 -p8976:8076 \
  -v "${CAS_KEYSTORE}":/etc/cas/thekeystore \
  -e SP_SSL_KEYSTORE_PATH="/etc/cas/thekeystore"\
  --name "fediz" apereo/fediz-client-webapp:2025
docker logs -f fediz &
sleep 15
docker ps | grep "fediz"
retVal=$?
if [ $retVal == 0 ]; then
    echo "Fediz docker container is running."
    docker logs -f fediz &
else
    echo "Fediz docker container failed to start."
fi
echo "Waiting for Fediz to respond..."
until curl -k -L --output /dev/null --silent --fail https://localhost:9876/fediz; do
    printf '.'
    sleep 1
done
echo "Fediz is up and running."
