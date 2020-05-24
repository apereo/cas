#!/bin/bash

clear

dkc() {
   export CID=$(docker ps -aqf "name=$1");
   docker stop $CID 2>/dev/null
   docker rm -f $CID 2>/dev/null
}

printHelp() {
    echo -e "\nUsage: ./testcas.sh [--no-container] --category [category1,category2,...] [--help] [--test TestClass] [--ignore-failures] [--no-wrapper] [--no-retry] [--debug] [--coverage-report] [--coverage-upload] [--no-parallel] \n"
    echo -e "Available test categories are:\n"
    echo -e "simple,memcached,cassandra,groovy,kafka,ldap,rest,mfa,jdbc,mssql,oracle,radius,couchdb,\
mariadb,files,postgres,dynamodb,couchbase,uma,saml,mail,aws,jms,hazelcast,jmx,ehcache,\
oauth,oidc,redis,webflow,mongo,ignite,influxdb,zookeeper,mysql,x509,shell,cosmosdb"
    echo -e "\nPlease see the test script for details.\n"
}

container=true
uploadCoverage=false
parallel="--parallel "
gradleCmd="./gradlew"
flags="--build-cache -x javadoc -x check -DignoreTestFailures=false -DskipNestedConfigMetadataGen=true \
-DshowStandardStreams=true --no-daemon --configure-on-demand"

