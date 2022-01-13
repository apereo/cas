#!/bin/bash

set -e

echo Starting database
${PWD}/ci/tests/postgres/run-postgres-server.sh
echo Database started

echo Starting mail server
${PWD}/ci/tests/mail/run-mail-server.sh
echo Email server started
