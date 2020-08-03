#!/bin/bash

echo "Running ZooKeeper docker image..."
docker stop zookeeper || true && docker rm zookeeper || true
docker run --name zookeeper -p 2181:2181 -p 2888:2888 -p 3888:3888 -p 8080:8080 -d zookeeper:latest
echo "Waiting for ZooKeeper image to prepare..."
sleep 5
docker ps | grep "zookeeper"
echo "ZooKeeper docker image is running."
