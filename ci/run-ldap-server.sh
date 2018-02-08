#!/bin/bash

echo "Copying LDIF files to prepare LDAP server..."
cat ./support/cas-server-support-ldap/src/test/resources/ldif/ldap-base.ldif >>  ./ci/ldap/users.ldif
cat ./support/cas-server-support-consent-ldap/src/test/resources/ldif/ldap-consent.ldif >>  ./ci/ldap/users.ldif
cat ./support/cas-server-support-ldap/src/test/resources/ldif/users-groups.ldif >> ./ci/ldap/users.ldif
cat ./support/cas-server-support-x509-core/src/test/resources/ldif/users-x509.ldif >> ./ci/ldap/users.ldif

echo "Building LDAP docker image..."
docker build --tag="apereocastests/ldap"  ./ci/ldap
docker images

echo "Running LDAP docker image"
docker run -d -p 10389:389 --name="ldap-server" apereocastests/ldap
docker ps -a
