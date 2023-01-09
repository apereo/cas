#!/bin/bash

echo "Starting Redis database"
"${PWD}/ci/tests/redis/run-redis-server.sh"
echo "Redis started"

