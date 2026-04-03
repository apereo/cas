#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &
export DOCKER_IMAGE="registry.k8s.io/etcd:3.6.10-0"
echo "Running etcd docker container"
docker stop etcd-server || true && docker rm etcd-server || true
docker run --rm -d --name etcd-server \
  -p 2379:2379 -p 2380:2380 \
  ${DOCKER_IMAGE} \
  /usr/local/bin/etcd \
  --listen-client-urls=http://0.0.0.0:2379 \
  --advertise-client-urls=http://0.0.0.0:2379
sleep 2
echo "etcd server is running"
