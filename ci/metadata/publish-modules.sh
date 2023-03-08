#!/bin/bash

if [[ -z $CAS_PROJECT_DB_URL_ENDPOINT || -z $CAS_PROJECT_DB_API_KEY ]]; then
  echo "Unable to execute without proper credentials"
  exit 1
fi

echo "Running Gradle build to determine CAS version..."
casVersion=$(./gradlew casVersion --no-configuration-cache --no-daemon -q)
if [ $? -eq 1 ]; then
  echo "Unable to determine CAS version. Aborting..."
  exit 1
fi

echo "Publishing CAS modules for CAS version ${casVersion}"
./gradlew --no-configuration-cache --build-cache --configure-on-demand \
  --no-daemon --parallel publishProjectModules -x test -x javadoc -x check
if [ $? -eq 1 ]; then
  echo "Unable to successfully generate metadata for project modules"
  exit 1
fi

coords="\"collection\":\"casmodules\",\"database\":\"apereocas\",\"dataSource\":\"cascluster\"";
function execPostRequest() {
  action="$1"
  data="$2"
  curl --silent --request POST \
    "${CAS_PROJECT_DB_URL_ENDPOINT}/action/${action}" \
    --header 'Content-Type: application/json' \
    --header "api-key: ${CAS_PROJECT_DB_API_KEY}" \
    --data-raw "$data" &> /dev/null
  echo -e "Done"
}

echo "Clearing previous records for ${casVersion}"
execPostRequest "deleteMany" "{${coords},\"filter\":{\"version\":{\"\$eq\":\"$casVersion\"}}}"

echo "Clearing SNAPSHOT records for ${casVersion}"
execPostRequest "deleteMany" "{${coords},\"filter\":{\"version\":{\"\$regex\":\"-SNAPSHOT\$\"}}}"

echo "Uploading records for ${casVersion}"
input=$(cat build/modules.json)
execPostRequest "insertMany" "{${coords},\"documents\": ${input}}"
if [ $? -eq 1 ]; then
  echo "Unable to upload records for CAS version ${casVersion}. Aborting..."
  exit 1
fi
exit 0
