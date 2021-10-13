#!/bin/bash

RED="\e[31m"
GREEN="\e[32m"
YELLOW="\e[33m"
CYAN="\e[36m"
ENDCOLOR="\e[0m"

function printcyan() {
  printf "${CYAN}$1${ENDCOLOR}\n"
}
function printgreen() {
  printf "${GREEN}$1${ENDCOLOR}\n"
}
function printyellow() {
  printf "${YELLOW}$1${ENDCOLOR}\n"
}
function printred() {
  printf "${RED}$1${ENDCOLOR}\n"
}

casVersion=(`cat $PWD/gradle.properties | grep "version" | cut -d= -f2`)
echo -n "Running Puppeteer tests for Apereo CAS Server: " && printcyan "${casVersion}"

DEBUG_PORT="5000"
DEBUG_SUSPEND="n"
DAEMON=""
BUILDFLAGS=""

while (( "$#" )); do
  case "$1" in
  --scenario)
    scenario="$2"
    shift 2
    ;;
  --install-puppeteer|--install)
      INSTALL_PUPPETEER="true"
      shift 1
      ;;
  --debug)
    DEBUG="true"
    shift 1
    ;;
  --debug-port|--port)
    DEBUG_PORT="$2"
    shift 2
    ;;
  --debug-suspend|--suspend)
    DEBUG_SUSPEND="y"
    shift 1
    ;;
  --rebuild)
    REBUILD="true"
    shift 1
    ;;
  --headless)
    export HEADLESS="true"
    shift 1;
    ;;
  --rerun)
    RERUN="true"
    shift 1;
    ;;
  *)
    BUILDFLAGS="${BUILDFLAGS} $1"
    shift 1;
    ;;
  esac
done

if [[ -z "$scenario" ]] ; then
  printred "Missing scenario name for the test"
  exit 1
fi
if [[ ! -d "${scenario}" ]]; then
  printred "Scenario ${scenario} doesn't exist."
  exit 1;
fi

