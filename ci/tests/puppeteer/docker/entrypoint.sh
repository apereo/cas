#!/bin/bash

function printgreen() {
  GREEN="\e[32m"
  ENDCOLOR="\e[0m"
  printf "✅ ${GREEN}$1${ENDCOLOR}\n"
}

java -version
echo "================================"
echo -e "JVM runtime arguments:\n${RUN_ARGS}"
echo "================================"
echo -e "CAS properties:\n${CAS_PROPERTIES}"
echo "================================"
printgreen "Launching CAS server in Docker container..."
echo "================================"
java ${RUN_ARGS} \
  -Dlog.console.stacktraces=true \
  -Dcom.sun.net.ssl.checkRevocation=false \
  -jar cas.war \
  --server.port=${SERVER_PORT} \
  --spring.profiles.active=none \
  --server.ssl.key-store="/etc/cas/thekeystore" \
  ${CAS_PROPERTIES}

