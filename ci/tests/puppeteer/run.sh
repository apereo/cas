#!/bin/bash

tmp="${TMPDIR}"
if [[ -z "${tmp}" ]] ; then
  tmp="/tmp"
fi
export TMPDIR=${tmp}
echo "Using temp directory: ${TMPDIR}"

# Paths passed as arguments on command line get converted
# by msys2 on windows, in some cases that is good but not with --somearg=file:/${PWD}/ci/...
# so this converts path to windows format
PORTABLE_PWD=${PWD}
command -v cygpath > /dev/null && test ! -z "$MSYSTEM"
if [[ $? -eq 0 ]]; then
  PORTABLE_PWD=$(cygpath -w "$PWD")
fi

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
DRYRUN=""
CLEAR="true"

while (( "$#" )); do
  case "$1" in
  --scenario)
    scenario="$2"
    shift 2
    ;;
  --install-puppeteer|--install|--i)
      INSTALL_PUPPETEER="true"
      shift 1
      ;;
  --debug|--d)
    DEBUG="true"
    shift 1
    ;;
  --debug-port|--port)
    DEBUG_PORT="$2"
    shift 2
    ;;
  --debug-suspend|--suspend|--s)
    DEBUG_SUSPEND="y"
    shift 1
    ;;
  --rebuild|--r|--build)
    REBUILD="true"
    shift 1
    ;;
  --dry-run|--y)
    DRYRUN="true"
    shift 1
    printyellow "Skipping execution of test scenario while in dry-run mode."
    ;;
  --hbo)
    export HEADLESS="true"
    REBUILD="true"
    BUILDFLAGS="${BUILDFLAGS} --offline"
    shift 1;
    ;;
  --headless|--h)
    export HEADLESS="true"
    shift 1;
    ;;
  --rerun|--resume|--r)
    RERUN="true"
    shift 1;
    ;;
  --noclear|--nc)
    CLEAR=""
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

