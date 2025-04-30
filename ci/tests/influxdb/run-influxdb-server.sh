#!/bin/bash

set -e
if [[ -z $TMPDIR ]]; then
  TMPDIR=${TEMP:-/tmp}
fi
GREEN="\e[32m"
ENDCOLOR="\e[0m"

function printgreen() {
  printf "🍀 ${GREEN}$1${ENDCOLOR}\n"
}

export DOCKER_IMAGE="quay.io/influxdb/influxdb3-core:latest"
printgreen "Running InfluxDb docker container..."
docker stop influxdb-server || true && docker rm influxdb-server || true
docker run -d --rm --name="influxdb-server" \
  -p 8181:8181 -p 8083:8083 -p 8086:8086 \
  -e INFLUXDB3_AUTH_TOKEN="${INFLUXDB3_AUTH_TOKEN}" \
  ${DOCKER_IMAGE} serve --node-id=node0 --object-store=memory
sleep 5
docker ps | grep "influxdb-server"
retVal=$?
if [ $retVal == 0 ]; then
    printgreen "InfluxDb docker container is running."
else
    echo "InfluxDb docker container failed to start."
    exit $retVal
fi
export INFLUXDB3_AUTH_TOKEN=$(curl -X POST "http://localhost:8181/api/v3/configure/token/admin" \
  --header 'Accept: application/json' \
  --header 'Content-Type: application/json' | jq -r '.token')
rm -Rf "${TMPDIR}/.influxdb-token"
echo "${INFLUXDB3_AUTH_TOKEN}" > "${TMPDIR}/.influxdb-token"
echo "InfluxDb token: ${INFLUXDB3_AUTH_TOKEN} written to ${TMPDIR}/.influxdb-token"

docker exec influxdb-server influxdb3 create database CasEventsDatabase --token "${INFLUXDB3_AUTH_TOKEN}"
docker exec influxdb-server influxdb3 create database CasInfluxDbEvents --token "${INFLUXDB3_AUTH_TOKEN}"
printgreen "InfluxDb databases created."
