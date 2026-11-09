#!/bin/bash

# Builds the PKI of the scenario under overlay/${SCENARIO}: a CA (CasTestCA) with a valid and a revoked client
# certificate and a current CRL that lists the revoked one, a client certificate from CasTestUnlistedCA, for which no
# CRL exists, and a client certificate from CasTestExpiredCrlCA, whose CRL (expired-ca.crl, committed with its key in
# the scenario folder) expired on 2025-01-02. Every client certificate names a CRL distribution point on the express
# server the script starts (port 5615), for the variation that uses revocation-checker=CRL.
# This script is sourced by run.sh: it must not exit on success.

CRL_SCENARIO_DIR="${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}"
CRL_WORK_DIR="${PWD}/ci/tests/puppeteer/overlay/${SCENARIO}"
CRL_SERVER_URL="http://localhost:5615"

function crlFail() {
  echo "Unable to create the X.509 test PKI: $1"
  exit 1
}

function crlWriteCaConfig() {
  local caDir="$1"
  local caCert="$2"
  local caKey="$3"
  cat > "${caDir}/ca.cnf" <<CNF
[ ca ]
default_ca = CA_default
[ CA_default ]
dir = ${caDir}
database = \$dir/index.txt
crlnumber = \$dir/crlnumber
certificate = ${caCert}
private_key = ${caKey}
default_md = sha256
default_crl_days = 30
[ req ]
distinguished_name = req_dn
[ req_dn ]
[ v3_ca ]
basicConstraints = critical,CA:TRUE
keyUsage = critical,keyCertSign,cRLSign
subjectKeyIdentifier = hash
CNF
  touch "${caDir}/index.txt"
  echo 1000 > "${caDir}/crlnumber"
}

function crlCreateCa() {
  local name="$1"
  local caDir="${CRL_WORK_DIR}/${name}"
  mkdir -p "${caDir}"
  crlWriteCaConfig "${caDir}" "${caDir}/ca.pem" "${caDir}/ca.key"
  openssl req -x509 -config "${caDir}/ca.cnf" -extensions v3_ca -newkey rsa:2048 -nodes -days 3650 \
    -keyout "${caDir}/ca.key" -out "${caDir}/ca.pem" -subj "/C=US/O=Apereo/CN=${name}" >/dev/null 2>&1 \
    || crlFail "CA ${name} could not be created"
}

function crlIssueCert() {
  local caDir="$1"
  local cn="$2"
  local crlName="$3"
  cat > "${CRL_WORK_DIR}/${cn}.ext" <<EXT
basicConstraints = CA:FALSE
keyUsage = critical,digitalSignature,keyEncipherment
extendedKeyUsage = clientAuth
crlDistributionPoints = URI:${CRL_SERVER_URL}/${crlName}
EXT
  openssl req -new -newkey rsa:2048 -nodes -keyout "${CRL_WORK_DIR}/${cn}.key" -out "${CRL_WORK_DIR}/${cn}.csr" \
    -subj "/C=US/O=Apereo/CN=${cn}" >/dev/null 2>&1 || crlFail "request for ${cn} could not be created"
  openssl x509 -req -sha256 -days 365 -in "${CRL_WORK_DIR}/${cn}.csr" -CA "${caDir}/ca.pem" -CAkey "${caDir}/ca.key" \
    -CAcreateserial -CAserial "${caDir}/ca.srl" -extfile "${CRL_WORK_DIR}/${cn}.ext" -out "${CRL_WORK_DIR}/${cn}.pem" >/dev/null 2>&1 \
    || crlFail "certificate for ${cn} could not be issued"
}

echo "Creating X.509 certificates and CRLs in ${CRL_WORK_DIR}"
rm -rf "${CRL_WORK_DIR}"
mkdir -p "${CRL_WORK_DIR}" || crlFail "${CRL_WORK_DIR} could not be created"

crlCreateCa "CasTestCA"
crlIssueCert "${CRL_WORK_DIR}/CasTestCA" "crlvalid" "ca.crl"
crlIssueCert "${CRL_WORK_DIR}/CasTestCA" "crlrevoked" "ca.crl"
openssl ca -config "${CRL_WORK_DIR}/CasTestCA/ca.cnf" -revoke "${CRL_WORK_DIR}/crlrevoked.pem" -crl_reason keyCompromise >/dev/null 2>&1 \
  || crlFail "crlrevoked could not be revoked"
openssl ca -config "${CRL_WORK_DIR}/CasTestCA/ca.cnf" -gencrl -out "${CRL_WORK_DIR}/ca.crl" >/dev/null 2>&1 \
  || crlFail "the CRL of CasTestCA could not be generated"

crlCreateCa "CasTestUnlistedCA"
crlIssueCert "${CRL_WORK_DIR}/CasTestUnlistedCA" "crlunlisted" "unlisted-ca.crl"

mkdir -p "${CRL_WORK_DIR}/CasTestExpiredCrlCA"
cp "${CRL_SCENARIO_DIR}/expired-ca.pem" "${CRL_WORK_DIR}/CasTestExpiredCrlCA/ca.pem"
cp "${CRL_SCENARIO_DIR}/expired-ca.key" "${CRL_WORK_DIR}/CasTestExpiredCrlCA/ca.key"
crlIssueCert "${CRL_WORK_DIR}/CasTestExpiredCrlCA" "crlexpired" "expired-ca.crl"
cp "${CRL_SCENARIO_DIR}/expired-ca.crl" "${CRL_WORK_DIR}/expired-ca.crl"

openssl crl -in "${CRL_WORK_DIR}/ca.crl" -noout -text | grep -A1 "Serial Number" \
  || crlFail "the CRL of CasTestCA does not list the revoked certificate"
