#!/bin/bash
docker stop cosmosdb || true && docker rm cosmosdb || true
ipaddr="$(ifconfig | grep "inet " | grep -Fv 127.0.0.1 | awk '{print $2}' | head -n 1)"
echo "System IP address is $ipaddr"
docker pull mcr.microsoft.com/cosmosdb/linux/azure-cosmos-emulator
docker run --rm -p 8081:8081 -p 10251:10251 -p 10252:10252 -p 10253:10253 -p 10254:10254 \
  -m 4g --cpus=2.0 --name=cosmosdb \
  -e AZURE_COSMOS_EMULATOR_PARTITION_COUNT=4 \
  -e AZURE_COSMOS_EMULATOR_ENABLE_DATA_PERSISTENCE=true \
  -e AZURE_COSMOS_EMULATOR_IP_ADDRESS_OVERRIDE="$ipaddr" \
  -d mcr.microsoft.com/cosmosdb/linux/azure-cosmos-emulator

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
