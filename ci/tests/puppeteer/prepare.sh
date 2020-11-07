#!/bin/bash

echo "Installing jq"
sudo apt-get install jq

echo "Installing Puppeteer"
npm i --prefix ./ci/tests/puppeteer puppeteer

echo "Creating overlay work directory"
rm -Rf ./ci/tests/puppeteer/overlay
mkdir ./ci/tests/puppeteer/overlay
cd ./ci/tests/puppeteer/overlay

echo "Fetching CAS overlay"
curl -k https://casinit.herokuapp.com/starter.tgz | tar -xzvf -

dname="${dname:-CN=cas.example.org,OU=Example,OU=Org,C=US}"
subjectAltName="${subjectAltName:-dns:example.org,dns:localhost,ip:127.0.0.1}"
keystore="./thekeystore"
echo -e "\nGenerating keystore ${keystore} for CAS with DN=${dname}, SAN=${subjectAltName} ..."
[ -f "${keystore}" ] && rm "${keystore}"
keytool -genkey -noprompt -alias cas -keyalg RSA -keypass changeit -storepass changeit \
  -keystore "${keystore}" -dname "${dname}" -ext SAN="${subjectAltName}"
[ -f "${keystore}" ] && echo "Created ${keystore}"
