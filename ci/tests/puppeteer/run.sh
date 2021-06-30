#!/bin/bash

scenario="$1"
if [[ -z "$scenario" ]] ; then
  echo "Usage: ./ci/tests/puppeteer/run.sh ${PWD}/ci/tests/puppeteer/scenarios/[scenario name]"
  exit 1
fi
if [[ ! -d "${scenario}" ]]; then
  echo "${scenario} doesn't exist."
  exit 1;
fi

scenarioName=${scenario##*/}
export SCENARIO="${scenarioName}"

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
  npm_install_cmd="npm i --prefix "$PWD"/ci/tests/puppeteer puppeteer jsonwebtoken axios request"
  eval $npm_install_cmd || eval $npm_install_cmd || eval $npm_install_cmd
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
if [[ "${REBUILD}" != "false" ]]; then
  echo -e "\nBuilding CAS found in $PWD for dependencies [${dependencies}]"
  ./gradlew :webapp:cas-server-webapp-tomcat:build -DskipNestedConfigMetadataGen=true -x check -x javadoc \
    --no-daemon --build-cache --configure-on-demand --parallel -PcasModules="${dependencies}"
else
  echo -e "\nNot rebuilding war because REBUILD=false"
fi
cp "$PWD"/webapp/cas-server-webapp-tomcat/build/libs/cas-server-webapp-tomcat-*-SNAPSHOT.war "$PWD"/cas.war
if [ $? -eq 1 ]; then
  echo "Unable to build or locate the CAS web application file. Aborting test..."
  exit 1
fi

# paths passed as arguments on command line get converted
# by msys2 on windows, in some cases that is good but not with --somearg=file:/${PWD}/ci/...
# so this converts path to windows format
PORTABLE_PWD=${PWD}
command -v cygpath > /dev/null && test ! -z "$MSYSTEM"
if [[ $? -eq 0 ]]; then
  PORTABLE_PWD=$(cygpath -w $PWD)
fi

initScript=$(cat "${config}" | jq -j '.initScript // empty')
initScript="${initScript//\$\{PWD\}/${PWD}}"
initScript="${initScript//\$\{SCENARIO\}/${scenarioName}}"
[ -n "${initScript}" ] && \
  echo "Initialization script: ${initScript}" && \
  chmod +x "${initScript}" && \
  eval "export SCENARIO=${scenarioName}"; eval "${initScript}"

runArgs=$(cat "${config}" | jq -j '.jvmArgs // empty')
runArgs="${runArgs//\$\{PWD\}/${PWD}}"
[ -n "${runArgs}" ] && echo -e "JVM runtime arguments: [${runArgs}]"

properties=$(cat "${config}" | jq -j '.properties // empty | join(" ")')
properties="${properties//\$\{PWD\}/${PORTABLE_PWD}}"
properties="${properties//\$\{SCENARIO\}/${scenarioName}}"
properties="${properties//\%\{random\}/${random}}"
if [[ "$DEBUG" == "debug" ]]; then
  echo -e "Enabling debugger on port $DEBUG_PORT"
  runArgs="${runArgs} -Xrunjdwp:transport=dt_socket,address=$DEBUG_PORT,server=y,suspend=$DEBUG_SUSPEND"
fi
echo -e "\nLaunching CAS with properties [${properties}], run arguments [${runArgs}] and dependencies [${dependencies}]"
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
export NODE_TLS_REJECT_UNAUTHORIZED=0
node --unhandled-rejections=strict ${scriptPath} ${config}
RC=$?
if [[ $RC -ne 0 ]]; then
  echo "Script: ${scriptPath} with config: ${config} failed with return code ${RC}"
fi
echo -e "*************************************\n"

exitScript=$(cat "${config}" | jq -j '.exitScript // empty')
exitScript="${exitScript//\$\{PWD\}/${PWD}}"
exitScript="${exitScript//\$\{SCENARIO\}/${scenarioName}}"

[ -n "${exitScript}" ] && \
  echo "Exit script: ${exitScript}" && \
  chmod +x "${exitScript}" && \
  eval "export SCENARIO=${scenarioName}"; eval "${exitScript}"

if [[ "${CI}" != "true" ]]; then
  echo -e "Hit enter to cleanup scenario ${scenario} that ended with exit code $RC \n"
  read -r
fi

echo -e "\nKilling process ${pid} ..."
kill -9 $pid
rm "$PWD"/cas.war
rm "$PWD"/ci/tests/puppeteer/overlay/thekeystore
rm -Rf "$PWD"/ci/tests/puppeteer/overlay

if [[ "${CI}" == "true" ]]; then
  docker container stop $(docker container ls -aq) >/dev/null 2>&1 || true
  docker container rm $(docker container ls -aq) >/dev/null 2>&1 || true
fi

exit $RC
