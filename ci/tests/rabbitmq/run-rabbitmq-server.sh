#!/bin/bash

echo "Running RabbitMQ docker image..."
docker stoprabbitmq-server || true
docker run -d -p 5672:5672 -p 15672:15672 --hostname rabbitmq \
  --name rabbitmq-server -e RABBITMQ_DEFAULT_USER=user \
  -e RABBITMQ_DEFAULT_PASS=password \
  rabbitmq:3-management

docker ps | grep "rabbitmq"
retVal=$?
if [ $retVal == 0 ]; then
    echo "RabbitMQ docker image is running."
else
    echo "RabbitMQ docker image failed to start."
    exit $retVal
fi
