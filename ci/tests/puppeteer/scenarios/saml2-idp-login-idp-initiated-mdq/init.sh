#!/bin/bash

echo -e "Removing previous SAML metadata directory from ${SCENARIO_FOLDER}"
rm -Rf "${SCENARIO_FOLDER}/saml-md"
rm -Rf "${SCENARIO_FOLDER}/saml-sp"

if [[ -z $TMPDIR ]]; then
  TMPDIR=${TEMP:-/tmp}
fi
if [[ "${SCENARIO_VARIATION}" == "local" ]]; then
  # The script serves and signs the metadata itself: it trusts signer.crt, and other.crt signs metadata it must refuse.
  MDQ_SIGNATURE_DIR="${PWD}/ci/tests/puppeteer/overlay/${SCENARIO}"
  echo -e "Generating MDQ metadata signing key pairs in ${MDQ_SIGNATURE_DIR}"
  rm -Rf "${MDQ_SIGNATURE_DIR}"
  mkdir -p "${MDQ_SIGNATURE_DIR}" "${SCENARIO_FOLDER}/saml-md"
  for MDQ_KEY_NAME in signer other; do
    if ! openssl req -x509 -newkey rsa:2048 -nodes -sha256 -days 3650 -subj "/CN=mdq-${MDQ_KEY_NAME}" \
      -keyout "${MDQ_SIGNATURE_DIR}/${MDQ_KEY_NAME}.key" -out "${MDQ_SIGNATURE_DIR}/${MDQ_KEY_NAME}.crt" >/dev/null 2>&1; then
      echo "Unable to generate the ${MDQ_KEY_NAME} MDQ metadata signing key pair"
      exit 1
    fi
  done
  echo -e "MDQ metadata signing key pairs are ready"
else
  echo -e "Fetching InCommon MDQ signing certificate..."
  mkdir ${SCENARIO_FOLDER}/saml-md
  curl -k --retry 5 --retry-delay 2 --retry-connrefused --max-time 30 -o $TMPDIR/inc-md-cert-mdq.pem https://mdq.incommon.org/certs/inc-md-cert-mdq.pem
fi
