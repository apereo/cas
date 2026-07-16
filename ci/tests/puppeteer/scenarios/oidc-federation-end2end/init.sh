#!/bin/bash

tmp="${TMPDIR}"
if [[ -z "${tmp}" ]]; then
  tmp="/tmp"
fi
export TMPDIR="${tmp}"
SCENARIO_DIR="${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}"
TEMP_DIR="${SCENARIO_DIR}/temp"
REPO_URL="https://github.com/pac4j/simple-spring-boot-pac4j-demos.git"
REPO_DIR="${TMPDIR}/simple-spring-boot-pac4j-demos"
PID_FILE="${TEMP_DIR}/pac4j-demo.pid"
LOG_FILE="${TEMP_DIR}/pac4j-demo.log"
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

is_pid_listening_on_8080() {
  local target_pid="$1"
  lsof -tiTCP:8080 -sTCP:LISTEN 2>/dev/null | grep -Fxq "${target_pid}"
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

echo "Preparing pac4j demo from ${REPO_URL} (branch: cicas)"
if [[ -d "${REPO_DIR}/.git" ]]; then
  git -C "${REPO_DIR}" fetch origin cicas
  git -C "${REPO_DIR}" checkout cicas
  git -C "${REPO_DIR}" reset --hard origin/cicas
else
  rm -Rf "${REPO_DIR}"
  git clone --single-branch --branch cicas "${REPO_URL}" "${REPO_DIR}"
fi

if [[ -f "${PID_FILE}" ]]; then
  old_pid=$(cat "${PID_FILE}")
  if [[ -n "${old_pid}" ]] && kill -0 "${old_pid}" >/dev/null 2>&1; then
    if curl --output /dev/null --silent --fail http://localhost:8080; then
      echo "pac4j demo already running on port 8080 (pid: ${old_pid})"
      echo "RP pre-check done. CAS OP/TA startup is handled by Puppeteer scenario instances."
      finish 0
    fi
  fi
  rm -f "${PID_FILE}"
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
JVM_TRUSTSTORE_ARGS="-Djavax.net.ssl.trustStore=${TRUSTSTORE_FILE} -Djavax.net.ssl.trustStorePassword=${TRUSTSTORE_PASSWORD} -Djavax.net.ssl.trustStoreType=PKCS12"

echo "Starting pac4j demo on port 8080"
if [[ -x "${REPO_DIR}/mvnw" ]]; then
  nohup "${REPO_DIR}/mvnw" -q -DskipTests -f "${REPO_DIR}/pom.xml" spring-boot:run -Dspring-boot.run.arguments="--server.port=8080" -Dspring-boot.run.jvmArguments="${JVM_TRUSTSTORE_ARGS}" >"${LOG_FILE}" 2>&1 &
else
  echo "Unable to start pac4j demo: Maven Wrapper (./mvnw) is required in ${REPO_DIR}"
  finish 1
fi
pid=$!
echo "${pid}" >"${PID_FILE}"
echo "pac4j demo bootstrap started (pid: ${pid}); waiting for port 8080..."

counter=0
max_attempts=300
until curl --output /dev/null --silent --fail http://localhost:8080; do
  counter=$((counter + 1))
  if (( counter % 5 == 0 )); then
    elapsed=$((counter * 2))
    echo "Still waiting for pac4j demo (${elapsed}s elapsed)..."
  fi
  if ! kill -0 "${pid}" >/dev/null 2>&1; then
    echo "pac4j demo process ${pid} terminated before opening port 8080"
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
