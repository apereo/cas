#!/bin/bash
set -e

echo Starting MySQL
"${PWD}"/ci/tests/mysql/run-mysql-server.sh

echo Starting Mail Server
"${PWD}"/ci/tests/mail/run-mail-server.sh
