#!/bin/bash

RED="\e[31m"
GREEN="\e[32m"
ENDCOLOR="\e[0m"

function printred() {
  printf "\n🔥 ${RED}$1${ENDCOLOR}\n"
}

function printgreen() {
  printf "\n🍀 ${GREEN}$1${ENDCOLOR}\n"
}

printgreen "Running Apache Syncope docker container..."
COMPOSE_FILE=./ci/tests/syncope/docker-compose.yml
test -f $COMPOSE_FILE || COMPOSE_FILE=docker-compose.yml
docker compose -f $COMPOSE_FILE down >/dev/null 2>/dev/null || true
docker compose -f $COMPOSE_FILE up -d
docker logs syncope-syncope-1 -f &
printgreen "Waiting for Apache Syncope server to come online...\n"
sleep 35
until $(curl --output /dev/null --silent --head --fail http://localhost:18080/syncope/); do
    printf '.'
    sleep 1
done
printgreen "Apache Syncope docker container is running."


echo "Creating derived attribute"
curl -X 'POST' \
  'http://localhost:18080/syncope/rest/schemas/DERIVED' \
  -H 'accept: */*' \
  -H 'Authorization: Basic YWRtaW46cGFzc3dvcmQ=' \
  -H 'Content-Type: application/json' \
  -d '{
    "_class": "org.apache.syncope.common.lib.to.DerSchemaTO",
    "key": "description",
    "anyTypeClass": "BaseUser",
    "labels": {},
    "expression": "'\''email: '\'' + email"
  }
' | jq
if [ $? -ne 0 ]; then
  printred "Failed to create derived attribute"
  exit 1
fi
echo -e "\n-----------------\n"

echo "Creating phoneNumber plain schema..."
curl -X 'POST' \
  'http://localhost:18080/syncope/rest/schemas/PLAIN' \
  -H 'accept: */*' \
  -H 'X-Syncope-Domain: Master' \
  -H 'Authorization: Basic YWRtaW46cGFzc3dvcmQ=' \
  -H 'Content-Type: application/json' \
  -d '{
    "_class": "org.apache.syncope.common.lib.to.PlainSchemaTO",
    "key": "phoneNumber",
    "anyTypeClass": "BaseUser"
}'

echo -e "Creating givenName plain schema...\n"
curl -X 'POST' \
  'http://localhost:18080/syncope/rest/schemas/PLAIN' \
  -H 'accept: */*' \
  -H 'X-Syncope-Domain: Master' \
  -H 'Authorization: Basic YWRtaW46cGFzc3dvcmQ=' \
  -H 'Content-Type: application/json' \
  -d '{
    "_class": "org.apache.syncope.common.lib.to.PlainSchemaTO",
    "key": "givenName",
    "anyTypeClass": "BaseUser"
}'
if [ $? -ne 0 ]; then
  printred "Failed to create plain schema"
  exit 1
fi
echo -e "\n-----------------\n"

echo -e "Creating security question...\n"
curl -X 'POST' \
  'http://localhost:18080/syncope/rest/securityQuestions' \
  -H 'accept: */*' \
  -H 'X-Syncope-Domain: Master' \
  -H 'Authorization: Basic YWRtaW46cGFzc3dvcmQ=' \
  -H 'Content-Type: application/json' \
  -d '{
    "key": "DefaultSecurityQuestion",
    "content": "What is your favorite city?"
}'
if [ $? -ne 0 ]; then
  printred "Failed to create security question"
  exit 1
fi

SECURITY_QUESTION_KEY=$(curl -X 'GET' \
  'http://localhost:18080/syncope/rest/securityQuestions' \
  -H 'accept: */*' \
  -H 'X-Syncope-Domain: Master' \
  -H 'Authorization: Basic YWRtaW46cGFzc3dvcmQ=' \
  -H 'Content-Type: application/json' | jq -r '.[0].key')
echo -e "Created security question with key ${SECURITY_QUESTION_KEY}\n"
echo -e "\n-----------------\n"

echo -e "Updating configuration parameters...\n"
curl -X 'POST' \
  'http://localhost:18080/syncope/rest/keymaster/conf/return.password.value' \
  -H 'accept: */*' \
  -H 'X-Syncope-Domain: Master' \
  -H 'Authorization: Basic c3luY29wZTpzeW5jb3Bl' \
  -H 'Content-Type: application/json' \
  -d 'true'
