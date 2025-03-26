#!/bin/bash

PUPPETEER_DIR=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" &>/dev/null && pwd)

tmp="${TMPDIR}"
if [[ -z "${tmp}" ]]; then
  tmp="/tmp"
fi
export TMPDIR=${tmp}
echo "Using temp directory: ${TMPDIR}"

# Paths passed as arguments on command line get converted
# by msys2 on windows, in some cases that is good but not with --somearg=file:/${PWD}/ci/...
# so this converts path to windows format
PORTABLE_PWD=${PWD}
PORTABLE_TMPDIR=${TMPDIR}
command -v cygpath >/dev/null && test ! -z "$MSYSTEM"
if [[ $? -eq 0 ]]; then
  PORTABLE_TMPDIR=$(cygpath -w "$TMPDIR")
  PORTABLE_PWD=$(cygpath -w "$PWD")
fi

RED="\e[31m"
GREEN="\e[32m"
YELLOW="\e[33m"
CYAN="\e[36m"
ENDCOLOR="\e[0m"

# Set the default values for the build variables
DEBUG_PORT="5000"
DEBUG_SUSPEND="n"
DAEMON=""
BUILDFLAGS=""
DRYRUN=""
INITONLY="false"
NATIVE_BUILD="false"
NATIVE_RUN="false"
BUILD_SPAWN="background"
QUIT_QUIETLY="false"
DISABLE_LINTER="false"

function printcyan() {
  printf "🔷 ${CYAN}$1${ENDCOLOR}\n"
}
function printgreen() {
  printf "☘️  ${GREEN}$1${ENDCOLOR}\n"
}
function printyellow() {
  printf "⚠️  ${YELLOW}$1${ENDCOLOR}\n"
}
function printred() {
  printf "🔥  ${RED}$1${ENDCOLOR}\n"
}

function progressbar() {
  current="$1"
  total="$2"

  bar_size=60
  bar_char_done="#"
  bar_char_todo="-"
  bar_percentage_scale=2

  # calculate the progress in percentage
  percent=$(bc <<<"scale=$bar_percentage_scale; 100 * $current / $total")
  # The number of done and todo characters
  done=$(bc <<<"scale=0; $bar_size * $percent / 100")
  todo=$(bc <<<"scale=0; $bar_size - $done")

  # build the done and todo sub-bars
  done_sub_bar=$(printf "%${done}s" | tr " " "${bar_char_done}")
  todo_sub_bar=$(printf "%${todo}s" | tr " " "${bar_char_todo}")

  # output the bar
  echo -ne "\rProgress: [${done_sub_bar}${todo_sub_bar}] ${percent}%"
  if [ $total -eq $current ]; then
    echo -e "\n"
  fi
}

function sleepfor() {
  tasks_in_total="$1"
  for current_task in $(seq "$tasks_in_total"); do
    sleep 1
    progressbar "$current_task" "$tasks_in_total"
  done
}

function fetchCasVersion() {
  casVersion=($(cat "$PWD"/gradle.properties | grep "version" | cut -d= -f2))
  echo -e -n "Running Puppeteer tests for Apereo CAS Server: ${casVersion}\n"
}

