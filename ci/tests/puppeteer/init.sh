#!/bin/bash

dependencies="$1"
scriptPath="$2"

echo -e "*************************************"
echo -e "CAS dependencies: ${dependencies}"
echo -e "Script path: ${scriptPath}\n"
echo -e "*************************************\n"

rm -Rf work && mkdir work && cd work
curl https://casinit.herokuapp.com/starter.tgz -d dependencies="${dependencies}" | tar -xzvf -
# curl http://localhost:8080/starter.tgz -d dependencies="${dependencies}" | tar -xzvf -
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

echo -e "*************************************"
echo -e "Running ${scriptPath}\n"
node ${scriptPath}
echo -e "*************************************\n"

echo -e "\nKilling process ${pid} ..."
kill -9 $pid
cd .. && rm -Rf work
