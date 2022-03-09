#!/bin/bash

echo "Running Gradle build to determine CAS version..."
casVersion=$(./gradlew casVersion --daemon -q)

echo "Building CAS command-line shell version $casVersion"
./gradlew :support:cas-server-support-shell:build -DskipNestedConfigMetadataGen=true \
  -x check -x javadoc  --daemon --build-cache --configure-on-demand --parallel

echo "Running CAS command-line shell version $casVersion"
java -jar support/cas-server-support-shell/build/libs/cas-server-support-shell-${casVersion}.jar \
  @ci/tests/shell/cas-shell-script.sh 2>&1 | tee cas-shell.out