#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Running DynamoDb docker image..."
docker run -d -p 8000:8000 --name "dynamodb-server" amazon/dynamodb-local:1.12.0

docker ps | grep "dynamodb-server"
retVal=$?
if [ $retVal == 0 ]; then
    echo "DynamoDb docker image is running."
else
    echo "DynamoDb docker image failed to start."
    exit $retVal
fi
