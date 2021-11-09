#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Running LDAP docker image"
docker stop ldap-server || true && docker rm ldap-server || true
docker run --rm  -d -p 10389:389 -p 1389:389 --name="ldap-server" mmoayyed/ldap

docker ps | grep "ldap-server"
retVal=$?
if [ $retVal == 0 ]; then
    echo "LDAP docker image is running."
else
    echo "LDAP docker image failed to start."
    exit $retVal
fi

echo "Running OpenLDAP docker image"
# Create an empty OpenLdap server for the company Example Inc. and the domain example.org.
docker stop openldap-server || true && docker rm openldap-server || true
docker run --rm -d -p 11389:389 -p 11636:636 --name="openldap-server" \
--volume $PWD/ci/tests/ldap/ldif:/container/service/slapd/assets/config/bootstrap/ldif/custom \
--env LDAP_ADMIN_PASSWORD="P@ssw0rd" --env LDAP_TLS=false \
osixia/openldap --copy-service --loglevel debug

docker ps | grep "openldap-server"
retVal=$?
if [ $retVal == 0 ]; then
    echo "OpenLDAP docker image is running."
else
    echo "OpenLDAP docker image failed to start."
    exit $retVal
fi