requiredEnvVars=$(jq -j '.conditions.env // empty' < "${config}")
if [[ ! -z ${requiredEnvVars} ]]; then
  echo "Checking for required environment variables"
  for e in ${requiredEnvVars//,/ } ; do
    if [[ -z "${!e}" ]]; then
      printred "Required environment variable ${e} is not set; skipping test execution."
      if [[ "${CI}" == "true" && ! -d ~/.npm ]] ; then
        # creating folder so setup-node post action cleanup doesn't bomb out
        mkdir ~/.npm
      fi
      exit 0
    fi
  done
fi

scenarioName=${scenario##*/}
enabled=$(jq -j '.enabled' < "${config}")
if [[ "${enabled}" == "false" ]]; then
  printyellow "\nTest scenario ${scenarioName} is not enabled. \nReview the scenario configuration at ${config} and enable the test."
  exit 0
fi

export SCENARIO="${scenarioName}"
export SCENARIO_PATH="${scenario}"

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

project=$(jq -j '.project // "tomcat"' < "${config}")
projectType=war
if [[ $project == starter* ]]; then
  projectType=jar
fi

casWebApplicationFile="${PWD}/webapp/cas-server-webapp-${project}/build/libs/cas-server-webapp-${project}-${casVersion}.${projectType}"
if [[ ! -f "$casWebApplicationFile" ]]; then
  printyellow "CAS web application at ${casWebApplicationFile} cannot be found. Rebuilding..."
  REBUILD="true"
fi

dependencies=$(jq -j '.dependencies' < "${config}")

buildScript=$(jq -j '.buildScript // empty' < "${config}")
BUILD_SCRIPT=""
if [[ -n "${buildScript}" ]]; then
  buildScript="${buildScript//\$\{PWD\}/${PORTABLE_PWD}}"
  buildScript="${buildScript//\$\{SCENARIO\}/${scenarioName}}"
  buildScript="${buildScript//\%\{random\}/${random}}"
  printgreen "Including build script [${buildScript}]"
  BUILD_SCRIPT="-DbuildScript=${buildScript}"
fi

if [[ "${REBUILD}" == "true" && "${RERUN}" != "true" ]]; then
  FLAGS=$(echo $BUILDFLAGS | sed 's/ //')
  printgreen "\nBuilding CAS found in $PWD for dependencies [${dependencies}] with flags [${FLAGS}]"

  ./gradlew :webapp:cas-server-webapp-${project}:build \
    -DskipNestedConfigMetadataGen=true -x check -x javadoc --build-cache --configure-on-demand --parallel \
    ${BUILD_SCRIPT} ${DAEMON} -DcasModules="${dependencies}" -q ${BUILDFLAGS}

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
  serverPort=8443
  processIds=()
  instances=$(jq -j '.instances // 1' < "${config}")
  for (( c = 1; c <= instances; c++ ))
  do
    initScript=$(jq -j '.initScript // empty' < "${config}")
    initScript="${initScript//\$\{PWD\}/${PWD}}"
    initScript="${initScript//\$\{SCENARIO\}/${scenarioName}}"
    [ -n "${initScript}" ] && \
      printgreen "Initialization script: ${initScript}" && \
      chmod +x "${initScript}" && \
      eval "export SCENARIO=${scenarioName}"; eval "${initScript}"

    runArgs=$(jq -j '.jvmArgs // empty' < "${config}")
    runArgs="${runArgs//\$\{PWD\}/${PWD}}"
    runArgs="${runArgs} -Xms512m -Xmx2048m -Xss128m -server"
    [ -n "${runArgs}" ] && echo -e "JVM runtime arguments: [${runArgs}]"

    properties=$(jq -j '.properties // empty | join(" ")' < "${config}")

    filter=".instance$c.properties // empty | join(\" \")"
    properties="$properties $(cat $config | jq -j -f <(echo "$filter"))"

    properties="${properties//\$\{PWD\}/${PORTABLE_PWD}}"
    properties="${properties//\$\{SCENARIO\}/${scenarioName}}"
    properties="${properties//\%\{random\}/${random}}"
    properties="${properties//\$\{TMPDIR\}/${TMPDIR}}"

    if [[ "$DEBUG" == "true" ]]; then
      printgreen "Remote debugging is enabled on port $DEBUG_PORT"
      runArgs="${runArgs} -Xrunjdwp:transport=dt_socket,address=$DEBUG_PORT,server=y,suspend=$DEBUG_SUSPEND"
    fi
    runArgs="${runArgs} -noverify -XX:TieredStopAtLevel=1 "
    printf "\nLaunching CAS instance #%s with properties [%s], run arguments [%s] and dependencies [%s]\n" "${c}" "${properties}" "${runArgs}" "${dependencies}"

    springAppJson=$(jq -j '.SPRING_APPLICATION_JSON // empty' < "${config}")
    [ -n "${springAppJson}" ] && export SPRING_APPLICATION_JSON=${springAppJson}

    printcyan "Launching CAS instance #${c} under port ${serverPort}"
    java ${runArgs} -Dlog.console.stacktraces=true -jar "$PWD"/cas.${projectType} \
       -Dcom.sun.net.ssl.checkRevocation=false --server.port=${serverPort}\
       --spring.profiles.active=none --server.ssl.key-store="$keystore" \
       ${properties} &
    pid=$!
    printcyan "Waiting for CAS instance #${c} under process id ${pid}"
    until curl -k -L --output /dev/null --silent --fail https://localhost:${serverPort}/cas/login; do
       echo -n '.'
       sleep 1
    done
    processIds+=( $pid )
    serverPort=$((serverPort + 1))
    if [[ "$DEBUG" == "true" ]]; then
      DEBUG_PORT=$((DEBUG_PORT + 1))
    fi
  done

  printgreen "\nReady!"
fi

RC=-1
if [[ "${DRYRUN}" != "true" ]]; then
  if [[ "${CLEAR}" == "true" ]]; then
    clear
  fi
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

  exitScript=$(jq -j '.exitScript // empty' < "${config}")
  exitScript="${exitScript//\$\{PWD\}/${PWD}}"
  exitScript="${exitScript//\$\{SCENARIO\}/${scenarioName}}"

  [ -n "${exitScript}" ] && \
    printgreen "Exit script: ${exitScript}" && \
    chmod +x "${exitScript}" && \
    eval "export SCENARIO=${scenarioName}"; eval "${exitScript}"

  printgreen "Done!\n"
fi

if [[ "${RERUN}" != "true" ]]; then
  if [[ "${CI}" != "true" ]]; then
    printgreen "Hit enter to clean up scenario ${scenario}\n"
    read -r
  fi

  for p in "${processIds[@]}"; do
    printgreen "Killing CAS process ${p}..."
    kill -9 "$p"
  done
  
  printgreen "Removing previous build artifacts..."
  rm "$PWD"/cas.${projectType}
  rm "$PWD"/ci/tests/puppeteer/overlay/thekeystore
  rm -Rf "$PWD"/ci/tests/puppeteer/overlay

  if [[ "${CI}" == "true" ]]; then
    docker stop $(docker container ls -aq) >/dev/null 2>&1 || true
    docker rm $(docker container ls -aq) >/dev/null 2>&1 || true
  fi
fi
printgreen "Bye!\n"
exit $RC
