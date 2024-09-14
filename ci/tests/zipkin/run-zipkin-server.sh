#!/bin/bash
export DOCKER_IMAGE="openzipkin/zipkin:latest"
echo "Running SCIM docker container..."
docker stop zipkin-server || true && docker rm zipkin-server || true
docker run --rm --name="zipkin-server" \
  -p 9411:9411 -d ${DOCKER_IMAGE}
echo "Waiting for Zipkin container to prepare..."
sleep 10
docker ps | grep "zipkin-server"
echo "Zipkin docker container is running."
