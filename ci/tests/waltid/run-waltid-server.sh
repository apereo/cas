#!/bin/bash

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
sleep 5
docker ps
printgreen "Ready!"
