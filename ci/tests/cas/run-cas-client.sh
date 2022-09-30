#echo "Starting CAS client application"
#
#KEYSTORE=${CAS_KEYSTORE:-../../overlay/thekeystore}
#if [[ -z $TMPDIR ]]; then
#  TMPDIR=${TEMP:-/tmp}
#fi
#cat > "${TMPDIR}"/sslconfig.properties <<- EOM
#ignoreSslFailures=true
#EOM
#
#echo "SSL configuration file: ${TMPDIR}/sslconfig.properties"
#cat ${TMPDIR}/sslconfig.properties
#
#docker stop casclient || true && docker rm casclient || true
#echo -e "Mapping CAS keystore in Docker container to ${CAS_KEYSTORE}"
#docker run --rm -d \
#  --mount type=bind,source="${CAS_KEYSTORE}",target=/etc/cas/thekeystore \
#  --mount type=bind,source="${TMPDIR}/sslconfig.properties",target=/etc/cas/config/sslconfig.properties \
#  -p 8444:8444 \
#  --name casclient apereo/bootiful-cas-client
#
#docker logs -f casclient &
#sleep 15
#counter=0
#until $(curl -k --output /dev/null --silent --head --fail https://localhost:8444); do
#    let counter++
#    printf '.'
#    sleep 2
#    if [[ $counter -gt 45 ]] ; then
#      rm ${TMPDIR}/bootiful-cas-client.jar
#      echo "Timed out waiting for client application."
#      exit 1
#    fi
#done
#echo "Started CAS client application..."

echo "Starting CAS client application"

KEYSTORE=${CAS_KEYSTORE:-../../overlay/thekeystore}

if [[ -z $TMPDIR ]]; then
  TMPDIR=${TEMP:-/tmp}
fi

if [[ ! -f ${TMPDIR}/bootiful-cas-client.jar ]] ; then
  echo "Downloading Bootiful CAS client..."
  curl -L https://github.com/apereo/bootiful-cas-client/releases/download/v1.4.0/bootiful-cas-client.jar -o ${TMPDIR}/bootiful-cas-client.jar
fi

cat > ${TMPDIR}/sslConfig.properties <<- EOM
ignoreSslFailures=true
EOM

cat ${TMPDIR}/sslConfig.properties
java -jar ${TMPDIR}/bootiful-cas-client.jar \
  --management.endpoints.web.exposure.include=* \
  --management.endpoint.shutdown.enabled=true \
  --endpoints.shutdown.enabled=true \
  --server.ssl.key-store="${KEYSTORE}" \
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
