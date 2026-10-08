#!/bin/bash

set -e

"${PWD}"/ci/tests/httpbin/run-httpbin-server.sh

echo "Starting MongoDb replica set"
"${PWD}"/ci/tests/mongodb/run-mongodb-server-clustered.sh
echo "MongoDb replica set started"
