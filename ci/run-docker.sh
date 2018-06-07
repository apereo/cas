#!/bin/bash

if [ "$MATRIX_JOB_TYPE" == "TEST" ]; then
#    set -o errexit

#    sudo apt update -y

#    echo "Upgrading Docker..."
#    sudo apt install --only-upgrade docker-ce -y
#    echo "Upgrade Done"

    docker info
fi

pwd
./ci/run-ldap-server.sh
./run-mssql-server.sh
./run-mail-server.sh
./run-influxdb-server.sh
./run-dynamodb-server.sh
./run-couchbase-server.sh
./run-couchdb-server.sh
./run-cassandra-server.sh


