#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Running Redis + Sentinel docker image..."

COMPOSE_FILE=./ci/tests/redis/docker-compose.yml
test -f $COMPOSE_FILE || COMPOSE_FILE=docker-compose.yml
docker-compose -f $COMPOSE_FILE down >/dev/null 2>/dev/null || true
docker-compose -f $COMPOSE_FILE up -d

COUNT_REDIS=$(docker ps | grep "redis_server_"| wc -l)
COUNT_SENTINEL=$(docker ps | grep "redis_sentinel_"| wc -l)
if [ "$COUNT_REDIS" -eq 3 -a "$COUNT_SENTINEL" -eq 3 ]; then
    echo "Redis + sentinel docker images are running."
else
    echo "Redis + sentinel docker images failed to start."
    exit 1
fi
