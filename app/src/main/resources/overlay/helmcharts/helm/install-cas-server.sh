#!/bin/sh
NAMESPACE=${1:-default}

helm upgrade --install cas-server --namespace $NAMESPACE ./cas-server