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

  versionNumbers=${casVersion%%-*}
  versionNumbers=${versionNumbers//./}  
  echo "CAS version number is: $versionNumbers"
  collectionName="casmodules$versionNumbers"
  echo "CAS module collection is $collectionName"
  
  coords="\"collection\":\"$collectionName\",\"database\":\"apereocas\",\"dataSource\":\"cascluster\"";
  
  echo "Removing previous records for ${casVersion} from ${coords}"
  execPostRequestWithHttpie "deleteMany" "{${coords},\"filter\":{\"version\":{\"\$eq\":\"$casVersion\"}}}"

  echo "Removing SNAPSHOT records for ${casVersion} from ${coords}"
  execPostRequestWithHttpie "deleteMany" "{${coords},\"filter\":{\"version\":{\"\$regex\":\"-SNAPSHOT\$\"}}}"

  echo "Uploading module records for ${casVersion} to ${coords}"
  input=$(cat build/modules.json)
  execPostRequestWithHttpie "insertMany" "{${coords},\"documents\": ${input}}"
  
  if [ $? -eq 1 ]; then
    echo "Unable to upload modules for CAS version ${casVersion}. Aborting..."
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
