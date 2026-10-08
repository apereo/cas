#!/bin/bash

function printgreen() {
  GREEN="\e[32m"
  ENDCOLOR="\e[0m"
  printf "✅ ${GREEN}$1${ENDCOLOR}\n"
}

if [[ "${CAS_NATIVE:-false}" != "true" ]]; then
  java -version
fi
echo "================================"
echo -e "JVM runtime arguments:\n${RUN_ARGS}"
echo "================================"
echo -e "CAS properties:\n${CAS_PROPERTIES}"
echo "================================"
printgreen "Launching CAS server in Docker container..."
echo "================================"
read -r -a runtimeArguments <<< "${RUN_ARGS//$'\n'/ }"
if [[ "${CAS_NATIVE:-false}" == "true" ]]; then
  nativeArguments=()
  for argument in "${runtimeArguments[@]}"; do
    if [[ "${argument}" == -D* ]]; then
      nativeArguments+=("${argument}")
    fi
  done
  casCommand=(./cas "${nativeArguments[@]}" -DTEST_TYPE=PUPPETEER -DVALIDATE_CONFIGURATION_ENABLED=false
    -Dlog.console.stacktraces=true -Dcom.sun.net.ssl.checkRevocation=false --spring.main.lazy-initialization=false)
else
  casCommand=(java "${runtimeArguments[@]}" -Dlog.console.stacktraces=true -Dcom.sun.net.ssl.checkRevocation=false -jar cas.war)
fi
exec "${casCommand[@]}" \
  --server.port=${SERVER_PORT} \
  --spring.profiles.active=none \
  --cas.audit.slf4j.use-single-line=true \
  --server.ssl.key-store="/etc/cas/thekeystore" \
  ${CAS_PROPERTIES}
