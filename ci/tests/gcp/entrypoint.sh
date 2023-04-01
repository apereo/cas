#!/bin/bash

echo "Setting project id to ${GCP_PROJECT_ID}"
gcloud config set project "${GCP_PROJECT_ID}"

# Start the process in the background to allow subsequent services to start
echo "Starting Firestore emulator on port ${FIRESTORE_PORT}"
gcloud beta emulators firestore start --host-port="0.0.0.0:${FIRESTORE_PORT}"

## The last command must not be sent to the background
## to allow the Docker container to keep running
#echo "Starting PubSub emulator on port ${PUBSUB_PORT}"
#gcloud beta emulators pubsub start --host-port="0.0.0.0:${PUBSUB_PORT}"
