#!/bin/bash

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
export DOCKER_IMAGE="otel/opentelemetry-collector:0.165.0"
docker rm -f otel-server >/dev/null 2>&1 || true
docker run --quiet --rm -d -p 4317:4317 -p 4318:4318 --name otel-server \
  -v "$PWD"/ci/tests/otel/otel-collector-config.yaml:/etc/otelcol/config.yaml \
  ${DOCKER_IMAGE} >/dev/null

for _ in $(seq 1 60); do
  otelStatus=$(curl -s -o /dev/null -w "%{http_code}" -X POST -H "Content-Type: application/json" \
    -d "{}" http://localhost:4318/v1/traces)
  if [[ "${otelStatus}" == "200" ]]; then
    break
  fi
  sleep 1
done

if [[ "${otelStatus}" != "200" ]]; then
  printred "OpenTelemetry Collector did not accept OTLP requests in time."
  docker logs otel-server
  exit 1
fi

printgreen "OpenTelemetry Collector is accepting OTLP requests; only its warnings and errors are logged."
docker logs -f otel-server &
