#!/bin/bash

webAppServerType="$1"

mv webapp/cas-server-webapp-eureka-server/build/libs/cas-server-webapp-eureka-server-*.war \
  webapp/cas-server-webapp-eureka-server/build/libs/caseurekaserver.war

dname="${dname:-CN=cas.example.org,OU=Example,OU=Org,C=US}"
subjectAltName="${subjectAltName:-dns:example.org,dns:localhost,ip:127.0.0.1}"
keystore="./thekeystore"
echo "Generating keystore ${keystore} for CAS with DN=${dname}, SAN=${subjectAltName}"
[ -f "${keystore}" ] && rm "${keystore}"
keytool -genkey -noprompt -alias cas -keyalg RSA -keypass changeit -storepass changeit \
  -keystore "${keystore}" -dname "${dname}" -ext SAN="${subjectAltName}"

echo "Launching CAS eureka web application server..."
java -jar webapp/cas-server-webapp-eureka-server/build/libs/caseurekaserver.war --server.ssl.key-store="${keystore}" &> /dev/null &
pid=$!
echo "Launched CAS eureka server with pid ${pid}. Waiting for CAS eureka server to come online..."
sleep 30
cmd=`curl -k --connect-timeout 60 -s -o /dev/null -I \
-w "%{http_code}" https://localhost:8761`
kill -9 "${pid}"
[ -f "${keystore}" ] && rm "${keystore}"
echo "CAS eureka server is responding with HTTP status code ${cmd}."
if [ "$cmd" == 200 ]; then
  echo "CAS eureka server is successfully up and running."
  exit 0
else
  echo "CAS eureka server failed to start successfully."
  exit 1
fi