function parseArguments() {
  while (("$#")); do
    case "$1" in
    --nbr)
      NATIVE_RUN="true"
      NATIVE_BUILD="true"
      BUILDFLAGS="${BUILDFLAGS} --no-configuration-cache"
      QUIT_QUIETLY="true"
      shift 1
      ;;
    --nr | --native-run)
      NATIVE_RUN="true"
      BUILDFLAGS="${BUILDFLAGS} --no-configuration-cache"
      shift 1
      ;;
    --native | --graalvm | --nb)
      NATIVE_BUILD="true"
      BUILDFLAGS="${BUILDFLAGS} --no-configuration-cache"
      shift 1
      ;;
    --scenario | --sc)
      scenario="$2"
      shift 2
      ;;
    --install-puppeteer | --install | --i)
      INSTALL_PUPPETEER="true"
      shift 1
      ;;
    --debug | --d | --g)
      DEBUG="true"
      shift 1
      ;;
    --debug-port | --port)
      DEBUG_PORT="$2"
      shift 2
      ;;
    --debug-suspend | --suspend | --s)
      DEBUG_SUSPEND="y"
      shift 1
      ;;
    --rebuild | --build | --b)
      REBUILD="true"
      shift 1
      ;;
    --dry-run | --y | --dry)
      DRYRUN="true"
      shift 1
      ;;
    --bo | -bo)
      REBUILD="true"
      BUILDFLAGS="${BUILDFLAGS} --offline"
      shift 1
      ;;
    --hol | -hol)
      export HEADLESS="true"
      BUILDFLAGS="${BUILDFLAGS} --offline"
      DISABLE_LINTER="true"
      shift 1
      ;;
    --hoy)
      export HEADLESS="true"
      BUILDFLAGS="${BUILDFLAGS} --offline"
      DRYRUN="true"
      shift 1
      ;;
    --ho | -ho)
      export HEADLESS="true"
      BUILDFLAGS="${BUILDFLAGS} --offline"
      shift 1
      ;;
    --hr | -hr)
      export HEADLESS="true"
      RERUN="true"
      DISABLE_LINTER="true"
      shift 1
      ;;
    --hd | --hdo)
      export HEADLESS="true"
      DEBUG="true"
      BUILDFLAGS="${BUILDFLAGS} --offline"
      shift 1
      ;;
    --hb | -hb)
      export HEADLESS="true"
      REBUILD="true"
      shift 1
      ;;
    --bod | --dob)
      REBUILD="true"
      DEBUG="true"
      BUILDFLAGS="${BUILDFLAGS} --offline"
      shift 1
      ;;
    --hbod | -hbod | --hbdo)
      export HEADLESS="true"
      REBUILD="true"
      BUILDFLAGS="${BUILDFLAGS} --offline"
      DEBUG="true"
      shift 1
      ;;
    --hboy)
      export HEADLESS="true"
      REBUILD="true"
      BUILDFLAGS="${BUILDFLAGS} --offline"
      DRYRUN="true"
      shift 1
      ;;
    --hbo | -hbo)
      export HEADLESS="true"
      REBUILD="true"
      BUILDFLAGS="${BUILDFLAGS} --offline"
      shift 1
      ;;
    --headless | --h)
      export HEADLESS="true"
      shift 1
      ;;
    --rerun | --resume | --r)
      RERUN="true"
      DISABLE_LINTER="true"
      shift 1
      ;;
    --bogy | --boyd | --body)
      REBUILD="true"
      BUILDFLAGS="${BUILDFLAGS} --offline"
      DRYRUN="true"
      DEBUG="true"
      shift 1
      ;;
    --boy | -boy)
      REBUILD="true"
      BUILDFLAGS="${BUILDFLAGS} --offline"
      DRYRUN="true"
      shift 1
      ;;
    --initonly | --io)
      REBUILD="false"
      BUILDFLAGS="${BUILDFLAGS} --offline"
      DRYRUN="true"
      INITONLY="true"
      export HEADLESS="true"
      shift 1
      ;;
    --nolint | --no-lint | --nol | --nl)
      DISABLE_LINTER="true"
      shift 1
      ;;
    *)
      BUILDFLAGS="${BUILDFLAGS} $1"
      shift 1
      ;;
    esac
  done
}

