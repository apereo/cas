#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Running Redis docker image..."
docker run -d -p 6379:6379 --name redis-server redis:latest

docker ps | grep "redis-server"
retVal=$?
if [ $retVal == 0 ]; then
    echo "Redis docker image is running."
else
    echo "Redis docker image failed to start."
    exit $retVal
fi
