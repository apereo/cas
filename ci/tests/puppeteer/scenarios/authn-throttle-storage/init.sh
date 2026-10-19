#!/bin/bash
"${PWD}/ci/tests/postgres/run-postgres-server.sh" || exit 1
"${PWD}/ci/tests/mongodb/run-mongodb-server.sh" || exit 1
"${PWD}/ci/tests/redis/run-redis-server.sh" || exit 1
"${PWD}/ci/tests/ldap/run-ldap-server.sh" || exit 1
"${PWD}/ci/tests/ldap/add-ldap-entries.sh" "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/throttle.ldif" || exit 1
