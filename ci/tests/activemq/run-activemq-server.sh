#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Running ActiveMQ docker image..."
docker run -d --name activemq-server -p 61616:61616 -p 8161:8161 rmohr/activemq
docker ps | grep "activemq"
retVal=$?
if [ $retVal == 0 ]; then
    echo "ActiveMQ docker image is running."
else
    echo "ActiveMQ docker image failed to start."
    exit $retVal
fi
