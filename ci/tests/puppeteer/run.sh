#!/bin/bash

dependencies="$1"
scriptPath="$2"

echo -e "*************************************"
echo -e "CAS dependencies: ${dependencies}"
echo -e "Script path: ${scriptPath}\n"
echo -e "*************************************\n"

cd ./ci/tests/puppeteer/overlay
ls

echo -e "\nBuilding CAS found in $PWD..."
./gradlew clean build --no-daemon -PcasModules="${dependencies}"

echo -e "\nLaunching CAS..."
java -jar build/libs/cas.war --server.ssl.key-store=./thekeystore &> /dev/null &
pid=$!
echo -e "\nWaiting for CAS under pid ${pid}"
until curl -k -L --output /dev/null --silent --fail https://localhost:8443/cas/login; do
    printf '.'
    sleep 2
done
echo -e "\n\nReady!"

echo -e "*************************************"
echo -e "Running ${scriptPath}\n"
node ${scriptPath}
echo -e "*************************************\n"

echo -e "\nKilling process ${pid} ..."
kill -9 $pid

