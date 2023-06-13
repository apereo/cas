#!/bin/bash

BUILD_OPTIONS="-x check -x test -x javadoc --configure-on-demand --max-workers=8 --no-configuration-cache -DskipNestedConfigMetadataGen=true"

RED="\e[31m"
GREEN="\e[32m"
YELLOW="\e[33m"
ENDCOLOR="\e[0m"

BUILD="true"
RUN="true"

while (( "$#" )); do
  case "$1" in
  --build)
    BUILD="$2"
    shift 2
    ;;
  --run)
      RUN="$2"
      shift 2
      ;;
  esac
done

function printred() {
  printf "${RED}$1${ENDCOLOR}\n"
}
function printgreen() {
  printf "${GREEN}$1${ENDCOLOR}\n"
}
function printyellow() {
  printf "${YELLOW}$1${ENDCOLOR}\n"
}

if [[ "${BUILD}" == "true" ]]; then
  if [[ "${CI}" == "true" ]]; then
    BUILD_OPTIONS="${BUILD_OPTIONS} --no-daemon"
  fi
  printgreen "Building CAS native image with options..."
  export GRAALVM_BUILDTOOLS_MAX_PARALLEL_BUILDS=8
  tasks="./gradlew :webapp:cas-server-webapp-native:build :webapp:cas-server-webapp-native:nativeCompile ${BUILD_OPTIONS}"
  echo "$tasks"
  echo -e "***************************************************************************************"
  eval "$tasks"

  if [[ $? -ne 0 ]]; then
    printred "CAS native image build failed"
    exit 1
  fi
fi

printgreen "CAS native image build is successfully built"
cd ./webapp/cas-server-webapp-native || exit
ls -al ./build/native

if [[ "${RUN}" == "true" ]]; then
  keystore="/etc/cas/thekeystore"
  if [[ -f "${keystore}" ]]; then
    printyellow "Keystore ${keystore} already exists and will not be created again!"
  else
    dname="${dname:-CN=mmoayyed.unicon.net,OU=Example,OU=Org,C=US}"
    subjectAltName="${subjectAltName:-dns:unicon.net,dns:localhost,ip:127.0.0.1}"
    sudo mkdir -p /etc/cas
    printgreen "Generating keystore ${keystore} for CAS with DN=${dname}, SAN=${subjectAltName}"
    [ -f "${keystore}" ] && sudo rm "${keystore}"
    sudo keytool -genkey -noprompt -alias cas -keyalg RSA -keypass changeit -storepass changeit \
      -keystore "${keystore}" -dname "${dname}" -ext SAN="${subjectAltName}"
    if [[ $? -ne 0 ]]; then
      printred "Unable to create CAS keystore ${keystore}"
      exit 1
    fi
  fi

  printgreen "Launching CAS native image..."
  ./build/native/nativeCompile/cas --spring.profiles.active=native &
  pid=$!
  sleep 20
  
  if [[ "${CI}" == "true" ]]; then
    curl -k -L --connect-timeout 10 --output /dev/null --silent --fail https://localhost:8443/cas/login
    if [[ $? -ne 0 ]]; then
      printred "CAS native image failed to launch"
      kill -9 $pid
      exit 1
    fi
  else
    # We cannot do this in Github Actions/CI; curl seems to hang indefinitely
    until curl -k -L --connect-timeout 10 --output /dev/null --silent --fail https://localhost:8443/cas/login; do
       echo -n '.'
       sleep 1
    done
  fi
  kill -9 $pid
  [ "$CI" = "true" ] && pkill cas
fi
printgreen "Built and validated CAS native image successfully."
exit 0
