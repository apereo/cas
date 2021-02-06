#!/bin/bash

set -e

echo "Running casinit"
java -jar app/build/libs/app.jar &
pid=$!
sleep 30
if [[ -d tmp ]] ; then
  rm -rf tmp
fi
mkdir tmp
cd tmp
# create project dir from initializer with support boot admin, metrics, and git service registry
curl http://localhost:8080/starter.tgz -d dependencies=core,bootadmin,metrics,gitsvc | tar -xzvf -
echo Killing initializer pid $pid
kill -9 $pid &> /dev/null

echo "Building War and Jib Docker Image"
chmod -R 777 ./*.sh
./gradlew clean build jibBuildTar --refresh-dependencies

imageTag=(v$(./gradlew casVersion --q))
echo "Image tag is ${imageTag}"

echo "Loading CAS image into k3s"
sudo k3s ctr images import build/jib-image.tar

cd helm
chmod +x *.sh

export NAMESPACE=${1:-default}
if [[ $NAMESPACE != "default" ]]; then
  kubectl create namespace $NAMESPACE || true
fi

echo "Creating Keystore and secret for keystore"
./create-cas-server-keystore-secret.sh $NAMESPACE

echo "Creating tls secret for ingress to use"
./create-ingress-tls.sh $NAMESPACE

echo "Creating truststore with server/ingress certs and put in configmap"
./create-truststore.sh $NAMESPACE

# Set KUBECONFIG for helm
export KUBECONFIG=/etc/rancher/k3s/k3s.yaml

# Lint chart
echo Lint check on cas-server helm chart
helm lint cas-server

# k3s comes with Traefik so we could try using that instead at some point
echo "Installing ingress controller and waiting for it to start"
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
kubectl create namespace ingress-nginx || true
# install with some options that we don't necessarily need here but may be important in some deployments
# proxy-buffer-size is needed for nginx to handle large OIDC related headers and avoids 502 error from nginx
# use-forwarded-headers is needed if your ingress controller is behind another proxy, should be false if not behind another proxy
# enable-underscores-in-headers is important if you are trying to use a header with an underscore from another proxy
helm upgrade --install --namespace ingress-nginx ingress-nginx ingress-nginx/ingress-nginx \
  --set controller.config.enable-underscores-in-headers=true \
  --set controller.config.use-forwarded-headers=true \
  --set controller.config.proxy-buffer-size=16k

kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=120s

echo "Deleting cas-server helm chart if it already exists"
helm delete cas-server --namespace $NAMESPACE || true

echo "Install cas-server helm chart"
echo "Using local jib image imported into k3s"
helm upgrade --install cas-server --namespace $NAMESPACE --set image.pullPolicy=Never --set image.tag="${imageTag}" ./cas-server

echo "Waiting for startup"
kubectl wait --for=condition=ready --timeout=150s --namespace $NAMESPACE pod cas-server-0 || true
kubectl wait --for=condition=available --timeout=150s --namespace $NAMESPACE deployment cas-server-boot-admin || true

echo "Describing cas-server pod"
kubectl describe pod --namespace $NAMESPACE cas-server-0
echo "Describing cas bootadmin pod"
kubectl describe pod --namespace $NAMESPACE -l cas.server-type=bootadmin

echo "Pod Status:"
kubectl get pods --namespace $NAMESPACE

echo "CAS Server Logs..."
kubectl logs cas-server-0 --namespace $NAMESPACE | tee cas.out
echo "CAS Boot Admin Server Logs..."
kubectl logs -l cas.server-type=bootadmin --tail=-1 --namespace $NAMESPACE | tee cas-bootadmin.out
echo "Checking cas server log for startup message"
grep "Started CasWebApplication" cas.out
echo "Checking bootadmin server log for startup message"
grep "Started CasSpringBootAdminServerWebApplication" cas-bootadmin.out

echo "Running chart built-in test"
helm test --namespace $NAMESPACE cas-server

echo "Checking login page"
curl -k -H "Host: cas.example.org" https://127.0.0.1/cas/login > login.txt
grep "password" login.txt

