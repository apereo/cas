#!/bin/bash

# Generate redis certificate via:
# openssl x509 -req -in redis.csr -signkey redis.key -out redis.crt -days 5000
#
# Check expiration date via:
# openssl x509 -noout -enddate -in redis.crt

GREEN="\e[32m"
ENDCOLOR="\e[0m"

function printgreen() {
  printf "🍀 ${GREEN}$1${ENDCOLOR}\n"
}


COMPOSE_FILE=./ci/tests/redis/docker-compose.yml
test -f $COMPOSE_FILE || COMPOSE_FILE=docker-compose.yml
docker compose -f $COMPOSE_FILE down >/dev/null 2>/dev/null || true
docker compose -f $COMPOSE_FILE up -d
docker compose -f $COMPOSE_FILE logs &
sleep 15
docker ps
COUNT_REDIS=$(docker ps | grep "redis_" | wc -l)
if [ "$COUNT_REDIS" -eq 6 ]; then
    printgreen "Redis + sentinel docker containers are running."
else
    echo "Redis + sentinel docker containers failed to start."
    exit 1
fi
