#!/bin/bash

# docker stop configserver || true && docker rm configserver || true
CONFIG_SOURCE="${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/configserver"
rm -f "${CONFIG_SOURCE}/thekeystore"
