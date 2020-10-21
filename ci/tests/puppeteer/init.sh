node github actions:
  npm i puppeteer

#!/bin/bash

rm -Rf work && mkdir work && cd work
echo -e "\nFetching CAS for dependencies ${dependencies} ..."
curl https://casinit.herokuapp.com/starter.tgz -d dependencies="${dependencies}" | tar -xzvf -
echo -e "\nBuilding CAS..."
./gradlew clean build --no-daemon

dname="${dname:-CN=cas.example.org,OU=Example,OU=Org,C=US}"
subjectAltName="${subjectAltName:-dns:example.org,dns:localhost,ip:127.0.0.1}"
keystore="./thekeystore"
echo -e "\nGenerating keystore ${keystore} for CAS with DN=${dname}, SAN=${subjectAltName} ..."
[ -f "${keystore}" ] && rm "${keystore}"
keytool -genkey -noprompt -alias cas -keyalg RSA -keypass changeit -storepass changeit \
  -keystore "${keystore}" -dname "${dname}" -ext SAN="${subjectAltName}"
echo -e "\nLaunching CAS..."
java -jar build/libs/cas.war --server.ssl.key-store="${keystore}" &> /dev/null &
pid=$!
echo -e "\nWaiting for CAS under pid ${pid}"
until curl -k -L --output /dev/null --silent --fail https://localhost:8443/cas/login; do
    printf '.'
    sleep 1
done
echo -e "\n\nReady!"

node ${scriptPath}

echo -e "\nKilling process ${pid} ..."
kill -9 $pid