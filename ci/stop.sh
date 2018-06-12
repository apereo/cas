#!/bin/bash

docker kill $(docker ps -q) >/dev/null
docker rm $(docker ps -a -q) >/dev/null
docker rmi $(docker images -q) >/dev/null

