#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Running LDAP docker image"
docker stop ldap-server || true && docker rm ldap-server || true
docker run -d -p 10389:389 --name="ldap-server" mmoayyed/ldap

docker ps | grep "ldap-server"
retVal=$?
if [ $retVal == 0 ]; then
    echo "LDAP docker image is running."
else
    echo "LDAP docker image failed to start."
    exit $retVal
fi
