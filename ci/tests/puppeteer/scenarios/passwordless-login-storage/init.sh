#!/bin/bash
"${PWD}/ci/tests/mysql/run-mysql-server.sh" || exit 1
"${PWD}/ci/tests/mongodb/run-mongodb-server.sh" || exit 1
"${PWD}/ci/tests/mail/run-mail-server.sh" || exit 1
