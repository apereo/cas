#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Building Kafka image..."
COMPOSE_FILE=./ci/tests/kafka/docker-compose.yml
test -f $COMPOSE_FILE || COMPOSE_FILE=docker-compose.yml
docker-compose -f $COMPOSE_FILE up -d
sleep 5
docker ps
COUNT_KAFKA=$(docker ps | grep "kafka_"| wc -l)
if [[ ${COUNT_KAFKA} -eq 2 ]]; then
    echo "Kafka docker images are running."
else
    echo "Kafka docker images failed to start."
    exit 1
fi
