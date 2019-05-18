#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Running Redis + Sentinel docker image..."
docker-compose -f ./ci/tests/redis/docker-compose-sentinel.yml up -d

COUNT_REDIS=$(docker ps | grep "redis_server_"| wc -l)
COUNT_SENTINEL=$(docker ps | grep "redis_sentinel_"| wc -l)
if [ "$COUNT_REDIS" -eq 3 -a "$COUNT_SENTINEL" -eq 3 ]; then
    echo "Redis + sentinel docker images are running."
else
    echo "Redis + sentinel docker images failed to start."
    exit 1
fi
