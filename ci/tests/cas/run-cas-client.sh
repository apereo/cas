#!/bin/bash
echo "Starting CAS client application"

KEYSTORE=${CAS_KEYSTORE:-../../overlay/thekeystore}

if [[ -z $TMPDIR ]]; then
  TMPDIR=${TEMP:-/tmp}
fi

if [[ ! -f ${TMPDIR}/bootiful-cas-client.jar ]] ; then
  echo "Downloading Bootiful CAS client..."
  curl -L https://github.com/apereo/bootiful-cas-client/releases/download/v1.4.1/bootiful-cas-client.jar -o "${TMPDIR}"/bootiful-cas-client.jar
fi

cat > ${TMPDIR}/sslConfig.properties <<- EOM
ignoreSslFailures=true
EOM

cat ${TMPDIR}/sslConfig.properties
#java -Xdebug -Xrunjdwp:transport=dt_socket,address=*:8000,server=y,suspend=n \
java -jar ${TMPDIR}/bootiful-cas-client.jar \
  --management.endpoints.web.exposure.include=* \
  --management.endpoint.shutdown.access=UNRESTRICTED \
  --endpoints.shutdown.enabled=true \
  --server.ssl.key-store="${KEYSTORE}" \
  --cas.single-logout.enabled=true \
  --cas.ssl-config-file=${TMPDIR}/sslConfig.properties &
pid=$!
echo "CAS client application process id $pid"
sleep 5
counter=0
until $(curl -k --output /dev/null --silent --head --fail https://localhost:8444); do
    let counter++
    printf '.'
    sleep 1
    if [[ $counter -gt 30 ]] ; then
      rm ${TMPDIR}/bootiful-cas-client.jar
      echo "Timed out waiting for client application."
      exit 1
    fi
done
echo "Started CAS client application..."
