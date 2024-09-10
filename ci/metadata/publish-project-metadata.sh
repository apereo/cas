#!/bin/bash

function publishProjectModules() {
  echo "Creating CAS module metadata for CAS version ${casVersion}"
  ./gradlew --no-configuration-cache --build-cache --configure-on-demand \
    --no-daemon --parallel generateProjectModulesMetadata -x test -x javadoc -x check
  if [ $? -eq 1 ]; then
    echo "Unable to successfully generate metadata for project modules"
    exit 1
  fi

  versionNumbers=${casVersion%%-*}
  versionNumbers=${versionNumbers//./}
  echo "CAS simple version number is: $versionNumbers"
  collectionName="casmodules$versionNumbers"
  echo "CAS module collection is $collectionName"
  
  echo "Uploading module records for ${casVersion} to $collectionName"
  mongoimport --uri "$CAS_MODULE_METADATA_MONGODB_URL" \
    --collection "$collectionName" --file build/modules.json \
    --type json --jsonArray --drop
  if [ $? -eq 1 ]; then
    echo "Unable to upload module metadata for CAS version ${casVersion}. Aborting..."
    exit 1
  fi
}

if [[ -z $CAS_MODULE_METADATA_MONGODB_URL ]]; then
  echo "Unable to publish module metadata without a URL."
  exit 1
fi

echo "Checking CAS version..."
casVersion=$(cat gradle.properties | grep version | awk -F"=" '{printf $2}')
publishProjectModules
