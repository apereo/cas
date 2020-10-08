#!/bin/bash

docker_user="$1"
docker_psw="$2"
echo "$docker_psw" | docker login --username "$docker_user" --password-stdin

imageTag="$3"
if [ -z "$imageTag" ]; then
  version=(`cat gradle.properties | grep "cas.version" | cut -d= -f2`)
  imageTag="v$version" 
fi

echo "Pushing CAS docker image tagged as $imageTag to apereo/cas..."
docker push apereo/cas:"$imageTag" \
	&& echo "Pushed apereo/cas:$imageTag successfully.";