function validateScenario() {
  if [[ -z "$scenario" ]]; then
    printred "Missing scenario name for the test"
    exit 1
  fi
  if [[ ! -d "${scenario}" ]]; then
    printred "Scenario ${scenario} doesn't exist."
    exit 1
  fi

  echo -e "******************************************************"
  printgreen "Scenario: ${scenario}"
  if [[ "${NATIVE_BUILD}" == "true" || "${NATIVE_RUN}" == "true" ]]; then
    printcyan "Running Graal VM native image CAS build"
    echo "GRAALVM_HOME: $GRAALVM_HOME"
    java --version
  fi
  echo -e -n "Node version: " && node --version
  echo -e "******************************************************\n"

  config="${scenario}/script.json"
  echo "Using scenario configuration file: ${config}"
  jq '.' "${config}" -e >/dev/null
  if [ $? -ne 0 ]; then
    printred "Failed to parse scenario configuration file ${config}"
    exit 1
  fi

  requiredEnvVars=$(jq -j '.conditions.env // empty' "${config}")
  if [[ ! -z ${requiredEnvVars} ]]; then
    echo "Checking for required environment variables"
    for e in ${requiredEnvVars//,/ }; do
      if [[ -z "${!e}" ]]; then
        printred "Required environment variable ${e} is not set; skipping test execution."
        if [[ "${CI}" == "true" && ! -d ~/.npm ]]; then
          # creating folder so setup-node post action cleanup doesn't bomb out
          mkdir ~/.npm
        fi
        exit 0
      fi
    done
  fi

  docker info >/dev/null 2>&1
  dockerInstalled=$?

  dockerRequired=$(jq -j '.conditions.docker // empty' "${config}")
  if [[ "${dockerRequired}" == "true" ]]; then
    echo "Checking if Docker is available..."
    if [[ "$CI" == "true" && "${RUNNER_OS}" != "Linux" ]]; then
      printyellow "Not running test in CI that requires Docker, because non-linux GitHub runner can't run Docker."
      exit 0
    fi

    if [[ $dockerInstalled -ne 0 ]]; then
      printred "Docker engine is not running. Skipping running test since the test requires Docker."
      exit 1
    fi
    if [[ "$CI" == "true" ]]; then
      printgreen "Docker engine is available"
      docker --version
    fi
  fi

  buildDockerImage=$(jq -j '.requirements.docker.build // empty' "${config}")
  if [[ "${buildDockerImage}" == "true" && $dockerInstalled -ne 0 ]]; then
    printred "Docker engine is not running. The test is unable to build a Docker image."
    exit 1
  fi

  cdsEnabled=$(jq -j 'if .requirements.cds.enabled == "" or .requirements.cds.enabled == null or .requirements.cds.enabled == true then true else false end' "${config}")

  scenarioName=${scenario##*/}
  enabled=$(jq -j '.enabled' "${config}")
  if [[ "${enabled}" == "false" ]]; then
    printyellow "Test scenario ${scenarioName} is not enabled. \nReview the scenario configuration at ${config} and re-enable the test."
    exit 0
  fi

  export SCENARIO="${scenarioName}"
  export SCENARIO_PATH="${scenario}"
  export SCENARIO_FOLDER=$(cd -- "${SCENARIO_PATH}" &>/dev/null && pwd)

  scriptPath="${scenario}/script.js"
}

function prepareScenario() {
  if [[ "${CI}" == "true" ]]; then
    printgreen "DEBUG flag is turned off while running CI"
    DEBUG=""
    printgreen "Gradle daemon is turned off while running CI"
    DAEMON="--no-daemon"

    printgreen "Creating configuration directories.."
    sudo mkdir -p /etc/cas/config
    sudo mkdir -p /etc/cas/static
    sudo mkdir -p /etc/cas/saml
    sudo mkdir -p /etc/cas/services
    sudo mkdir -p /etc/cas/oidc
    sudo chmod -R 777 /etc/cas
    ls -al /etc/cas
  fi

  if [[ "${RERUN}" == "true" ]]; then
    REBUILD="false"
  fi

  if [[ "${INITONLY}" == "true" ]]; then
    REBUILD="false"
  fi

  if [[ "${CI}" == "" || "${CI}" == "false" ]]; then
    BUILD_SPAWN="foreground"
  fi

  if [[ "${NATIVE_BUILD}" == "true" ]]; then
    REBUILD="true"
    BUILD_SPAWN="foreground"
  fi

  if [[ "${DRYRUN}" == "true" ]]; then
    printyellow "Skipping execution of test scenario while in dry-run/initialize-only mode."
  fi

  random=$(openssl rand -hex 8)

  if [[ ! -d "${PUPPETEER_DIR}/node_modules/puppeteer" || "${INSTALL_PUPPETEER}" == "true" ]]; then
    printgreen "Installing Puppeteer"
    cd "$PUPPETEER_DIR"
    npm install --fetch-timeout 5000 --fetch-retries 3 --fetch-retry-maxtimeout 30000 --no-audit
    cd -
  else
    printgreen "Using existing Puppeteer modules..."
  fi

  if [[ "${DISABLE_LINTER}" == "false" ]]; then
    printgreen "Running ESLint on scenario [${scenarioName}] from ${PUPPETEER_DIR}..."
    pushd "$PUPPETEER_DIR" || exit 1
    npx eslint "./scenarios/${SCENARIO}/script.js"
    if [ $? -ne 0 ]; then
      printred "Found linting errors; unable to run the scenario [${scenarioName}]"
      printred "Please run: npx eslint --fix ${scriptPath}"
      exit 1
    fi
    echo ./scenarios/"${SCENARIO}"/script.json jq >/dev/null
    if [ $? -ne 0 ]; then
      printred "Found linting errors in scenario configuration; unable to run the scenario [${scenarioName}]"
      exit 1
    fi
    popd || exit 1
  fi

  if [[ "${RERUN}" != "true" ]]; then
    echo "Creating overlay work directory"
    rm -Rf "${PUPPETEER_DIR}/overlay"
    mkdir "${PUPPETEER_DIR}/overlay"
  fi

  createCasKeystore
}

function createCasKeystore() {
    overlayDirectory="${PUPPETEER_DIR}/overlay"
    mkdir -p "${overlayDirectory}"
    keystore="${overlayDirectory}/thekeystore"
    public_cert="${overlayDirectory}/server.crt"

    export CAS_KEYSTORE="${keystore}"
    export CAS_CERT="${public_cert}"

    if [[ "${RERUN}" != "true" && "${INITONLY}" != "true" ]]; then
      if [[ -f "${keystore}" ]]; then
        printcyan "Keystore ${keystore} already exists and will not be created again"
      else
        dname="${dname:-CN=cas.example.org,OU=Example,OU=Org,C=US}"
        subjectAltName="${subjectAltName:-dns:example.org,dns:localhost,dns:host.k3d.internal,dns:host.docker.internal,ip:127.0.0.1}"
        printgreen "Generating keystore ${keystore} for CAS with\nDN=${dname}, SAN=${subjectAltName} ..."
        [ -f "${public_cert}" ] && rm "${public_cert}"
        keytool -genkey -noprompt -alias cas -keyalg RSA -keypass changeit -storepass changeit \
          -keystore "${keystore}" -dname "${dname}" -ext "SAN=$subjectAltName"
        [ -f "${keystore}" ] && echo "Created ${keystore}"
        printgreen "Exporting cert for adding to trust bundles if needed by test"
        keytool -export -noprompt -alias cas -keypass changeit -storepass changeit \
          -keystore "${keystore}" -file "${public_cert}" -rfc
      fi
    fi
}

function buildAndRun() {
  createCasKeystore
  
  if [[ "${NATIVE_BUILD}" == "false" && "${NATIVE_RUN}" == "false" ]]; then
    project=$(jq -j '.server // "tomcat"' "${config}")
    projectType=war
    if [[ $project == starter* ]]; then
      projectType=jar
    fi
  else
    project="native"
    printgreen "Project type is chosen to be ${project}"
  fi

  if [[ "${NATIVE_BUILD}" == "false" && "${NATIVE_RUN}" == "false" ]]; then
    casWebApplicationFile="${PWD}/webapp/cas-server-webapp-${project}/build/libs/cas-server-webapp-${project}-${casVersion}.${projectType}"
  else
    casWebApplicationFile="${PWD}/webapp/cas-server-webapp-${project}/build/native/nativeCompile/cas"
  fi
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

  if [[ "${NATIVE_BUILD}" == "false" && "${NATIVE_RUN}" == "false" ]]; then
    targetArtifact=./webapp/cas-server-webapp-${project}/build/libs/cas-server-webapp-${project}-${casVersion}.${projectType}
  else
    targetArtifact=./webapp/cas-server-webapp-${project}/build/${project}/nativeCompile/cas
    if [[ ! -f "$targetArtifact" ]]; then
      NATIVE_BUILD="true"
    fi
  fi
  echo "Target artifact generated by the build: ${targetArtifact}"

  if [[ "${REBUILD}" == "true" && "${RERUN}" != "true" ]]; then
    if [[ "${NATIVE_BUILD}" == "true" || "${NATIVE_RUN}" == "true" ]]; then
      DEFAULT_PUPPETEER_BUILD_CTR=45
    elif [[ "${CI}" == "true" && ! -z "${GRADLE_BUILDCACHE_PSW}" ]]; then
      DEFAULT_PUPPETEER_BUILD_CTR=30
    else
      DEFAULT_PUPPETEER_BUILD_CTR=30
    fi
    PUPPETEER_BUILD_CTR=${PUPPETEER_BUILD_CTR:-$DEFAULT_PUPPETEER_BUILD_CTR}

    FLAGS=$(echo $BUILDFLAGS | sed 's/ //')
    printgreen "Building CAS found in $PWD for dependencies [${dependencies}] with flags [${FLAGS}]"

    if [[ -d ./webapp/cas-server-webapp-${project}/build/libs ]]; then
      rm -rf ./webapp/cas-server-webapp-${project}/build/libs
    fi

    BUILD_TASKS=":webapp:cas-server-webapp-${project}:build"
    if [[ "${NATIVE_BUILD}" == "true" ]]; then
      BUILD_TASKS="${BUILD_TASKS} :webapp:cas-server-webapp-${project}:nativeCompile -DaotSpringActiveProfiles=none"
    fi

    rm -rf ${targetArtifact}
    BUILD_COMMAND=$(printf '%s' \
      "./gradlew ${BUILD_TASKS} -DskipSpringBootDevTools=true -DskipNestedConfigMetadataGen=true -x check -x test -x javadoc --build-cache --configure-on-demand --parallel \
         ${BUILD_SCRIPT} ${DAEMON} -DskipBootifulLaunchScript=true -DcasModules="${dependencies}" --no-watch-fs --max-workers=8 ${BUILDFLAGS}")
    printcyan "Executing build command in the ${BUILD_SPAWN}:\n\n${BUILD_COMMAND}"

    if [[ "${BUILD_SPAWN}" == "background" ]]; then
      printcyan "Launching build in background to make observing slow builds easier..."
      $BUILD_COMMAND >build.log 2>&1 &
      pid=$!
      sleepfor 25
      printgreen "Current Java processes found for PID ${pid}"
      ps -ef | grep $pid | grep java
      if [[ $? -ne 0 ]]; then
        # This check is mainly for running on windows in CI
        printcyan "Java not running after starting gradle ... trying again"
        cat build.log
        kill $pid
        $BUILD_COMMAND >build.log 2>&1 &
        pid=$!
      fi
      printcyan "Waiting for build to finish. Process id is ${pid} - Waiting for ${targetArtifact}"
      counter=0
      until [[ -f ${targetArtifact} ]]; do
        let counter++
        if [[ $counter -gt $PUPPETEER_BUILD_CTR ]]; then
          printred "\nBuild is taking too long; build counter ${counter} is greater than ${PUPPETEER_BUILD_CTR}. Aborting..."
          printred "Build log"
          cat build.log
          printred "Build thread dump..."
          jstack $pid || true
          exit 3
        fi
        echo -n '.'
        sleepfor 60
      done
      wait $pid
      if [ $? -ne 0 ]; then
        printred "Failed to build CAS web application. Examine the build output."
        cat build.log
        exit 2
      else
        printgreen "Background build successful. Build output was:"
        cat build.log
        rm build.log
      fi
    else
      printcyan "Launching CAS build in the foreground..."
      $BUILD_COMMAND
      pid=$!
      wait $pid
      if [[ ! -e "${targetArtifact}" ]]; then
        printred "Failed to build CAS web application: ${targetArtifact}."
        exit 2
      fi
    fi
  fi

  if [[ "${RERUN}" != "true" && "${NATIVE_BUILD}" == "false" ]]; then
    cp "${casWebApplicationFile}" "$PWD"/cas.${projectType}
    if [ $? -eq 1 ]; then
      printred "Unable to build or locate the CAS web application file. Aborting test..."
      exit 1
    fi
  fi

  if [[ "${buildDockerImage}" == "true" ]]; then
    docker rmi "cas-${scenarioName}":latest --force >/dev/null 2>&1

    if [[ -f "$SCENARIO_FOLDER/docker/Dockerfile" ]]; then
      dockerContextDirectory="$SCENARIO_FOLDER/docker"
    else
      dockerContextDirectory="$PWD/ci/tests/puppeteer/docker"
    fi
    printcyan "Building Docker image for scenario ${scenarioName} via $dockerContextDirectory"

    cp "$PWD/cas.${projectType}" "$dockerContextDirectory"
    cp $keystore "$dockerContextDirectory"

    javaVersion=($(cat $PWD/gradle.properties | grep "sourceCompatibility" | cut -d= -f2))
    buildArguments="--build-arg JAVA_VERSION=${javaVersion} "
    buildArguments+="--build-arg SCENARIO_FOLDER=${SCENARIO_FOLDER} "
    buildArguments+="--build-arg SCENARIO_PATH=${SCENARIO_PATH} "
    buildArguments+="--build-arg SCENARIO=${SCENARIO} "

    environmentVariables=$(jq -j '.environmentVariables // empty | join(";")' "$config")
    IFS=';' read -r -a variables <<<"$environmentVariables"
    for env in "${variables[@]}"; do
      buildArguments+="--build-arg \"$env\" "
    done
    #  ls -al $dockerContextDirectory
    #  echo $buildArguments

    docker build \
      "$buildArguments" \
      --file "$dockerContextDirectory/Dockerfile" \
      -t cas-"${scenarioName}":latest \
      "$dockerContextDirectory"
    RC=$?
    rm "$dockerContextDirectory/cas.${projectType}"
    rm "$dockerContextDirectory/thekeystore"
    if [ $RC -ne 0 ]; then
      printred "Unable to build CAS Docker image."
      exit 2
    fi
    docker images --filter "reference=cas-${scenarioName}"
    printgreen "Built Docker image cas-${scenarioName}"
  fi

  if [[ "${RERUN}" != "true" && ("${NATIVE_BUILD}" == "false" || "${NATIVE_RUN}" == "true") ]]; then
    environmentVariables=$(jq -j '.environmentVariables // empty | join(";")' "$config")
    IFS=';' read -r -a variables <<<"$environmentVariables"
    for env in "${variables[@]}"; do
      cmd="export \"$env\""
      if [[ "${CI}" != "true" ]]; then
        echo "$cmd"
      fi
      eval "$cmd"
    done

    systemProperties=$(jq -r 'if (.systemProperties // []) | length == 0 then "" else .systemProperties[] | "-D" + . end' "${config}" | paste -sd' ' -)
    
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

      if [[ "${NATIVE_RUN}" == "true" ]]; then
        printcyan "Waiting for bootstrap script to complete before CAS native image runs"
        if [[ "$CI" == "true" ]]; then
          sleep 25
        else
          sleep 10
        fi
      fi
    fi

    serverPort=8443
    processIds=()
    instances=$(jq -j '.instances // 1' "${config}")
    if [[ ! -z "$instances" ]]; then
      printcyan "Found instances: ${instances}"
    fi
    for ((c = 1; c <= instances; c++)); do
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

      if [[ "${NATIVE_RUN}" == "true" && -n "${initScript}" ]]; then
        printcyan "Waiting for initialization scripts to complete before CAS native image runs"
        if [[ "$CI" == "true" ]]; then
          sleep 25
        else
          sleep 10
        fi
      fi

      if [[ "${INITONLY}" == "false" ]]; then
        runArgs=$(jq -j '.jvmArgs // empty' "${config}")
        runArgs="${runArgs//\$\{PWD\}/${PWD}}"
        runArgs="${runArgs} -Xms512m -Xmx2048m -Xss128m -server"
        runArgs="${runArgs} --add-modules java.se --add-exports java.base/jdk.internal.ref=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED"
        runArgs="${runArgs} --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.management/sun.management=ALL-UNNAMED"
        runArgs="${runArgs} --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED"
        [ -n "${runArgs}" ] && echo -e "JVM runtime arguments: [${runArgs}]"

        properties=$(jq -j '.properties // empty | join(" ")' "${config}")

        filter=".instance$c.properties // empty | join(\" \")"
        echo "$filter" >$TMPDIR/filter.jq
        properties="$properties $(cat $config | jq -j -f $TMPDIR/filter.jq)"
        rm "$TMPDIR"/filter.jq
        properties="${properties//\$\{PWD\}/${PORTABLE_PWD}}"
        properties="${properties//\$\{SCENARIO\}/${scenarioName}}"
        properties="${properties//\%\{random\}/${random}}"
        properties="${properties//\$\{TMPDIR\}/${PORTABLE_TMPDIR}}"

        if [[ -n "${currentVariationProperties}" ]]; then
          properties="${properties} ${currentVariationProperties}"
          printcyan "Variation properties to include: ${currentVariationProperties}"
        fi
        
        if [[ "$DEBUG" == "true" ]]; then
          printgreen "Remote debugging is enabled on port $DEBUG_PORT"
          runArgs="${runArgs} -Xrunjdwp:transport=dt_socket,address=$DEBUG_PORT,server=y,suspend=$DEBUG_SUSPEND"
        fi
        runArgs="${runArgs} -XX:TieredStopAtLevel=1 "
        printcyan "Launching CAS instance #${c} with properties [${properties}], system properties [${systemProperties}], run arguments [${runArgs}] and dependencies [${dependencies}]"

        springAppJson=$(jq -j '.SPRING_APPLICATION_JSON // empty' "${config}")
        [ -n "${springAppJson}" ] && export SPRING_APPLICATION_JSON=${springAppJson}

        printcyan "Cleaning leftover artifacts from previous runs..."
        rm -rf "$TMPDIR/keystore.jwks"
        rm -rf "$TMPDIR/cas"

        if [[ "${NATIVE_RUN}" == "true" ]]; then
          printcyan "Launching CAS instance #${c} under port ${serverPort} from ${targetArtifact}"
          ${targetArtifact} -Dcom.sun.net.ssl.checkRevocation=false \
            -Dlog.console.stacktraces=true \
            -DaotSpringActiveProfiles=none \
            "$systemProperties" \
            --spring.main.lazy-initialization=false \
            --spring.devtools.restart.enabled=false \
            --management.endpoints.web.discovery.enabled=true \
            --server.port=${serverPort} \
            --spring.profiles.active=none \
            --server.ssl.key-store="$keystore" ${properties} &
        elif [[ "${buildDockerImage}" == "true" ]]; then
          printcyan "Launching docker container cas-${scenarioName}:latest"
          docker run -d --rm \
            --name="cas-${scenarioName}" \
            -e SPRING_APPLICATION_JSON=${springAppJson} \
            -e SERVER_PORT=${serverPort} \
            -e RUN_ARGS="${runArgs}" \
            -e CAS_PROPERTIES="${properties}" \
            -p ${serverPort}:${serverPort} \
            -p 5005:5005 \
            -p 8080:8080 \
            cas-${scenarioName}:latest
          docker logs -f cas-${scenarioName} 2>/dev/null &
        else
          casArtifactToRun="$PWD/cas.${projectType}"
          if [[ "${cdsEnabled}" == "true" ]]; then
            printgreen "The scenario ${scenarioName} will run with CDS"
            rm -rf ${PWD}/cas 2>/dev/null
            printcyan "Extracting CAS to ${PWD}/cas"
            java -Djarmode=tools -jar "$PWD"/cas.${projectType} extract >/dev/null 2>&1
            printcyan "Launching CAS from ${PWD}/cas/cas.${projectType} to perform a training run"
            java -XX:ArchiveClassesAtExit=${PWD}/cas/cas.jsa -Dspring.context.exit=onRefresh -jar ${PWD}/cas/cas.${projectType} >/dev/null 2>&1
            printcyan "Generated archive cache file ${PWD}/cas/cas.jsa"
            runArgs="${runArgs} -XX:SharedArchiveFile=${PWD}/cas/cas.jsa"
            casArtifactToRun="${PWD}/cas/cas.${projectType}"
          else
            printcyan "The scenario ${scenarioName} will run without CDS"
          fi

          printcyan "Launching CAS instance #${c} under port ${serverPort} from ${casArtifactToRun}"
          java ${runArgs} \
            -Dlog.console.stacktraces=true \
            "$systemProperties" \
            -jar "${casArtifactToRun}" \
            -Dcom.sun.net.ssl.checkRevocation=false \
            --server.port=${serverPort} \
            --spring.main.lazy-initialization=false \
            --spring.profiles.active=none \
            --spring.devtools.restart.enabled=false \
            --management.endpoints.web.discovery.enabled=true \
            --server.ssl.key-store="$keystore" \
            --cas.audit.engine.enabled=true \
            --cas.audit.slf4j.use-single-line=true \
            ${properties} &
        fi
        pid=$!
        printcyan "Waiting for CAS instance #${c} under process id ${pid}"
        casLogin="https://localhost:${serverPort}/cas/login"

        healthCheckUrls=$(jq -r '.healthcheck?.urls[]?' "${config}" 2>/dev/null)
        if [[ -n "$healthCheckUrls" ]]; then
          url_array=()
          while IFS= read -r url; do
            url_array+=("$url")
          done <<< "$healthCheckUrls"
          
          for url in "${url_array[@]}"; do
            printcyan "Checking healthcheck url: $url"
            until curl -I -k --connect-timeout 10 --output /dev/null --silent --fail "$url"; do
              echo -n '.'
              sleep 2
            done
          done
        else
          printcyan "No healthcheck urls found"
          until curl -I -k --connect-timeout 10 --output /dev/null --silent --fail $casLogin; do
            echo -n '.'
            sleep 2
          done
        fi
        printcyan "CAS server ${casLogin} is up and running under process id ${pid}"

        processIds+=($pid)
        serverPort=$((serverPort + 1))
        if [[ "$DEBUG" == "true" ]]; then
          DEBUG_PORT=$((DEBUG_PORT + 1))
        fi
      fi
    done

    printgreen "Ready!"
    if [[ "${INITONLY}" == "false" ]]; then
      readyScript=$(jq -j '.readyScript // empty' <"${config}")
      readyScript="${readyScript//\$\{PWD\}/${PWD}}"
      readyScript="${readyScript//\$\{SCENARIO\}/${scenarioName}}"
      scripts=$(echo "$readyScript" | tr ',' '\n')

      for script in ${scripts}; do
        printgreen "Running ready script: ${script}"
        chmod +x "${script}"
        eval "${script}"
      done
    fi
  fi

  RC=-1
  if [[ "${NATIVE_BUILD}" == "true" ]]; then
    RC=0
  fi

  if [[ "${DRYRUN}" != "true" && ("${NATIVE_BUILD}" == "false" || "${NATIVE_RUN}" == "true") ]]; then

    export NODE_TLS_REJECT_UNAUTHORIZED=0

    if [[ "${NATIVE_RUN}" == "false" ]]; then

      max_retries=1
      if [[ "$CI" == "true" ]]; then
        max_retries=3
      fi

      retry_count=0
      while [ $retry_count -lt $max_retries ]; do
        echo -e "**************************************************************************"
        echo -e "Attempt: #${retry_count}: Running ${scriptPath}\n"
        node --unhandled-rejections=strict --no-experimental-websocket ${scriptPath} ${config}
        RC=$?

        if [[ $RC -ne 0 ]]; then
          printred "Script: ${scriptPath} with config: ${config} failed with return code ${RC}"
          ((retry_count++))
          sleepfor 3
        else
          break
        fi
      done
    else
      printyellow "Running test scenario against a CAS native-image executable is disabled for scenario ${scriptPath}"
      RC=0
    fi
    echo -e "**************************************************************************\n"

    exitScript=$(jq -j '.exitScript // empty' "${config}")
    exitScript="${exitScript//\$\{PWD\}/${PWD}}"
    exitScript="${exitScript//\$\{SCENARIO\}/${scenarioName}}"

    [ -n "${exitScript}" ] &&
      printcyan "Exit script: ${exitScript}" &&
      chmod +x "${exitScript}" &&
      eval "${exitScript}"

    if [[ $RC -ne 0 ]]; then
      printred "Test scenario [${scenarioName}] has failed with exit code ${RC}.\n"
    else
      printgreen "Test scenario [${scenarioName}] has passed successfully.\n"
    fi
  fi

  if [[ "${RERUN}" != "true" ]]; then
    if [[ "${INITONLY}" == "true" ]]; then
      printyellow "Test scenario is running in initialization-only mode."
      printyellow "This allows for the bootstrapping and initialization of the test scenario without actually running the test suite"
    fi

    if [[ "${CI}" != "true" && "${QUIT_QUIETLY}" == "false" ]]; then
      printgreen "Hit Enter to clean up scenario ${scenario}\n"
      read -r
    fi

    for p in "${processIds[@]}"; do
      printgreen "Killing CAS process ${p}..."
      kill -9 "$p"
    done

    printgreen "Removing previous build artifacts..."
    rm -Rf "${PWD}"/cas >/dev/null 2>&1
    rm -f "$PWD"/cas.${projectType} >/dev/null 2>&1
    rm -f "${public_cert}" >/dev/null 2>&1
    rm -Rf "${PUPPETEER_DIR}/overlay" >/dev/null 2>&1

    if [[ "${CI}" == "true" && $dockerInstalled -eq 0 ]]; then
      printgreen "Stopping Docker containers..."
      docker stop $(docker container ls -aq) >/dev/null 2>&1 || true
      docker rm $(docker container ls -aq) >/dev/null 2>&1 || true
    fi
    if [[ "${buildDockerImage}" == "true" ]]; then
      printgreen "Stopping CAS Docker container.."
      docker stop cas-${scenarioName} >/dev/null 2>&1
      docker rm cas-${scenarioName} >/dev/null 2>&1
    fi
  fi
}

function weAreDone() {
  printgreen "Bye!\n"
  exit $RC
}

fetchCasVersion
parseArguments "$@"
validateScenario
prepareScenario

variationPropsArray=$(jq -c -r '.variations // [] | .[].properties // empty' "${config}")
if [[ -z "$variationPropsArray" ]]; then
    printcyan "No variations are defined for test scenario ${scenarioName} in ${config}"
    buildAndRun
else
  variationsArray=()
  while IFS= read -r line; do
      variationsArray+=("$line")
  done <<< "$variationPropsArray"

  for index in "${!variationsArray[@]}"; do
      element=${variationsArray[index]}
      currentVariationProperties=$(echo "${element}" | jq -j -r -c '. // empty | join(" ")')
      printcyan "Running test scenario ${scenarioName}, variation: ${index}"
      buildAndRun
      echo "================================================================"
  done
fi

weAreDone
