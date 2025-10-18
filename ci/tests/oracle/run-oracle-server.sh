#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

export DOCKER_IMAGE="gvenzl/oracle-free:23"
echo "Running Oracle docker container..."
docker stop oracle-db || true && docker rm oracle-db || true
docker run --rm -d -e ORACLE_PASSWORD=Oradoc_db1 -p 1521:1521 --name oracle-db --rm ${DOCKER_IMAGE}
echo "Waiting for Oracle docker container to prepare..."
sleep 30
docker ps | grep "oracle-db"
retVal=$?
if [ $retVal == 0 ]; then
   echo "Oracle docker container is running."
else
   echo "Oracle docker container failed to start."
   exit $retVal
fi
