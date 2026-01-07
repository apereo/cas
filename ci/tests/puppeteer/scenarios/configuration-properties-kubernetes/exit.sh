#!/bin/bash

echo "Stopping minikube..."
minikube stop
minikube delete 2>/dev/null
rm -rf ${SCENARIO_FOLDER}/k8config/thekeystore
