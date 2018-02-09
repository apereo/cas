#!/bin/bash

echo "Pulling InfluxDb docker image..."
docker pull store/influxdata/influxdb:1.2.2

echo "Running InfluxDb docker image..."
docker run -p 8083:8083 -p 8086:8086 store/influxdata/influxdb:1.2.2
docker ps


