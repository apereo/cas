#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

RED="\e[31m"
GREEN="\e[32m"
ENDCOLOR="\e[0m"

function printgreen() {
  printf "🍀 ${GREEN}$1${ENDCOLOR}\n"
}

function printred() {
  printf "🚨  ${RED}$1${ENDCOLOR}\n"
}

printgreen "Starting Eureka Server..."
docker stop eureka-server || true && docker rm eureka-server || true
docker run -d --name eureka-server -p 8761:8761 steeltoeoss/eureka-server
docker ps
