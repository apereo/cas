#!/bin/bash

while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Pulling FreeRadius docker image..."
docker pull tpdock/freeradius:2.2.9

echo "Running FreeRadius docker image..."
docker run -itd --name radius-server -e RADIUS_LISTEN_IP=* -e RADIUS_CLIENTS=secret@127.0.0.1 -p 1812:1812/udp -p 1813:1813/udp tpdock/freeradius:2.2.9

docker ps | grep "radius-server"
retVal=$?
if [ $retVal == 0 ]; then
    echo "FreeRadius docker image is running."
else
    echo "FreeRadius docker image failed to start."
    exit $retVal
fi

