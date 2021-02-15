#!/usr/bin/env sh

rm -Rf ./tmp
curl -k http://localhost:8080/starter.tgz -d dependencies="$1" -d type=cas-config-server-overlay -d baseDir=tmp | tar -xzvf -
