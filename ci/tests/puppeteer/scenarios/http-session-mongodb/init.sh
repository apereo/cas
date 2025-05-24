#!/bin/bash

set -e

"${PWD}"/ci/tests/httpbin/run-httpbin-server.sh
echo Starting MongoDb database
${PWD}/ci/tests/mongodb/run-mongodb-server.sh
echo MongoDb started
