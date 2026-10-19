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
docker rm -f influxdb-server >/dev/null 2>&1 || true
for _ in $(seq 1 30); do
  docker container inspect influxdb-server >/dev/null 2>&1 || break
  sleep 1
done
docker run --quiet  -d --rm --name="influxdb-server" \
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
INFLUXDB3_AUTH_TOKEN=""
for _ in $(seq 1 30); do
  INFLUXDB3_AUTH_TOKEN=$(curl -s -X POST "http://localhost:8181/api/v3/configure/token/admin" \
    --header 'Accept: application/json' \
    --header 'Content-Type: application/json' | jq -r '.token // empty' 2>/dev/null || true)
  if [[ -n "${INFLUXDB3_AUTH_TOKEN}" ]]; then
    break
  fi
  echo "Waiting for InfluxDb to accept requests..."
  sleep 2
done
if [[ -z "${INFLUXDB3_AUTH_TOKEN}" ]]; then
  echo "Unable to obtain an InfluxDb admin token."
  exit 1
fi
export INFLUXDB3_AUTH_TOKEN
rm -Rf "${TMPDIR}/.influxdb-token"
echo "${INFLUXDB3_AUTH_TOKEN}" > "${TMPDIR}/.influxdb-token"
echo "InfluxDb token: ${INFLUXDB3_AUTH_TOKEN} written to ${TMPDIR}/.influxdb-token"

docker exec influxdb-server influxdb3 create database CasEventsDatabase --token "${INFLUXDB3_AUTH_TOKEN}"
docker exec influxdb-server influxdb3 create database CasInfluxDbEvents --token "${INFLUXDB3_AUTH_TOKEN}"
printgreen "InfluxDb databases created."
