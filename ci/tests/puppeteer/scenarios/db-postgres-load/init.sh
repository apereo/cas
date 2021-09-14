#!/bin/bash

set -e

echo Starting database
${PWD}/ci/tests/postgres/run-postgres-server.sh
echo Database started
