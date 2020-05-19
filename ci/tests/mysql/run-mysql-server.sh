#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Running MySQL docker image..."
docker run -p 3306:3306 --name mysql-server -e MYSQL_ROOT_PASSWORD=password -d mysql:8.0.20

docker ps | grep "mysql-server"
retVal=$?
if [ $retVal == 0 ]; then
    echo "MySQL docker image is running."
else
    echo "MySQL docker image failed to start."
    exit $retVal
fi
