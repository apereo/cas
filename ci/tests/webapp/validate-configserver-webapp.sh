#!/bin/bash

mv webapp/cas-server-webapp-config-server/build/libs/cas-server-webapp-config-server-*.war \
  webapp/cas-server-webapp-config-server/build/libs/casconfigserver.war

dname="${dname:-CN=cas.example.org,OU=Example,OU=Org,C=US}"
subjectAltName="${subjectAltName:-dns:example.org,dns:localhost,ip:127.0.0.1}"
keystore="./thekeystore"
echo "Generating keystore ${keystore} for CAS with DN=${dname}, SAN=${subjectAltName}"
[ -f "${keystore}" ] && rm "${keystore}"
keytool -genkey -noprompt -alias cas -keyalg RSA -keypass changeit -storepass changeit \
  -keystore "${keystore}" -dname "${dname}" -ext SAN="${subjectAltName}"

echo "Launching CAS config server web application..."
java -jar webapp/cas-server-webapp-config-server/build/libs/casconfigserver.war \
  --spring.security.user.password=Mellon --server.ssl.key-store="${keystore}" &> /dev/null &
pid=$!
echo "Launched CAS config server with pid ${pid}. Waiting for CAS config server to come online..."
sleep 60
cmd=`curl -L -k --user casuser:Mellon --connect-timeout 60 -s \
-o /dev/null -I -w "%{http_code}" https://localhost:8888/casconfigserver/actuator/cas/default`
kill -9 "${pid}"
[ -f "${keystore}" ] && rm "${keystore}"
echo "CAS config server is responding with HTTP status code ${cmd}."
if [ "$cmd" == 200 ]; then
  echo "CAS config server is successfully up and running."
  exit 0
else
  echo "CAS config server failed to start successfully."
  exit 1
fi
