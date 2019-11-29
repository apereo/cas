#!/bin/bash

echo "Running Terracotta docker image..."
docker stop tc-server
docker run --rm --name tc-server -p 9410:9410 -d \
 --env OFFHEAP_RESOURCE1_NAME=main \
 --env OFFHEAP_RESOURCE2_NAME=extra \
 --env OFFHEAP_RESOURCE1_SIZE=256 \
 --env OFFHEAP_RESOURCE2_SIZE=16 \
terracotta/terracotta-server-oss:5.6.4

for i in {1..10}
do
  sleep 1
  docker logs tc-server | grep -i "server started"
  if [[ $? -eq 0 ]] ; then
    break;
  fi
done
docker logs tc-server
