#!/bin/bash

function downloadTomcat() {
  tomcatVersion="9.0.43"
  tomcatVersionTag="v${tomcatVersion}"
  tomcatUrl="https://downloads.apache.org/tomcat/tomcat-9/${tomcatVersionTag}/bin/apache-tomcat-${tomcatVersion}.zip"

  export CATALINA_HOME=./apache-tomcat-${tomcatVersion}
  rm -Rf ${CATALINA_HOME}
  wget --no-check-certificate ${tomcatUrl}
  unzip apache-tomcat-${tomcatVersion}.zip
  chmod +x ${CATALINA_HOME}/bin/*.sh
  touch ${CATALINA_HOME}/logs/catalina.out ; tail -F ${CATALINA_HOME}/logs/catalina.out &
}

