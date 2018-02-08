#!/bin/bash

git clone --depth 1 https://github.com/jtgasper3/docker-images.git

echo "Copying LDIF files to prepare LDAP server..."
cat ./support/cas-server-support-ldap/src/test/resources/ldif/ldap-base.ldif >>  docker-images/389-ds/users.ldap

echo "Building LDAP docker image..."
docker build --tag="apereocastests/ldap"  ./docker-images/389-ds
docker images

echo "Running LDAP docker image"
docker run -d -p 10389:389 --name="ldap-server" apereocastests/ldap
docker ps -a
