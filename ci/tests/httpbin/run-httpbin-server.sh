#!/bin/bash

RED="\e[31m"
GREEN="\e[32m"
ENDCOLOR="\e[0m"

function printgreen() {
  printf "🍀 ${GREEN}$1${ENDCOLOR}\n"
}

function printred() {
  printf "🚨  ${RED}$1${ENDCOLOR}\n"
}

printgreen "Running httpbin docker container..."
COMPOSE_FILE="${PWD}/ci/tests/httpbin/docker-compose.yml"
test -f $COMPOSE_FILE || COMPOSE_FILE=docker-compose.yml
docker compose -f $COMPOSE_FILE down >/dev/null 2>/dev/null || true
docker compose -f $COMPOSE_FILE up -d
export REQUEST_BASKET_AUTHZ_TOKEN="SV00cPIKRdWjGkN1vkbEbPdhtvV5vIJ0ajygcdnZVBgl";
# docker compose -f $COMPOSE_FILE logs &
