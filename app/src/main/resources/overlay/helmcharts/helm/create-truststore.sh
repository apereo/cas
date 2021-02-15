#!/bin/sh
NAMESPACE=${1:-default}
INGRESS_CERT_FILE=cas-ingress.crt
CAS_CERT_FILE=cas.crt
CAS_KEYSTORE=../etc/cas/thekeystore
TRUST_STORE=../etc/cas/truststore
JAVA_CACERTS=${2:-/etc/ssl/certs/java/cacerts}

STORE_PASS=changeit

set -e

if [ -f ${TRUST_STORE} ]; then
  rm ${TRUST_STORE}
fi

if [ -f "${JAVA_CACERTS}" ]; then
  keytool -importkeystore -noprompt -srckeystore "${JAVA_CACERTS}" -srcstorepass "${STORE_PASS}" -destkeystore "${TRUST_STORE}" -deststoretype PKCS12 -deststorepass "${STORE_PASS}"
else
  echo "Missing ${JAVA_CACERTS} JAVA_HOME is ${JAVA_HOME}"
  if [ -d "${JAVA_HOME}" ]; then
    find ${JAVA_HOME} -name cacerts -print
    find ${JAVA_HOME} -name cacerts -exec keytool -importkeystore -noprompt -srckeystore {} -srcstorepass "${STORE_PASS}" -destkeystore "${TRUST_STORE}" -deststoretype PKCS12 -deststorepass "${STORE_PASS}" \;
  fi
fi

# create truststore that trusts ingress cert
if [ -f "${INGRESS_CERT_FILE}" ] ; then
  keytool -importcert -noprompt -keystore "${TRUST_STORE}" -storepass "${STORE_PASS}" -alias cas-ingress -file "${INGRESS_CERT_FILE}" -storetype PKCS12
else
  echo "Missing ingress cert file to put in trust bundle: ${INGRESS_CERT_FILE}"
fi

# add cas server cert to trust store
if [ -f "${CAS_KEYSTORE}" ] ; then
  keytool -exportcert -keystore "${CAS_KEYSTORE}" -storepass "${STORE_PASS}" -alias cas -file "${CAS_CERT_FILE}" -rfc
  keytool -importcert -noprompt -storepass "${STORE_PASS}" -keystore "${TRUST_STORE}" -alias cas -file "${CAS_CERT_FILE}" -storetype PKCS12
else
  echo "Missing keystore ${CAS_KEYSTORE} to put cas cert in trust bundle"
fi
kubectl delete configmap cas-truststore --namespace "${NAMESPACE}" || true
kubectl create configmap cas-truststore --namespace "${NAMESPACE}" --from-file=truststore=${TRUST_STORE}