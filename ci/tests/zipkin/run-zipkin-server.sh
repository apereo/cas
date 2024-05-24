#!/bin/bash
export DOCKER_IMAGE="openzipkin/zipkin:latest"
echo "Running SCIM docker image..."
docker stop zipkin-server || true && docker rm zipkin-server || true
docker run --rm --name="zipkin-server" \
  -p 9411:9411 -d ${DOCKER_IMAGE}
echo "Waiting for Zipkin image to prepare..."
sleep 10
docker ps | grep "zipkin-server"
echo "Zipkin docker container is running."
