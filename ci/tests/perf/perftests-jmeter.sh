#!/bin/bash
clear

RED="\e[31m"
GREEN="\e[32m"
YELLOW="\e[33m"
ENDCOLOR="\e[0m"

function printred() {
  printf "${RED}$1${ENDCOLOR}\n"
}
function printgreen() {
  printf "${GREEN}$1${ENDCOLOR}\n"
}

jmeterVersion=5.4.3
gradle="./gradlew "
gradleBuild=""
gradleBuildOptions="--build-cache --configure-on-demand --no-daemon --parallel --max-workers=8 "
webAppServerType="$1"
testCategory="${2:-cas}"

casProperties=""
case "$testCategory" in
  oidc)
    casProperties="--cas.authn.oidc.core.issuer=https://localhost:8443/cas/oidc --cas.service-registry.json.location=file://${PWD}/ci/tests/perf/oidc/services"
    jmeterScript="etc/loadtests/jmeter/CAS_OIDC.jmx"
    casModules="oidc,reports"
    ;;
  cas)
    jmeterScript="etc/loadtests/jmeter/CAS_CAS.jmx"
    ;;
esac

retVal=0
if [[ ! -f webapp/cas-server-webapp-"${webAppServerType}"/build/libs/cas.war ]]; then
  echo -e "***********************************************"
  echo -e "Build started at $(date)"
  echo -e "***********************************************"
  gradleBuild="$gradleBuild :webapp:cas-server-webapp-${webAppServerType}:build -x check -x test -x javadoc
  -DskipNestedConfigMetadataGen=true -DcasModules=${casModules} "
  tasks="$gradle $gradleBuildOptions $gradleBuild"
  echo $tasks
  echo -e "***************************************************************************************"
  eval $tasks
  retVal=$?
fi

if [ $retVal == 0 ]; then
  printgreen "Gradle build finished successfully.\nPreparing CAS web application WAR artifact..."
  if [[ ! -f webapp/cas-server-webapp-"${webAppServerType}"/build/libs/cas.war ]]; then
    mv webapp/cas-server-webapp-"${webAppServerType}"/build/libs/cas-server-webapp-"${webAppServerType}"-*-SNAPSHOT.war \
      webapp/cas-server-webapp-"${webAppServerType}"/build/libs/cas.war
  fi

  if [ $? -eq 1 ]; then
    printred "Unable to build or locate the CAS web application file. Aborting test..."
    exit 1
  fi

  dname="${dname:-CN=cas.example.org,OU=Example,OU=Org,C=US}"
  subjectAltName="${subjectAltName:-dns:example.org,dns:localhost,ip:127.0.0.1}"
  keystore="./thekeystore"
  printgreen "Generating keystore ${keystore} for CAS with DN=${dname}, SAN=${subjectAltName}"
  [ -f "${keystore}" ] && rm "${keystore}"
  keytool -genkey -noprompt -alias cas -keyalg RSA -keypass changeit -storepass changeit \
    -keystore "${keystore}" -dname "${dname}" -ext SAN="${subjectAltName}"
  printgreen "Launching CAS web application ${webAppServerType} server with properties [${casProperties}]"
  casOutput="/tmp/cas.log"

  # -Xdebug -Xrunjdwp:transport=dt_socket,address=*:5000,server=y,suspend=n
  java -jar webapp/cas-server-webapp-"${webAppServerType}"/build/libs/cas.war \
      --server.ssl.key-store=${keystore} --cas.service-registry.core.init-from-json=true \
      --cas.server.name=https://localhost:8443 --cas.server.prefix=https://localhost:8443/cas \
      --cas.audit.engine.enabled=true --spring.profiles.active=none \
      --cas.monitor.endpoints.endpoint.defaults.access=ANONYMOUS \
      --management.endpoints.web.exposure.include=* \
      --management.endpoints.enabled-by-default=true \
      --logging.level.org.apereo.cas=warn ${casProperties} &
  pid=$!
  printgreen "Launched CAS with pid ${pid}. Waiting for CAS server to come online..."
  until curl -k -L --output /dev/null --silent --fail https://localhost:8443/cas/login; do
    echo -n '.'
    sleep 2
  done
  printgreen -e "\n\nReady!"

  if [[ ! -f "/tmp/apache-jmeter-${jmeterVersion}.zip" ]]; then
      printgreen "Downloading JMeter ${jmeterVersion}..."
      curl -o /tmp/apache-jmeter-${jmeterVersion}.zip \
        -L https://downloads.apache.org/jmeter/binaries/apache-jmeter-${jmeterVersion}.zip
      rm -rf /tmp/apache-jmeter-${jmeterVersion}
      unzip -q -d /tmp /tmp/apache-jmeter-${jmeterVersion}.zip
      printgreen "Unzipped /tmp/apache-jmeter-${jmeterVersion}.zip rc=$?"
      chmod +x /tmp/apache-jmeter-${jmeterVersion}/bin/jmeter
  fi
  
  echo -e "***************************************************************************************"
  printgreen "Running JMeter tests via ${jmeterScript}..."
  export HEAP="-Xms1g -Xmx4g -XX:MaxMetaspaceSize=512m"
  /tmp/apache-jmeter-${jmeterVersion}/bin/jmeter -n -t "${jmeterScript}" >results.log
  echo -e "***************************************************************************************"

  java ci/tests/perf/EvalJMeterTestResults.java ./results.log
  retVal=$?

  echo -e "***************************************************************************************"
  echo -e "Gradle build finished at $(date) with exit code $retVal"
  echo -e "***************************************************************************************"

  if [ $retVal == 0 ]; then
    printgreen "Gradle build finished successfully."
  else
    printred "Gradle build did NOT finish successfully."
  fi

  kill -9 "${pid}"
  [ -f "${keystore}" ] && rm "${keystore}"
  [ -f "${casOutput}" ] && rm "${casOutput}"
  exit $retVal
else
  printred "Gradle build did NOT finish successfully."
  exit $retVal
fi
