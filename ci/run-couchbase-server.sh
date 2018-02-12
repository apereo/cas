#!/bin/bash

if [ "$PUBLISH_SNAPSHOTS" == "false" ]; then
    while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

    echo "Pulling Couchbase docker image..."
    docker pull couchbase/server:4.6.4

    echo "Running Couchbase docker image..."
    docker run -d --name couchbase -p 8091-8094:8091-8094 -p 11210:11210 couchbase/server:4.6.4

    docker ps | grep "couchbase"
    retVal=$?
    if [ $retVal == 0 ]; then
        echo "Couchbase docker image is running."
    else
        echo "Couchbase docker image failed to start."
        exit $retVal
    fi

    echo "Waiting for Couchbase server to come online..."
    until $(curl --output /dev/null --silent --head --fail http://localhost:8091); do
        printf '.'
        sleep 1
    done
fi

echo -e "\nCreating default Couchbase bucket..."
curl -X POST -d 'name=default' -d 'bucketType=couchbase' -d 'ramQuotaMB=120' -d 'authType=none' -d 'proxyPort=11216' http://localhost:8091/pools/default/buckets
curl -X POST -d name=casbucket -d bucketType=couchbase -d ramQuotaMB=120 -d authType=none -d proxyPort=11217 http://localhost:8091/pools/default/buckets
curl http://localhost:8091/pools/default/buckets


