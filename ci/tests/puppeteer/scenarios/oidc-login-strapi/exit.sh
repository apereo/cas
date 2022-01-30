#!/usr/bin/env bash
set -e
set -m
SCENARIO="oidc-login-strapi"
STRAPI_FOLDER=${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/strapi
pid=$(cat $STRAPI_FOLDER/strapi.pid)
echo "Killing any processes strapi script started"
# msys2 ps doesn't support ps format (e.g. -o)
kill $(ps | grep $pid | awk '{print $1}')
