#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

RED="\e[31m"
GREEN="\e[32m"
ENDCOLOR="\e[0m"

function printgreen() {
  printf "🍀 ${GREEN}$1${ENDCOLOR}\n"
}

function printred() {
  printf "🚨  ${RED}$1${ENDCOLOR}\n"
}

COMPOSE_FILE=./ci/tests/oidc/docker-compose.yml
test -f $COMPOSE_FILE || COMPOSE_FILE=docker-compose.yml
docker compose -f $COMPOSE_FILE down >/dev/null 2>/dev/null || true

printgreen "Generating self-signed certificate for OpenID Connect Test Suite..."
mkdir -p ./ci/tests/oidc/certs
openssl req -x509 -newkey rsa:2048 -nodes \
  -keyout ./ci/tests/oidc/certs/localhost.key \
  -out ./ci/tests/oidc/certs/localhost.crt \
  -days 365 \
  -subj "/CN=localhost"

printgreen "Running OpenID Connect Test Suite docker container..."
docker compose -f $COMPOSE_FILE up -d
docker logs oidc-suite -f &
printgreen "Waiting for test suite to come online...\n"
sleep 5
docker ps | grep "oidc-suite"
retVal=$?
if [ $retVal == 0 ]; then
    printgreen "OpenID Connect Test Suite docker container is running."
else
    printred "OpenID Connect Test Suite docker container failed to start."
    exit $retVal
fi
