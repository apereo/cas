#!/bin/bash

export DOCKER_IMAGE="zookeeper:latest"
echo "Running ZooKeeper docker container..."
docker stop zookeeper || true && docker rm zookeeper || true
docker run --rm --name zookeeper -p 2181:2181 -p 2888:2888 -p 3888:3888 -p 8080:8080 -d ${DOCKER_IMAGE}
echo "Waiting for ZooKeeper container to prepare..."
sleep 5
docker ps | grep "zookeeper"
echo "ZooKeeper docker container is running."
