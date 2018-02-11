#!/bin/bash

echo "Pulling DynamoDb docker image..."
docker pull ryanratcliff/dynamodb

echo "Running DynamoDb docker image..."
docker run -d -p 8000:8000 --name "dynamodb-server" ryanratcliff/dynamodb

docker ps | grep "dynamodb-server"
retVal=$?
if [ $retVal == 0 ]; then
    echo "DynamoDb docker image is running."
else
    echo "DynamoDb docker image failed to start."
    exit $retVal
fi

