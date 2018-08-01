#!/bin/bash


# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Running MongoDb docker image..."
docker run -d -p 8081:8081 --name="mongodb-server" mongo:4.0

docker ps | grep "mongodb-server"
retVal=$?
if [ $retVal == 0 ]; then
    echo "MongoDb docker image is running."
else
    echo "MongoDb docker image failed to start."
    exit $retVal
fi

