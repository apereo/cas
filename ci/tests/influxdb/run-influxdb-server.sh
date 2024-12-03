#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &
export DOCKER_IMAGE="influxdb:2.7.11"
echo "Running InfluxDb docker container..."
docker stop influxdb-server || true && docker rm influxdb-server || true
docker run --rm -d -p 8083:8083 -p 8086:8086 \
  --name="influxdb-server" ${DOCKER_IMAGE}
sleep 15
docker exec influxdb-server influx setup --username \
  root --password password --org CAS --bucket casEventsDatabase --force

docker exec influxdb-server influx bucket create \
  --org CAS --name CasInfluxDbEvents

docker ps | grep "influxdb-server"
retVal=$?
if [ $retVal == 0 ]; then
    echo "InfluxDb docker container is running."
else
    echo "InfluxDb docker container failed to start."
    exit $retVal
fi
