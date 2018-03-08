#!/bin/bash

if [ "$MATRIX_JOB_TYPE" == "TEST" ]; then
    docker kill $(docker ps -q)
    docker rm $(docker ps -a -q)
    docker rmi $(docker images -q)
fi

