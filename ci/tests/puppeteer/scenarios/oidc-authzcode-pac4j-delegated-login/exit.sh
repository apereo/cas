#!/bin/bash
rm -rf ${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/truststore > /dev/null

pkill -f npm
clientDir="./oauth2-client"
rm -Rf "${clientDir}" > /dev/null