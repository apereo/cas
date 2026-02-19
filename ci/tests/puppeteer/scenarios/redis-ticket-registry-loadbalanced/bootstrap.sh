#!/bin/bash
echo Starting Redis database
${PWD}/ci/tests/redis/run-redis-server.sh
${PWD}/ci/tests/httpbin/run-httpbin-server.sh

