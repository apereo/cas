#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &
export DOCKER_IMAGE="mmoayyed/radius-server:latest"
echo "Running Radius docker container..."
docker stop radius-server || true && docker rm radius-server || true
docker run --rm --name radius-server -d -p 1812-1813:1812-1813/udp ${DOCKER_IMAGE}

docker ps | grep "radius-server"
retVal=$?
if [ $retVal == 0 ]; then
    echo "Radius docker container is running."
else
    echo "Radius docker container failed to start."
    exit $retVal
fi
