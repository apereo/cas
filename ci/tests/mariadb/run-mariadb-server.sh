#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &
export DOCKER_IMAGE="mariadb:11.8.2"
echo "Running MariaDb docker container..."
docker stop mariadb || true
docker run --rm -p 3306:3306 --rm --name mariadb \
  -e MYSQL_ROOT_PASSWORD=mypass -d ${DOCKER_IMAGE}

docker ps | grep "mariadb"
retVal=$?
if [ $retVal == 0 ]; then
    echo "MariaDb docker container is running."
else
    echo "MariaDb docker container failed to start."
    exit $retVal
fi
