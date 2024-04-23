#!/bin/bash
set -e
clear
${PWD}/ci/tests/postgres/run-postgres-server.sh
echo "Waiting for PostgreSQL database to get ready..."
sleep 10
docker exec postgres-server psql -U postgres -d webapp -c "CREATE TABLE USERS (username VARCHAR(255), password VARCHAR(255), enabled BOOLEAN);"
docker exec postgres-server psql -U postgres -d webapp -c "INSERT INTO USERS (username, password, enabled) VALUES('casuser', '6a158d9847a80e99511b2a7866233e404b305fdb7c953a30deb65300a57a0655', TRUE);"
docker exec postgres-server psql -U postgres -d webapp -c "SELECT * FROM USERS"

docker exec postgres-server psql -U postgres -d webapp -c "CREATE TABLE AUTHORITIES (username VARCHAR(255), authority VARCHAR(255));"
docker exec postgres-server psql -U postgres -d webapp -c "INSERT INTO AUTHORITIES (username, authority) VALUES('casuser', 'ADMIN');"
docker exec postgres-server psql -U postgres -d webapp -c "SELECT * FROM AUTHORITIES"
echo "Ready!"
