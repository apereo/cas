#!/bin/bash

"${PWD}"/ci/tests/httpbin/run-httpbin-server.sh

SCENARIO_DIRECTORY="${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}"
if [[ -f "${TMPDIR:-/tmp}/cas-rest-config-server.pid" ]]; then
  kill "$(cat "${TMPDIR:-/tmp}/cas-rest-config-server.pid")" 2>/dev/null || true
fi
echo "Starting REST configuration backend"
nohup node "${SCENARIO_DIRECTORY}/config-server.js" > "${TMPDIR:-/tmp}/cas-rest-config-server.log" 2>&1 &
echo $! > "${TMPDIR:-/tmp}/cas-rest-config-server.pid"
for _ in $(seq 1 30); do
  if curl -fsS http://localhost:5618/config/names >/dev/null 2>&1; then
    echo "REST configuration backend is ready"
    break
  fi
  sleep 1
done
curl -fsS http://localhost:5618/config/names || exit 1
