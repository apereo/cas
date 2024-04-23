#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Running DynamoDb docker image..."
docker stop dynamodb-server || true && docker rm dynamodb-server || true
docker run --rm -d -p 8000:8000 --name "dynamodb-server" amazon/dynamodb-local:2.2.1

docker ps | grep "dynamodb-server"
retVal=$?
if [ $retVal == 0 ]; then
    echo "DynamoDb docker container is running."
else
    echo "DynamoDb docker container failed to start."
    exit $retVal
fi
