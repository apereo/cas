#!/bin/bash

job=$1
token=$2

if [ -z "$token" ] || [ -z "$job" ]
  then
    read -p "Travis CI job id: " job;
    read -p "Travis CI authorization token: " token;
fi

travisDebugEndpoint=https://api.travis-ci.org/job/${job}/debug

echo "Posting request to Travis CI ${travisDebugEndpoint} to start a debuggable build"

curl -s -X POST \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -H "Travis-API-Version: 3" \
  -H "Authorization: token ${token}" \
  -d "{\"quiet\": true}" \
  ${travisDebugEndpoint}
