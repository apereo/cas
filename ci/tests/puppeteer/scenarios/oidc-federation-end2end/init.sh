#!/bin/bash

tmp="${TMPDIR}"
if [[ -z "${tmp}" ]]; then
  tmp="/tmp"
fi
export TMPDIR="${tmp}"
SCENARIO_DIR="${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}"
TEMP_DIR="${SCENARIO_DIR}/temp"
CONTAINER_NAME="pac4j-oidc-federation-rp"
LOG_FILE="${TEMP_DIR}/pac4j-demo.log"
PAC4J_DEMO_IMAGE="${PAC4J_DEMO_IMAGE:-leleuj/simple-spring-boot-pac4j-demos:cicas803}"
CAS_CERT_FILE="${CAS_CERT:-${PWD}/ci/tests/puppeteer/overlay/server.crt}"
TRUSTSTORE_FILE="${TEMP_DIR}/pac4j-demo-truststore.p12"
TRUSTSTORE_PASSWORD="changeit"

mkdir -p "${TEMP_DIR}"
finish() {
  local rc="${1:-0}"
  if [[ "${BASH_SOURCE[0]}" != "${0}" ]]; then
    return "${rc}"
  fi
  exit "${rc}"
}
is_port_listened() {
  lsof -tiTCP:8080 -sTCP:LISTEN >/dev/null 2>&1
}

prepare_truststore() {
  if [[ ! -f "${CAS_CERT_FILE}" ]]; then
    echo "Unable to find CAS certificate at ${CAS_CERT_FILE}"
    echo "Expected certificate from Puppeteer keystore generation is missing."
    finish 1
  fi

  if ! keytool -list -alias cas -keystore "${TRUSTSTORE_FILE}" -storepass "${TRUSTSTORE_PASSWORD}" -storetype PKCS12 >/dev/null 2>&1; then
    echo "Creating truststore ${TRUSTSTORE_FILE} from ${CAS_CERT_FILE}"
    keytool -importcert -noprompt -trustcacerts \
      -alias cas \
      -file "${CAS_CERT_FILE}" \
      -keystore "${TRUSTSTORE_FILE}" \
      -storepass "${TRUSTSTORE_PASSWORD}" \
      -storetype PKCS12 >/dev/null 2>&1
  else
    echo "Reusing truststore ${TRUSTSTORE_FILE} (alias: cas)"
  fi
}

if docker inspect -f '{{.State.Running}}' "${CONTAINER_NAME}" 2>/dev/null | grep -q true; then
  if curl --output /dev/null --silent --fail http://localhost:8080; then
    echo "pac4j demo already running in container ${CONTAINER_NAME} on port 8080"
    echo "RP pre-check done. CAS OP/TA startup is handled by Puppeteer scenario instances."
    finish 0
  fi
  echo "Removing unresponsive pac4j demo container ${CONTAINER_NAME}"
  docker rm -f "${CONTAINER_NAME}" >/dev/null 2>&1 || true
fi

if is_port_listened; then
  if curl --output /dev/null --silent --fail http://localhost:8080; then
    echo "A service is already responding on http://localhost:8080; reusing it for RP checks."
    echo "RP pre-check done. CAS OP/TA startup is handled by Puppeteer scenario instances."
    finish 0
  fi
  listener_pids=$(lsof -tiTCP:8080 -sTCP:LISTEN 2>/dev/null | tr '\n' ' ' | sed 's/[[:space:]]*$//')
  echo "Port 8080 is already in use by process(es): ${listener_pids}"
  echo "Please stop the process on 8080 (e.g. nginx) and rerun the scenario."
  finish 1
fi
prepare_truststore

if ! docker info >/dev/null 2>&1; then
  echo "Docker engine is not running; the pac4j RP demo requires Docker."
  finish 1
fi

if ! docker image inspect "${PAC4J_DEMO_IMAGE}" >/dev/null 2>&1; then
  echo "Docker image ${PAC4J_DEMO_IMAGE} not found locally; pulling..."
  if ! docker pull "${PAC4J_DEMO_IMAGE}"; then
    echo "Unable to pull Docker image ${PAC4J_DEMO_IMAGE}."
    echo "Build it locally with: docker build -t ${PAC4J_DEMO_IMAGE} <simple-spring-boot-pac4j-demos repo>"
    finish 1
  fi
fi

echo "Starting pac4j demo container ${CONTAINER_NAME} from image ${PAC4J_DEMO_IMAGE} on port 8080 (host networking)"
docker run --quiet -d --rm \
  --name "${CONTAINER_NAME}" \
  --network=host \
  -v "${TRUSTSTORE_FILE}":/etc/cas/cas-truststore.p12:ro \
  -e JAVA_OPTS="-Djavax.net.ssl.trustStore=/etc/cas/cas-truststore.p12 -Djavax.net.ssl.trustStorePassword=${TRUSTSTORE_PASSWORD} -Djavax.net.ssl.trustStoreType=PKCS12" \
  "${PAC4J_DEMO_IMAGE}" \
  --server.port=8080 \
  --app.base-url=http://localhost:8080
if [[ $? -ne 0 ]]; then
  echo "Failed to start pac4j demo container ${CONTAINER_NAME}"
  finish 1
fi

: > "${LOG_FILE}"
docker logs -f "${CONTAINER_NAME}" >>"${LOG_FILE}" 2>&1 &
echo "pac4j demo container started; waiting for port 8080..."

counter=0
max_attempts=300
until curl --output /dev/null --silent --fail http://localhost:8080; do
  counter=$((counter + 1))
  if (( counter % 5 == 0 )); then
    elapsed=$((counter * 2))
    echo "Still waiting for pac4j demo (${elapsed}s elapsed)..."
  fi
  if ! docker inspect -f '{{.State.Running}}' "${CONTAINER_NAME}" 2>/dev/null | grep -q true; then
    echo "pac4j demo container ${CONTAINER_NAME} terminated before opening port 8080"
    tail -n 200 "${LOG_FILE}" || true
    finish 1
  fi
  if [[ ${counter} -gt ${max_attempts} ]]; then
    echo "Timed out waiting for pac4j demo on port 8080"
    tail -n 200 "${LOG_FILE}" || true
    finish 1
  fi
  sleep 2
done
echo "pac4j demo started on http://localhost:8080"
