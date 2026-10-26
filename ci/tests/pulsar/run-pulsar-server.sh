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

printgreen "Running Pulsar docker container..."
export DOCKER_IMAGE="apachepulsar/pulsar:5.0.1"
docker stop pulsar-server || true

docker run --quiet  --rm -d --name "pulsar-server" \
  -p 6650:6650 -p 9988:8080 \
  ${DOCKER_IMAGE} bin/pulsar standalone -a 127.0.0.1

printgreen "Waiting for the Pulsar broker to report itself healthy..."
for _ in $(seq 1 60); do
  if curl -sf "http://localhost:9988/admin/v2/brokers/health" > /dev/null; then
    printgreen "Pulsar docker container is running."
    break
  fi
  sleep 2
done
if ! curl -sf "http://localhost:9988/admin/v2/brokers/health" > /dev/null; then
  printred "Pulsar broker did not become healthy in time."
  docker logs pulsar-server | tail -50
  exit 1
fi
