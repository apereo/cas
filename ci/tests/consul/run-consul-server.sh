#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &
echo "Running Consul docker image"
set -e
echo Starting Consul
docker stop consul || true && docker rm consul || true
docker run --rm -d --name=consul -p 8500:8500 -p 8400:8400 \
  -p 8600:8600 -e CONSUL_BIND_INTERFACE=eth0 consul:1.15.4
sleep 5
docker exec consul sh -c "consul kv put 'config/cas-consul/cas/authn/accept/users' 'securecas::p@SSw0rd'"
echo Consul started
