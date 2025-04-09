set -e

chmod +x "${PWD}/ci/tests/httpbin/run-httpbin-server.sh"
${PWD}/ci/tests/httpbin/run-httpbin-server.sh

echo Starting Apache ZooKeeper Server
${PWD}/ci/tests/zookeeper/run-zookeeper-server.sh

docker exec zookeeper zkCli.sh -server localhost:2181 create /config  &> /dev/null
docker exec zookeeper zkCli.sh -server localhost:2181 create /config/cas &> /dev/null
docker exec zookeeper zkCli.sh -server localhost:2181 create /config/cas/cas &> /dev/null
docker exec zookeeper zkCli.sh -server localhost:2181 create /config/cas/cas/audit &> /dev/null
docker exec zookeeper zkCli.sh -server localhost:2181 create /config/cas/cas/audit/engine &> /dev/null
docker exec zookeeper zkCli.sh -server localhost:2181 create /config/cas/cas/audit/engine/enabled &> /dev/null
docker exec zookeeper zkCli.sh -server localhost:2181 set /config/cas/cas/audit/engine/enabled "false" &> /dev/null

docker exec zookeeper zkCli.sh -server localhost:2181 create /config/cas/cas/authn &> /dev/null
docker exec zookeeper zkCli.sh -server localhost:2181 create /config/cas/cas/authn/accept &> /dev/null
docker exec zookeeper zkCli.sh -server localhost:2181 create /config/cas/cas/authn/accept/users &> /dev/null
docker exec zookeeper zkCli.sh -server localhost:2181 set /config/cas/cas/authn/accept/users "zookeeper::p@SSword" &> /dev/null

echo -e "\n\nReady!"
