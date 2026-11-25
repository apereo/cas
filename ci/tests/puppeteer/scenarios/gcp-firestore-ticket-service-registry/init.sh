#!/bin/bash
# Starts the GCP emulators (Firestore on 9980) and httpbin. The GCP script runs in a shell of its own, since it
# defines helper functions of its own that would replace those of run.sh if it were sourced.
"${PWD}/ci/tests/gcp/run-gcp-server.sh" || return 1
"${PWD}/ci/tests/httpbin/run-httpbin-server.sh" || return 1

echo "Waiting for the Firestore emulator to answer on port 9980..."
for _ in $(seq 1 60); do
  if curl -s -o /dev/null "http://localhost:9980/"; then
    echo "Firestore emulator is ready"
    return 0
  fi
  sleep 2
done
echo "Firestore emulator did not answer on port 9980"
return 1
