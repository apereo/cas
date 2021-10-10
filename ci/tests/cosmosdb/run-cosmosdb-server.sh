#!/bin/bash
docker stop cosmosdb || true && docker rm cosmosdb || true
dockerPlatform=$(docker version --format '{{json .Server.Os}}')
echo "Trying to determine Docker image for platform $dockerPlatform"

if [[ $dockerPlatform =~ "windows" ]]; then
#  docker run --name cosmosdb --memory 2GB \
#    -d --tty -p 8081:8081 -p 8900:8900 -p 8901:8901 -p 8902:8902 -p 10250:10250 -p 10251:10251 \
#    -p 10252:10252 -p 10253:10253 -p 10254:10254 -p 10255:10255 -p 10256:10256 -p 10350:10350 \
#    mcr.microsoft.com/cosmosdb/windows/azure-cosmos-emulator
  echo "Windows platform is not supported for CosmosDb"
  exit 0
fi

if [[ "${CI}" == "true" ]]; then
  echo "Azure CosmosDb emulator for Linux is not quite ready yet."
  echo "There are intermittent out-of-service errors while running with CI."
  echo "The emulator and test suite will be re-enabled once it exits public preview."
  exit 0
fi

ipaddr="$(ifconfig | grep "inet " | grep -Fv 127.0.0.1 | awk '{print $2}' | head -n 1)"
echo "System IP address is $ipaddr"
docker pull mcr.microsoft.com/cosmosdb/linux/azure-cosmos-emulator
docker run --rm -p 8081:8081 -p 10251:10251 -p 10252:10252 -p 10253:10253 -p 10254:10254 \
  -m 4g --cpus=2.0 --name=cosmosdb \
  -e AZURE_COSMOS_EMULATOR_PARTITION_COUNT=4 \
  -e AZURE_COSMOS_EMULATOR_ENABLE_DATA_PERSISTENCE=true \
  -e AZURE_COSMOS_EMULATOR_IP_ADDRESS_OVERRIDE="$ipaddr" \
  -d mcr.microsoft.com/cosmosdb/linux/azure-cosmos-emulator
docker logs -f cosmosdb &
echo "Waiting for CosmosDb to come online..."
sleep 30
docker ps | grep "cosmosdb"
echo "Fetching CosmosDb certificate..."
rm -Rf ./emulatorcert.crt
curl -k https://localhost:8081/_explorer/emulator.pem > emulatorcert.crt
cat emulatorcert.crt
echo "Removing precious certificate from $JAVA_HOME/lib/security/cacerts"
sudo keytool -delete -alias "cosmosdb" -keystore "$JAVA_HOME"/lib/security/cacerts -storepass changeit -noprompt
echo "Adding certificate to $JAVA_HOME/lib/security/cacerts"
sudo keytool -importcert -file ./emulatorcert.crt -keystore "$JAVA_HOME"/lib/security/cacerts -alias "cosmosdb" --storepass changeit -noprompt
rm -Rf ./emulatorcert.crt
sudo keytool -list -keystore "$JAVA_HOME"/lib/security/cacerts -alias "cosmosdb" --storepass changeit