scenarioName=${scenario##*/}
export SCENARIO="${scenarioName}"

if [[ "${CI}" == "true" ]]; then
  printgreen "DEBUG flag is turned off while running CI"
  DEBUG=""
  printgreen "Gradle daemon is turned off while running CI"
  DAEMON="--no-daemon"
fi

if [[ "${RERUN}" == "true" ]]; then
  REBUILD="false"
fi

#echo "Installing jq"
#sudo apt-get install jq

random=$(openssl rand -hex 8)

if [[ ! -d "$PWD"/ci/tests/puppeteer/node_modules/puppeteer || "${INSTALL_PUPPETEER}" == "true" ]]; then
  printgreen "Installing Puppeteer"
  cd "$PWD"/ci/tests/puppeteer
  npm_install_cmd="npm install"
  eval $npm_install_cmd || eval $npm_install_cmd || eval $npm_install_cmd
  cd -
else
  printgreen "Using existing Puppeteer modules..."
fi

if [[ "${RERUN}" != "true" ]]; then
  echo "Creating overlay work directory"
  rm -Rf "$PWD"/ci/tests/puppeteer/overlay
  mkdir "$PWD"/ci/tests/puppeteer/overlay
fi

keystore="$PWD"/ci/tests/puppeteer/overlay/thekeystore
export CAS_KEYSTORE="${keystore}"

if [[ "${RERUN}" != "true" ]]; then
  dname="${dname:-CN=cas.example.org,OU=Example,OU=Org,C=US}"
  subjectAltName="${subjectAltName:-dns:example.org,dns:localhost,ip:127.0.0.1}"
  printgreen "\nGenerating keystore ${keystore} for CAS with\nDN=${dname}, SAN=${subjectAltName} ..."
  [ -f "${keystore}" ] && rm "${keystore}"
  keytool -genkey -noprompt -alias cas -keyalg RSA -keypass changeit -storepass changeit \
    -keystore "${keystore}" -dname "${dname}"
  [ -f "${keystore}" ] && echo "Created ${keystore}"
fi

echo -e "******************************************************"
printgreen "Scenario: ${scenario}"
echo -e "******************************************************\n"

config="${scenario}/script.json"
echo "Using scenario configuration file: ${config}"
jq '.' "${config}" -e >/dev/null
if [ $? -ne 0 ]; then
 printred "\nFailed to parse scenario configuration file ${config}"
 exit 1
fi

project=$(cat "${config}" | jq -j '.project // "tomcat"')
projectType=war
if [[ $project == starter* ]]; then
  projectType=jar
fi


casWebApplicationFile="${PWD}/webapp/cas-server-webapp-${project}/build/libs/cas-server-webapp-${project}-${casVersion}.${projectType}"
if [[ ! -f "$casWebApplicationFile" ]]; then
  printyellow "CAS web application at ${casWebApplicationFile} cannot be found. Rebuilding..."
  REBUILD="true"
fi

dependencies=$(cat "${config}" | jq -j '.dependencies')

if [[ "${REBUILD}" == "true" && "${RERUN}" != "true" ]]; then
  printgreen "\nBuilding CAS found in $PWD for dependencies [${dependencies}] with flags [${BUILDFLAGS}]"

  ./gradlew :webapp:cas-server-webapp-${project}:build \
    -DskipNestedConfigMetadataGen=true -x check -x javadoc \
    ${DAEMON} --build-cache --configure-on-demand --parallel \
    -PcasModules="${dependencies}" -q ${BUILDFLAGS}

  if [ $? -eq 1 ]; then
    printred "\nFailed to build CAS web application. Examine the build output."
    exit 1
  fi
fi

if [[ "${RERUN}" != "true" ]]; then
  cp ${casWebApplicationFile} "$PWD"/cas.${projectType}
  if [ $? -eq 1 ]; then
    printred "Unable to build or locate the CAS web application file. Aborting test..."
    exit 1
  fi
fi


if [[ "${RERUN}" != "true" ]]; then
  # Paths passed as arguments on command line get converted
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
    printgreen "Initialization script: ${initScript}" && \
    chmod +x "${initScript}" && \
    eval "export SCENARIO=${scenarioName}"; eval "${initScript}"

  runArgs=$(cat "${config}" | jq -j '.jvmArgs // empty')
  runArgs="${runArgs//\$\{PWD\}/${PWD}}"
  [ -n "${runArgs}" ] && echo -e "JVM runtime arguments: [${runArgs}]"

  properties=$(cat "${config}" | jq -j '.properties // empty | join(" ")')
  properties="${properties//\$\{PWD\}/${PORTABLE_PWD}}"
  properties="${properties//\$\{SCENARIO\}/${scenarioName}}"
  properties="${properties//\%\{random\}/${random}}"
  if [[ "$DEBUG" == "true" ]]; then
    printyellow "Enabling debugger on port $DEBUG_PORT"
    runArgs="${runArgs} -Xrunjdwp:transport=dt_socket,address=$DEBUG_PORT,server=y,suspend=$DEBUG_SUSPEND"
  fi
  echo -e "\nLaunching CAS with properties [${properties}], run arguments [${runArgs}] and dependencies [${dependencies}]"
  java ${runArgs} -jar "$PWD"/cas.${projectType} ${properties} \
    -Dcom.sun.net.ssl.checkRevocation=false \
    --spring.profiles.active=none --server.ssl.key-store="$keystore" &
  pid=$!
  printgreen "\nWaiting for CAS under process id ${pid}"
  until curl -k -L --output /dev/null --silent --fail https://localhost:8443/cas/login; do
      echo -n '.'
      sleep 1
  done
  printgreen "\n\nReady!"
fi


clear
scriptPath="${scenario}/script.js"
echo -e "*************************************"
echo -e "Running ${scriptPath}\n"
export NODE_TLS_REJECT_UNAUTHORIZED=0
node --unhandled-rejections=strict ${scriptPath} ${config}
RC=$?
if [[ $RC -ne 0 ]]; then
  printred "Script: ${scriptPath} with config: ${config} failed with return code ${RC}"
fi
echo -e "*************************************\n"

exitScript=$(cat "${config}" | jq -j '.exitScript // empty')
exitScript="${exitScript//\$\{PWD\}/${PWD}}"
exitScript="${exitScript//\$\{SCENARIO\}/${scenarioName}}"

[ -n "${exitScript}" ] && \
  printgreen "Exit script: ${exitScript}" && \
  chmod +x "${exitScript}" && \
  eval "export SCENARIO=${scenarioName}"; eval "${exitScript}"

printgreen "Done!\n"

if [[ "${RERUN}" != "true" ]]; then
  if [[ "${CI}" != "true" ]]; then
    printgreen "Hit enter to cleanup scenario ${scenario} that ended with exit code $RC \n"
    read -r
  fi

  printyellow "\nKilling CAS process ${pid} ..."
  kill -9 $pid
  printyellow "\nRemoving previous build artifacts ..."
  rm "$PWD"/cas.${projectType}
  rm "$PWD"/ci/tests/puppeteer/overlay/thekeystore
  rm -Rf "$PWD"/ci/tests/puppeteer/overlay

  if [[ "${CI}" == "true" ]]; then
    docker stop $(docker container ls -aq) >/dev/null 2>&1 || true
    docker rm $(docker container ls -aq) >/dev/null 2>&1 || true
  fi
fi
exit $RC
