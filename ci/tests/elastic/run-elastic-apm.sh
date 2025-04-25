#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &
export DOCKER_IMAGE="docker.elastic.co/apm/apm-server:9.0.0"
echo "Running Elastic APM Server docker container"
docker stop elastic-apm || true && docker rm elastic-apm || true
docker run -d \
  -p 8800:8200 --rm \
  --name=elastic-apm \
  --user=apm-server \
  --volume="$(pwd)/ci/tests/elastic/apm-server.yml:/usr/share/apm-server/apm-server.yml:ro" \
  ${DOCKER_IMAGE} --strict.perms=false -e
sleep 5
docker ps | grep "elastic-apm"
