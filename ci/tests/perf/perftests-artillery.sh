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

gradle="./gradlew "
gradleBuild=""
gradleBuildOptions="--build-cache --configure-on-demand --no-daemon --parallel --max-workers=8 --no-configuration-cache "
webAppServerType="${1:-tomcat}"
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
    artilleryScript="etc/loadtests/artillery/saml.yml"
    casModules="saml-idp,reports"
    ;;
  oidc)
    casProperties="--cas.authn.oidc.core.issuer=https://localhost:8443/cas/oidc "
    casProperties="${casProperties} --cas.service-registry.json.location=file://${PWD}/ci/tests/perf/oidc/services "
    casProperties="${casProperties} --cas.authn.oidc.jwks.file-system.jwks-file=file://${PWD}/ci/tests/perf/oidc/keystore.jwks "
    casProperties="${casProperties} --spring.main.lazy-initialization=false "
    artilleryScript="etc/loadtests/artillery/oidc.yml"
    casModules="oidc,reports"
    ;;
  cas)
    artilleryScript="etc/loadtests/artillery/cas.yml"
    ;;
esac

retVal=0
echo -e "******************************************************************************************"
printgreen "Build started at $(date) for test category ${testCategory} and web application server ${webAppServerType}\n"
echo -e "******************************************************************************************"
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
      --spring.profiles.active=none \
      --server.ssl.key-store=${keystore} \
      --cas.service-registry.core.init-from-json=true \
      --cas.server.name=https://localhost:8443 \
      --cas.server.prefix=https://localhost:8443/cas \
      --cas.audit.engine.enabled=false \
      --spring.profiles.active=none \
      --cas.audit.slf4j.use-single-line=true \
      --cas.http-client.allow-local-urls=true \
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

  printgreen "Installing Artillery...\n"
  npm install -g artillery
  artillery --version
  printgreen "Running Artillery tests via ${artilleryScript}...\n"
  artillery run "${artilleryScript}" --insecure --output /tmp/artillery.json
  failedUsers=$(< /tmp/artillery.json jq -r '.aggregate.counters."vusers.failed"')
  if [ "$failedUsers" -gt 0 ]; then
    printred "Artillery tests failed with ${failedUsers} failed users.\n"
    retVal=1
  else
    printgreen "Artillery tests completed successfully.\n"
    retVal=0
  fi

  echo -e "***************************************************************************************"
  echo -e "Gradle build finished at $(date) with exit code $retVal"
  echo -e "***************************************************************************************"

  if [ $retVal == 0 ]; then
    printgreen "Gradle build finished successfully.\n"
  else
    printred "Gradle build did NOT finish successfully.\n"
  fi

  kill -9 "${pid}"
  [ -f "${keystore}" ] && rm "${keystore}"
  [ -f "${casOutput}" ] && rm "${casOutput}"
  exit $retVal
else
  printred "Gradle build did NOT finish successfully."
  exit $retVal
fi
