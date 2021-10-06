#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Running MySQL docker image..."
docker stop mysql-server || true
docker run --rm -p 3306:3306 --name mysql-server --rm \
  -e MYSQL_ROOT_PASSWORD=password -d mysql:8.0.26 --lower_case_table_names=1

docker ps | grep "mysql-server"
retVal=$?
if [ $retVal == 0 ]; then
    echo "MySQL docker image is running."
else
    echo "MySQL docker image failed to start."
    exit $retVal
fi
