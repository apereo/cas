#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

GREEN="\e[32m"
ENDCOLOR="\e[0m"

function printgreen() {
  printf "🍀 ${GREEN}$1${ENDCOLOR}\n"
}


printgreen "Building Kafka image..."
COMPOSE_FILE=./ci/tests/kafka/docker-compose.yml
test -f $COMPOSE_FILE || COMPOSE_FILE=docker-compose.yml
docker compose -f $COMPOSE_FILE up -d
docker compose -f $COMPOSE_FILE logs &
sleep 10
docker ps | grep "kafka-server"
retVal=$?
if [ $retVal == 0 ]; then
    printgreen "Kafka docker container is running."
else
    echo "Kafka docker container failed to start."
    exit $retVal
fi

