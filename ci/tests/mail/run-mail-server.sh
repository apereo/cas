#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &
export DOCKER_IMAGE="mmoayyed/mockmock:latest"

echo "Running MockMock docker container"
docker stop email-server || true && docker rm email-server || true
docker run --rm -d -p25000:25000 -p8282:8282 --name "email-server" ${DOCKER_IMAGE}

docker ps | grep "email-server"
retVal=$?
if [ $retVal == 0 ]; then
    echo "MockMock docker container is running."
else
    echo "MockMock docker container failed to start."
    exit $retVal
fi
