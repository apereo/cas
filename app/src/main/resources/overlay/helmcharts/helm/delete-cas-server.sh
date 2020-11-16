#!/usr/bin/env bash
NAMESPACE=${1:-default}
helm delete --namespace $NAMESPACE cas-server