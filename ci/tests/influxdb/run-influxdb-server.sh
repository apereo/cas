#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Running InfluxDb docker image..."
docker run -d -p 8083:8083 -p 8086:8086 --name="influxdb-server" store/influxdata/influxdb:1.2.2

docker ps | grep "influxdb-server"
retVal=$?
if [ $retVal == 0 ]; then
    echo "InfluxDb docker image is running."
else
    echo "InfluxDb docker image failed to start."
    exit $retVal
fi
