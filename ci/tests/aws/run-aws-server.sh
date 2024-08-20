#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &
export DOCKER_IMAGE="localstack/localstack:3.6"
echo "Running localstack docker image..."
docker stop localstack || true && docker rm localstack || true
docker run --rm -d -e 'DEBUG=1' -e 'SERVICES=ses,ssm,events,cloudwatch,logs,s3,s3api,secretsmanager,sqs,sts' \
  -p 4560-4599:4560-4599 \
  -p 8080:8080 \
  --name localstack \
  -e PROVIDER_OVERRIDE_CLOUDWATCH=v2 \
  -e DOCKER_HOST=unix:///var/run/docker.sock \
  ${DOCKER_IMAGE}
sleep 2
docker ps | grep "localstack"
retVal=$?
if [ $retVal == 0 ]; then
    echo "Verifying email identity..."
    docker exec localstack bash -c "awslocal ses verify-email-identity --email hello@example.com"
    echo "Verified email identity."

    echo "Create Log group..."
    docker exec localstack bash -c "awslocal logs create-log-group --log-group-name cas-log-group"
    docker exec localstack bash -c "awslocal logs create-log-stream --log-group-name cas-log-group --log-stream-name cas-log-stream"
    
    for i in {1..50} ; do
        docker exec localstack bash -c "awslocal logs put-log-events \
          --log-group-name cas-log-group \
          --log-stream-name cas-log-stream \
          --log-events timestamp=$(date +%s)000,message=\"This is a test log message with id ${i}\""
    done
    
    echo "localstack docker container is running."
else
    echo "localstack docker container failed to start."
    exit $retVal
fi
