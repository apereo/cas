#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Building Firestore image..."
docker build ci/tests/gcp/ -t cas/firestore:latest

echo "Running Firestore docker image..."
docker stop firestore-server || true
docker run --name firestore-server --rm -d -p 8080:8080 cas/firestore

docker ps | grep "firestore-server"
retVal=$?
if [ $retVal == 0 ]; then
    echo "Firestore docker container is running."
else
    echo "Firestore docker container failed to start."
    exit $retVal
fi
