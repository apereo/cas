#!/bin/sh
NAMESPACE=${1:-default}
EXAMPLE=${2:-example1}

helm upgrade --install cas-server --values values-${EXAMPLE}.yaml --namespace ${NAMESPACE} ./cas-server
