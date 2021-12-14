echo "Starting CAS client application"

if [[ ! -f /tmp/bootiful-cas-client.jar ]] ; then
  echo "Downloading Bootiful CAS client..."
  (cd /tmp && curl -OL https://github.com/apereo/bootiful-cas-client/releases/download/v1.4.0/bootiful-cas-client.jar)
fi

cat > /tmp/sslConfig.properties <<- EOM
ignoreSslFailures=true
EOM

cat /tmp/sslConfig.properties
java -jar /tmp/bootiful-cas-client.jar \
  --server.ssl.key-store="${CAS_KEYSTORE}" \
  --cas.ssl-config-file=/tmp/sslConfig.properties &
pid=$!
echo "CAS client application process id $pid"
sleep 5
until $(curl -k --output /dev/null --silent --head --fail https://localhost:8444); do
    printf '.'
    sleep 1
done
echo "Started CAS client application..."
