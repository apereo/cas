#!/bin/bash
# Runs once for both CAS nodes. An initScript would run again before the second node starts,
# restarting the shared backend under the first node.
"${PWD}/ci/tests/mysql/run-mysql-server.sh" || exit 1
"${PWD}/ci/tests/httpbin/run-httpbin-server.sh" || exit 1
