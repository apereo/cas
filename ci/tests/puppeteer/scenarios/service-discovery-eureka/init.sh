#!/bin/bash

docker stop eureka-server || true && docker rm eureka-server || true
properties=$(echo "{}" | tr -d '[:space:]')
docker run -d --rm --name eureka-server \
  --mount type=bind,source="${CAS_KEYSTORE}",target=/etc/cas/thekeystore \
  -e SPRING_APPLICATION_JSON="${properties}" \
  -p8761:8761 apereo/cas-discovery-server:7.0.0-RC7
#docker logs -f eureka-server &
