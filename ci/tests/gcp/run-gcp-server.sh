#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

RED="\e[31m"
GREEN="\e[32m"
ENDCOLOR="\e[0m"

function printgreen() {
  printf "🍀 ${GREEN}$1${ENDCOLOR}\n"
}

function printred() {
  printf "🚨  ${RED}$1${ENDCOLOR}\n"
}

function runContainer() {
 printgreen "Building GCP $1 image..."
 docker build ci/tests/gcp/ --build-arg GCLOUD_COMPONENT="$1" -t cas/gcp-$1:latest
 retVal=$?
 if [ $retVal == 0 ]; then
     printgreen "GCP $1 docker container is built successfully."
 else
     printred "GCP $1 docker container failed to build."
     exit $retVal
 fi

 printgreen "Running GCP $1 docker container..."
 docker stop gcp-$1-server || true

 ports=""
 case "$1" in
  "pubsub")
    ports="8085:8085"
    ;;
  "firestore")
    ports="9980:9980"
    ;;
 esac

 docker run --name gcp-$1-server --rm -d -p $ports cas/gcp-$1:latest
# docker logs gcp-$1-server &
 sleep 5
 docker ps | grep "gcp-$1-server"
 retVal=$?
 if [ $retVal == 0 ]; then
     printgreen "GCP $1 docker container is running."
 else
     printred "GCP $1 docker container failed to start."
     exit $retVal
 fi
}

runContainer "firestore"
runContainer "pubsub"

printgreen "Running GCP storage server..."
docker run -d --name gcp-storage-server -p 4553:4443 -p 8100:8000 \
  fsouza/fake-gcs-server -scheme both
sleep 5
docker ps | grep "gcp-storage-server"
retVal=$?
if [ $retVal == 0 ]; then
   printgreen "GCP storage docker container is running."
else
   printred "GCP storage docker container failed to start."
   exit $retVal
fi


echo "Running GCP secret manager..."
docker run --rm -d --name gcp-secret-manager -p 9090:9090 -p 8080:8080 \
  ghcr.io/blackwell-systems/gcp-secret-manager-emulator-dual:latest
sleep 5
docker ps | grep "gcp-secret-manager"
retVal=$?
if [ $retVal == 0 ]; then
   printgreen "GCP secret manager docker container is running."
else
   printred "GCP secret manager docker container failed to start."
   exit $retVal
fi
