#!/bin/bash
# Signs the service provider metadata aggregate with a key made for this run (no xmlsec1 needed), and writes
# a copy changed after signing, an unsigned copy and a second certificate that did not sign anything.
METADATA_SIGNATURE_DIR="${PWD}/ci/tests/puppeteer/overlay/${SCENARIO}"
echo -e "Removing previous SAML metadata directory and generated metadata in ${METADATA_SIGNATURE_DIR}"
rm -Rf "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-md" "${METADATA_SIGNATURE_DIR}"
mkdir -p "${METADATA_SIGNATURE_DIR}"
for METADATA_KEY_NAME in signer other; do
  if ! openssl req -x509 -newkey rsa:2048 -nodes -sha256 -days 3650 -subj "/CN=metadata-${METADATA_KEY_NAME}" \
    -keyout "${METADATA_SIGNATURE_DIR}/${METADATA_KEY_NAME}.key" -out "${METADATA_SIGNATURE_DIR}/${METADATA_KEY_NAME}.crt" >/dev/null 2>&1; then
    echo "Unable to generate the ${METADATA_KEY_NAME} metadata signing key pair"
    exit 1
  fi
done
if ! node "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/sign-metadata.js" "${METADATA_SIGNATURE_DIR}"; then
  echo "Unable to sign the service provider metadata"
  exit 1
fi
