#!/bin/bash
"${PWD}/ci/tests/httpbin/run-httpbin-server.sh" || exit 1
"${PWD}/ci/tests/mongodb/run-mongodb-server.sh" || exit 1
"${PWD}/ci/tests/redis/run-redis-server.sh" || exit 1
