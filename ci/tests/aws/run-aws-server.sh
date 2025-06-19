#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &
export DOCKER_IMAGE="localstack/localstack:4.5.0"
echo "Running localstack docker container..."
docker stop localstack || true && docker rm localstack || true
docker run --rm -d -e 'DEBUG=1' -e 'SERVICES=kinesis,firehose,ses,ssm,events,cloudwatch,logs,s3,s3api,secretsmanager,sqs,sts' \
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

    echo "Creating S3 bucket for Security Lake..."
    export AWS_ENDPOINT_URL="http://localhost:4566"
    docker exec localstack bash -c "awslocal --endpoint-url=$AWS_ENDPOINT_URL s3api create-bucket --bucket security-lake-logs"

    echo "Creating S3 bucket for Security Lake..."
    docker exec localstack bash -c "awslocal --endpoint-url=$AWS_ENDPOINT_URL firehose create-delivery-stream \
        --delivery-stream-name security-lake-stream \
        --s3-destination-configuration RoleARN=\"arn:aws:iam::000000000000:role/test-role\",BucketARN=\"arn:aws:s3:::security-lake-logs\""

    echo "Verifying email identity..."
    docker exec localstack bash -c "awslocal ses verify-email-identity --email hello@example.com"
    echo "Verified email identity."

    echo "Create Log group..."
    docker exec localstack bash -c "awslocal logs create-log-group --log-group-name cas-log-group"
    docker exec localstack bash -c "awslocal logs create-log-stream --log-group-name cas-log-group --log-stream-name cas-log-stream"

    words=("info" "debug" "trace" "warn" "error")

    for i in {1..25} ; do
        random_index=$((RANDOM % 5))
        logLevel=${words[$random_index]}
        docker exec localstack bash -c "awslocal logs put-log-events \
          --log-group-name cas-log-group \
          --log-stream-name cas-log-stream \
          --log-events '[{\"timestamp\": '\"$(date +%s000)\"', \"message\": \"[${logLevel}] This is a ${logLevel} log message, id: ${i}\"}]'" >/dev/null 2>&1
    done

    echo "localstack docker container is running."
else
    echo "localstack docker container failed to start."
    exit $retVal
fi
