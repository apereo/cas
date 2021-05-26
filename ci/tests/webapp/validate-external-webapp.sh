#!/bin/bash
clear
type="$1"

case "$1" in
    cas-server)
        project="cas-server-webapp"
        url="/login"
        ;;
    config-server)
        project="cas-server-webapp-config-server"
        url="/env/default"
        
        dname="${dname:-CN=cas.example.org,OU=Example,OU=Org,C=US}"
        subjectAltName="${subjectAltName:-dns:example.org,dns:localhost,ip:127.0.0.1}"
        sudo mkdir -p /etc/cas
        keystore="/etc/cas/casconfigserver.jks"
        echo "Generating keystore ${keystore} for CAS with DN=${dname}, SAN=${subjectAltName}"
        [ -f "${keystore}" ] && rm "${keystore}"
        sudo keytool -genkey -noprompt -alias cas -keyalg RSA -keypass changeit -storepass changeit \
          -keystore "${keystore}" -dname "${dname}" -ext SAN="${subjectAltName}"
        ;;
    bootadmin-server)
        project="cas-server-webapp-bootadmin-server"
        url=""
        ;;
    discovery-server)
        project="cas-server-webapp-eureka-server"
        url=""
        ;;
    *)
        echo -e "Unable to determine web application project $1"
        exit 1
esac

echo -e "Validating web application project: ${project}"

echo -e "Checking for Apache Tomcat version..."
tomcatVersion=$(cat gradle.properties | grep tomcatVersion | awk -F"=" '{printf $2}')
echo "Apache Tomcat version: ${tomcatVersion}"

tomcatVersionTag="v${tomcatVersion}"
tomcatUrl="https://downloads.apache.org/tomcat/tomcat-9/${tomcatVersionTag}/bin/apache-tomcat-${tomcatVersion}.zip"

export CATALINA_HOME=./apache-tomcat-${tomcatVersion}
rm -Rf ${CATALINA_HOME}
rm -Rf apache-tomcat-${tomcatVersion}.zip

echo -e "Downloading Apache Tomcat from ${tomcatUrl}"
wget --no-check-certificate ${tomcatUrl}
unzip apache-tomcat-${tomcatVersion}.zip >/dev/null 2>&1

clear
echo -e "Building project ${project}..."
./gradlew :webapp:${project}:build \
  -DskipNestedConfigMetadataGen=true -x check -x javadoc -q \
  --no-daemon --build-cache --configure-on-demand --parallel

echo -e "Removing Apache Tomcat default web applications..."
rm -Rf ${CATALINA_HOME}/webapps/examples ${CATALINA_HOME}/webapps/docs \
  ${CATALINA_HOME}/webapps/manager ${CATALINA_HOME}/webapps/host-manager

echo -e "Preparing web application ${project} for deployment..."
mv webapp/${project}/build/libs/*-SNAPSHOT.war ${CATALINA_HOME}/webapps/cas.war
chmod +x ${CATALINA_HOME}/bin/*.sh
touch ${CATALINA_HOME}/logs/catalina.out ; tail -F ${CATALINA_HOME}/logs/catalina.out &

export SPRING_SECURITY_USER_NAME=casuser
export SPRING_SECURITY_USER_PASSWORD=password
export SPRING_PROFILES_ACTIVE=native

echo "Starting Apache Tomcat..."
${CATALINA_HOME}/bin/startup.sh & >/dev/null 2>&1
pid=$!
echo -e "Launched Apache Tomcat with pid ${pid}"
sleep 30
clear
healthurl="http://localhost:8080/cas${url}"
echo "Checking web application startup via ${healthurl} ..."
rc=`curl -L -k -u casuser:password --connect-timeout 60 -s -o /dev/null -I -w "%{http_code}" ${healthurl}`
${CATALINA_HOME}/bin/shutdown.sh & >/dev/null 2>&1
sleep 5
kill -9 $pid
rm -Rf ${CATALINA_HOME}
rm -Rf apache-tomcat-${tomcatVersion}.zip
clear
if [ "$rc" == 200 ]; then
    echo "Deployed the web application successfully."
    exit 0
else
    echo "Failed to deploy the web application with status $rc."
    exit 1
fi
