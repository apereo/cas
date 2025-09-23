#!/bin/bash
set -e
clear
"${PWD}"/ci/tests/httpbin/run-httpbin-server.sh
"${PWD}"/ci/tests/mongodb/run-mongodb-server.sh
echo "Waiting for MongoDb database to get ready..."
sleep 10

function runscript {
  docker exec mongodb-server mongosh \
    --authenticationDatabase admin mongodb://root:secret@localhost:27017/cas \
    --eval "$1"
  return 0;
}

runscript "db.getName()"
runscript "db.MongoDbProperty.insertOne({name: 'cas.authn.accept.users', value: 'mongouser::p@SSw0rd'})"
runscript "db.MongoDbProperty.insertOne({name: 'cas.audit.engine.enabled', value: 'false'})"

echo "Ready!"


