#!/bin/sh
NAMESPACE=${1:-default}
DOMAIN1=cas.example.org
DOMAIN2=kubernetes.docker.internal
CERT_NAME=cas-server-tls
KEY_FILE=cas.key
CERT_FILE=cas.crt

openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout ${KEY_FILE} -out ${CERT_FILE} -subj "/CN=${DOMAIN1}/O=${DOMAIN1}" \
  -addext "subjectAltName = DNS:${DOMAIN1},DNS:${DOMAIN2}"

kubectl create secret tls ${CERT_NAME} --namespace $NAMESPACE --key ${KEY_FILE} --cert ${CERT_FILE}