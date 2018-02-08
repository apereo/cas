#!/bin/bash

git clone --depth 1 https://github.com/jtgasper3/docker-images.git
cp ./ci/ldap/users.ldif docker-images/openldap
echo "Building LDAP docker image..."
docker build --tag="apereocastests/openldap"  ./docker-images/openldap
echo "Running LDAP docker image"
docker images
docker run -d -p 10389:389 --name="ldap-server" apereocastests/openldap
docker ps -a
