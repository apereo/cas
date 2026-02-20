#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

RED="\e[31m"
GREEN="\e[32m"
ENDCOLOR="\e[0m"

function printred() {
  printf "🔥 ${RED}$1${ENDCOLOR}\n"
}
function printgreen() {
  printf "☘️  ${GREEN}$1${ENDCOLOR}\n"
}

echo "Running Pulsar docker container..."
export DOCKER_IMAGE="apachepulsar/pulsar:4.1.3"
docker stop pulsar-server || true

docker run --rm -d --name "pulsar-server" \
  -p 6650:6650 -p 9988:8080 \
  ${DOCKER_IMAGE} bin/pulsar standalone
sleep 5
docker ps | grep "pulsar-server"
retVal=$?
if [ $retVal == 0 ]; then
    printgreen "Pulsar docker container is running."
else
    printred "Pulsar docker container failed to start."
    exit $retVal
fi
