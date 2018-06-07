#!/bin/bash

if [ "$MATRIX_JOB_TYPE" == "TEST" ]; then
#    set -o errexit

#    sudo apt update -y

#    echo "Upgrading Docker..."
#    sudo apt install --only-upgrade docker-ce -y
#    echo "Upgrade Done"

    docker info
fi

./ci/run-ldap-server.sh
./ci/run-mssql-server.sh
./ci/run-mail-server.sh
./ci/run-influxdb-server.sh
./ci/run-dynamodb-server.sh
./ci/run-couchbase-server.sh
./ci/run-couchdb-server.sh
./ci/run-cassandra-server.sh


