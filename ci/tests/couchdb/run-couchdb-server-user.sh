#!/bin/bash


# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Running CouchDb docker image..."
docker run -d -p 5984:5984 --name="couchdb-server" apache/couchdb:2.2

docker ps | grep "couchdb-server"
retVal=$?
if [ $retVal == 0 ]; then
    echo "CouchDb docker image is running."
else
    echo "CouchDb docker image failed to start."
    exit $retVal
fi

curl -X PUT http://127.0.0.1:5984/_node/couchdb@127.0.0.1/_config/admins/admin -d '"password"'
retVal=$?
if [ $retVal == 0 ]; then
    echo "CouchDb admin initialized."
else
    echo "CouchDb admin failed to initialize."
    exit $retVal
fi

curl -X PUT http://admin:password@localhost:5984/_users/org.couchdb.user:cas \
     -H "Accept: application/json" \
     -H "Content-Type: application/json" \
     -d '{"name": "cas", "password": "password", "roles": [], "type": "user"}'
retVal=$?
if [ $retVal == 0 ]; then
    echo "CouchDb admin initialized."
else
    echo "CouchDb admin failed to initialize."
    exit $retVal
fi
