#!/bin/bash

if [ "$MATRIX_JOB_TYPE" == "TEST" ]; then
    # while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

    echo "Cloning 389-ds repository..."
    git clone --depth 1 --single-branch https://github.com/jtgasper3/docker-images.git
    
    echo "Copying base LDIF files to prepare LDAP server..."
    cp ./ci/ldap/ds-setup.inf docker-images/389-ds
    cp ./ci/ldap/users.ldif docker-images/389-ds

    echo "Building LDAP docker image..."
    docker rmi apereocastests/ldap --force
    docker build --tag="apereocastests/ldap" ./docker-images/389-ds

    rm -Rf docker-images
    # rm -f ./ci/ldap/users.ldif

    echo "Running LDAP docker image"
    docker run -d -p 10389:389 --name="ldap-server" apereocastests/ldap

    docker ps | grep "ldap-server"
    retVal=$?
    if [ $retVal == 0 ]; then
        echo "LDAP docker image is running."
    else
        echo "LDAP docker image failed to start."
        exit $retVal
    fi
fi


