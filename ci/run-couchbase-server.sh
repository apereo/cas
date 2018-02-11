#!/bin/bash

echo "Running Couchbase docker image..."
docker run -d --name couchbase -p 8091-8094:8091-8094 -p 11210:11210 couchbase
docker ps

until $(curl --output /dev/null --silent --head --fail http://localhost:8091); do
    printf '.'
    sleep 1
done

echo "Creating default Couchbase bucket..."
curl -X POST -d name=default -d ramQuotaMB=120 -d authType=none -d proxyPort=11216 http://localhost:8091/pools/default/buckets


