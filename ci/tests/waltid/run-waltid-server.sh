#!/bin/bash

# NOTE: this script is sourced by ci/tests/puppeteer/run.sh, not executed, so an `exit`
# here terminates the whole scenario runner. Only the failure path may exit.

RED="\e[31m"
GREEN="\e[32m"
ENDCOLOR="\e[0m"

function printred() {
  printf "\n🔥 ${RED}$1${ENDCOLOR}\n"
}

function printgreen() {
  printf "\n🍀 ${GREEN}$1${ENDCOLOR}\n"
}

printgreen "Running walt.id docker container..."
COMPOSE_FILE=./ci/tests/waltid/docker-compose.yml
test -f $COMPOSE_FILE || COMPOSE_FILE=docker-compose.yml
docker compose -f $COMPOSE_FILE down >/dev/null 2>/dev/null || true
docker compose -f $COMPOSE_FILE up --quiet-pull -d
if [ $? -ne 0 ]; then
  printred "Failed to start walt.id docker container!"
  exit 1
fi

printgreen "Waiting for the walt.id wallet to become available..."
WALLET_READY=false
for _ in $(seq 1 90); do
  # No -f: any HTTP response means the service is listening. Only a connection
  # failure counts as not ready.
  if curl -sS -o /dev/null --max-time 5 http://localhost:7006/wallet 2>/dev/null; then
    WALLET_READY=true
    break
  fi
  sleep 2
done

if [[ "${WALLET_READY}" != "true" ]]; then
  printred "The walt.id wallet did not become available on port 7006!"
  docker compose -f $COMPOSE_FILE ps
  docker compose -f $COMPOSE_FILE logs --no-color --tail 150
  exit 1
fi

docker ps
printgreen "Ready!"
