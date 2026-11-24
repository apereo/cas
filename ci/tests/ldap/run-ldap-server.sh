#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

RED="\e[31m"
GREEN="\e[32m"
YELLOW="\e[33m"
ENDCOLOR="\e[0m"

function printgreen() {
  printf "☘️  ${GREEN}$1${ENDCOLOR}\n"
}

function printred() {
  printf "🚨  ${RED}$1${ENDCOLOR}\n"
}

export DOCKER_IMAGE="osixia/openldap:1.5.0"

printgreen "Running LDAP docker container"
docker stop ldap-server || true && docker rm ldap-server || true
docker run --quiet  --rm  -d -p 10389:389 -p 1389:389 --name="ldap-server" mmoayyed/ldap

docker ps | grep "ldap-server"
retVal=$?
if [ $retVal == 0 ]; then
    printgreen "LDAP docker container is running."
else
    printred "LDAP docker container failed to start."
    exit $retVal
fi

printgreen "Running OpenLDAP docker container"
# Create an empty OpenLdap server for the company Example Inc. and the domain example.org.
docker stop openldap-server || true && docker rm openldap-server || true
docker run --quiet -d -p 11389:389 -p 11636:636 --name="openldap-server" \
--volume $PWD/ci/tests/ldap/ldif:/container/service/slapd/assets/config/bootstrap/ldif/custom \
--env LDAP_ADMIN_PASSWORD="P@ssw0rd" --env LDAP_CONFIG_PASSWORD="P@ssw0rd" --env LDAP_TLS=false \
${DOCKER_IMAGE} --copy-service --loglevel debug

#docker logs -f openldap-server &
for attempt in $(seq 1 60); do
  if docker logs openldap-server 2>&1 | grep -q "slapd starting"; then
    printgreen "OpenLDAP docker container is running and slapd has started."
    exit 0
  fi
  if ! docker ps --format '{{.Names}}' | grep -q "^openldap-server$"; then
    break
  fi
  sleep 1
done
printred "OpenLDAP docker container did not start slapd; tests that need it will not find it on port 11389."
docker ps -a --filter name=openldap-server
docker logs openldap-server 2>&1 | tail -80 || true
