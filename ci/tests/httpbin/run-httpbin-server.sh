#!/bin/bash

echo "Running httpbin docker container..."
COMPOSE_FILE="${PWD}/ci/tests/httpbin/docker-compose.yml"
test -f $COMPOSE_FILE || COMPOSE_FILE=docker-compose.yml
docker compose -f $COMPOSE_FILE down >/dev/null 2>/dev/null || true
docker compose -f $COMPOSE_FILE up -d
export REQUEST_BASKET_AUTHZ_TOKEN="SV00cPIKRdWjGkN1vkbEbPdhtvV5vIJ0ajygcdnZVBgl";
# docker compose -f $COMPOSE_FILE logs &
