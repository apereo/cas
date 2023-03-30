#!/bin/bash

set -xe

# Config gcloud project
echo "Setting project id to ${FIRESTORE_PROJECT_ID}"
gcloud config set project "${FIRESTORE_PROJECT_ID}"
#
## Start emulator
echo "Starting Firestore emulator"
gcloud beta emulators firestore start --host-port="0.0.0.0:${PORT}"
