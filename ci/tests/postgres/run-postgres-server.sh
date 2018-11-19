#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Building Postgres image..."
docker build ci/tests/postgres/ -t cas/postgres:latest

echo "Running Postgres docker image..."
docker run --name postgres-server -e POSTGRES_PASSWORD=password -d -p 5432:5432 cas/postgres

docker ps | grep "postgres-server"
retVal=$?
if [ $retVal == 0 ]; then
    echo "Postgres docker image is running."
else
    echo "Postgres docker image failed to start."
    exit $retVal
fi
