#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Running Cassandra docker image..."
docker stop cassandra || true && docker rm cassandra || true
docker run --rm --name cassandra -d -p 9042:9042 cassandra:4.0

docker ps | grep "cassandra"
retVal=$?
if [ $retVal == 0 ]; then
    echo "Cassandra docker image is running."
else
    echo "Cassandra docker image failed to start."
    exit $retVal
fi

echo "Waiting for Cassandra server to come online..."
sleep 70
#cmd="curl -I http://localhost:9042"
#retVal=0
#while [ $retVal -ne 8 ]
#do
#  printf '.'
#  sleep 1
#  eval $cmd
#  retVal=$?
#done

echo "Creating Cassandra keyspace: cas"
docker exec cassandra cqlsh -e "CREATE KEYSPACE cas WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor' : 1}"
sleep 5

echo "Creating Cassandra users table"
docker exec cassandra cqlsh -e "CREATE TABLE cas.users_table ( id UUID PRIMARY KEY, user_attr text, pwd_attr text )"
sleep 5

echo "Creating Cassandra services table"
docker exec cassandra cqlsh -e "CREATE TABLE cas.casservices ( id bigint PRIMARY KEY, data text )"
sleep 5

echo "Creating Cassandra user record"
docker exec cassandra cqlsh -e "INSERT INTO cas.users_table (id,user_attr,pwd_attr) VALUES (6ab09bec-e68e-48d9-a5f8-97e6fb4c9b47, 'casuser','Mellon') USING TTL 86400 AND TIMESTAMP 123456789;"
sleep 5

