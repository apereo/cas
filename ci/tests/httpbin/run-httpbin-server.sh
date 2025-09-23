#!/bin/bash

echo "Running httpbin docker container..."
COMPOSE_FILE="${PWD}/ci/tests/httpbin/docker-compose.yml"
test -f $COMPOSE_FILE || COMPOSE_FILE=docker-compose.yml
docker compose -f $COMPOSE_FILE down >/dev/null 2>/dev/null || true
docker compose -f $COMPOSE_FILE up -d
# docker compose -f $COMPOSE_FILE logs &
