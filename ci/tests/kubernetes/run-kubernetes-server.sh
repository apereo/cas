#!/bin/bash

GREEN="\e[32m"
ENDCOLOR="\e[0m"

function printgreen() {
  printf "🍀 ${GREEN}$1${ENDCOLOR}\n"
}

printgreen "Checking minikube and kubectl versions..."
minikube version
kubectl version --client

minikube stop 2>/dev/null
minikube delete 2>/dev/null

printgreen "Starting minikube..."
minikube start --driver=docker

echo "Mounting volumes into minikube..."
minikube mount ${SCENARIO_FOLDER}/k8config:/etc/cas/ &
sleep 3

printgreen "Checking minikube status..."
minikube status

#kubectl create deployment cas --image=apereo/cas:latest \
#  --dry-run=client -o=yaml > deployment.yaml
#echo --- >> deployment.yaml
#kubectl create service clusterip cas --tcp=8080:8080 --tcp=8443:8443 \
#  --dry-run=client -o=yaml >> deployment.yaml

printgreen "Loading CAS Docker image ${DOCKER_IMAGE} for Kubernetes..."
minikube image load ${DOCKER_IMAGE}
#minikube image list

printgreen "Creating CAS deployment and service for CAS ${DOCKER_IMAGE}..."
kubectl create service clusterip cas --tcp=8080:8080 --tcp=8443:8443
printgreen "Applying CAS deployment configuration for ${SCENARIO_FOLDER}..."
envsubst < "$PWD"/ci/tests/kubernetes/deployment.yaml | kubectl apply -f -

printgreen "Enabling addons..."
minikube addons enable metrics-server

podName=$(kubectl get pods -o jsonpath='{.items[0].metadata.name}')
printgreen "Waiting for CAS pod ${podName} to get ready..."
kubectl wait --for=condition=Ready pod/"${podName}" --timeout=90s
kubectl rollout status deployment/cas -n default --timeout=180s
kubectl logs -f "$podName" &
sleep 15

#printgreen "Running minikube dashboard..."
#minikube dashboard &

printgreen "Mapping CAS service ports..."
kubectl port-forward svc/cas 8443:8443 &
sleep 5

until curl -k -L --output /dev/null --silent --fail https://localhost:8443/cas/login; do
  echo -n .
  sleep 2
done

printgreen "Kubernetes server is running!"
