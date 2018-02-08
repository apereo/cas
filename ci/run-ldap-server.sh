#!/bin/bash

git clone --depth 1 https://github.com/jtgasper3/docker-images.git
cp ./ci/ldap/users.ldif docker-images/openldap
docker build --tag="apereo/openldap" .
docker run -d -p 10389:389 --name="ldap-server" apereo/openldap
docker ps -a
