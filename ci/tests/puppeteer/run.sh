#!/bin/bash

#echo "Installing jq"
#sudo apt-get install jq

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

echo -e "******************************************************"
echo -e "Scenario: ${scenario}"
echo -e "******************************************************\n"

config="${scenario}/script.json"
echo "Using scenario configuration file: ${config}"

dependencies=$(cat "${config}" | jq -j '.dependencies')
echo -e "\nBuilding CAS found in $PWD for dependencies [${dependencies}]"
./gradlew :webapp:cas-server-webapp-tomcat:build -DskipNestedConfigMetadataGen=true -x check -x javadoc \
  --no-daemon --build-cache --configure-on-demand --parallel -PcasModules="${dependencies}"
mv "$PWD"/webapp/cas-server-webapp-tomcat/build/libs/cas-server-webapp-tomcat-*.war "$PWD"/cas.war

initScript=$(cat "${config}" | jq -j '.initScript // empty')
[ -z "$result" ] && echo "Initialization script is: ${initScript}" && eval "$initScript"

properties=$(cat "${config}" | jq -j '.properties // empty | join(" ")')
echo -e "\nLaunching CAS with properties [${properties}] and dependencies [${dependencies}]"
java -jar "$PWD"/cas.war ${properties} --spring.profiles.active=none --server.ssl.key-store="$keystore" &
pid=$!
echo -e "\nWaiting for CAS under process id ${pid}"
until curl -k -L --output /dev/null --silent --fail https://localhost:8443/cas/login; do
    echo -n '.'
    sleep 1
done
echo -e "\n\nReady!"

scriptPath="${scenario}/script.js"
echo -e "*************************************"
echo -e "Running ${scriptPath}\n"
node --unhandled-rejections=strict ${scriptPath} ${config}
echo -e "*************************************\n"

docker container stop $(docker container ls -aq) >/dev/null 2>/dev/null
docker container rm $(docker container ls -aq) >/dev/null 2>/dev/null

echo -e "\nKilling process ${pid} ..."
kill -9 $pid
rm "$PWD"/cas.war
rm "$PWD"/ci/tests/puppeteer/overlay/thekeystore
rm -Rf "$PWD"/ci/tests/puppeteer/overlay
