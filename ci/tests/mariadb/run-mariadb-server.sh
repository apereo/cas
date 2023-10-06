#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Running MariaDb docker image..."
docker stop mariadb || true
docker run --rm -p 3306:3306 --rm --name mariadb \
  -e MYSQL_ROOT_PASSWORD=mypass -d mariadb:11.1.2

docker ps | grep "mariadb"
retVal=$?
if [ $retVal == 0 ]; then
    echo "MariaDb docker container is running."
else
    echo "MariaDb docker container failed to start."
    exit $retVal
fi
