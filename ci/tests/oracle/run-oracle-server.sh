#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Running Oracle docker image..."
docker run -d -p 1521:1521 --name oracle-db store/oracle/database-enterprise:12.2.0.1-slim
echo "Waiting for Oracle image to prepare..."
sleep 90
docker ps | grep "oracle-db"
#echo "Waiting for Oracle server to come online..."
#until curl --output /dev/null --silent --fail telnet://127.0.0.1:1521; do
#    printf '.'
#    sleep 2
#done
echo "Oracle docker image is running."
