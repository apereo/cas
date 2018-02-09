#!/bin/bash

echo "Pulling DynamoDb docker image..."
docker pull ryanratcliff/dynamodb

echo "Running DynamoDb docker image..."
docker run -d -p 8000:8000 ryanratcliff/dynamodb
docker ps

