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

rm -rf cas-shell.ignore
awk "{ gsub(/\\\$PWD/, \"$PWD/ci/tests/shell\"); print }" \
  "$PWD/ci/tests/shell/cas-shell-script.sh" \
  > "$PWD/ci/tests/shell/cas-shell-script.sh.ignore"
COMMANDS=$(cat "${PWD}"/ci/tests/shell/cas-shell-script.sh.ignore)
printgreen "Running CAS command-line shell commands:"
echo "${COMMANDS}"

printgreen "Running CAS command-line shell version ${casVersion}"
java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5006 \
  -jar support/cas-server-support-shell/build/libs/cas-server-support-shell-${casVersion}.jar \
  @ci/tests/shell/cas-shell-script.sh.ignore \
  --spring.shell.interactive.enabled=false > cas-shell.ignore
pid=$!
tail -F cas-shell.ignore &
printgreen "Launched CAS command-line shell under process id ${pid}. Waiting for commands to finish..."
sleep 5
exitRequest="Command quit returned an error: EXECUTION_ERROR"
while :; do
    if grep -q "APPLICATION FAILED TO START" "cas-shell.ignore"; then
        printred "CAS command-line shell failed to start successfully"
        kill -9 ${pid} &> /dev/null
        exit 1
    fi
    if grep -q "Caused by: " "cas-shell.ignore"; then
        printred "CAS command-line shell encountered an error during execution"
        kill -9 ${pid} &> /dev/null
        exit 1
    fi
    if grep -q ${exitRequest} "cas-shell.ignore"; then
        printgreen "Found ${GREEN}${exitRequest}${ENDCOLOR}"
        rm -Rf ./cas-db-schema.sql
        break
    fi
    echo "Waiting for CAS command-line shell to finish..."
    sleep 2
done
printgreen "CAS command-line shell version ${casVersion} finished successfully!"
kill -9 ${pid} &> /dev/null
exit 0
