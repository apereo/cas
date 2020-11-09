#!/bin/bash

java -jar app/build/libs/app.jar &
sleep 30
mkdir tmp
cd tmp
curl http://localhost:8080/starter.tgz -d dependencies=core | tar -xzvf -

echo "Building Overlay"
chmod -R 777 ./*.sh
./gradlew clean build jibDockerBuild --refresh-dependencies

echo "Downloading Shell"
./gradlew downloadShell

echo "Listing Views"
./gradlew listTemplateViews

echo "Configuration Metadata Export"
./gradlew exportConfigMetadata

echo "Exploding WAR"
./gradlew explodeWar

echo "Build Container Image w/ Docker"
./docker-build.sh

echo "Build Container Image w/ Docker Compose"
docker-compose build
