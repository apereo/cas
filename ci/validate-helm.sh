#!/bin/bash

echo "Running casinit"
java -jar app/build/libs/app.jar &
sleep 30
mkdir tmp
cd tmp
curl http://localhost:8080/starter.tgz -d dependencies=core,bootadmin | tar -xzvf -

echo "Building War and Jib Docker Image"
chmod -R 777 ./*.sh
./gradlew clean build jibBuildTar --refresh-dependencies

imageTag=(v$(./gradlew casVersion --q))
echo "Image tag is ${imageTag}"

echo "Loading CAS image into k3s"
sudo k3s ctr images import build/jib-image.tar

cd helm
chmod +x *.sh

echo "Creating Keystore and secret for keystore"
./create-cas-server-keystore-secret.sh

echo "Creating tls secret for ingress to use"
./create-ingress-tls.sh

echo "Creating truststore with server/ingress certs and put in configmap"
./create-truststore.sh

# Lint chart
helm lint cas-server

# k3s comes with Traefik so we could try using that instead at some point
echo "Installing ingress controller and waiting for it to start"
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
kubectl create namespace ingress-nginx
helm install --namespace ingress-nginx ingress-nginx ingress-nginx/ingress-nginx
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=120s

echo "Install cas-server helm chart"
echo "Using local jib image imported into k3s"
helm upgrade --install cas-server --set image.pullPolicy=Never --set image.tag="${imageTag}" ./cas-server
sleep 15
kubectl describe pod cas-server-0
sleep 60
kubectl logs cas-server-0 | tee cas.out
grep "Started CasWebApplication" cas.out
curl -k -v -H "Host: cas.example.org" https://127.0.0.1/cas/login > login.txt
grep "password" login.txt
helm test cas-server
kubectl get pods