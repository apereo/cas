#!/bin/bash

gradle="./gradlew "
gradleBuild=""
gradleBuildOptions="--build-cache --configure-on-demand --no-daemon "

webAppServerType="$1"

createConfig() {
  configDir=$1
  mkdir -p ${configDir}
  cat > ${configDir}/cas.properties <<EOF
management.endpoints.web.exposure.include=status,health
management.endpoint.status.enabled=true
management.endpoint.health.enabled=true
cas.monitor.endpoints.endpoint.defaults.access[0]=IP_ADDRESS
cas.monitor.endpoints.endpoint.defaults.requiredIpAddresses[0]=127\\.0\\.0\\.1
management.endpoint.health.show-details=always
management.health.memoryHealthIndicator.enabled=true
EOF
}

dumpOutput() {
  msg=$1
  file=$2
echo "Output Start [$msg]"
cat $file
echo "Output End [$msg]"
}

testUrl() {
  uri=$1
  requiredcontent=$2
  echo "Testing https://localhost:8443/cas$uri"
  output=/tmp/output
  rc=`curl -k --connect-timeout 60 -o ${output} -w "%{http_code}" https://localhost:8443/cas$uri`
  if [ "$rc" == 200 ]; then
    grep ${requiredcontent} ${output}
    if [[ $? -eq 0 ]] ; then
      echo "Test of URI ${uri} contained required content"
      return 0
    else
      echo "Test of URI ${uri} failed, it did not contain required content: ${requiredcontent}"
      dumpOutput ${uri} ${output}
      return 2
    fi
  else
    echo "Test of URI ${uri} resulted in status code RC=$rc"
    dumpOutput ${uri} ${output}
    return 1
  fi
}

echo -e "***********************************************"
echo -e "Gradle build started at `date` for web application server ${webAppServerType}"
echo -e "***********************************************"

gradleBuild="$gradleBuild :webapp:cas-server-webapp-${webAppServerType}:build -x check -x test -x javadoc -DskipNestedConfigMetadataGen=true -DskipGradleLint=true "

if [[ "${TRAVIS_COMMIT_MESSAGE}" == *"[show streams]"* ]]; then
    gradleBuild="$gradleBuild -DshowStandardStreams=true "
fi

if [[ "${TRAVIS_COMMIT_MESSAGE}" == *"[rerun tasks]"* ]]; then
    gradleBuild="$gradleBuild --rerun-tasks "
fi

if [[ "${TRAVIS_COMMIT_MESSAGE}" == *"[refresh dependencies]"* ]]; then
    gradleBuild="$gradleBuild --refresh-dependencies "
fi

if [ -z "$gradleBuild" ]; then
    echo "Gradle build will be ignored since no commands are specified to run."
else
    tasks="$gradle $gradleBuildOptions $gradleBuild"
    echo -e "***************************************************************************************"

    echo $tasks
    echo -e "***************************************************************************************"

    eval $tasks
    retVal=$?

    echo -e "***************************************************************************************"
    echo -e "Gradle build finished at `date` with exit code $retVal"
    echo -e "***************************************************************************************"

    if [ $retVal == 0 ]; then
        echo -e "Gradle build finished successfully.\nPreparing CAS web application WAR artifact..."
        mv webapp/cas-server-webapp-"${webAppServerType}"/build/libs/cas-server-webapp-"${webAppServerType}"-*.war \
          webapp/cas-server-webapp-"${webAppServerType}"/build/libs/cas.war

        dname="${dname:-CN=cas.example.org,OU=Example,OU=Org,C=US}"
        subjectAltName="${subjectAltName:-dns:example.org,dns:localhost,ip:127.0.0.1}"
        keystore="./thekeystore"
        echo "Generating keystore ${keystore} for CAS with DN=${dname}, SAN=${subjectAltName}"
        [ -f "${keystore}" ] && rm "${keystore}"
        keytool -genkey -noprompt -alias cas -keyalg RSA -keypass changeit -storepass changeit \
          -keystore "${keystore}" -dname "${dname}" -ext SAN="${subjectAltName}"

        echo "Launching CAS web application ${webAppServerType} server..."
        configDir="/tmp/config"
        createConfig ${configDir}
        java -jar webapp/cas-server-webapp-"${webAppServerType}"/build/libs/cas.war \
          --server.ssl.key-store="${keystore}" --cas.standalone.configurationDirectory=${configDir} &> /dev/null &
        pid=$!
        echo "Launched CAS with pid ${pid}. Waiting for CAS server to come online..."
        sleep 60
        echo "Testing status of server with pid ${pid}."
        testUrl "/login" "Username" && testUrl "/actuator/health" "UP" && testUrl "/actuator/status" "UP"
        retVal=$?
        kill -9 "${pid}"
        [ -f "${keystore}" ] && rm "${keystore}"
        [ -d "${configDir}" ] && rm -rf "${configDir}"
        exit $retVal
    else
        echo "Gradle build did NOT finish successfully."
        exit $retVal
    fi
fi

}
}