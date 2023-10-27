#!/bin/bash

PUPPETEER_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
PUPPETEER_BUILD_CTR=${PUPPETEER_BUILD_CTR:-8}

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
PORTABLE_TMPDIR=${TMPDIR}
command -v cygpath > /dev/null && test ! -z "$MSYSTEM"
if [[ $? -eq 0 ]]; then
  PORTABLE_TMPDIR=$(cygpath -w "$TMPDIR")
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

casVersion=($(cat "$PWD"/gradle.properties | grep "version" | cut -d= -f2))
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
  --debug|--d|--g)
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
  --rebuild|--build|--b)
    REBUILD="true"
    shift 1
    ;;
  --dry-run|--y)
    DRYRUN="true"
    shift 1
    ;;
  --bo)
    REBUILD="true"
    BUILDFLAGS="${BUILDFLAGS} --offline"
    shift 1;
    ;;
  --ho)
    export HEADLESS="true"
    BUILDFLAGS="${BUILDFLAGS} --offline"
    shift 1;
    ;;
  --hr)
    export HEADLESS="true"
    RERUN="true"
    shift 1;
    ;;
  --hd)
    export HEADLESS="true"
    DEBUG="true"
    BUILDFLAGS="${BUILDFLAGS} --offline"
    shift 1;
    ;;
  --hb)
    export HEADLESS="true"
    REBUILD="true"
    shift 1;
    ;;
  --bod|--dob)
    REBUILD="true"
    DEBUG="true"
    BUILDFLAGS="${BUILDFLAGS} --offline"
    shift 1;
    ;;
  --hbdo|--hbod)
    export HEADLESS="true"
    REBUILD="true"
    DEBUG="true"
    BUILDFLAGS="${BUILDFLAGS} --offline"
    shift 1;
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
  --bogy|--boyd|--body)
    REBUILD="true"
    BUILDFLAGS="${BUILDFLAGS} --offline"
    DRYRUN="true"
    DEBUG="true"
    shift 1;
    ;;
  --boy)
    REBUILD="true"
    BUILDFLAGS="${BUILDFLAGS} --offline"
    DRYRUN="true"
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

requiredEnvVars=$(jq -j '.conditions.env // empty' "${config}")
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

docker info > /dev/null 2>&1
dockerInstalled=$?

dockerRequired=$(jq -j '.conditions.docker // empty' "${config}")
if [[ "${dockerRequired}" == "true" ]]; then
  echo "Checking if Docker is available..."
  if [[ "$CI" == "true" && "${RUNNER_OS}" != "Linux" ]]; then
    printyellow "Not running test in CI that requires Docker, because non-linux GitHub runner can't run Docker."
    exit 0
  fi
  if [[ $dockerInstalled -ne 0 ]] ; then
    printred "Docker engine is not running. Skipping running test since the test requires Docker."
    exit 0
  fi
fi


