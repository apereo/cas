#!/bin/sh
NAMESPACE=${1:-default}
SUBJECT=/CN=cas.example.org/OU=Auth/O=example
SAN=DNS:casadmin.example.org,DNS:cas.example.org
SECRET_NAME=cas-server-ingress-tls
KEY_FILE=cas-ingress.key
CERT_FILE=cas-ingress.crt

set -e

# create certificate for external ingress
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout "${KEY_FILE}" -out ${CERT_FILE} -subj "${SUBJECT}" \
  -addext "subjectAltName = $SAN"

kubectl delete secret "${SECRET_NAME}" --namespace "${NAMESPACE}" || true
# create tls secret with key and cert
kubectl create secret tls "${SECRET_NAME}" --namespace "${NAMESPACE}" --key "${KEY_FILE}" --cert "${CERT_FILE}"

