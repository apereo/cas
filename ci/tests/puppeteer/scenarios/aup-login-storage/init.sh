#!/bin/bash
"${PWD}/ci/tests/httpbin/run-httpbin-server.sh" || exit 1
"${PWD}/ci/tests/postgres/run-postgres-server.sh" || exit 1
"${PWD}/ci/tests/mongodb/run-mongodb-server.sh" || exit 1
"${PWD}/ci/tests/redis/run-redis-server.sh" || exit 1
"${PWD}/ci/tests/ldap/run-ldap-server.sh" || exit 1
"${PWD}/ci/tests/ldap/add-ldap-entries.sh" "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/aup.ldif" || exit 1

for attempt in {1..30}; do
  docker exec postgres-server psql -U postgres -c "SELECT 1" >/dev/null 2>&1 && break
  echo "Waiting for Postgres, attempt ${attempt}"
  sleep 2
done
echo "Creating the AUP table in Postgres"
docker exec postgres-server psql -U postgres -c \
  "CREATE TABLE IF NOT EXISTS aup_users (username VARCHAR(255) PRIMARY KEY, aupAccepted BOOLEAN NOT NULL DEFAULT FALSE); INSERT INTO aup_users (username, aupAccepted) VALUES ('casuser', FALSE) ON CONFLICT (username) DO UPDATE SET aupAccepted = FALSE;" || exit 1
