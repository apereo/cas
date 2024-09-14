#!/bin/bash
export DOCKER_IMAGE="personify/personify-scim-server:2.1.2.RELEASE"
echo "Running SCIM docker container..."
docker stop scim-server || true && docker rm scim-server || true
docker run --rm --name="scim-server" -p 9666:8080 -d ${DOCKER_IMAGE}
echo "Waiting for SCIM container to prepare..."
sleep 10
docker ps | grep "scim-server"
echo "SCIM docker container is running."
