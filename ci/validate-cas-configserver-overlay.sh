#!/bin/bash

java -jar app/build/libs/app.jar &
pid=$!
sleep 15
rm -Rf tmp &> /dev/null
mkdir tmp
cd tmp
curl http://localhost:8080/starter.tgz -d type=cas-config-server-overlay | tar -xzvf -
kill -9 $pid

echo "Building CAS Config Server Overlay"
./gradlew clean build --no-daemon

java -jar build/libs/casconfigserver.war --server.ssl.enabled=false --spring.security.user.password=password --spring.security.user.name=casuser &
pid=$!
sleep 5

echo "Launched CAS with pid ${pid}. Waiting for server to come online..."
until curl -u casuser:password -k -L --output /dev/null --silent --fail http://localhost:8888/casconfigserver/env/default; do
    echo -n '.'
    sleep 5
done
echo -e "\n\nReady!"
kill -9 $pid

echo "Building Container Image via Spring Boot"
./gradlew bootBuildImage
