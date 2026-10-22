#!/bin/bash

if [[ -f "${TMPDIR:-/tmp}/cas-rest-config-server.pid" ]]; then
  echo "Stopping REST configuration backend"
  kill "$(cat "${TMPDIR:-/tmp}/cas-rest-config-server.pid")" 2>/dev/null || true
  rm -f "${TMPDIR:-/tmp}/cas-rest-config-server.pid"
fi
cat "${TMPDIR:-/tmp}/cas-rest-config-server.log" 2>/dev/null || true
