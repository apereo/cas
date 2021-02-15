#!/bin/bash

java -jar app/build/libs/app.jar &
pid=$!
sleep 15
rm -Rf tmp &> /dev/null
mkdir tmp
cd tmp
curl http://localhost:8080/starter.tgz -d type=cas-mgmt-overlay | tar -xzvf -
kill -9 $pid

echo "Building CAS Mgmt Overlay"
./gradlew clean build --no-daemon

touch ./users.json
java -jar build/libs/cas-management.war --mgmt.user-properties-file=file:${PWD}/users.json --server.ssl.enabled=false &
pid=$!
sleep 5
echo "Launched CAS with pid ${pid}. Waiting for server to come online..."
echo "Waiting for server to come online..."
until curl -k -L --fail http://localhost:8443/cas-management; do
    echo -n '.'
    sleep 5
done
echo -e "\n\nReady!"
kill -9 $pid

echo "Build Container Image w/ Docker"
chmod +x *.sh
./docker-build.sh

