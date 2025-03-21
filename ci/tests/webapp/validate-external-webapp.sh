#!/bin/bash
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
        [ -f "${keystore}" ] && sudo rm "${keystore}"
        sudo keytool -genkey -noprompt -alias cas -keyalg RSA -keypass changeit -storepass changeit \
          -keystore "${keystore}" -dname "${dname}" -ext SAN="${subjectAltName}"
        ;;
    *)
        echo -e "Unable to determine web application project $1"
        exit 1
esac

echo -e "Validating web application project: ${project}"

echo -e "Checking for Apache Tomcat version..."
tomcatVersion=$(cat gradle/libs.versions.toml | grep "^tomcat = " | awk -F"=" '{printf $2}' | tr -d ' "')
echo "Apache Tomcat version: ${tomcatVersion}"

tomcatVersionTag="v${tomcatVersion}"
tomcatUrl="https://archive.apache.org/dist/tomcat/tomcat-11/${tomcatVersionTag}/bin/apache-tomcat-${tomcatVersion}.zip"

export CATALINA_HOME=./apache-tomcat-${tomcatVersion}
rm -Rf ${CATALINA_HOME}
rm -Rf apache-tomcat-${tomcatVersion}.zip

echo -e "Downloading Apache Tomcat from ${tomcatUrl}"
success=false
if [[ ! -f "apache-tomcat-${tomcatVersion}.zip" ]]; then
  for i in $(seq 1 5); do
      echo "Attempt $i - Downloading Apache Tomcat from ${tomcatUrl}"
      wget --no-check-certificate --timeout=30 --tries=3 "${tomcatUrl}" > /dev/null 2>&1 && success=true && break
      echo "Download failed. Retrying..."
      sleep 10
  done
fi
if [ "$success" = false ]; then
  echo "Failed to download Apache Tomcat ${tomcatVersion}"
  exit 1
fi

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
if [ "$rc" == 200 ]; then
    echo "Deployed the web application successfully."
    exit 0
else
    echo "Failed to deploy the web application with status $rc."
    exit 1
fi
