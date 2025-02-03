#!/bin/bash

echo "Setting up k3d cluster"
# This sets up a kubernetes cluster with one node and the ingress-nginx ingress controller installed.
# Script installs k3d which is a k3s kubernetes cluster running in docker.
# Script installs helm but assumes kubectl is already installed (github ubuntu runner has it).
# Script tested MSYS2 bash on Windows
# Users must manually add host entry for: 127.0.0.1 host.k3d.internal

HTTPS_PORT=${HTTPS_PORT:-443}
HTTP_PORT=${HTTP_PORT:-80}

K3D_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
CURL_OPTS="-v --retry 5 --retry-delay 15"
OS=$(uname|tr '[:upper:]' '[:lower:]')
echo "OS is ${OS}"
case "$OS" in
  # msys/mingw for Windows
  mingw*)
    HELM_INSTALL_OPTS="--no-sudo"
    PING_ARGS="-n 1"
    ;;
  *)
    HELM_INSTALL_OPTS=""
    PING_ARGS="-c 1"
    ;;
esac

if [[ ! -z "${GITHUB_PATH}" ]]; then
  echo "/usr/local/bin/kubectl" >> ${GITHUB_PATH}
fi

k3d version > /dev/null 2>&1
k3d_installed=$?
helm version > /dev/null 2>&1
helm_installed=$?
kubectl > /dev/null 2>&1
kubectl_installed=$?

set -e
if [[ ${k3d_installed} -ne 0 ]]; then
  echo "Installing k3d"
  # allow script to be cached
  if [[ ! -f ${K3D_DIR}/install.sh ]]; then
    curl ${CURL_OPTS} -o ${K3D_DIR}/install.sh https://raw.githubusercontent.com/k3d-io/k3d/main/install.sh
  fi
  chmod +x ${K3D_DIR}/install.sh
  ${K3D_DIR}/install.sh
fi
echo "k3d version"
k3d version

if [[ ${helm_installed} -ne 0 ]]; then
  echo "Installing helm"
  if [[ ! -f ${K3D_DIR}/install.sh ]]; then
    curl ${CURL_OPTS} -o ${K3D_DIR}/get_helm.sh https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3
    chmod +x ${K3D_DIR}/get_helm.sh
    ${K3D_DIR}/get_helm.sh $HELM_INSTALL_OPTS
  fi
fi
echo "helm version"
helm version

if [[ ${kubectl_installed} -ne 0 ]]; then
  echo "Please install kubectl for your OS from https://kubernetes.io/docs/tasks/tools/"
  exit 1
fi

set +e
k3d cluster list | grep cas
cluster_found=$?
set -e
if [[ $cluster_found -ne 0 ]]; then
  echo "Creating cas k3d cluster"
  k3d cluster create cas  -p "$HTTP_PORT:80@loadbalancer" -p "$HTTPS_PORT:443@loadbalancer" --k3s-arg "--disable=traefik,metrics-server@server:0" --k3s-arg "--disable-cloud-controller@server:0"
else
  echo "Setting k8s context to k3d-cas"
  kubectl config use-context k3d-cas
fi

echo "kubectl version"
kubectl version

echo "Checking for host entry"
set +e
ping ${PING_ARGS} host.k3d.internal > /dev/null 2>&1
host_found=$?
set -e
if [[ $host_found -ne 0 ]]; then
  if [[ "${CI}" == "true" ]]; then
    echo '127.0.0.1 host.k3d.internal' | sudo tee -a /etc/hosts > /dev/null
    sudo cat /etc/hosts
  else
    echo "You need to create a host entry for:"
    echo "127.0.0.1 host.k3d.internal"
    exit 1
  fi
fi
ping ${PING_ARGS} host.k3d.internal > /dev/null 2>&1

helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo add bitnami https://charts.bitnami.com/bitnami

kubectl create namespace ingress-nginx || true
set +e
helm list -A | grep ingress-nginx
ingress_controller_found=$?
set -e
if [[ $ingress_controller_found -ne 0 ]]; then
  helm install ingress-nginx ingress-nginx/ingress-nginx -n ingress-nginx
  kubectl wait --namespace ingress-nginx \
    --for condition=ready pod \
    --selector=app.kubernetes.io/component=controller \
    --timeout=120s
fi
