#!/bin/bash

webAppServerType="$1"

createConfig() {
  configDir=$1
  echo "Creating config in ${configDir}"
  mkdir -p ${configDir}
  cat > ${configDir}/cas.properties <<EOF
management.endpoints.web.exposure.include=health,info,env,loggers
management.endpoint.health.enabled=true
management.endpoint.info.enabled=true
cas.monitor.endpoints.endpoint.defaults.access[0]=IP_ADDRESS
cas.monitor.endpoints.endpoint.defaults.requiredIpAddresses[0]=127\\\\.0\\\\.0\\\\.1|0:0:0:0:0:0:0:1
management.endpoint.health.show-details=always
spring.cloud.discovery.client.composite-indicator.enabled=false
management.health.defaults.enabled=false
management.health.ping.enabled=true
management.health.diskSpace.enabled=true
management.endpoint.env.enabled=true
management.endpoint.loggers.enabled=true
# memoryHealthIndicator requires cas-server-core-monitor which is not present by default
management.health.memoryHealthIndicator.enabled=true
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
  echo "Testing https://localhost:8443/cas$uri"
  local output="/tmp/testoutput"
  rc=`curl --silent -k --connect-timeout 60 -o ${output} -w "%{http_code}" https://localhost:8443/cas$uri`
  if [ "$rc" == 200 ]; then
    grep --count ${requiredcontent} ${output}
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
casOutput="/tmp/logs/cas.log"
[ ! -d /tmp/logs ] && mkdir /tmp/logs
createConfig ${configDir}

cmd="java -jar webapp/cas-server-webapp-${webAppServerType}/build/libs/cas.war \\
  --server.ssl.key-store=${keystore} --cas.standalone.configurationDirectory=${configDir}"
exec $cmd > ${casOutput} 2>&1 &
pid=$!
echo "Launched CAS with pid ${pid}. Waiting for CAS server to come online..."
sleep 60
echo "Testing status of server with pid ${pid}."
testUrl "/login" "Username"
retValLogin=$?
testUrl "/actuator/health" "UP"
retValHealth=$?
testUrl "/actuator/health/ping" "{\"status\":\"UP\"}"
retValPing=$?
testUrl "/actuator/info" "java"
retValInfo=$?
testUrl "/actuator/loggers" "FATAL"
retValLoggers=$?
testUrl "/actuator/env" "systemProperties"
retValEnv=$?
[[ ${retValLogin} -eq 0 ]] && \
[[ ${retValHealth} -eq 0 ]] && \
[[ ${retValPing} -eq 0 ]] && \
[[ ${retValInfo} -eq 0 ]] && \
[[ ${retValLoggers} -eq 0 ]] && \
[[ ${retValEnv} -eq 0 ]]
retVal=$?
if [[ ${retVal} -ne 0 ]]; then
  dumpOutput cas.log ${casOutput}
fi
kill -9 "${pid}"
[ -f "${keystore}" ] && rm "${keystore}"
[ -d "${configDir}" ] && rm -rf "${configDir}"
[ -f "${casOutput}" ] && rm "${casOutput}"
exit $retVal
