#!/bin/bash

imageTag="$1"

if [ -z "$imageTag" ]; then
  version=(`cat gradle.properties | grep "cas.version" | cut -d= -f2`)
  imageTag="v$version" 
fi

echo "Building CAS docker image tagged as [$imageTag]"
# read -p "Press [Enter] to continue..." any_key;

docker build --tag="apereo/cas:$imageTag" . \
  && echo "Built CAS image successfully tagged as apereo/cas:$imageTag" \
  && docker images "apereo/cas:$imageTag"