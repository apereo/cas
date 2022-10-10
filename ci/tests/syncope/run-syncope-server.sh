#!/bin/bash

echo "Running Syncope Docker image..."
COMPOSE_FILE=./ci/tests/syncope/docker-compose.yml
test -f $COMPOSE_FILE || COMPOSE_FILE=docker-compose.yml
docker-compose -f $COMPOSE_FILE down >/dev/null 2>/dev/null || true
docker-compose -f $COMPOSE_FILE up -d
echo -e "Waiting for Syncope server to come online...\n"
sleep 30
until $(curl --output /dev/null --silent --head --fail http://localhost:18080/syncope/); do
    printf '.'
    sleep 1
done
echo -e "\nSyncope Docker image is running."
clear

echo "Creating sample user: syncopecas..."
curl -X 'POST' \
  'http://localhost:18080/syncope/rest/users?storePassword=true' \
  -H 'accept: application/json' \
  -H 'Prefer: return-content' \
  -H 'X-Syncope-Null-Priority-Async: false' \
  -H 'Authorization: Basic YWRtaW46cGFzc3dvcmQ=' \
  -H 'Content-Type: application/json' \
  -d '{
        "_class": "org.apache.syncope.common.lib.request.UserCR",
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
  }'

echo "-----------------"

echo "Creating sample user: casuser..."
curl -X 'POST' \
  'http://localhost:18080/syncope/rest/users?storePassword=true' \
  -H 'accept: application/json' \
  -H 'Prefer: return-content' \
  -H 'X-Syncope-Null-Priority-Async: false' \
  -H 'Authorization: Basic YWRtaW46cGFzc3dvcmQ=' \
  -H 'Content-Type: application/json' \
  -d '{
  "_class": "org.apache.syncope.common.lib.request.UserCR",
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
}'

clear
echo -e "Ready!\n"
