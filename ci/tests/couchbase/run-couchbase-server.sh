#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &
function configureNode() {
  echo -e "\n*************************************************************"
  echo -e "Setting default memory quota for the pool"
  echo -e "*************************************************************"
  curl http://localhost:8091/pools/default -d memoryQuota=1024 | jq

  echo -e "\n*************************************************************"
  echo -e "Initialize node..."
  echo -e "*************************************************************"
  curl http://localhost:8091/nodes/self/controller/settings \
  -d path=/opt/couchbase/var/lib/couchbase/data \
  -d index_path=/opt/couchbase/var/lib/couchbase/data | jq

  echo -e "*************************************************************"
  echo -e "Rename node..."
  echo -e "*************************************************************"
  curl  http://localhost:8091/node/controller/rename -d hostname=127.0.0.1 | jq
}

function loadBuckets() {
  echo -e "\n*************************************************************"
  echo -e "Loading Couchbase buckets..."
  echo -e "*************************************************************"
  curl -u 'admin:password' http://localhost:8091/pools/default/buckets | jq
}

function createBuckets() {
  echo -e "\n*************************************************************"
  echo -e "Creating Couchbase buckets..."
  echo -e "*************************************************************"

  echo -e "Creating Couchbase testbucket bucket..."
  curl -u 'admin:password' -d 'name=testbucket' -d 'bucketType=couchbase' \
  -d 'ramQuotaMB=220' -d 'authType=sasl' -d 'saslPassword=password' \
  http://localhost:8091/pools/default/buckets | jq

  echo -e "Creating Couchbase pplbucket bucket..."
  curl -u 'admin:password' -d 'name=pplbucket' -d 'bucketType=couchbase' \
  -d 'ramQuotaMB=220' -d 'authType=sasl' -d 'saslPassword=password' http://localhost:8091/pools/default/buckets | jq

  echo -e "Creating Couchbase casbucket bucket..."
  curl -u 'admin:password' -d 'name=casbucket' -d 'bucketType=couchbase' \
    -d 'ramQuotaMB=120' -d authType='none' http://localhost:8091/pools/default/buckets | jq
}

function populateBuckets() {
  echo -e "\n*************************************************************"
  echo -e "Creating document/accounts..."
  echo -e "*************************************************************"
  curl -u 'admin:password'  http://localhost:8093/query/service \
    -d 'statement=INSERT INTO `pplbucket` (KEY,VALUE) VALUES("accounts", {"username": "casuser", "psw": "Mellon", "firstname": "CAS", "lastname":"User"})' | jq

  curl -u 'admin:password'  http://localhost:8093/query/service \
    -d 'statement=INSERT INTO `pplbucket` (KEY,VALUE) VALUES("bad-accounts", {"username": "nopsw", "firstname": "hello", "lastname":"world"})' | jq
}

function createIndex() {
  echo -e "\n*************************************************************"
  echo -e "Creating index settings..."
  echo -e "*************************************************************"
  curl -u 'admin:password' 'http://localhost:8091/settings/indexes' -d 'indexerThreads=0' -d 'logLevel=info' \
    -d 'maxRollbackPoints=5' -d 'memorySnapshotInterval=200' \
    -d 'stableSnapshotInterval=5000' -d 'storageMode=memory_optimized' | jq
  sleep 10
  
  echo -e "\n*************************************************************"
  echo -e "Creating index..."
  echo -e "*************************************************************"
  curl -u 'admin:password' http://localhost:8093/query/service \
    -d 'statement=CREATE INDEX accounts_idx ON testbucket(username)' -d 'namespace=default' | jq
  sleep 10
  curl -u 'admin:password' http://localhost:8093/query/service \
    -d 'statement=CREATE INDEX accounts_idx ON pplbucket(username)' -d 'namespace=default' | jq
  sleep 10
}

function createPrimaryIndex() {
  echo -e "\n*************************************************************"
  echo -e "Creating primary index..."
  echo -e "*************************************************************"
  curl -u 'admin:password' http://localhost:8093/query/service -d 'statement=CREATE PRIMARY INDEX `primary-idx` ON `testbucket`' -d 'namespace=default' | jq
  sleep 5
  curl -u 'admin:password' http://localhost:8093/query/service -d 'statement=CREATE PRIMARY INDEX `primary-idx` ON `pplbucket`' -d 'namespace=default' | jq
  sleep 5
  curl -u 'admin:password' http://localhost:8093/query/service -d 'statement=CREATE PRIMARY INDEX `primary-idx` ON `casbucket`' -d 'namespace=default' | jq
  sleep 5
}

echo "Running Couchbase docker image..."
docker stop couchbase || true && docker rm couchbase || true
docker run --rm  -d --name couchbase -p 8091-8094:8091-8094 -p 11210:11210 couchbase/server:7.1.3
echo "Waiting for Couchbase server to come online..."
sleep 20
until $(curl --output /dev/null --silent --head --fail http://localhost:8091); do
    printf '.'
    sleep 1
done

configureNode

echo -e "\n*************************************************************"
echo -e "Setting cluster services..."
echo -e "*************************************************************"
curl http://localhost:8091/node/controller/setupServices -d 'services=kv%2Cn1ql%2Cindex'

echo -e "\n*************************************************************"
echo -e "Setup Administrator username and password..."
echo -e "*************************************************************"
curl http://localhost:8091/settings/web -d password=password -d username=admin -d port=8091 -d roles=full_admin

createBuckets
loadBuckets
createIndex
populateBuckets
sleep 5
createPrimaryIndex

docker ps | grep "couchbase"
retVal=$?
if [ $retVal == 0 ]; then
    echo "Couchbase docker image is running."
else
    echo "Couchbase docker image failed to start."
    exit $retVal
fi