while (( "$#" )); do
    case "$1" in
    --no-container)
        container=false
        shift
        ;;
    --no-parallel)
        parallel=""
        shift
        ;;
    --coverage-report)
        currentDir=`pwd`
        case "${currentDir}" in
            *api*|*core*|*support*|*webapp*)
                coverage="jacocoTestReport "
                ;;
            *)
                coverage="jacocoRootReport "
                ;;
        esac
        shift
        ;;
    --coverage-upload)
        uploadCoverage=true
        shift
        ;;
    --no-wrapper)
        gradleCmd="gradle"
        shift
        ;;
    --help)
        printHelp
        exit 0
        ;;
    --debug)
        debug="--debug-jvm "
        parallel=""
        shift
        ;;
    --test)
        tests="--tests \"$2\" "
        shift 2
        ;;
    --no-retry)
        flags+=" -DskipTestRetry=true"
        shift
        ;;
    --ignore-failures)
        flags+=" -DignoreTestFailures=true"
        shift
        ;;
    --category)
        category="$2"
        for item in $(echo "$category" | sed "s/,/ /g")
        do
            case "${item}" in
            test|simple|run|basic|unit|unittests)
                task+="testSimple "
                ;;
            memcached|memcache|kryo)
                test "${container}" == true && dkc ${category} && ./ci/tests/memcached/run-memcached-server.sh
                task+="testMemcached "
                ;;
            x509)
                task+="testX509 "
                ;;
            shell)
                task+="testSHELL "
                ;;
            uma)
                task+="testUma "
                ;;
            filesystem|files|file|fsys)
                task+="testFileSystem "
                ;;
            groovy|script)
                task+="testGroovy "
                ;;
            jmx|jmx)
                task+="testJMX "
                ;;
            hz|hazelcast)
                task+="testHazelcast "
                ;;
            mssql)
                test "${container}" == true && dkc ${category} && ./ci/tests/mssqlserver/run-mssql-server.sh
                task+="testMsSqlServer "
                ;;
            ignite)
                task+="testIgnite "
                ;;
            influx|influxdb)
                test "${container}" == true && dkc ${category} && ./ci/tests/influxdb/run-influxdb-server.sh
                task+="testInfluxDb "
                ;;
            cosmosdb|cosmos)
                task+="testCosmosDb "
                ;;
            ehcache)
                test "${container}" == true && dkc ${category} && ./ci/tests/ehcache/run-terracotta-server.sh
                task+="testEhcache "
                ;;
            ldap|ad|activedirectory)
                test "${container}" == true && dkc ${category} && ./ci/tests/ldap/run-ldap-server.sh
                test "${container}" == true && dkc samba && ./ci/tests/ldap/run-ad-server.sh true
                task+="testLdap "
                ;;
            couchbase)
                test "${container}" == true && dkc ${category} && ./ci/tests/couchbase/run-couchbase-server.sh
                task+="testCouchbase "
                ;;
            mongo|mongodb)
                test "${container}" == true && dkc ${category} && ./ci/tests/mongodb/run-mongodb-server.sh
                task+="testMongoDb "
                ;;
            couchdb)
                test "${container}" == true && dkc ${category} &&./ci/tests/couchdb/run-couchdb-server.sh
                task+="testCouchDb "
                ;;
            rest|restful|restapi)
                task+="testRestful "
                ;;
            mysql)
                test "${container}" == true && dkc ${category} &&./ci/tests/mysql/run-mysql-server.sh
                task+="testMySQL "
                ;;
            maria|mariadb)
                test "${container}" == true && dkc ${category} &&./ci/tests/mariadb/run-mariadb-server.sh
                task+="testMariaDb "
                ;;
            jdbc|jpa|database|db|hibernate|rdbms|hsql)
                task+="testJDBC "
                ;;
            postgres|pg|postgresql)
                test "${container}" == true && dkc ${category} &&./ci/tests/postgres/run-postgres-server.sh
                task+="testPostgres "
                ;;
            cassandra)
                test "${container}" == true && dkc ${category} &&./ci/tests/cassandra/run-cassandra-server.sh
                task+="testCassandra "
                ;;
            kafka)
                task+="testKafka "
                ;;
            oauth)
                task+="testOAuth "
                ;;
            aws)
                test "${container}" == true && dkc ${category} &&./ci/tests/aws/run-aws-server.sh
                task+="testAWS "
                ;;
            oidc)
                task+="testOIDC "
                ;;
            mfa|duo|gauth|webauthn|authy|fido|u2f|swivel|acceptto)
                task+="testMFA "
                ;;
            saml|saml2)
                task+="testSAML "
                ;;
            radius)
                test "${container}" == true && dkc ${category} &&./ci/tests/radius/run-radius-server.sh
                task+="testRadius "
                ;;
            mail|email)
                test "${container}" == true && ./ci/tests/mail/run-mail-server.sh
                task+="testMail "
                ;;
            zoo|zookeeper)
                test "${container}" == true && dkc ${category} && ./ci/tests/zookeeper/run-zookeeper-server.sh
                task+="testZooKeeper "
                ;;
            dynamodb|dynamo)
                test "${container}" == true && dkc ${category} && ./ci/tests/dynamodb/run-dynamodb-server.sh
                task+="testDynamoDb "
                ;;
            webflow|swf)
                task+="testWebflow "
                ;;
            oracle)
                test "${container}" == true && dkc ${category} && ./ci/tests/oracle/run-oracle-server.sh
                task+="testOracle "
                ;;
            redis)
                test "${container}" == true && dkc ${category} && ./ci/tests/redis/run-redis-server.sh
                task+="testRedis "
                ;;
            activemq|amq|jms)
                test "${container}" == true && dkc ${category} && ./ci/tests/activemq/run-activemq-server.sh
                task+="testJMS "
                ;;
            simple|unit)
                task+="testSimple "
                ;;
            esac
        done
        shift 2
        ;;
    *)
        echo -e "Unable to accept parameter: $1"
        printHelp
        exit 1
        ;;
    esac
done

if [[ -z "$task" ]]
then
  printHelp
  exit 1
fi

cmdstring="\033[1m$gradleCmd \e[32m$task\e[39m$tests\e[39m $flags ${coverage}${debug}${parallel}\e[39m"
printf "$cmdstring \e[0m\n"

cmd="$gradleCmd $task $tests $flags ${coverage} ${debug} ${parallel}"
eval "$cmd"
retVal=$?
echo -e "***************************************************************************************"
echo -e "Gradle build finished at `date` with exit code $retVal"
echo -e "***************************************************************************************"

if [ $retVal == 0 ]; then
    if [ $uploadCoverage = true ]; then
        echo "Uploading test coverage results for ${category}..."
        bash <(curl -s https://codecov.io/bash) -F "$category"
        echo "Gradle build finished successfully."
    fi
else
    echo "Gradle build did NOT finish successfully."
    exit $retVal
fi
