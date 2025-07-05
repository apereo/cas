#!/bin/bash
export DOCKER_IMAGE="jaegertracing/all-in-one:1.71.0"
echo "Running Jaeger docker container..."
docker stop jaeger-server || true && docker rm jaeger-server || true

docker run --rm --name="jaeger-server" \
  -p 6831:6831/udp \
  -p 6832:6832/udp \
  -p 4317:4317 \
  -p 16686:16686 \
  -p 14268:14268 \
  -d ${DOCKER_IMAGE}
echo "Waiting for Jaeger container to prepare..."
sleep 5
docker ps | grep "jaeger-server"
echo "Jaeger docker container is running."
