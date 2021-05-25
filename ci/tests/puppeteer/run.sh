#!/bin/bash

scenario="$1"
if [[ -z "$scenario" ]] ; then
  echo Usage: ./ci/tests/puppeteer/run.sh ${PWD}/ci/tests/puppeteer/scenarios/[scenario folder]
  exit 1
fi
if [[ ! -d "${scenario}" ]]; then
  echo "${scenario} doesn't exist."
  exit 1;
fi

# note if debugging you might need to call
# await page.setDefaultNavigationTimeout(0);
if [[ "${CI}" == "true" ]]; then
  DEBUG=${2}
else
  DEBUG=${2:-debug}
fi

DEBUG_PORT=${3:-5000}
DEBUG_SUSPEND=${4:-n}

#echo "Installing jq"
#sudo apt-get install jq

random=$(openssl rand -hex 8)

if [[ ! -d "$PWD"/ci/tests/puppeteer/node_modules/puppeteer ]] ; then
  echo "Installing Puppeteer"
  npm i --prefix "$PWD"/ci/tests/puppeteer puppeteer jsonwebtoken axios
else
  echo "Using existing Puppeteer modules..."
fi

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

echo -e "******************************************************"
echo -e "Scenario: ${scenario}"
echo -e "******************************************************\n"

config="${scenario}/script.json"
echo "Using scenario configuration file: ${config}"

dependencies=$(cat "${config}" | jq -j '.dependencies')
echo -e "\nBuilding CAS found in $PWD for dependencies [${dependencies}]"
./gradlew :webapp:cas-server-webapp-tomcat:build -DskipNestedConfigMetadataGen=true -x check -x javadoc \
  --no-daemon --build-cache --configure-on-demand --parallel -PcasModules="${dependencies}"
cp "$PWD"/webapp/cas-server-webapp-tomcat/build/libs/cas-server-webapp-tomcat-*.war "$PWD"/cas.war

initScript=$(cat "${config}" | jq -j '.initScript // empty')
initScript="${initScript//\$\{PWD\}/${PWD}}"
[ -n "${initScript}" ] && \
  echo "Initialization script: ${initScript}" && \
  chmod +x "${initScript}" && \
  eval "${initScript}"

runArgs=$(cat "${config}" | jq -j '.jvmArgs // empty')
runArgs="${runArgs//\$\{PWD\}/${PWD}}"
[ -n "${runArgs}" ] && echo -e "\JVM runtime arguments: [${runArgs}]"

properties=$(cat "${config}" | jq -j '.properties // empty | join(" ")')
properties="${properties//\$\{PWD\}/${PWD}}"
properties="${properties//\%\{random\}/${random}}"
if [[ "$DEBUG" == "debug" ]]; then
  echo -e "Enabling debugger on port $DEBUG_PORT"
  runArgs="${runArgs} -Xrunjdwp:transport=dt_socket,address=$DEBUG_PORT,server=y,suspend=$DEBUG_SUSPEND"
fi

echo -e "\nLaunching CAS with properties [${properties}], JVM arguments [${jvmArgs}] and dependencies [${dependencies}]"
java ${runArgs} -jar "$PWD"/cas.war ${properties} \
  --spring.profiles.active=none --server.ssl.key-store="$keystore" &
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
RC=$?
if [[ $RC -ne 0 ]]; then
  echo "Script: ${scriptPath} with config: ${config} failed with return code ${RC}"
fi
echo -e "*************************************\n"

exitScript=$(cat "${config}" | jq -j '.exitScript // empty')
exitScript="${exitScript//\$\{PWD\}/${PWD}}"
[ -n "${exitScript}" ] && \
  echo "Exit script: ${exitScript}" && \
  chmod +x "${exitScript}" && \
  eval "${exitScript}"

if [[ "${CI}" == "true" ]]; then  
  docker container stop $(docker container ls -aq) >/dev/null 2>&1 || true
  docker container rm $(docker container ls -aq) >/dev/null 2>&1 || true
fi

if [[ "${CI}" != "true" ]]; then
  echo -e "Hit enter to cleanup scenario ${scenario} that ended with exit code $RC \n"
  read -r
fi

echo -e "\nKilling process ${pid} ..."
kill -9 $pid
rm "$PWD"/cas.war
rm "$PWD"/ci/tests/puppeteer/overlay/thekeystore
rm -Rf "$PWD"/ci/tests/puppeteer/overlay
exit $RC
