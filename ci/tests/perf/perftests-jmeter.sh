#!/bin/bash
clear

RED="\e[31m"
GREEN="\e[32m"
YELLOW="\e[33m"
ENDCOLOR="\e[0m"

function printred() {
  printf "🔥 ${RED}$1${ENDCOLOR}\n"
}
function printgreen() {
  printf "☘️  ${GREEN}$1${ENDCOLOR}\n"
}

tmp="${TMPDIR}"
if [[ -z "${tmp}" ]]; then
  tmp="/tmp"
fi
export TMPDIR=${tmp}
echo "Using temp directory: ${TMPDIR}"

jmeterVersion=5.6.3
gradle="./gradlew "
gradleBuild=""
gradleBuildOptions="--build-cache --configure-on-demand --no-daemon --parallel --max-workers=8 --no-configuration-cache "
webAppServerType="$1"
testCategory="${2:-cas}"

casProperties=""
case "$testCategory" in
  saml)
    rm -Rf "${PWD}"/ci/tests/perf/saml/md/*
    casProperties="--cas.authn.saml-idp.core.entity-id=https://cas.apereo.org/saml/idp"
    casProperties="${casProperties} --cas.authn.saml-idp.metadata.file-system.location=file://${PWD}/ci/tests/perf/saml/md"
    casProperties="${casProperties} --cas.service-registry.json.location=file://${PWD}/ci/tests/perf/saml/services"
    casProperties="${casProperties} --cas.http-client.host-name-verifier=none "
    casProperties="${casProperties} --spring.main.lazy-initialization=false "
    jmeterScript="etc/loadtests/jmeter/CAS_SAML2.jmx"
    casModules="saml-idp,reports"
    ;;
  oidc)
    casProperties="--cas.authn.oidc.core.issuer=https://localhost:8443/cas/oidc "
    casProperties="${casProperties} --cas.service-registry.json.location=file://${PWD}/ci/tests/perf/oidc/services "
    casProperties="${casProperties} --cas.authn.oidc.jwks.file-system.jwks-file=file://${PWD}/ci/tests/perf/oidc/keystore.jwks "
    casProperties="${casProperties} --spring.main.lazy-initialization=false "
    jmeterScript="etc/loadtests/jmeter/CAS_OIDC.jmx"
    casModules="oidc,reports"
    ;;
  cas)
    jmeterScript="etc/loadtests/jmeter/CAS_CAS.jmx"
    ;;
esac

retVal=0
echo -e "**********************************************************"
echo -e "Build started at $(date) for test category ${testCategory}"
echo -e "**********************************************************"
gradleBuild="$gradleBuild clean :webapp:cas-server-webapp-${webAppServerType}:build -x check -x test -x javadoc --no-configuration-cache -DskipNestedConfigMetadataGen=true -DcasModules=${casModules} "
tasks="$gradle $gradleBuildOptions $gradleBuild"
printgreen "$tasks"
echo -e "\n***************************************************************************************\n"
eval "$tasks"
retVal=$?

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
  printgreen "Launching CAS web application ${webAppServerType} server with properties [${casProperties}] \n"
  casOutput="/tmp/cas.log"

  "${PWD}"/ci/tests/httpbin/run-httpbin-server.sh
  
  # -Xdebug -Xrunjdwp:transport=dt_socket,address=*:5000,server=y,suspend=n
  echo "Properties: ${casProperties}"
  java -Dlog.console.stacktraces=true \
      -jar webapp/cas-server-webapp-"${webAppServerType}"/build/libs/cas.war \
      --server.ssl.key-store=${keystore} \
      --cas.service-registry.core.init-from-json=true \
      --cas.server.name=https://localhost:8443 \
      --cas.server.prefix=https://localhost:8443/cas \
      --cas.audit.engine.enabled=false \
      --spring.profiles.active=none \
      --cas.http-client.allow-local-urls=true \
      --cas.audit.slf4j.use-single-line=true \
      --cas.monitor.endpoints.endpoint.defaults.access=ANONYMOUS \
      --management.endpoints.web.exposure.include=* \
      --management.endpoints.access.default=UNRESTRICTED \
      --logging.level.org.apereo.cas=info ${casProperties} &
  pid=$!
  printgreen "Launched CAS with pid ${pid} with modules ${casModules}. Waiting for CAS server to come online..."
  sleep 15
  until curl -k -L --output /dev/null --silent --fail https://localhost:8443/cas/login; do
    echo -n '.'
    sleep 2
  done
#  curl -k -H "Content-Type:application/json" \
#    -X POST "https://localhost:8443/cas/actuator/loggers/org.apereo.cas" \
#    -d '{"configuredLevel": "DEBUG"}'
  printgreen "\n\nReady!\n"

  case "$testCategory" in
    saml)
      cert=$(cat "${PWD}/ci/tests/perf/saml/md/idp-signing.crt" | sed 's/-----BEGIN CERTIFICATE-----//g' | sed 's/-----END CERTIFICATE-----//g')
      export IDP_SIGNING_CERTIFICATE=$cert
      echo -e "Using signing certificate:\n$IDP_SIGNING_CERTIFICATE"
      cert=$(cat "${PWD}/ci/tests/perf/saml/md/idp-encryption.crt" | sed 's/-----BEGIN CERTIFICATE-----//g' | sed 's/-----END CERTIFICATE-----//g')
      export IDP_ENCRYPTION_CERTIFICATE=$cert
      echo -e "Using encryption certificate:\n$IDP_ENCRYPTION_CERTIFICATE"
      "${PWD}/ci/tests/saml2/run-saml-server.sh"
      ;;
  esac
  
#  read -r
  
  if [[ ! -f "/tmp/apache-jmeter-${jmeterVersion}.zip" ]]; then
      printgreen "Downloading JMeter ${jmeterVersion}..."
      curl -o /tmp/apache-jmeter-${jmeterVersion}.zip \
        -L https://downloads.apache.org/jmeter/binaries/apache-jmeter-${jmeterVersion}.zip
      rm -rf /tmp/apache-jmeter-${jmeterVersion}
      unzip -q -d /tmp /tmp/apache-jmeter-${jmeterVersion}.zip
      printgreen "Unzipped /tmp/apache-jmeter-${jmeterVersion}.zip rc=$?"
      chmod +x /tmp/apache-jmeter-${jmeterVersion}/bin/jmeter
    echo "jmeter.save.saveservice.output_format=xml" >> /tmp/apache-jmeter-${jmeterVersion}/bin/jmeter.properties
    echo "jmeter.save.saveservice.response_data=true" >> /tmp/apache-jmeter-${jmeterVersion}/bin/jmeter.properties
  fi

  clear
  echo -e "***************************************************************************************"
  printgreen "Running JMeter tests via ${jmeterScript}..."
  export HEAP="-Xms1g -Xmx6g -XX:MaxMetaspaceSize=512m"
  /tmp/apache-jmeter-${jmeterVersion}/bin/jmeter -l /tmp/jmeter-results.xml -n -t "${jmeterScript}" >results.log
  echo -n "JMeter results are written to " && ls /tmp/jmeter-results.xml
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
