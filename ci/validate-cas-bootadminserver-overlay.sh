#!/bin/bash

source ./ci/functions.sh

java -jar app/build/libs/app.jar &
pid=$!
sleep 15
rm -Rf tmp &> /dev/null
mkdir tmp
cd tmp
curl http://localhost:8080/starter.tgz -d type=cas-bootadmin-server-overlay | tar -xzvf -
kill -9 $pid

echo "Building CAS Spring Boot Admin Server Overlay"
./gradlew clean build --no-daemon

java -jar build/libs/casbootadminserver.war --server.ssl.enabled=false --spring.security.user.password=password --spring.security.user.name=casuser &
pid=$!
sleep 5

echo "Launched CAS with pid ${pid}. Waiting for server to come online..."
until curl -u casuser:password -k -L --output /dev/null --silent --fail http://localhost:8444/; do
    echo -n '.'
    sleep 5
done
echo -e "\n\nReady!"
kill -9 $pid

echo "Building Docker image with Jib"
chmod -R 777 ./*.sh
./gradlew jibDockerBuild

downloadTomcat
mv build/libs/casbootadminserver.war ${CATALINA_HOME}/webapps/casbootadminserver.war

export SPRING_SECURITY_USER_PASSWORD=password
export SPRING_SECURITY_USER_NAME=casuser

${CATALINA_HOME}/bin/startup.sh & >/dev/null 2>&1
pid=$!
sleep 30
rc=`curl -L -k -u casuser:password -o /dev/null --connect-timeout 60 -s  -I -w "%{http_code}" http://localhost:8444/casbootadminserver`
${CATALINA_HOME}/bin/shutdown.sh & >/dev/null 2>&1
kill -9 $pid
if [ "$rc" == 200 ]; then
    echo "Deployed the web application successfully."
    exit 0
else
    echo "Failed to deploy the web application with status $rc."
    exit 1
fi

