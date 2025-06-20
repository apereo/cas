#!/bin/bash

export DOCKER_IMAGE="cassandra:5.0"

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &
echo "Generating security keys and certificates..."
"$PWD"/ci/tests/cassandra/generate-keys.sh

echo "Running Cassandra docker container..."
docker stop cassandra || true && docker rm cassandra || true
#docker run --rm --name cassandra -d -p 9042:9042 cassandra:4.0.6
docker run --rm --name cassandra -d -p 7199:7199 -p 7000:7000 -p 7001:7001 -p 9042:9042 \
  -v "$PWD"/ci/tests/cassandra/cassandra.yaml:/etc/cassandra/cassandra.yaml \
  -v "$PWD"/ci/tests/cassandra/cqlshrc:/root/.cassandra/cqlshrc \
  -v "$PWD"/ci/tests/cassandra/security:/security \
  -e CASSANDRA_USER=cassandra -e CASSANDRA_PASSWORD=cassandra \
  ${DOCKER_IMAGE}
  
#docker logs -f cassandra &

docker ps | grep "cassandra"
retVal=$?
if [ $retVal == 0 ]; then
    echo "Cassandra docker container is running."
else
    echo "Cassandra docker container failed to start."
    exit $retVal
fi

echo "Waiting for Cassandra server to come online..."
sleep 70

echo "Creating Cassandra keyspace: cas"
docker exec cassandra cqlsh --ssl -e "CREATE KEYSPACE cas WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor' : 1}"
sleep 5

echo "Creating new super user: cassystem"
docker exec cassandra cqlsh --ssl -u cassandra -p cassandra -e "CREATE ROLE cassystem WITH PASSWORD = 'RUstriFINFiENtrO' AND LOGIN = true"
sleep 5

echo "Creating Cassandra users table"
docker exec cassandra cqlsh --ssl -e "CREATE TABLE cas.users_table ( id UUID PRIMARY KEY, user_attr text, pwd_attr text )"
sleep 5

echo "Creating Cassandra services table"
docker exec cassandra cqlsh --ssl -e "CREATE TABLE cas.casservices ( id bigint PRIMARY KEY, data text )"
sleep 5

echo "Creating Cassandra user record"
docker exec cassandra cqlsh --ssl -e "INSERT INTO cas.users_table (id,user_attr,pwd_attr) VALUES (6ab09bec-e68e-48d9-a5f8-97e6fb4c9b47, 'casuser','Mellon') USING TTL 86400 AND TIMESTAMP 123456789;"
sleep 5

docker exec cassandra nodetool status
