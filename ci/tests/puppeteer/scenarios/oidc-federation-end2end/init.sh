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

prepare_truststore() {
  if [[ ! -f "${CAS_CERT_FILE}" ]]; then
    echo "Unable to find CAS certificate at ${CAS_CERT_FILE}"
    echo "Expected certificate from Puppeteer keystore generation is missing."
    return 1
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

image_platform() {
  docker image inspect "${PAC4J_DEMO_IMAGE}" --format '{{.Os}}/{{.Architecture}}' 2>/dev/null
}

host_platform() {
  docker version --format '{{.Server.Os}}/{{.Server.Arch}}' 2>/dev/null
}

report_platform_mismatch() {
  local imagePlatform hostPlatform
  imagePlatform=$(image_platform)
  hostPlatform=$(host_platform)
  if [[ -n "${imagePlatform}" && -n "${hostPlatform}" && "${imagePlatform}" != "${hostPlatform}" ]]; then
    echo "Image ${PAC4J_DEMO_IMAGE} is built for [${imagePlatform}] while the Docker host is [${hostPlatform}]."
    echo "Publish the image for both platforms, i.e."
    echo "  docker buildx build --platform linux/amd64,linux/arm64 -t ${PAC4J_DEMO_IMAGE} --push ."
    return 0
  fi
  return 1
}

dump_container_diagnostics() {
  local exitCode
  exitCode=$(docker inspect -f '{{.State.ExitCode}}' "${CONTAINER_NAME}" 2>/dev/null)
  echo "pac4j demo container ${CONTAINER_NAME} exited with code [${exitCode:-unknown}]"
  docker logs --tail 200 "${CONTAINER_NAME}" >"${LOG_FILE}" 2>&1 || true
  if [[ -s "${LOG_FILE}" ]]; then
    cat "${LOG_FILE}"
  else
    echo "No container logs are available."
  fi
  report_platform_mismatch
}

start_pac4j_demo() {
  local counter=0
  local maxAttempts=150

  if docker inspect -f '{{.State.Running}}' "${CONTAINER_NAME}" 2>/dev/null | grep -q true; then
    if curl --output /dev/null --silent --fail http://localhost:8080; then
      echo "pac4j demo already running in container ${CONTAINER_NAME} on port 8080"
      echo "RP pre-check done. CAS OP/TA startup is handled by Puppeteer scenario instances."
      return 0
    fi
    echo "Removing unresponsive pac4j demo container ${CONTAINER_NAME}"
  fi
  docker rm -f "${CONTAINER_NAME}" >/dev/null 2>&1 || true

  prepare_truststore || return 1

  if ! docker info >/dev/null 2>&1; then
    echo "Docker engine is not running; the pac4j RP demo requires Docker."
    return 1
  fi

  if ! docker image inspect "${PAC4J_DEMO_IMAGE}" >/dev/null 2>&1; then
    echo "Docker image ${PAC4J_DEMO_IMAGE} not found locally; pulling..."
    if ! docker pull "${PAC4J_DEMO_IMAGE}"; then
      echo "Unable to pull Docker image ${PAC4J_DEMO_IMAGE}."
      echo "Build it locally with: docker build -t ${PAC4J_DEMO_IMAGE} <simple-spring-boot-pac4j-demos repo>"
      return 1
    fi
  fi
  report_platform_mismatch

  echo "Starting pac4j demo container ${CONTAINER_NAME} from image ${PAC4J_DEMO_IMAGE} on port 8080 (host networking)"
  if ! docker run --quiet -d \
    --name "${CONTAINER_NAME}" \
    --network=host \
    -v "${TRUSTSTORE_FILE}":/etc/cas/cas-truststore.p12:ro \
    -e JAVA_OPTS="-Djavax.net.ssl.trustStore=/etc/cas/cas-truststore.p12 -Djavax.net.ssl.trustStorePassword=${TRUSTSTORE_PASSWORD} -Djavax.net.ssl.trustStoreType=PKCS12" \
    "${PAC4J_DEMO_IMAGE}" \
    --server.port=8080 \
    --app.base-url=http://localhost:8080; then
    echo "Failed to start pac4j demo container ${CONTAINER_NAME}"
    report_platform_mismatch
    return 1
  fi

  echo "pac4j demo container started; waiting for port 8080..."
  until curl --output /dev/null --silent --fail http://localhost:8080; do
    counter=$((counter + 1))
    if (( counter % 5 == 0 )); then
      echo "Still waiting for pac4j demo ($((counter * 2))s elapsed)..."
    fi
    if ! docker inspect -f '{{.State.Running}}' "${CONTAINER_NAME}" 2>/dev/null | grep -q true; then
      echo "pac4j demo container ${CONTAINER_NAME} terminated before opening port 8080"
      dump_container_diagnostics
      return 1
    fi
    if [[ ${counter} -gt ${maxAttempts} ]]; then
      echo "Timed out waiting for pac4j demo on port 8080"
      dump_container_diagnostics
      return 1
    fi
    sleep 2
  done
  echo "pac4j demo started on http://localhost:8080"
  docker logs "${CONTAINER_NAME}" >"${LOG_FILE}" 2>&1 || true
}

start_pac4j_demo
initExitCode=$?
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
  exit "${initExitCode}"
fi
return "${initExitCode}"
