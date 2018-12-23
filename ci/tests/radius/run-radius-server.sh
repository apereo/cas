#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Running Radius docker image..."
docker run --name radius-server -d -p 1812-1813:1812-1813/udp mmoayyed/radius-server

docker ps | grep "radius-server"
retVal=$?
if [ $retVal == 0 ]; then
    echo "Radius docker image is running."
else
    echo "Radius docker image failed to start."
    exit $retVal
fi
