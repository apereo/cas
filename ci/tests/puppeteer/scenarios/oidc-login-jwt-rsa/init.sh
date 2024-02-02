#!/bin/bash
set -e
if [[ -z $TMPDIR ]]; then
  TMPDIR=${TEMP:-/tmp}
fi
echo "Removing previous keystore at ${TMPDIR}/keystore.jwks"
rm -f "${TMPDIR}/keystore.jwks"

${PWD}/ci/tests/httpbin/run-httpbin-server.sh
