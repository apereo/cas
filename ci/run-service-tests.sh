#!/usr/bin/env bash

for test in {"CASSANDRA" "COUCHBASE" "DYNAMODB" "FILESYSTEM" "IGNITE" "INFLUXDB" "LDAP" "MAIL" "MEMCACHED" "MONGODB" "REDIS" "COUCHDB"}; do
    testScript="./ci/run-${test,,}-server.sh"
    if [ -f $testScript ]; then
        chmod 777 $testScript
        ${testScript}
    fi

    chmod 777 ./ci/install.sh
    MATRIX_SERVER=$test ./ci/install.sh
    $retVal=$?

    if [ ! $retVal ]; then
        if [ -f $testScript ]; then
            docker kill $(docker ps -q)
        fi
    else
        ./ci/stop.sh
        break "-$retVal"
    fi
done
