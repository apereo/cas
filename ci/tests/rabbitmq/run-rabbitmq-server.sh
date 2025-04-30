#!/bin/bash

GREEN="\e[32m"
ENDCOLOR="\e[0m"

function printgreen() {
  printf "🍀 ${GREEN}$1${ENDCOLOR}\n"
}


export DOCKER_IMAGE="rabbitmq:4-management"
printgreen "Running RabbitMQ docker container..."
docker stop rabbitmq-server || true && docker rm rabbitmq-server || true
docker run -d -p 5672:5672 -p 15672:15672 --hostname rabbitmq \
  --name rabbitmq-server -e RABBITMQ_DEFAULT_USER=rabbituser \
  -e RABBITMQ_DEFAULT_PASS=bugsbunny \
  ${DOCKER_IMAGE}

docker ps | grep "rabbitmq"
retVal=$?
if [ $retVal == 0 ]; then
    printgreen "RabbitMQ docker container is running."
else
    echo "RabbitMQ docker container failed to start."
    exit $retVal
fi
