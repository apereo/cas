#!/bin/bash

if [ "$MATRIX_JOB_TYPE" == "TEST" ]; then
    # while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

    echo "Running Ignite docker image..."
    docker run -d -p 11211:11211 -p 47100:47100 -p 47500:47500 -p 49112:49112 --name "ignite-server" apacheignite/ignite:2.4.0

    docker ps | grep "ignite-server"
    retVal=$?
    if [ $retVal == 0 ]; then
        echo "Ignite docker image is running."
    else
        echo "Ignite docker image failed to start."
        exit $retVal
    fi
fi
