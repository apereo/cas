#!/bin/bash

SCENARIO_DIR="${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}"
TEMP_DIR="${SCENARIO_DIR}/temp"
PID_FILE="${TEMP_DIR}/pac4j-demo.pid"
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

if [[ -f "${PID_FILE}" ]]; then
  pid=$(cat "${PID_FILE}")
  if [[ -n "${pid}" ]] && kill -0 "${pid}" >/dev/null 2>&1; then
    echo "Stopping pac4j demo process ${pid}"
    pkill -TERM -P "${pid}" >/dev/null 2>&1 || true
    kill "${pid}" >/dev/null 2>&1 || true
  fi
  rm -f "${PID_FILE}"
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
