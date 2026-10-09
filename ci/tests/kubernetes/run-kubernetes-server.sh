#!/bin/bash

RED="\e[31m"
GREEN="\e[32m"
ENDCOLOR="\e[0m"

function printgreen() {
  printf "🍀 ${GREEN}$1${ENDCOLOR}\n"
}

function printred() {
  printf "🚨  ${RED}$1${ENDCOLOR}\n"
}

OS="$(uname -s | tr '[:upper:]' '[:lower:]')"
ARCH="$(uname -m)"

case "$ARCH" in
  x86_64|amd64) ARCH="amd64" ;;
  arm64|aarch64) ARCH="arm64" ;;
  *)
    printred "Unsupported architecture: $ARCH"
    exit 1
    ;;
esac

if [[ "$OS" == "darwin" ]]; then
  PLATFORM="darwin"
elif [[ "$OS" == "linux" ]]; then
  PLATFORM="linux"
else
  printred "Unsupported OS: $OS"
  exit 1
fi

printgreen "Detected platform: $PLATFORM/$ARCH"

install_kubectl() {
  if command -v kubectl >/dev/null 2>&1; then
    printgreen "kubectl is already installed"
    return
  fi

  printgreen "Installing kubectl..."
  KUBECTL_VERSION="$(curl -fsSL https://dl.k8s.io/release/stable.txt)"
  curl -fsSLO "https://dl.k8s.io/release/${KUBECTL_VERSION}/bin/${PLATFORM}/${ARCH}/kubectl"
  chmod +x kubectl
  sudo mv kubectl /usr/local/bin/kubectl
}

install_minikube() {
  if command -v minikube >/dev/null 2>&1; then
    printgreen "minikube is already installed"
    return
  fi

  printgreen "Installing minikube..."
  curl -fsSLO "https://github.com/kubernetes/minikube/releases/latest/download/minikube-${PLATFORM}-${ARCH}"
  chmod +x "minikube-${PLATFORM}-${ARCH}"
  sudo mv "minikube-${PLATFORM}-${ARCH}" /usr/local/bin/minikube
}

install_kubectl
install_minikube

minikube stop >/dev/null 2>&1 || true
minikube delete >/dev/null 2>&1 || true

printgreen "Starting minikube..."
minikube start --driver=docker

minikube version
kubectl version --client

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
if [[ "${CAS_NATIVE_RUN:-false}" == "true" ]]; then
  nativeStartupLog="${CAS_NATIVE_STARTUP_LOG:-${PWD}/ci/tests/puppeteer/overlay/cas-instance-1.log}"
  : > "${nativeStartupLog}"
  kubectl logs -f "$podName" > >(tee -a "${nativeStartupLog}") 2>&1 &
else
  kubectl logs -f "$podName" &
fi
sleep 15

#printgreen "Running minikube dashboard..."
#minikube dashboard &

printgreen "Mapping CAS service ports..."
kubectl port-forward svc/cas 8443:8443 &
sleep 5

nativeStartupDeadline=$((SECONDS + 180))
while true; do
  if [[ "${CAS_NATIVE_RUN:-false}" != "true" ]] || {
    grep -q 'Started CasNativeWebApplication in' "${nativeStartupLog}" 2>/dev/null &&
      grep -q 'Ready to process requests' "${nativeStartupLog}" 2>/dev/null
  }; then
    if curl -k -L --connect-timeout 10 --max-time 10 --output /dev/null --silent --fail https://localhost:8443/cas/login; then
      break
    fi
  fi
  if [[ "${CAS_NATIVE_RUN:-false}" == "true" && ${SECONDS} -ge ${nativeStartupDeadline} ]]; then
    printred "CAS native startup did not complete in Kubernetes."
    exit 1
  fi
  echo -n .
  sleep 2
done

printgreen "Kubernetes server is running!"
