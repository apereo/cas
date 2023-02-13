#!/bin/bash

echo "Running RabbitMQ docker image..."
docker stop rabbitmq-server || true && docker rm rabbitmq-server || true
docker run -d -p 5672:5672 -p 15672:15672 --hostname rabbitmq \
  --name rabbitmq-server -e RABBITMQ_DEFAULT_USER=rabbituser \
  -e RABBITMQ_DEFAULT_PASS=bugsbunny \
  rabbitmq:3-management

docker ps | grep "rabbitmq"
retVal=$?
if [ $retVal == 0 ]; then
    echo "RabbitMQ docker container is running."
else
    echo "RabbitMQ docker container failed to start."
    exit $retVal
fi
