#!/bin/bash
echo "Copying the Google Authenticator accounts to ${TMPDIR}/${SCENARIO}-accounts.json"
cp -f "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/accounts.json" "${TMPDIR}/${SCENARIO}-accounts.json"
"${PWD}/ci/tests/mail/run-mail-server.sh"
