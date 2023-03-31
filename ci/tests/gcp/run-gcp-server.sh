#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Building GCP image..."
docker build ci/tests/gcp/ -t cas/gcp:latest

echo "Running GCP docker image..."
docker stop gcp-server || true
docker run --name gcp-server -d --rm -p 9980:9980 -p 8085:8085 cas/gcp
sleep 5
docker ps | grep "gcp-server"
retVal=$?
if [ $retVal == 0 ]; then
    echo "GCP docker container is running."
else
    echo "GCP docker container failed to start."
    exit $retVal
fi
