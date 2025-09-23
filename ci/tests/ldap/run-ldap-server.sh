#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

export DOCKER_IMAGE="osixia/openldap:latest"

echo "Running LDAP docker container"
docker stop ldap-server || true && docker rm ldap-server || true
docker run --rm  -d -p 10389:389 -p 1389:389 --name="ldap-server" mmoayyed/ldap

docker ps | grep "ldap-server"
retVal=$?
if [ $retVal == 0 ]; then
    echo "LDAP docker container is running."
else
    echo "LDAP docker container failed to start."
    exit $retVal
fi

echo "Running OpenLDAP docker container"
# Create an empty OpenLdap server for the company Example Inc. and the domain example.org.
docker stop openldap-server || true && docker rm openldap-server || true
docker run --rm -d -p 11389:389 -p 11636:636 --name="openldap-server" \
--volume $PWD/ci/tests/ldap/ldif:/container/service/slapd/assets/config/bootstrap/ldif/custom \
--env LDAP_ADMIN_PASSWORD="P@ssw0rd" --env LDAP_CONFIG_PASSWORD="P@ssw0rd" --env LDAP_TLS=false \
${DOCKER_IMAGE} --copy-service --loglevel debug

#docker logs -f openldap-server &
docker ps | grep "openldap-server"
retVal=$?
if [ $retVal == 0 ]; then
    echo "OpenLDAP docker container is running."
else
    echo "OpenLDAP docker container failed to start."
    exit $retVal
fi
