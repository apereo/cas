#!/bin/bash

task="$1"
tests="$2"
debug="$3"

case $task in
test|simple|run|basic|unit)
    task="test"
    category="SIMPLE"
    ;;
memcached|memcache|kryo)
    task="testMemcached"
    category="MEMCACHED"
    ;;
filesystem|files|file|fsys)
    task="testFileSystem"
    category="FILESYSTEM"
    ;;
groovy)
    task="testGroovy"
    category="GROOVY"
    ;;
ldap)
    task="testLdap"
    category="LDAP"
    ;;
mongo|mongodb)
    task="testMongoDb"
    category="MONGODB"
    ;;
couchdb)
    task="testCouchDb"
    category="COUCHDB"
    ;;
rest|restful)
    task="testRestful"
    category="RESTFULAPI"
    ;;
mysql)
    task="testMySQL"
    category="MYSQL"
    ;;
maria|mariadb)
    task="testMariaDb"
    category="MariaDb"
    ;;
jdbc|jpa|database|hibernate)
    task="testJDBC"
    category="JDBC"
    ;;
postgres|pg|postgresql)
    task="testPostgres"
    category="POSTGRES"
    ;;
cassandra)
    task="testCassandra"
    category="CASSANDRA"
    ;;
oauth)
    task="testOAuth"
    category="OAUTH"
    ;;
oidc)
    task="testOIDC"
    category="OIDC"
    ;;
saml|saml2)
    task="testSAML"
    category="SAML"
    ;;
radius)
    task="testRadius"
    category="RADIUS"
    ;;
mail|email)
    task="testMail"
    category="MAIL"
    ;;
zoo|zookeeper)
    task="testZooKeeper"
    category="ZOOKEEPER"
    ;;
dynamodb|dynamo)
    task="testDynamoDb"
    category="DYNAMODB"
    ;;
webflow|swf)
    task="testWebflow"
    category="WEBFLOW"
    ;;
oracle)
    task="testOracle"
    category="ORACLE"
    ;;
redis)
    task="testRedis"
    category="REDIS"
    ;;
activemq|amq)
    task="testActiveMQ"
    category="ActiveMQ"
    ;;
esac

if [ -z "${tests}" ] || [ "${tests}" == "-" ]; then
    tests=""
else
    tests="--tests \"${tests}\""
fi

if [ ! -z "${debug}" ]; then
    debug="--debug-jvm"
fi

clear
echo -e "Running Gradle with task [$task], category [$category] including test(s) [$tests] with debug mode [$debug]\n"

cmd="./gradlew $task $debug -DtestCategoryType=$category $tests \
--build-cache --parallel -x javadoc -x check -DignoreTestFailures=false -DskipNestedConfigMetadataGen=true \
-DskipGradleLint=true -DshowStandardStreams=true --no-daemon --configure-on-demand "

echo -e "$cmd\n"
eval "$cmd"

