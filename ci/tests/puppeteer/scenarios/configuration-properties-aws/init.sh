#!/bin/bash
set -e

"${PWD}"/ci/tests/httpbin/run-httpbin-server.sh
"${PWD}"/ci/tests/aws/run-aws-server.sh

function awslocal {
  docker exec -i localstack awslocal "$@"
}

echo "Waiting for localstack to accept requests..."
for _ in $(seq 1 30); do
  if awslocal ssm describe-parameters >/dev/null 2>&1; then
    break
  fi
  sleep 2
done

echo "Storing settings in SSM, Secrets Manager and S3..."
awslocal ssm put-parameter --name "/cas/cas.authn.accept.users" --value "awsuser::Mellon" --type String --overwrite
awslocal secretsmanager create-secret --name "cas.server.scope" --secret-string "example.com"
awslocal s3api create-bucket --bucket cas-config
printf "cas.logout.follow-service-redirects=false\ncas.audit.engine.enabled=false\n" | awslocal s3 cp - s3://cas-config/cas.properties
echo "Ready!"
