#!/bin/bash


# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Running CouchDb docker image..."
docker run -d -p 5984:5984 --name="couchdb-server" apache/couchdb:2.1

docker ps | grep "couchdb-server"
retVal=$?
if [ $retVal == 0 ]; then
    echo "CouchDb docker image is running."
else
    echo "CouchDb docker image failed to start."
    exit $retVal
fi

