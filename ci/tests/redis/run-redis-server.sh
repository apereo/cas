#!/bin/bash

# Generate redis certificate via:
# openssl x509 -req -in redis.csr -signkey redis.key -out redis.crt -days 5000
#
# Check expiration date via:
# openssl x509 -noout -enddate -in redis.crt
RED="\e[31m"
GREEN="\e[32m"
ENDCOLOR="\e[0m"

function printgreen() {
  printf "🍀 ${GREEN}$1${ENDCOLOR}\n"
}
function printred() {
  printf "🚨  ${RED}$1${ENDCOLOR}\n"
}

CLUSTERED="false"
while (( "$#" )); do
    case "$1" in
        --clustered)
            CLUSTERED="true"
            shift
            ;;
    esac
done

if [ "$CLUSTERED" = "true" ]; then
    printgreen "Starting Redis docker containers in clustered mode..."
    COMPOSE_FILE=$PWD/ci/tests/redis/docker-compose-clustered.yml
    case "$(uname -s)" in
      Darwin)
        export REDIS_CLUSTER_ANNOUNCE_HOST="$(ipconfig getifaddr en0)"
        ;;
      Linux)
        export REDIS_CLUSTER_ANNOUNCE_HOST="$(hostname -I | awk '{print $1}')"
        ;;
    esac
    printgreen "Redis cluster announce host: $REDIS_CLUSTER_ANNOUNCE_HOST"
else
    printgreen "Starting Redis docker container..."
    COMPOSE_FILE=$PWD/ci/tests/redis/docker-compose.yml
fi

test -f $COMPOSE_FILE || COMPOSE_FILE=docker-compose.yml
docker compose -f $COMPOSE_FILE down >/dev/null 2>/dev/null || true
docker compose -f $COMPOSE_FILE up --quiet-pull -d
docker compose -f $COMPOSE_FILE logs &
sleep 10
docker ps

if [ "$CLUSTERED" = "true" ]; then
  printgreen "Creating Redis cluster..."
  docker exec redis-1 redis-cli --cluster create \
    redis-1:7001 \
    redis-2:7002 \
    redis-3:7003 \
    redis-4:7004 \
    redis-5:7005 \
    redis-6:7006 \
    --cluster-replicas 1 \
    --cluster-yes
else
    COUNT_REDIS=$(docker ps | grep "redis_" | wc -l)
    if [ "$COUNT_REDIS" -eq 6 ]; then
        printgreen "Redis + sentinel docker containers are running."
    else
        printred "Redis + sentinel docker containers failed to start."
        exit 1
    fi
fi
