#!/bin/bash

if [ "$MATRIX_JOB_TYPE" == "TEST" ]; then
    while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

    echo "Running Cassandra docker image..."
    docker run --name cassandra -d -p 9042:9042 cassandra:3.10

    docker ps | grep "cassandra"
    retVal=$?
    if [ $retVal == 0 ]; then
        echo "Cassandra docker image is running."
    else
        echo "Cassandra docker image failed to start."
        exit $retVal
    fi

    echo "Waiting for Cassandra server to come online..."
    until $(curl --output /dev/null --silent --head --fail http://localhost:9042); do
        printf '.'
        sleep 1
    done

    echo "Creating Cassandra keyspace: cas"
    docker exec -it cassandra cqlsh -e "CREATE KEYSPACE cas WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor' : 1}"

    echo "Creating Cassandra users table"
    docker exec -it cassandra cqlsh -e "CREATE TABLE cas.users_table ( id UUID PRIMARY KEY, user_attr text, pwd_attr text )"
fi