scenarioName=${scenario##*/}
enabled=$(jq -j '.enabled' "${config}")
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

if [[ "${DRYRUN}" == "true" ]]; then
  printyellow "Skipping execution of test scenario while in dry-run mode."
fi

#echo "Installing jq"
#sudo apt-get install jq

random=$(openssl rand -hex 8)

if [[ ! -d "${PUPPETEER_DIR}/node_modules/puppeteer" || "${INSTALL_PUPPETEER}" == "true" ]]; then
  printgreen "Installing Puppeteer"
  cd "$PUPPETEER_DIR"
  npm_install_cmd="npm install"
  eval $npm_install_cmd || eval $npm_install_cmd || eval $npm_install_cmd
  cd -
else
  printgreen "Using existing Puppeteer modules..."
fi

if [[ "${RERUN}" != "true" ]]; then
  echo "Creating overlay work directory"
  rm -Rf "${PUPPETEER_DIR}/overlay"
  mkdir "${PUPPETEER_DIR}/overlay"
fi

keystore="${PUPPETEER_DIR}/overlay/thekeystore"
public_cert="${PUPPETEER_DIR}/overlay/server.crt"
export CAS_KEYSTORE="${keystore}"
export CAS_CERT="${public_cert}"

if [[ "${RERUN}" != "true" ]]; then
  dname="${dname:-CN=cas.example.org,OU=Example,OU=Org,C=US}"
  subjectAltName="${subjectAltName:-dns:example.org,dns:localhost,dns:host.k3d.internal,dns:host.docker.internal,ip:127.0.0.1}"
  printgreen "\nGenerating keystore ${keystore} for CAS with\nDN=${dname}, SAN=${subjectAltName} ..."
  [ -f "${keystore}" ] && rm "${keystore}"
  [ -f "${public_cert}" ] && rm "${public_cert}"
  keytool -genkey -noprompt -alias cas -keyalg RSA -keypass changeit -storepass changeit \
    -keystore "${keystore}" -dname "${dname}" -ext "SAN=$subjectAltName"
  [ -f "${keystore}" ] && echo "Created ${keystore}"
  printgreen "\nExporting cert for adding to trust bundles if needed by test"
  keytool -export -noprompt -alias cas -keypass changeit -storepass changeit \
    -keystore "${keystore}" -file "${public_cert}" -rfc
fi

project=$(jq -j '.project // "tomcat"' "${config}")
projectType=war
if [[ $project == starter* ]]; then
  projectType=jar
fi

casWebApplicationFile="${PWD}/webapp/cas-server-webapp-${project}/build/libs/cas-server-webapp-${project}-${casVersion}.${projectType}"
if [[ ! -f "$casWebApplicationFile" ]]; then
  printyellow "CAS web application at ${casWebApplicationFile} cannot be found. Rebuilding..."
  REBUILD="true"
fi

dependencies=$(jq -j '.dependencies' "${config}")

buildScript=$(jq -j '.buildScript // empty' "${config}")
BUILD_SCRIPT=""
if [[ -n "${buildScript}" ]]; then
  buildScript="${buildScript//\$\{PWD\}/${PORTABLE_PWD}}"
  buildScript="${buildScript//\$\{SCENARIO\}/${scenarioName}}"
  buildScript="${buildScript//\%\{random\}/${random}}"
  printgreen "Including build script [${buildScript}]"
  BUILD_SCRIPT="-DbuildScript=${buildScript}"
fi

if [[ "${REBUILD}" == "true" && "${RERUN}" != "true" ]]; then
  if [[ "${CI}" == "true" && ! -z "${GRADLE_BUILDCACHE_PSW}" ]]; then
    # remote gradle cache employed
    DEFAULT_PUPPETEER_BUILD_CTR=8
  else
    DEFAULT_PUPPETEER_BUILD_CTR=30
  fi
  PUPPETEER_BUILD_CTR=${PUPPETEER_BUILD_CTR:-$DEFAULT_PUPPETEER_BUILD_CTR}

  FLAGS=$(echo $BUILDFLAGS | sed 's/ //')
  printgreen "\nBuilding CAS found in $PWD for dependencies [${dependencies}] with flags [${FLAGS}]"

  targetArtifact=./webapp/cas-server-webapp-${project}/build/libs/cas-server-webapp-${project}-${casVersion}.${projectType}
  if [[ -d ./webapp/cas-server-webapp-${project}/build/libs ]]; then
    rm -rf ./webapp/cas-server-webapp-${project}/build/libs
  fi

  buildcmd=$(printf '%s' \
      "./gradlew :webapp:cas-server-webapp-${project}:build \
      -DskipNestedConfigMetadataGen=true --no-configuration-cache -x check -x test -x javadoc --build-cache --configure-on-demand --parallel \
      ${BUILD_SCRIPT} ${DAEMON} -DcasModules="${dependencies}" --no-watch-fs --max-workers=8 ${BUILDFLAGS}")
  echo $buildcmd

  if [[ "${CI}" == "true" ]]; then
    printcyan "Launching build in background to make observing slow builds easier..."
    $buildcmd > build.log 2>&1 &
    pid=$!
    sleep 25
    printgreen "Current Java processes found for PID ${pid}"
    ps -ef | grep $pid | grep java
    if [[ $? -ne 0 ]]; then
      # This check is mainly for running on windows in CI
      printcyan "Java not running 20 seconds after starting gradlew ... trying again"
      cat build.log
      kill $pid
      $buildcmd > build.log 2>&1 &
      pid=$!
    fi
    printcyan "Waiting for build to finish. Process id is ${pid} - Waiting for ${targetArtifact}"
    counter=0
    until [[ -f ${targetArtifact} ]]; do
       let counter++
       if [[ $counter -gt $PUPPETEER_BUILD_CTR ]]; then
          printred "\nBuild is taking too long; aborting."
          printred "Build log"
          cat build.log
          printred "Build thread dump"
          jstack $pid || true
          exit 3
       fi
       echo -n '.'
       sleep 15
    done
    wait $pid
    if [ $? -ne 0 ]; then
      printred "\nFailed to build CAS web application. Examine the build output."
      cat build.log
      exit 2
    else
      printgreen "\nBackground build successful. Build output was:"
      cat build.log
      rm build.log
    fi
  else
    printcyan "Launching CAS build in a non-CI environment..."
    $buildcmd
    pid=$!
    wait $pid
    if [ $? -ne 0 ]; then
      printred "\nFailed to build CAS web application."
      exit 2
    fi
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
  environmentVariables=$(jq -j '.environmentVariables // empty | join(";")' "$config");
  IFS=';' read -r -a variables <<< "$environmentVariables"
  for env in "${variables[@]}"
  do
      cmd="export \"$env\""
      if [[ "${CI}" != "true" ]]; then
        echo "$cmd"
      fi
      eval "$cmd"
  done
  
  bootstrapScript=$(jq -j '.bootstrapScript // empty' "${config}")
  bootstrapScript="${bootstrapScript//\$\{PWD\}/${PWD}}"
  bootstrapScript="${bootstrapScript//\$\{SCENARIO\}/${scenarioName}}"

  if [[ -n "${bootstrapScript}" ]]; then
    printgreen "Running bootstrap script: ${bootstrapScript}"
    chmod +x "${bootstrapScript}"
    eval "${bootstrapScript}"
    if [[ $? -ne 0 ]]; then
      printred "Bootstrap script [${bootstrapScript}] failed."
      exit 1
    fi
  fi

  serverPort=8443
  processIds=()
  instances=$(jq -j '.instances // 1' "${config}")
  if [[ ! -z "$instances" ]]; then
    echo "Found instances: ${instances}"
  fi
  for (( c = 1; c <= instances; c++ ))
  do
    export SCENARIO="${scenarioName}"

    initScript=$(jq -j '.initScript // empty' "${config}")
    initScript="${initScript//\$\{PWD\}/${PWD}}"
    initScript="${initScript//\$\{SCENARIO\}/${scenarioName}}"
    scripts=$(echo "$initScript" | tr ',' '\n')

    for script in ${scripts}; do
      printgreen "Running initialization script: ${script}"
      chmod +x "${script}"
      eval "${script}"
      if [[ $? -ne 0 ]]; then
        printred "Initialization script [${script}] failed."
        exit 1
      fi
    done

    runArgs=$(jq -j '.jvmArgs // empty' "${config}")
    runArgs="${runArgs//\$\{PWD\}/${PWD}}"
    runArgs="${runArgs} -Xms512m -Xmx2048m -Xss128m -server"
    [ -n "${runArgs}" ] && echo -e "JVM runtime arguments: [${runArgs}]"

    properties=$(jq -j '.properties // empty | join(" ")' "${config}")

    filter=".instance$c.properties // empty | join(\" \")"
    echo "$filter" > $TMPDIR/filter.jq
    properties="$properties $(cat $config | jq -j -f $TMPDIR/filter.jq)"
    rm $TMPDIR/filter.jq
    properties="${properties//\$\{PWD\}/${PORTABLE_PWD}}"
    properties="${properties//\$\{SCENARIO\}/${scenarioName}}"
    properties="${properties//\%\{random\}/${random}}"
    properties="${properties//\$\{TMPDIR\}/${PORTABLE_TMPDIR}}"

    if [[ "$DEBUG" == "true" ]]; then
      printgreen "Remote debugging is enabled on port $DEBUG_PORT"
      runArgs="${runArgs} -Xrunjdwp:transport=dt_socket,address=$DEBUG_PORT,server=y,suspend=$DEBUG_SUSPEND"
    fi
    runArgs="${runArgs} -noverify -XX:TieredStopAtLevel=1 "
    printf "\nLaunching CAS instance #%s with properties [%s], run arguments [%s] and dependencies [%s]\n" "${c}" "${properties}" "${runArgs}" "${dependencies}"

    springAppJson=$(jq -j '.SPRING_APPLICATION_JSON // empty' "${config}")
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
  readyScript=$(jq -j '.readyScript // empty' < "${config}")
  readyScript="${readyScript//\$\{PWD\}/${PWD}}"
  readyScript="${readyScript//\$\{SCENARIO\}/${scenarioName}}"
  scripts=$(echo "$readyScript" | tr ',' '\n')

  for script in ${scripts}; do
    printgreen "Running ready script: ${script}"
    chmod +x "${script}"
    eval "${script}"
  done
fi

RC=-1
if [[ "${DRYRUN}" != "true" ]]; then
  if [[ "${CLEAR}" == "true" ]]; then
    clear
  fi
  scriptPath="${scenario}/script.js"
  echo -e "**************************************************************************"
  echo -e "Running ${scriptPath}\n"
  export NODE_TLS_REJECT_UNAUTHORIZED=0
  node --unhandled-rejections=strict ${scriptPath} ${config}
  RC=$?
  if [[ $RC -ne 0 ]]; then
    printred "Script: ${scriptPath} with config: ${config} failed with return code ${RC}"
  fi
  echo -e "**************************************************************************\n"

  exitScript=$(jq -j '.exitScript // empty' "${config}")
  exitScript="${exitScript//\$\{PWD\}/${PWD}}"
  exitScript="${exitScript//\$\{SCENARIO\}/${scenarioName}}"

  [ -n "${exitScript}" ] && \
    printcyan "Exit script: ${exitScript}" && \
    chmod +x "${exitScript}" && \
    eval "${exitScript}"

  if [[ $RC -ne 0 ]]; then
    printred "Test scenario [${scenarioName}] has failed.\n"
  else
    printgreen "Test scenario [${scenarioName}] has passed successfully!\n"
  fi
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
  rm "${keystore}"
  rm "${public_cert}"
  rm -Rf "${PUPPETEER_DIR}/overlay"

  if [[ "${CI}" == "true" && $dockerInstalled -eq 0 ]]; then
    docker stop $(docker container ls -aq) >/dev/null 2>&1 || true
    docker rm $(docker container ls -aq) >/dev/null 2>&1 || true
  fi
fi
printgreen "Bye!\n"
exit $RC
