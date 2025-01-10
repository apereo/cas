#!/bin/bash

RED="\e[31m"
GREEN="\e[32m"
YELLOW="\e[33m"
ENDCOLOR="\e[0m"

function printgreen() {
  printf "🍀 ${GREEN}$1${ENDCOLOR}\n"
}

function printred() {
  printf "🔥 ${RED}$1${ENDCOLOR}\n"
}

printgreen "Running Gradle build to determine CAS version..."
casVersion=(`cat ./gradle.properties | grep "version" | cut -d= -f2`)

printgreen "Building CAS command-line shell version $casVersion"
./gradlew :support:cas-server-support-shell:build -DskipNestedConfigMetadataGen=true \
  -x check -x javadoc  --no-daemon --build-cache --configure-on-demand --parallel

rm -rf cas-shell.out
COMMANDS=$(cat "${PWD}"/ci/tests/shell/cas-shell-script.sh)
printgreen "Running CAS command-line shell commands:"
echo "${COMMANDS}"

printgreen "Running CAS command-line shell version ${casVersion}"
java -jar support/cas-server-support-shell/build/libs/cas-server-support-shell-${casVersion}.jar \
  @ci/tests/shell/cas-shell-script.sh \
  --spring.shell.noninteractive.enabled=false \
  --spring.shell.script.enabled=true > cas-shell.out &
pid=$!
printgreen "Launched CAS command-line shell under process id ${pid}. Waiting for commands to finish..."
sleep 8
exitRequest="org.springframework.shell.ExitRequest"
while :; do
    if grep -q "APPLICATION FAILED TO START" "cas-shell.out"; then
        printred "CAS command-line shell failed to start successfully"
        kill -9 ${pid} &> /dev/null
        exit 1
    fi
    if grep -q ${exitRequest} "cas-shell.out"; then
        line_number=$(grep -n ${exitRequest} "cas-shell.out" | cut -d: -f1)
        printf "Found ${GREEN}${exitRequest}${ENDCOLOR} at line ${GREEN}${line_number}${ENDCOLOR} in CAS command-line shell output\n"
        sed -i -e "${line_number},\$d" "cas-shell.out"
        break
    fi
    echo "Waiting for CAS command-line shell to finish..."
    sleep 2
done
printgreen "CAS command-line shell version ${casVersion} finished successfully!"
kill -9 ${pid} &> /dev/null
exit 0
