#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Running InfluxDb docker image..."
docker stop influxdb-server || true && docker rm influxdb-server || true
docker run --rm -d -p 8083:8083 -p 8086:8086 \
  --name="influxdb-server" influxdb:2.0
sleep 15
docker exec influxdb-server influx setup --username \
  root --password password --org CAS --bucket casEventsDatabase --force

docker exec influxdb-server influx bucket create \
  --org CAS --name CasInfluxDbEvents

docker ps | grep "influxdb-server"
retVal=$?
if [ $retVal == 0 ]; then
    echo "InfluxDb docker image is running."
else
    echo "InfluxDb docker image failed to start."
    exit $retVal
fi
