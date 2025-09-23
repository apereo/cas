#!/bin/bash
set -e
clear

chmod +x "${PWD}/ci/tests/httpbin/run-httpbin-server.sh"
${PWD}/ci/tests/httpbin/run-httpbin-server.sh

${PWD}/ci/tests/postgres/run-postgres-server.sh
echo "Waiting for PostgreSQL database to get ready..."
sleep 10
docker exec postgres-server psql -U postgres -d casconfig -c "CREATE TABLE CAS_SETTINGS_TABLE (id VARCHAR(255), name VARCHAR(255), value VARCHAR(255));"
docker exec postgres-server psql -U postgres -d casconfig -c "INSERT INTO CAS_SETTINGS_TABLE (id, name, value) VALUES('1', 'cas.authn.accept.users', 'postgrescas::p@SSw0rd');"
docker exec postgres-server psql -U postgres -d casconfig -c "SELECT id, name, value FROM CAS_SETTINGS_TABLE"
echo "Ready!"
