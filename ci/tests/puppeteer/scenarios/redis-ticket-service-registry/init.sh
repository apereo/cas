#!/bin/bash

set -e

echo Starting Redis database
${PWD}/ci/tests/redis/run-redis-server.sh
echo Redis started
