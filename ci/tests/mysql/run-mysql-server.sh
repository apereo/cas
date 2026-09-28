#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

RED="\e[31m"
GREEN="\e[32m"
ENDCOLOR="\e[0m"

function printgreen() {
  printf "🍀 ${GREEN}$1${ENDCOLOR}\n"
}

function printred() {
  printf "🚨  ${RED}$1${ENDCOLOR}\n"
}

printgreen "Running MySQL docker container..."
export DOCKER_IMAGE="mysql:26.7"
docker stop mysql-server || true
# InnoDB's default REPEATABLE READ takes next-key locks, so a delete by a secondary key locks the gaps
# around the rows it matches and blocks inserts of neighbouring keys. Tests that delete by principal while
# others insert therefore deadlock on this engine and on no other. READ COMMITTED drops gap locking, which
# is the isolation most CAS deployments run anyway, and is set on the server rather than worked around in
# the tests.
docker run --quiet --rm -p 3306:3306 \
  --name mysql-server \
  -e MYSQL_DATABASE=cas \
  -e MYSQL_ROOT_PASSWORD=password -d ${DOCKER_IMAGE} \
  --lower_case_table_names=1 \
  --transaction-isolation=READ-COMMITTED

docker ps | grep "mysql-server"
retVal=$?
if [ $retVal == 0 ]; then
    printgreen "MySQL docker container is running."
else
    printred "MySQL docker container failed to start."
    exit $retVal
fi
