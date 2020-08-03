#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Running image for memcached..."
docker stop memcached || true && docker rm memcached || true
docker run  -p 11211:11211 --name memcached -d memcached:alpine memcached -m 256

docker ps | grep "memcached"
retVal=$?
if [ $retVal == 0 ]; then
    echo "Memcached docker image is running."
else
    echo "Memcached docker image failed to start."
    exit $retVal
fi
