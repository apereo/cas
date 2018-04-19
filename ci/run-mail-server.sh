#!/bin/bash

if [ "$MATRIX_JOB_TYPE" == "TEST" ]; then
    # while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

    echo "Running SMTP mail docker image..."
    wget -O MockMock.jar https://github.com/tweakers-dev/MockMock/blob/master/release/MockMock.jar?raw=true
    java -jar MockMock.jar -p 25000 &>/dev/null &
fi
