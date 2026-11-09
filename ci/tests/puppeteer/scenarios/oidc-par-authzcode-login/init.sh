#!/bin/bash

#
# Client attestation material, in the git-ignored overlay folder of the scenario:
#   attester-ca.pem   trust anchor that CAS is configured with (cas.authn.oidc.client-attestation.trust-anchors)
#   attester.pem/key  attester certificate issued by the trust anchor, which signs client attestations
#   rogue.pem/key     self-signed attester, which CAS must refuse
#
ATTESTATION_DIR="${PWD}/ci/tests/puppeteer/overlay/${SCENARIO}"
mkdir -p "${ATTESTATION_DIR}"
echo "Creating client attestation trust anchor and attester certificates in ${ATTESTATION_DIR}"

openssl ecparam -name prime256v1 -genkey -noout -out "${ATTESTATION_DIR}/attester-ca.key" \
  && openssl req -new -x509 -days 30 -key "${ATTESTATION_DIR}/attester-ca.key" \
    -subj "/CN=CAS Client Attestation CA" -out "${ATTESTATION_DIR}/attester-ca.pem" \
  && openssl ecparam -name prime256v1 -genkey -noout -out "${ATTESTATION_DIR}/attester.key" \
  && openssl req -new -key "${ATTESTATION_DIR}/attester.key" \
    -subj "/CN=CAS Client Attester" -out "${ATTESTATION_DIR}/attester.csr" \
  && openssl x509 -req -days 30 -in "${ATTESTATION_DIR}/attester.csr" -set_serial "0x$(openssl rand -hex 8)" \
    -CA "${ATTESTATION_DIR}/attester-ca.pem" -CAkey "${ATTESTATION_DIR}/attester-ca.key" \
    -out "${ATTESTATION_DIR}/attester.pem" \
  && openssl ecparam -name prime256v1 -genkey -noout -out "${ATTESTATION_DIR}/rogue.key" \
  && openssl req -new -x509 -days 30 -key "${ATTESTATION_DIR}/rogue.key" \
    -subj "/CN=Self-signed Client Attester" -out "${ATTESTATION_DIR}/rogue.pem"
if [[ $? -ne 0 || ! -s "${ATTESTATION_DIR}/attester.pem" || ! -s "${ATTESTATION_DIR}/rogue.pem" ]]; then
  echo "Unable to create client attestation certificates"
  exit 1
fi
openssl verify -CAfile "${ATTESTATION_DIR}/attester-ca.pem" "${ATTESTATION_DIR}/attester.pem"
