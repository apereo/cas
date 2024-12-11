#!/bin/bash

GREEN="\e[32m"
ENDCOLOR="\e[0m"

function printgreen() {
  printf "🍀 ${GREEN}$1${ENDCOLOR}\n"
}

printgreen "Building Postgres image..."
docker build ci/tests/postgres/ -t cas/postgres:latest

printgreen "Running Postgres docker container..."
docker stop postgres-server || true >/dev/null 2>&1
docker run --rm --name postgres-server --rm -e POSTGRES_PASSWORD=password -d -p 5432:5432 cas/postgres

docker ps | grep "postgres-server"
retVal=$?
if [ $retVal == 0 ]; then
    printgreen "Postgres docker container is running."
else
    echo "Postgres docker container failed to start."
fi
exit $retVal
