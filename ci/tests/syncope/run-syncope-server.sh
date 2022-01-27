#!/bin/bash

echo "Running Syncope Docker image..."
COMPOSE_FILE=./ci/tests/syncope/docker-compose.yml
test -f $COMPOSE_FILE || COMPOSE_FILE=docker-compose.yml
docker-compose -f $COMPOSE_FILE down >/dev/null 2>/dev/null || true
docker-compose -f $COMPOSE_FILE up -d

echo "Waiting for Syncope server to come online..."
sleep 20
until $(curl --output /dev/null --silent --head --fail http://localhost:18080); do
    printf '.'
    sleep 1
done
echo "Syncope Docker image is running."

echo "Creating sample users..."
curl -X 'POST' \
  'http://localhost:18080/syncope/rest/users?storePassword=true' \
  -H 'accept: application/json' \
  -H 'Prefer: return-content' \
  -H 'X-Syncope-Null-Priority-Async: false' \
  -H 'Authorization: Basic YWRtaW46cGFzc3dvcmQ=' \
  -H 'Content-Type: application/json' \
  -d '{
  "@class": "org.apache.syncope.common.lib.to.UserTO",
  "realm": "/",
  "username": "syncopecas",
  "password": "Mellon",
  "plainAttrs": [
      {
        "schema": "email",
        "values": [
          "syncopecas@syncope.org"
        ]
      }
    ]
}' | jq

echo "-----------------"

curl -X 'POST' \
  'http://localhost:18080/syncope/rest/users?storePassword=true' \
  -H 'accept: application/json' \
  -H 'Prefer: return-content' \
  -H 'X-Syncope-Null-Priority-Async: false' \
  -H 'Authorization: Basic YWRtaW46cGFzc3dvcmQ=' \
  -H 'Content-Type: application/json' \
  -d '{
  "@class": "org.apache.syncope.common.lib.to.UserTO",
  "realm": "/",
  "username": "casuser",
  "password": "Sync0pe",
  "plainAttrs": [
    {
      "schema": "email",
      "values": [
        "casuser@syncope.org"
      ]
    }
  ]
}' | jq

clear
echo -e "Ready!\n"
