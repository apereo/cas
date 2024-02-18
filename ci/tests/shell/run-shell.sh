#!/bin/bash

echo "Running Gradle build to determine CAS version..."
casVersion=$(./gradlew casVersion --no-daemon -q)

echo "Building CAS command-line shell version $casVersion"
./gradlew :support:cas-server-support-shell:build -DskipNestedConfigMetadataGen=true \
  -x check -x javadoc  --no-daemon --build-cache --configure-on-demand --parallel

echo "Running CAS command-line shell version $casVersion"
rm -rf cas-shell.out
java -jar support/cas-server-support-shell/build/libs/cas-server-support-shell-${casVersion}.jar @ci/tests/shell/cas-shell-script.sh > cas-shell.out &
pid=$!
echo "Launched CAS shell under process id ${pid}"
sleep 5
exitRequest="org.springframework.shell.ExitRequest"
unalias grep
while :; do
    if grep -q ${exitRequest} "cas-shell.out"; then
        line_number=$(grep -n ${exitRequest} "cas-shell.out" | cut -d: -f1)
        echo "Found exist request at line ${line_number} in CAS command-line shell output"
        sed -i -e "${line_number},\$d" "cas-shell.out"
        exit 0
    fi
    echo "Waiting for CAS command-line shell to finish..."
    sleep 2
done


