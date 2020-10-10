#!/bin/bash

tomcatVersion="9.0.39"
tomcatVersionTag="v${tomcatVersion}"
tomcatUrl="https://www-eu.apache.org/dist/tomcat/tomcat-9/${tomcatVersionTag}/bin/apache-tomcat-${tomcatVersion}.zip"

export CATALINA_HOME=./apache-tomcat-${tomcatVersion}
rm -Rf ${CATALINA_HOME}
wget ${tomcatUrl}
unzip apache-tomcat-${tomcatVersion}.zip

mv webapp/cas-server-webapp/build/libs/cas-server-webapp-*.war ${CATALINA_HOME}/webapps/cas.war
chmod +x ${CATALINA_HOME}/bin/*.sh
touch ${CATALINA_HOME}/logs/catalina.out ; tail -F ${CATALINA_HOME}/logs/catalina.out &
${CATALINA_HOME}/bin/startup.sh & >/dev/null 2>&1
sleep 30
rc=`curl -k --connect-timeout 60 -s -o /dev/null -I -w "%{http_code}" http://localhost:8080/cas/login`
${CATALINA_HOME}/bin/shutdown.sh & >/dev/null 2>&1
if [ "$rc" == 200 ]; then
    echo "Deployed the CAS web application successfully."
    exit 0
else
    echo "Failed to deploy the CAS web application with status $rc."
    exit 1
fi