if [ $? -ne 0 ]; then
  printred "Failed to update parameters"
  exit 1
fi
echo -e "\n-----------------\n"



echo -e "Creating sample user: syncopecas...\n"
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
        "securityQuestion": "'"${SECURITY_QUESTION_KEY}"'",
        "securityAnswer": "Paris",
        "plainAttrs": [
            {
              "schema": "email",
              "values": [
                "syncopecas@syncope.org"
              ]
            },
            {
              "schema": "givenName",
              "values": [
                "ApereoCAS"
              ]
            },
            {
              "schema": "phoneNumber",
              "values": [
                "1234567890"
              ]
            }
        ],
        "derAttrs": [
          {
            "schema": "description"
          }
        ]
  }'
if [ $? -ne 0 ]; then
  printred "Failed to create sample user"
  exit 1
fi
echo -e "\n-----------------\n"

echo -e "Creating sample user: casuser...\n"
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
    },
    {
      "schema": "phoneNumber",
      "values": [
        "3477464523"
      ]
    }
  ],
  "derAttrs": [
    {
      "schema": "description"
    }
  ]
}'

echo -e "\n-----------------\n"

echo "Creating sample user: mustChangePasswordUser..."
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
  "username": "mustChangePasswordUser",
  "password": "ChangePassword",
  "mustChangePassword": true,
  "plainAttrs": [
    {
      "schema": "email",
      "values": [
        "mustChangePasswordUser@syncope.org"
      ]
    },
    {
      "schema": "phoneNumber",
      "values": [
        "2345678901"
      ]
    }
  ],
  "derAttrs": [
    {
      "schema": "description"
    }
  ]
}'

if [ $? -ne 0 ]; then
  printred "Failed to create sample user"
  exit 1
fi

echo -e "\n-----------------\n"

echo -e "Creating sample user: syncopepasschange...\n"
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
  "username": "syncopepasschange",
  "password": "Sync0pe",
  "mustChangePassword": true,
  "plainAttrs": [
    {
      "schema": "email",
      "values": [
        "casuser@syncope.org"
      ]
    },
    {
      "schema": "phoneNumber",
      "values": [
        "3477464523"
      ]
    }
  ],
  "derAttrs": [
    {
      "schema": "description"
    }
  ]
}'

echo -e "\n-----------------\n"

echo -e "Creating sample user: syncopesuspend...\n"
SYNCOPESUSPEND_KEY=$(curl -X 'POST' \
  'http://localhost:18080/syncope/rest/users?storePassword=true' \
  -H 'accept: application/json' \
  -H 'Prefer: return-content' \
  -H 'X-Syncope-Null-Priority-Async: false' \
  -H 'Authorization: Basic YWRtaW46cGFzc3dvcmQ=' \
  -H 'Content-Type: application/json' \
  -d '{
  "_class": "org.apache.syncope.common.lib.request.UserCR",
  "realm": "/",
  "username": "syncopesuspend",
  "password": "Sync0pe",
  "plainAttrs": [
    {
      "schema": "email",
      "values": [
        "syncopesuspend@syncope.org"
      ]
    },
    {
      "schema": "phoneNumber",
      "values": [
        "311111111"
      ]
    }
  ],
  "derAttrs": [
    {
      "schema": "description"
    }
  ]
}' | jq -r '.entity.key')

echo -e "\n-----------------\n"

echo -e "Syncope user key: ${SYNCOPESUSPEND_KEY}"

echo -e "Suspending user: syncopesuspend...\n"

curl -X 'POST' \
  "http://localhost:18080/syncope/rest/users/${SYNCOPESUSPEND_KEY}/status" \
  -H 'accept: application/json' \
  -H 'Prefer: return-content' \
  -H 'X-Syncope-Null-Priority-Async: false' \
  -H 'Authorization: Basic YWRtaW46cGFzc3dvcmQ=' \
  -H 'X-Syncope-Domain: Master' \
  -H 'Content-Type: application/json' \
  -d '{
  "operation": "ADD_REPLACE",
  "value": "suspended",
  "onSyncope": true,
  "resources": [],
  "key": "'"${SYNCOPESUSPEND_KEY}"'",
  "type": "SUSPEND",
  "token": null
}'

printgreen "Ready!\n"