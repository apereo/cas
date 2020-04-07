#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Running MariaDb docker image..."
docker run -p 3306:3306 --name mariadb -e MYSQL_ROOT_PASSWORD=mypass -d mariadb:10.5.2

docker ps | grep "mariadb"
retVal=$?
if [ $retVal == 0 ]; then
    echo "MariaDb docker image is running."
else
    echo "MariaDb docker image failed to start."
    exit $retVal
fi
