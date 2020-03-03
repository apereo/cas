#!/bin/bash


gradle="./gradlew "
gradleBuild=""
gradleBuildOptions="--build-cache --configure-on-demand --no-daemon "

webAppServerType="tomcat"

createConfig() {
  local configDir=$1
  local port=${2:-8443}
  local clusterport=${3:-8888}
  local clusterindex=${4:-1}
  echo "Creating config in ${configDir}"
  mkdir -p ${configDir}
  cat > ${configDir}/cas.properties <<EOF
server.port=${port}
server.jetty.accesslog.filename=accesslog${clusterindex}
logging.level.org.apache.catalina.tribes=trace
logging.level.org.apereo.cas.web.CasSessionReplicationVerificationEndpoint=trace
management.endpoints.web.exposure.include=sessionReplicationVerify
cas.monitor.endpoints.endpoint.defaults.access[0]=IP_ADDRESS
cas.monitor.endpoints.endpoint.defaults.requiredIpAddresses[0]=127\\\\.0\\\\.0\\\\.1|0:0:0:0:0:0:0:1
spring.cloud.discovery.client.composite-indicator.enabled=false
management.endpoint.sessionReplicationVerify.enabled=true
# memoryHealthIndicator requires cas-server-core-monitor which is not present by default
cas.server.tomcat.clustering.enabled=true
#cas.server.tomcat.clustering.managerType=BACKUP
cas.server.tomcat.clustering.clusterMembers=127.0.0.1:${clusterport}:${clusterindex}
EOF
cat ${configDir}/cas.properties
}

dumpOutput() {
  local msg=$1
  local outputfile=$2
  echo "Output Start [${msg}]"
  if [[ -f "${outputfile}" ]]; then
    cat ${outputfile}
  else
    echo "File [${outputfile}] not found."
  fi
  echo -e "\nOutput End [${msg}]"
}

testUrl() {
  local uri=$1
  local requiredcontent=$2
  local port=${3:-8443}
  local method=${4:-GET}
  echo "Testing https://localhost:${port}/cas${uri}"
  local output="/tmp/testoutput${port}"
  local cookiejar="/tmp/cookiejar"
  local rc=$(curl --cookie ${cookiejar} --cookie-jar ${cookiejar} --retry 2 --retry-delay 15 --request $method --silent -k --connect-timeout 60 -o ${output} -w "%{http_code}" https://localhost:${port}/cas$uri)
  exitCode=$?
  if [[ $exitCode -eq 0 && "$rc" == "200" ]]; then
    grep --count ${requiredcontent} ${output}
    if [[ $? -eq 0 ]] ; then
      echo "Test of URI ${uri} on port ${port} contained required content"
      return 0
    else
      echo "Test of URI ${uri} on port ${port} failed, it did not contain required content: ${requiredcontent}"
      dumpOutput ${uri} ${output}
      return 2
    fi
  else
    echo "Test of URI ${uri} resulted in status code RC=$rc Exit Code: $exitCode"
    dumpOutput ${uri} ${output}
    return 1
  fi
}

launchServer() {
  servernum=$1
  port=$2
  clusterport=$3

  configDir="/tmp/config${servernum}"
  casOutput="/tmp/logs${servernum}/cas-output.log"
  [ ! -d /tmp/logs${servernum} ] && mkdir /tmp/logs${servernum}
  createConfig ${configDir} ${port} ${clusterport} ${servernum}

  cmd="java -jar -DbaseDir=/tmp/logs${servernum} webapp/cas-server-webapp-${webAppServerType}/build/libs/cas.war \\
    --server.ssl.key-store=${keystore} --cas.standalone.configurationDirectory=${configDir}"
  echo "Running: $cmd"
  exec $cmd > ${casOutput} 2>&1 &
  echo $! > /tmp/server${servernum}.pid
}


echo -e "***********************************************"
echo -e "Gradle build started at `date` for web application server ${webAppServerType}"
echo -e "***********************************************"

gradleBuild="$gradleBuild :webapp:cas-server-webapp-${webAppServerType}:build -x check -x test -x javadoc -DskipNestedConfigMetadataGen=true "

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
        cp webapp/cas-server-webapp-"${webAppServerType}"/build/libs/cas-server-webapp-"${webAppServerType}"-*.war \
          webapp/cas-server-webapp-"${webAppServerType}"/build/libs/cas.war

        dname="${dname:-CN=cas.example.org,OU=Example,OU=Org,C=US}"
        subjectAltName="${subjectAltName:-dns:example.org,dns:localhost,ip:127.0.0.1}"
        keystore="./thekeystore"
        echo "Generating keystore ${keystore} for CAS with DN=${dname}, SAN=${subjectAltName}"
        [ -f "${keystore}" ] && rm "${keystore}"
        keytool -genkey -noprompt -alias cas -keyalg RSA -keypass changeit -storepass changeit \
          -keystore "${keystore}" -dname "${dname}" -ext SAN="${subjectAltName}"

        launchServer 1 8443 8888
        pid=`cat /tmp/server1.pid`
        echo "Launched CAS with pid ${pid}. Waiting for CAS server to come online..."
        launchServer 2 8444 8889
        pid2=`cat /tmp/server2.pid`
        echo "Launched CAS with pid ${pid2}. Waiting for CAS server to come online..."

        sleep 60
        echo "Testing status of server with pid ${pid}."
        testUrl "/login" "Username"
        retValLogin=$?
        echo "Testing status of server with pid ${pid2}."
        testUrl "/login" "Username" 8444
        retValLogin2=$?
        testUrl "/actuator/sessionReplicationVerify" "."
        retValSession=$?
        testUrl "/actuator/sessionReplicationVerify/testkey/theattribute" "written" 8443 POST
        retValSession2=$?
        testUrl "/actuator/sessionReplicationVerify/testkey" "theattribute"
        retValSession3=$?
        testUrl "/actuator/sessionReplicationVerify/testkey" "theattribute" 8444
        retValSession4=$?

        [[ ${retValLogin} -eq 0 ]] && \
        [[ ${retValLogin2} -eq 0 ]] && \
        [[ ${retValSession} -eq 0 ]] && \
        [[ ${retValSession2} -eq 0 ]] && \
        [[ ${retValSession3} -eq 0 ]] && \
        [[ ${retValSession4} -eq 0 ]]
        retVal=$?
        if [[ ${retVal} -ne 0 ]]; then
          dumpOutput cas1.log /tmp/logs1/cas-output.log
          dumpOutput cas2.log /tmp/logs2/cas-output.log
        fi
        echo "Killing pid ${pid}"
        kill -9 "${pid}"
        echo "Killing pid ${pid2}"
        kill -9 "${pid2}"
        [ -f "${keystore}" ] && rm "${keystore}"
        [ -d "${configDir}" ] && rm -rf "${configDir}"
        [ -f "${cookiejar}" ] && rm "${cookiejar}"
        exit $retVal
    else
        echo "Gradle build did NOT finish successfully."
        exit $retVal
    fi
fi
