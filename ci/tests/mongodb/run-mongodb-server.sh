#!/bin/bash


# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Running MongoDb docker image..."
docker stop mongodb-server || true && docker rm mongodb-server || true
docker run -d -p 27017:27017 -e MONGO_INITDB_ROOT_USERNAME=root -e MONGO_INITDB_ROOT_PASSWORD=secret --name="mongodb-server" mongo:4.2.3

docker ps | grep "mongodb-server"
retVal=$?
if [ $retVal == 0 ]; then
    echo "MongoDb docker image is running."
else
    echo "MongoDb docker image failed to start."
    exit $retVal
fi

