#!/bin/bash
# Runs once for both CAS nodes. An initScript would run again before the second node starts,
# restarting the shared Pub/Sub emulator under the first node.
"${PWD}/ci/tests/gcp/run-gcp-server.sh" || exit 1
"${PWD}/ci/tests/httpbin/run-httpbin-server.sh" || exit 1

echo "Waiting for the Pub/Sub emulator to answer on port 8085..."
for _ in $(seq 1 60); do
  if curl -s -o /dev/null "http://localhost:8085/"; then
    echo "Pub/Sub emulator is ready"
    exit 0
  fi
  sleep 2
done
echo "Pub/Sub emulator did not answer on port 8085"
exit 1
