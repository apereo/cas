#!/bin/bash

java -jar app/build/libs/app.jar &
pid=$!
sleep 30
mkdir tmp
cd tmp
curl http://localhost:8080/starter.tgz -d type=cas-mgmt-overlay | tar -xzvf -
kill -9 $pid

echo "Running CAS Overlay with bootRun"
./gradlew clean build --no-daemon
java -jar build/libs/cas-management.war -Dserver.ssl.enabled=false -Dserver.port=8092 &> /dev/null &
pid=$!
echo "Launched CAS with pid ${pid} using bootRun. Waiting for CAS server to come online..."
until curl -k -L --output /dev/null --silent --fail http://localhost:8092/cas-management; do
    echo -n '.'
    sleep 5
done
echo -e "\n\nReady!"
kill -9 $pid

echo "Build Container Image w/ Docker"
./docker-build.sh
