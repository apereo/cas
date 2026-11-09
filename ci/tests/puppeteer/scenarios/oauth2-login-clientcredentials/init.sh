#!/bin/bash
# CAS removes expired client secrets when it loads services and saves the definition back,
# so the scenario runs against a copy of its service definitions.
SERVICES_DIR="${TMPDIR}/${SCENARIO}-services"
echo "Copying service definitions to ${SERVICES_DIR}"
rm -rf "${SERVICES_DIR}"
mkdir -p "${SERVICES_DIR}"
cp -f "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/services/"*.json "${SERVICES_DIR}"
