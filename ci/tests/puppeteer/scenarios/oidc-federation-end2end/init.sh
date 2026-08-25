#!/bin/bash

export TMPDIR="${TMPDIR:-/tmp}"
TEMP_DIR="${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/temp"
CONTAINER_NAME="pac4j-oidc-federation-rp"
LOG_FILE="${TEMP_DIR}/pac4j-demo.log"
PAC4J_DEMO_IMAGE="${PAC4J_DEMO_IMAGE:-leleuj/simple-spring-boot-pac4j-demos:cicas803}"
CAS_CERT_FILE="${CAS_CERT:-${PWD}/ci/tests/puppeteer/overlay/server.crt}"
TRUSTSTORE_FILE="${TEMP_DIR}/pac4j-demo-truststore.p12"
TRUSTSTORE_PASSWORD="changeit"

mkdir -p "${TEMP_DIR}"

fail_with_diagnostics() {
  echo "$1"
  docker inspect -f 'State: {{.State.Status}}, exit code: {{.State.ExitCode}}, error: {{.State.Error}}' "${CONTAINER_NAME}" 2>&1
  echo "Image platform: $(docker image inspect "${PAC4J_DEMO_IMAGE}" --format '{{.Os}}/{{.Architecture}}' 2>&1), host platform: $(docker version --format '{{.Server.Os}}/{{.Server.Arch}}' 2>&1)"
  docker logs --tail 200 "${CONTAINER_NAME}" 2>&1 | tee "${LOG_FILE}"
  return 1
}

start_pac4j_demo() {
  if [[ ! -f "${TRUSTSTORE_FILE}" || "${CAS_CERT_FILE}" -nt "${TRUSTSTORE_FILE}" ]]; then
    echo "Creating truststore ${TRUSTSTORE_FILE} from ${CAS_CERT_FILE}"
    rm -f "${TRUSTSTORE_FILE}"
    keytool -importcert -noprompt -trustcacerts -alias cas -file "${CAS_CERT_FILE}" \
      -keystore "${TRUSTSTORE_FILE}" -storepass "${TRUSTSTORE_PASSWORD}" -storetype PKCS12 || return 1
  else
    echo "Reusing truststore ${TRUSTSTORE_FILE}"
  fi

  if curl --output /dev/null --silent --fail http://localhost:8080; then
    echo "pac4j demo already running on port 8080"
    return 0
  fi
  docker rm -f "${CONTAINER_NAME}" >/dev/null 2>&1

  echo "Starting pac4j demo container ${CONTAINER_NAME} from image ${PAC4J_DEMO_IMAGE} on port 8080 (host networking)"
  docker run --quiet -d --name "${CONTAINER_NAME}" --network=host \
    -v "${TRUSTSTORE_FILE}":/etc/cas/cas-truststore.p12:ro \
    -e JAVA_OPTS="-Djavax.net.ssl.trustStore=/etc/cas/cas-truststore.p12 -Djavax.net.ssl.trustStorePassword=${TRUSTSTORE_PASSWORD} -Djavax.net.ssl.trustStoreType=PKCS12" \
    "${PAC4J_DEMO_IMAGE}" --server.port=8080 --app.base-url=http://localhost:8080 || return 1

  for ((counter = 1; counter <= 150; counter++)); do
    if curl --output /dev/null --silent --fail http://localhost:8080; then
      echo "pac4j demo started on http://localhost:8080"
      return 0
    fi
    if ! docker inspect -f '{{.State.Running}}' "${CONTAINER_NAME}" 2>/dev/null | grep -q true; then
      fail_with_diagnostics "pac4j demo container ${CONTAINER_NAME} terminated before opening port 8080"
      return 1
    fi
    if (( counter % 5 == 0 )); then
      echo "Still waiting for pac4j demo ($((counter * 2))s elapsed)..."
    fi
    sleep 2
  done
  fail_with_diagnostics "Timed out waiting for pac4j demo on port 8080"
}

start_pac4j_demo
initExitCode=$?
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
  exit "${initExitCode}"
fi
return "${initExitCode}"
