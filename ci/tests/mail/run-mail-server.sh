#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Running MockMock docker image"
docker stop email-server || true && docker rm email-server || true
docker run -d -p25000:25000 -p8282:8282 --name "email-server" mmoayyed/mockmock:latest

docker ps | grep "email-server"
retVal=$?
if [ $retVal == 0 ]; then
    echo "MockMock docker image is running."
else
    echo "MockMock docker image failed to start."
    exit $retVal
fi
