#!/bin/bash

if [[ -z $CAS_PROJECT_DB_URL_ENDPOINT || -z $CAS_PROJECT_DB_API_KEY ]]; then
  echo "Unable to execute without proper credentials"
  exit 1
fi

# Use Httpie to bypass curl limitations on POST parameter size
function execPostRequestWithHttpie() {
  action="$1"
  data="$2"
  echo "$data" | http -qq POST "${CAS_PROJECT_DB_URL_ENDPOINT}/action/${action}" \
                          Content-Type:application/json \
                          api-key:"${CAS_PROJECT_DB_API_KEY}"
  if [ $? -eq 1 ]; then
    echo "Unable to publish data"
    exit 1
  fi
  echo -e "Done"
}

function publishProjectModules() {
  echo "Publishing CAS modules for CAS version ${casVersion}"
  ./gradlew --no-configuration-cache --build-cache --configure-on-demand \
    --no-daemon --parallel publishProjectModules -x test -x javadoc -x check
  if [ $? -eq 1 ]; then
    echo "Unable to successfully generate metadata for project modules"
    exit 1
  fi

  coords="\"collection\":\"casmodules\",\"database\":\"apereocas\",\"dataSource\":\"cascluster\"";
  
  echo "Clearing previous records for ${casVersion}"
  execPostRequestWithHttpie "deleteMany" "{${coords},\"filter\":{\"version\":{\"\$eq\":\"$casVersion\"}}}"

  echo "Clearing SNAPSHOT records for ${casVersion}"
  execPostRequestWithHttpie "deleteMany" "{${coords},\"filter\":{\"version\":{\"\$regex\":\"-SNAPSHOT\$\"}}}"

  echo "Uploading module records for ${casVersion}"
  input=$(cat build/modules.json)
  execPostRequestWithHttpie "insertMany" "{${coords},\"documents\": ${input}}"
  
  if [ $? -eq 1 ]; then
    echo "Unable to upload records for CAS version ${casVersion}. Aborting..."
    exit 1
  fi
}

function publishProjectConfiguration() {
  echo "Publishing CAS configuration metadata for CAS version ${casVersion}"
  /gradlew :api:cas-server-core-api-configuration-model:generateConfigurationMetadata \
           :api:cas-server-core-api-configuration-model:generateConfigurationMetadata \
           --no-configuration-cache --build-cache --configure-on-demand \
           --no-daemon --parallel -x test -x javadoc -x check
  if [ $? -eq 1 ]; then
    echo "Unable to successfully generate metadata for CAS configuration"
    exit 1
  fi

  coords="\"collection\":\"casconfig\",\"database\":\"apereocas\",\"dataSource\":\"cascluster\"";

#  echo "Clearing previous records for ${casVersion}"
#  execPostRequestWithHttpie "deleteMany" "{${coords},\"filter\":{\"_id\":{\"\$ne\":null}}}"
  
  echo "Clearing previous records for ${casVersion}"
  execPostRequestWithHttpie "deleteMany" "{${coords},\"filter\":{\"version\":{\"\$eq\":\"$casVersion\"}}}"
  echo "Clearing SNAPSHOT records for ${casVersion}"
  execPostRequestWithHttpie "deleteMany" "{${coords},\"filter\":{\"version\":{\"\$regex\":\"-SNAPSHOT\$\"}}}"


  echo "Uploading configuration records for ${casVersion}"
  input=$(cat api/cas-server-core-api-configuration-model/build/spring-configuration-metadata.json)
  execPostRequestWithHttpie "insertOne" "{${coords},\"document\": {\"version\": \"$casVersion\", \"payload\": ${input} }}"

  if [ $? -eq 1 ]; then
    echo "Unable to upload records for CAS version ${casVersion}. Aborting..."
    exit 1
  fi
}

echo "Running Gradle build to determine CAS version..."
casVersion=$(./gradlew casVersion --no-configuration-cache --no-daemon -q)
if [ $? -eq 1 ]; then
  echo "Unable to determine CAS version. Aborting..."
  exit 1
fi

publishProjectModules
publishProjectConfiguration
