#!/bin/bash

docker kill $(docker ps -q) >/dev/null 2>&1
docker rm $(docker ps -a -q) >/dev/null 2>&1
docker rmi $(docker images -q) >/dev/null 2>&1

exit 0
