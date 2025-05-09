#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

GREEN="\e[32m"
ENDCOLOR="\e[0m"

function printgreen() {
  printf "🍀 ${GREEN}$1${ENDCOLOR}\n"
}


export DOCKER_IMAGE="amazon/dynamodb-local:2.6.1"
printgreen "Running DynamoDb docker container..."
docker stop dynamodb-server || true && docker rm dynamodb-server || true
docker run --rm -d -p 8000:8000 --name "dynamodb-server" ${DOCKER_IMAGE}

docker ps | grep "dynamodb-server"
retVal=$?
if [ $retVal == 0 ]; then
    printgreen "DynamoDb docker container is running."
else
    echo "DynamoDb docker container failed to start."
    exit $retVal
fi
