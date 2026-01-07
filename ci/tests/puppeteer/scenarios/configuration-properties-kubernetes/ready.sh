#!/bin/bash

"${PWD}"/ci/tests/httpbin/run-httpbin-server.sh

export DOCKER_IMAGE="cas-${SCENARIO}:latest"

rm -rf ${SCENARIO_FOLDER}/k8config/thekeystore
cp ${SCENARIO_FOLDER}/../../overlay/thekeystore ${SCENARIO_FOLDER}/k8config/thekeystore
echo "Copied thekeystore to ${SCENARIO_FOLDER}/k8config/thekeystore"

chmod +x "${PWD}/ci/tests/kubernetes/run-kubernetes-server.sh"
${PWD}/ci/tests/kubernetes/run-kubernetes-server.sh

echo "Ready!"
