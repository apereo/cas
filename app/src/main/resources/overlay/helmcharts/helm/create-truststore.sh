#!/bin/sh
NAMESPACE=${1:-default}
INGRESS_CERT_FILE=cas-ingress.crt
CAS_CERT_FILE=cas.crt
CAS_KEYSTORE=../etc/cas/thekeystore
TRUST_STORE=../etc/cas/truststore

set -e
# create truststore that trusts ingress cert
if [ -f "${INGRESS_CERT_FILE}" ] ; then
  keytool -importcert -noprompt -keystore "${TRUST_STORE}" -storepass changeit -alias cas-ingress -file "${INGRESS_CERT_FILE}" -storetype PKCS12
else
  echo "Missing ingress cert file to put in trust bundle: ${INGRESS_CERT_FILE}"
fi

# add cas server cert to trust store
if [ -f "${CAS_KEYSTORE}" ] ; then
  keytool -exportcert -keystore "${CAS_KEYSTORE}" -storepass changeit -alias cas -file "${CAS_CERT_FILE}" -rfc
  keytool -importcert -noprompt -storepass changeit -keystore "${TRUST_STORE}" -alias cas -file "${CAS_CERT_FILE}" -storetype PKCS12
else
  echo "Missing keystore ${CAS_KEYSTORE} to put cas cert in trust bundle"
fi
kubectl delete configmap cas-truststore --namespace "${NAMESPACE}" || true
kubectl create configmap cas-truststore --namespace "${NAMESPACE}" --from-file=truststore=${TRUST_STORE}