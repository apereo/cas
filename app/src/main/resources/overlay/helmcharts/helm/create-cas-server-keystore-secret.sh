#!/usr/bin/env bash
# This script needs bash for pushd/popd
set -e
NAMESPACE=${1:-default}
KEYSTORE=../etc/cas/thekeystore

# it's important that the service names are supported in the cert used for tomcat in cas-server and boot-admin
SUBJECT=CN=cas.example.org,OU=Example,OU=Org,C=US
SAN=dns:cas.example.org,dns:casadmin.example.org,dns:cas-server-boot-admin,dns:cas-server

if [ ! -f "$KEYSTORE" ] ; then
  pushd ..
  ./gradlew createKeyStore -PcertDir=./etc/cas -PcertificateDn="${SUBJECT}" -PcertificateSubAltName="${SAN}"
  popd
fi

kubectl delete secret cas-server-keystore || true
kubectl create secret generic cas-server-keystore --namespace "${NAMESPACE}" --from-file=thekeystore=$KEYSTORE