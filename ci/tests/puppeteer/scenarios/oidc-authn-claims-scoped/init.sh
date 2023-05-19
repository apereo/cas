#!/bin/bash

set -e

echo Starting MongoDb database
${PWD}/ci/tests/mongodb/run-mongodb-server.sh
echo MongoDb started
