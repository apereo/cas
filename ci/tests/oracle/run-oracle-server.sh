#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

export DOCKER_IMAGE="gvenzl/oracle-free:23"
echo "Running Oracle docker container..."
docker stop oracle-db || true
#docker run --rm -d -p 1521:1521 --name oracle-db --rm store/oracle/database-enterprise:12.2.0.1-slim
docker run --rm -d -p 1521:1521 --name oracle-db --rm ${DOCKER_IMAGE}
echo "Waiting for Oracle docker container to prepare..."
sleep 90
docker ps | grep "oracle-db"
#echo "Waiting for Oracle server to come online..."
#until curl --output /dev/null --silent --fail telnet://127.0.0.1:1521; do
#    printf '.'
#    sleep 2
#done
echo "Oracle docker container is running."
