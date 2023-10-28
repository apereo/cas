#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &
export REDIS_VERSION=${1:-7.2.0-v4}
export REDIS_SENTINEL_VERSION=${1:-7.2.1}

echo "Running Redis $REDIS_VERSION, Sentinel: $REDIS_SENTINEL_VERSION"

COMPOSE_FILE=./ci/tests/redis/docker-compose.yml
test -f $COMPOSE_FILE || COMPOSE_FILE=docker-compose.yml
docker-compose -f $COMPOSE_FILE down >/dev/null 2>/dev/null || true
docker-compose -f $COMPOSE_FILE up -d
docker-compose -f $COMPOSE_FILE logs &
sleep 15
docker ps
COUNT_REDIS=$(docker ps | grep "redis_" | wc -l)
if [ "$COUNT_REDIS" -eq 5 ]; then
    echo "Redis + sentinel docker images are running."
else
    echo "Redis + sentinel docker images failed to start."
    exit 1
fi
