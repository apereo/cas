#!/usr/bin/env bash
NAMESPACE=${1:-default}
KEYSTORE=/etc/cas/thekeystore

# it's important that the service names are supported in the cert used for tomcat in cas-server and boot-admin
SUBJECT=CN=cas.example.org,OU=Example,OU=Org,C=US
SAN=dns:cas.example.org,dns:casadmin.example.org,dns:cas-server-boot-admin,dns:cas-server

if [ ! -f "$KEYSTORE" ] ; then
  pushd ..
  ./gradlew createKeyStore -PcertificateDn="${SUBJECT}" -PcertificateSubAltName="${SAN}"
  popd
fi

kubectl delete secret cas-server-keystore
kubectl create secret generic cas-server-keystore --namespace "${NAMESPACE}" --from-file=thekeystore=$KEYSTORE