#!/bin/bash

echo "Running $GCLOUD_COMPONENT"

case "$GCLOUD_COMPONENT" in
"pubsub")
  echo "Starting PubSub emulator on port ${PUBSUB_PORT}"
  gcloud beta emulators pubsub start --verbosity=debug --project="${GCP_PROJECT_ID}" --host-port="0.0.0.0:${PUBSUB_PORT}"
  ;;
"firestore")
  echo "Starting Firestore emulator on port ${FIRESTORE_PORT}"
  gcloud beta emulators firestore start --project="${GCP_PROJECT_ID}" --host-port="0.0.0.0:${FIRESTORE_PORT}"
  ;;
esac
