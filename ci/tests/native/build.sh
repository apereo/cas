#!/bin/bash

RED="\e[31m"
GREEN="\e[32m"
YELLOW="\e[33m"
ENDCOLOR="\e[0m"

function printred() {
  printf "${RED}$1${ENDCOLOR}\n"
}
function printgreen() {
  printf "${GREEN}$1${ENDCOLOR}\n"
}
function printyellow() {
  printf "${YELLOW}$1${ENDCOLOR}\n"
}

printgreen "Building CAS Native Image. This may take several minutes..."
./gradlew :webapp:cas-server-webapp-native:build :webapp:cas-server-webapp-native:nativeCompile \
  --no-daemon -x check -x test -x javadoc --configure-on-demand \
  --max-workers=8 --no-configuration-cache -DskipNestedConfigMetadataGen=true

if [[ $? -ne 0 ]]; then
  printred "CAS native image build failed"
  exit 1
fi

printgreen "CAS native image build is successfully built"
cd ./webapp/cas-server-webapp-native || exit
ls -al ./build/native

keystore="/etc/cas/thekeystore"
if [[ -f "${keystore}" ]]; then
  printyellow "Keystore ${keystore} already exists and will not be created again"
else
  dname="${dname:-CN=cas.example.org,OU=Example,OU=Org,C=US}"
  subjectAltName="${subjectAltName:-dns:example.org,dns:localhost,ip:127.0.0.1}"
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
sleep 15
curl -k -L --connect-timeout 10 --output /dev/null --silent --fail https://localhost:8443/cas/login
if [[ $? -ne 0 ]]; then
  printred "CAS native image failed to launch"
  exit 1
fi
kill -9 $pid
[ "$CI" = "true" ] && pkill java
printgreen "Built and validated CAS native image successfully."
exit 0
