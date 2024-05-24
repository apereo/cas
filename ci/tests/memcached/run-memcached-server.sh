#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &
export DOCKER_IMAGE="memcached:alpine"
echo "Running image for memcached..."
docker stop memcached || true && docker rm memcached || true
docker run --rm  -p 11211:11211 --name memcached -d ${DOCKER_IMAGE} memcached -m 256

docker ps | grep "memcached"
retVal=$?
if [ $retVal == 0 ]; then
    echo "Memcached docker container is running."
else
    echo "Memcached docker container failed to start."
    exit $retVal
fi
