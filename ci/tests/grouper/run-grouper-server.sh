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

printgreen "Running Grouper docker container"
COMPOSE_FILE=./ci/tests/grouper/docker-compose.yml
test -f $COMPOSE_FILE || COMPOSE_FILE=docker-compose.yml
docker compose -f $COMPOSE_FILE down >/dev/null 2>/dev/null || true
docker compose -f $COMPOSE_FILE up -d
docker compose -f $COMPOSE_FILE logs &
printgreen "Waiting for Grouper server to come online..."
sleep 60
docker ps | grep "grouper"
retVal=$?
if [ $retVal == 0 ]; then
  max_attempts=5
  attempt=1
  until curl --connect-timeout 10 -k -L -u "GrouperSystem:@4HHXr6SS42@IHz2" --fail https://localhost:7443/grouper; do
    if [ $attempt -ge $max_attempts ]; then
      printred "Reached maximum attempts ($max_attempts). Exiting."
      echo "====================================="
      docker ps | grep "grouper"
      echo "====================================="
      printred "Grouper core container logs:"
      docker logs grouper-core &
      echo "====================================="
      printred "Grouper WS container logs:"
      docker logs grouper-ws &
      echo "====================================="
      exit 1
    fi
    echo -n '.'
    sleep 5
    attempt=$((attempt + 1))
  done
  printgreen "Grouper docker container is now running"
else
  printred "Grouper docker container failed to start."
  exit $retVal
fi
