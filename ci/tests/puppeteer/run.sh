#!/bin/bash

scenario="$1"

echo -e "*************************************"
echo -e "Scenario: ${scenario}"
echo -e "*************************************\n"

cd ./ci/tests/puppeteer/overlay
ls

config="${scenario}/script.json"
echo "Using scenario configuration: ${config}"

dependencies=$(cat "${config}" | jq -j '.dependencies')
echo -e "\nBuilding CAS found in $PWD for dependencies ${dependencies}..."
./gradlew clean build --no-daemon -PcasModules="${dependencies}"

echo -e "\nLaunching CAS..."
java -jar build/libs/cas.war --server.ssl.key-store=./thekeystore &> /dev/null &
pid=$!
echo -e "\nWaiting for CAS under pid ${pid}"
until curl -k -L --output /dev/null --silent --fail https://localhost:8443/cas/login; do
    printf '.'
    sleep 3
done
echo -e "\n\nReady!"

scriptPath="${scenario}/script.js"
echo -e "*************************************"
echo -e "Running ${scriptPath}\n"
node ${scriptPath}
echo -e "*************************************\n"

echo -e "\nKilling process ${pid} ..."
kill -9 $pid

