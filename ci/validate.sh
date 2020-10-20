#!/bin/bash

java -jar app/build/libs/app.jar &
sleep 10
mkdir tmp
cd tmp
curl http://localhost:8080/starter.tgz -d dependencies=core | tar -xzvf -

echo "Building Overlay"
./gradlew clean build jibDockerBuild --refresh-dependencies

echo "Downloading Shell"
./gradlew downloadShell

echo "Listing Views"
./gradlew listTemplateViews

echo "Exploding WAR"
./gradlew explodeWar
