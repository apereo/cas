#!/bin/bash
pkill -f npm
clientDir="./oauth2-client"
rm -Rf "${clientDir}" > /dev/null
