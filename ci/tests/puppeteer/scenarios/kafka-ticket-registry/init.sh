#!/bin/bash
chmod +x "${PWD}/ci/tests/kafka/run-kafka-server.sh"
"${PWD}/ci/tests/kafka/run-kafka-server.sh"

chmod +x "${PWD}/ci/tests/httpbin/run-httpbin-server.sh"
${PWD}/ci/tests/httpbin/run-httpbin-server.sh
