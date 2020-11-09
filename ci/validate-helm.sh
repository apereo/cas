#!/bin/bash

java -jar app/build/libs/app.jar &
sleep 30
mkdir tmp
cd tmp
curl http://localhost:8080/starter.tgz -d dependencies=core | tar -xzvf -

echo "Building Overlay"
chmod -R 777 ./*.sh
./gradlew clean build jibDockerBuild --refresh-dependencies

echo "Creating Keystore"
./gradlew -P certDir=etc/cas createKeystore

echo "Create secret for keystore"
kubectl create secret generic cas-server-keystore --from-file=thekeystore=etc/cas/thekeystore

imageTag=(`./gradlew casVersion --q`)

cd helm

echo "Installing ingress controller and waiting for it to start"
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
kubectl create namespace ingress-nginx
helm install --namespace ingress-nginx ingress-nginx ingress-nginx/ingress-nginx
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=120s

echo "Install cas-server helm chart"
helm upgrade --install cas-server --set image.tag=$imageTag ./cas-server
sleep 15
kubectl describe pod cas-server-0
kubectl logs cas-server-0