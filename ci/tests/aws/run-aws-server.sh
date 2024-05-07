#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Running localstack docker image..."
docker stop localstack || true && docker rm localstack || true
docker run --rm -d -e 'DEBUG=1' -e 'SERVICES=ssm,events,cloudwatch,logs,s3,s3api,secretsmanager,sqs,sts' \
  -p 4560-4599:4560-4599 -p 8080:8080 --name localstack \
  -e PROVIDER_OVERRIDE_CLOUDWATCH=v2 \
  -e DOCKER_HOST=unix:///var/run/docker.sock localstack/localstack:3.4

docker ps | grep "localstack"
retVal=$?
if [ $retVal == 0 ]; then
    echo "localstack docker container is running."
else
    echo "localstack docker container failed to start."
    exit $retVal
fi
