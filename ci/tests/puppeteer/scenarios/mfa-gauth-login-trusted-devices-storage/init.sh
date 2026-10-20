#!/bin/bash
echo "Copying the Google Authenticator account to ${TMPDIR}/${SCENARIO}-accounts.json"
cp -f "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/accounts.json" "${TMPDIR}/${SCENARIO}-accounts.json"
