#!/bin/bash

RED="\e[31m"
GREEN="\e[32m"
YELLOW="\e[33m"
ENDCOLOR="\e[0m"

function printred() {
  printf "${RED}$1${ENDCOLOR}\n"
}
function printgreen() {
  printf "${GREEN}$1${ENDCOLOR}\n"
}
function printyellow() {
  printf "${YELLOW}$1${ENDCOLOR}\n"
}

gradleUserHome="${GRADLE_USER_HOME:-$HOME/.gradle}"
mkdir -p "${gradleUserHome}"
if ! grep -q "org.gradle.internal.network.retry.max.attempts" "${gradleUserHome}/gradle.properties" 2>/dev/null; then
  echo "Limiting Gradle to 2 download attempts in CI to stay under Maven Central's rate limits"
  echo "systemProp.org.gradle.internal.network.retry.max.attempts=2" >> "${gradleUserHome}/gradle.properties"
fi

SCENARIO_CONFIG_FILE="$PWD/ci/tests/puppeteer/scenarios/$1/script.json"
echo "Checking scenario configuration file: ${SCENARIO_CONFIG_FILE}"

NODE_VERSION_REQUIRED=$(cat "$SCENARIO_CONFIG_FILE" | jq -j '.requirements.nodejs // empty')

NATIVE_BUILD_ENABLED=$(jq -r 'if .requirements.graalvm.build == null then true else .requirements.graalvm.build end' "$SCENARIO_CONFIG_FILE")
NATIVE_RUN_ENABLED=$(jq -r 'if .requirements.graalvm.run == null then true else .requirements.graalvm.run end' "$SCENARIO_CONFIG_FILE")
NATIVE_TEST_TIMEOUT=$(jq '([(.variations // [] | length), 1] | max) * 45 + 10' "$SCENARIO_CONFIG_FILE")

if [[ -z $NODE_VERSION_REQUIRED ]]; then
  echo "Scenario configuration does not specify a Node.js version"
  NODE_VERSION_REQUIRED=${NODE_CURRENT}
fi
if [[ -z $NATIVE_BUILD_ENABLED ]]; then
  NATIVE_BUILD_ENABLED="true"
fi
if [[ -z $NATIVE_RUN_ENABLED ]]; then
  NATIVE_RUN_ENABLED="true"
fi

printgreen "Node.js version: ${NODE_VERSION_REQUIRED}"
printgreen "Graal VM native build enabled: ${NATIVE_BUILD_ENABLED}"
printgreen "Graal VM native run enabled: ${NATIVE_RUN_ENABLED}"

echo "Finalizing GitHub environment variables..."
echo "NODE_VERSION_REQUIRED=$(echo $NODE_VERSION_REQUIRED)" >> $GITHUB_ENV
echo "NATIVE_BUILD_ENABLED=$(echo $NATIVE_BUILD_ENABLED)" >> $GITHUB_ENV
echo "NATIVE_RUN_ENABLED=$(echo $NATIVE_RUN_ENABLED)" >> $GITHUB_ENV
echo "NATIVE_TEST_TIMEOUT=${NATIVE_TEST_TIMEOUT}" >> "$GITHUB_ENV"
