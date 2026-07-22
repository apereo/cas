#!/bin/bash

SCENARIO_DIR="${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}"
TEMP_DIR="${SCENARIO_DIR}/temp"
CONTAINER_NAME="pac4j-oidc-federation-rp"
is_sourced=false
if [[ "${BASH_SOURCE[0]}" != "${0}" ]]; then
  is_sourced=true
fi

finish() {
  local rc="${1:-0}"
  if [[ "${is_sourced}" == "true" ]]; then
    return "${rc}"
  fi
  exit "${rc}"
}

if docker inspect "${CONTAINER_NAME}" >/dev/null 2>&1; then
  echo "Stopping pac4j demo container ${CONTAINER_NAME}"
  docker stop "${CONTAINER_NAME}" >/dev/null 2>&1 || true
  docker rm -f "${CONTAINER_NAME}" >/dev/null 2>&1 || true
fi

for listener in $(lsof -tiTCP:8080 -sTCP:LISTEN 2>/dev/null); do
  cmd=$(ps -p "${listener}" -o command= 2>/dev/null || true)
  if [[ "${cmd}" == *"simple-spring-boot-pac4j-demos"* ]]; then
    echo "Stopping pac4j demo listener ${listener} on port 8080"
    kill "${listener}" >/dev/null 2>&1 || true
  fi
done

rm -Rf "${TEMP_DIR}"

finish 0
