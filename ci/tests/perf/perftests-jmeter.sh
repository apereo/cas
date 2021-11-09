#!/bin/bash

jmeterVersion=5.3
gradle="./gradlew "
gradleBuild=""
gradleBuildOptions="--build-cache --configure-on-demand --no-daemon --parallel "
webAppServerType="$1"

echo -e "***********************************************"
echo -e "Build started at $(date)"
echo -e "***********************************************"
gradleBuild="$gradleBuild :webapp:cas-server-webapp-${webAppServerType}:build -x check -x test -x javadoc -DskipNestedConfigMetadataGen=true "
tasks="$gradle $gradleBuildOptions $gradleBuild"
echo $tasks
echo -e "***************************************************************************************"
eval $tasks
retVal=$?

if [ $retVal == 0 ]; then
  echo -e "Gradle build finished successfully.\nPreparing CAS web application WAR artifact..."
  mv webapp/cas-server-webapp-"${webAppServerType}"/build/libs/cas-server-webapp-"${webAppServerType}"-*-SNAPSHOT.war \
    webapp/cas-server-webapp-"${webAppServerType}"/build/libs/cas.war

  if [ $? -eq 1 ]; then
    echo "Unable to build or locate the CAS web application file. Aborting test..."
    exit 1
  fi

  dname="${dname:-CN=cas.example.org,OU=Example,OU=Org,C=US}"
  subjectAltName="${subjectAltName:-dns:example.org,dns:localhost,ip:127.0.0.1}"
  keystore="./thekeystore"
  echo "Generating keystore ${keystore} for CAS with DN=${dname}, SAN=${subjectAltName}"
  [ -f "${keystore}" ] && rm "${keystore}"
  keytool -genkey -noprompt -alias cas -keyalg RSA -keypass changeit -storepass changeit \
    -keystore "${keystore}" -dname "${dname}" -ext SAN="${subjectAltName}"
  echo "Launching CAS web application ${webAppServerType} server..."
  casOutput="/tmp/cas.log"
  cmd="java -jar webapp/cas-server-webapp-${webAppServerType}/build/libs/cas.war \\
      --server.ssl.key-store=${keystore} --cas.service-registry.core.init-from-json=true \\
      --spring.profiles.active=none --logging.level.org.apereo.cas=info"
  exec $cmd >${casOutput} 2>&1 &
  pid=$!
  echo "Launched CAS with pid ${pid}. Waiting for CAS server to come online..."
  until curl -k -L --output /dev/null --silent --fail https://localhost:8443/cas/login; do
    echo -n '.'
    sleep 2
  done
  echo -e "\n\nReady!"

  echo -e "***************************************************************************************"
  echo "CAS server output before tests have started:"
  cat ${casOutput}
  echo -e "***************************************************************************************"

  sudo mkdir -p /etc/cas/config/loadtests/jmeter/
  sudo cp etc/loadtests/jmeter/cas-users.csv /etc/cas/config/loadtests/jmeter/
  sudo chmod -R ugo+r /etc/cas/config/loadtests
  echo "Copied users file" && cat /etc/cas/config/loadtests/jmeter/cas-users.csv

  curl -LO https://downloads.apache.org/jmeter/binaries/apache-jmeter-${jmeterVersion}.zip
  unzip -q apache-jmeter-${jmeterVersion}.zip
  echo Unzipped apache-jmeter-${jmeterVersion}.zip rc=$?
  chmod +x apache-jmeter-${jmeterVersion}/bin/jmeter

  echo -e "***************************************************************************************"
  echo "Running JMeter tests..."
  apache-jmeter-${jmeterVersion}/bin/jmeter -n -t etc/loadtests/jmeter/CAS_CAS.jmx >results.log
  echo -e "***************************************************************************************"
  echo "CAS server warnings and errors:"
  grep -E WARN\|ERROR\|FATAL ${casOutput}
  echo -e "***************************************************************************************"

  java ci/tests/perf/EvalJMeterTestResults.java ./results.log

  retVal=$?

  echo -e "***************************************************************************************"
  echo -e "Gradle build finished at $(date) with exit code $retVal"
  echo -e "***************************************************************************************"

  if [ $retVal == 0 ]; then
    echo "Gradle build finished successfully."
  else
    echo "Gradle build did NOT finish successfully."
  fi

  kill -9 "${pid}"
  [ -f "${keystore}" ] && rm "${keystore}"
  [ -f "${casOutput}" ] && rm "${casOutput}"
  exit $retVal
else
  echo "Gradle build did NOT finish successfully."
  exit $retVal
fi
