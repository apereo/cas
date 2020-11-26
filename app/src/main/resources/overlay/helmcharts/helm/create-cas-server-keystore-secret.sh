#!/usr/bin/env bash
NAMESPACE=${1:-default}
kubectl create secret generic cas-server-keystore --namespace "${NAMESPACE}" --from-file=thekeystore=/etc/cas/thekeystore