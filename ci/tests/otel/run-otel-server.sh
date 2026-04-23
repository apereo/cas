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

printgreen "Running OpenTelemetry Collector docker container..."
export DOCKER_IMAGE="otel/opentelemetry-collector:0.150.1"
docker stop otel-server || true && docker rm otel-server || true
docker run --rm -p 4317:4317 -p 4318:4318 -p 8888:8888 -p 55679:55679 --name otel-server \
  -v "$PWD"/ci/tests/otel/otel-collector-config.yaml:/etc/otelcol/config.yaml \
  -d ${DOCKER_IMAGE}

sleep 2
docker ps | grep "otel-server"
retVal=$?
if [ $retVal == 0 ]; then
    printgreen "OpenTelemetry Collector docker container is running."
    docker logs -f otel-server &
else
    printred "OpenTelemetry Collector docker container failed to start."
    exit $retVal
fi


  
