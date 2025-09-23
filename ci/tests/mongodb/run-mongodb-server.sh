#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

GREEN="\e[32m"
ENDCOLOR="\e[0m"

function printgreen() {
  printf "🍀 ${GREEN}$1${ENDCOLOR}\n"
}


function runscript {
    docker exec mongodb-server mongosh --authenticationDatabase admin mongodb://root:secret@localhost:27017/cas --eval "$1"
    return 0;
}

export DOCKER_IMAGE="mongo:8.0"
printgreen "Running MongoDb docker container..."
docker stop mongodb-server || true && docker rm mongodb-server || true
docker run --rm -d -p 27017:27017 -e MONGO_INITDB_ROOT_USERNAME=root \
  -e MONGO_INITDB_ROOT_PASSWORD=secret --name="mongodb-server" \
  -v "$PWD"/ci/tests/mongodb/mongo-init.sh:/docker-entrypoint-initdb.d/mongo-init.sh:ro \
  ${DOCKER_IMAGE}
docker logs mongodb-server &
sleep 5
docker ps | grep "mongodb-server"
retVal=$?
if [ $retVal == 0 ]; then
    printgreen "MongoDb docker container is running."
else
    echo "MongoDb docker container failed to start."
    exit $retVal
fi

