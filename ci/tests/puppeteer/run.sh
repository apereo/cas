#!/bin/bash

echo "Installing jq"
sudo apt-get install jq

echo "Installing Puppeteer"
npm i --prefix "$PWD"/ci/tests/puppeteer puppeteer

echo "Creating overlay work directory"
rm -Rf "$PWD"/ci/tests/puppeteer/overlay
mkdir "$PWD"/ci/tests/puppeteer/overlay

dname="${dname:-CN=cas.example.org,OU=Example,OU=Org,C=US}"
subjectAltName="${subjectAltName:-dns:example.org,dns:localhost,ip:127.0.0.1}"
keystore="$PWD"/ci/tests/puppeteer/overlay/thekeystore
echo -e "\nGenerating keystore ${keystore} for CAS with DN=${dname}, SAN=${subjectAltName} ..."
[ -f "${keystore}" ] && rm "${keystore}"
keytool -genkey -noprompt -alias cas -keyalg RSA -keypass changeit -storepass changeit \
  -keystore "${keystore}" -dname "${dname}" -ext SAN="${subjectAltName}"
[ -f "${keystore}" ] && echo "Created ${keystore}"

scenario="$1"

echo -e "*************************************"
echo -e "Scenario: ${scenario}"
echo -e "*************************************\n"

config="${scenario}/script.json"
echo "Using scenario configuration file: ${config}"

dependencies=$(cat "${config}" | jq -j '.dependencies')
echo -e "\nBuilding CAS found in $PWD for dependencies [${dependencies}]"
./gradlew :webapp:cas-server-webapp-tomcat:clean :webapp:cas-server-webapp-tomcat:build --no-daemon -PcasModules="${dependencies}"
mv "$PWD"/webapp/cas-server-webapp-tomcat/build/libs/cas-server-webapp-tomcat-*.war "$PWD"/cas.war

properties=$(cat "${config}" | jq -j '.properties // empty | join(" ")')
echo -e "\nLaunching CAS with properties [${properties}] and dependencies [${dependencies}]"
java -jar "$PWD"/cas.war ${properties} --server.ssl.key-store="$keystore" &> /dev/null &
pid=$!
echo -e "\nWaiting for CAS under pid ${pid}"
until curl -k -L --output /dev/null --silent --fail https://localhost:8443/cas/login; do
    echo -n '.'
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

