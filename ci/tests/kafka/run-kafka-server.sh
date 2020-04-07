#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Building Kafka image..."
docker-compose -f ./ci/tests/kafka/docker-compose.yml up -d
sleep 5
COUNT_KAFKA=$(docker ps | grep "kafka_"| wc -l)
if [[ ${COUNT_KAFKA} -eq 3 ]]; then
    echo "Kafka docker images are running."
else
    echo "Kafka docker images failed to start."
    exit 1
fi
