#!/bin/bash

clear

printHelp() {
    echo -e "\nUsage: ./testcas.sh --category [category1,category2,...] [--help] [--ignore-failures] [--no-wrapper] [--no-retry] [--debug] [--coverage]\n"
    echo -e "Available test categories are:\n"
    echo -e "simple, memcached,cassandra,groovy,kafka,ldap,rest,mfa,jdbc,mssql,oracle,radius,couchdb,\
mariadb,files,postgres,dynamodb,couchbase,uma,saml,mail,aws,activemq,\
oauth,oidc,redis,webflow,mongo,ignite,influxdb,zookeeper,mysql"
    echo -e "\nPlease see the test script for details.\n"
}

parallel="--parallel "
gradleCmd="./gradlew"
flags="--build-cache -x javadoc -x check -DignoreTestFailures=false -DskipNestedConfigMetadataGen=true \
-DshowStandardStreams=true --no-daemon --configure-on-demand"

while (( "$#" )); do
    case "$1" in
    --coverage)
        coverage="jacocoRootReport "
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
        for item in $(echo "$2" | sed "s/,/ /g")
        do
            case "${item}" in
            test|simple|run|basic|unit|unittests)
                task+="test "
                category+="SIMPLE,"
                ;;
            memcached|memcache|kryo)
                task+="testMemcached "
                category+="MEMCACHED,"
                flags+=" -DCI=true"
                ;;
            uma)
                task+="testUma "
                category+="UMA,"
                ;;
            filesystem|files|file|fsys)
                task+="testFileSystem "
                category+="FILESYSTEM,"
                ;;
            groovy|script)
                task+="testGroovy "
                category+="GROOVY,"
                ;;
            mssql)
                task+="testMsSqlServer "
                category+="MsSqlServer,"
                ;;
            ignite)
                task+="testIgnite "
                category+="Ignite,"
                flags+=" -DCI=true"
                ;;
            influx|influxdb)
                task+="testInfluxDb "
                category+="InfluxDb,"
                flags+=" -DCI=true"
                ;;
            ldap|ad|activedirectory)
                task+="testLdap "
                category+="LDAP,"
                flags+=" -DCI=true"
                ;;
            couchbase)
                task+="testCouchbase "
                category+="COUCHBASE,"
                flags+=" -DCI=true"
                ;;
            mongo|mongodb)
                task+="testMongoDb "
                category+="MONGODB,"
                flags+=" -DCI=true"
                ;;
            couchdb)
                task+="testCouchDb "
                category+="COUCHDB,"
                flags+=" -DCI=true"
                ;;
            rest|restful|restapi)
                task+="testRestful "
                category+="RESTFULAPI,"
                ;;
            mysql)
                task+="testMySQL "
                category+="MYSQL,"
                ;;
            maria|mariadb)
                task+="testMariaDb "
                category+="MariaDb,"
                flags+=" -DCI=true"
                ;;
            jdbc|jpa|database|hibernate|rdbms|hsql)
                task+="testJDBC "
                category+="JDBC,"
                ;;
            postgres|pg|postgresql)
                task+="testPostgres "
                category+="POSTGRES,"
                flags+=" -DCI=true"
                ;;
            cassandra)
                task+="testCassandra "
                category+="CASSANDRA,"
                flags+=" -DCI=true"
                ;;
            kafka)
                task+="testKafka "
                category+="KAFKA,"
                flags+=" -DCI=true"
                ;;
            oauth)
                task+="testOAuth "
                category+="OAUTH,"
                ;;
            aws)
                task+="testAWS "
                category+="AmazonWebServices,"
                flags+=" -DCI=true"
                ;;
            oidc)
                task+="testOIDC "
                category+="OIDC,"
                ;;
            mfa|duo|gauth|webauthn|authy|fido|u2f|swivelacceptto)
                task+="testMFA "
                category+="MFA,"
                ;;
            saml|saml2)
                task+="testSAML "
                category+="SAML,"
                ;;
            radius)
                task+="testRadius "
                category+="RADIUS,"
                flags+=" -DCI=true"
                ;;
            mail|email)
                task+="testMail "
                category+="MAIL,"
                flags+=" -DCI=true"
                ;;
            zoo|zookeeper)
                task+="testZooKeeper "
                category+="ZOOKEEPER,"
                flags+=" -DCI=true"
                ;;
            dynamodb|dynamo)
                task+="testDynamoDb "
                category+="DYNAMODB,"
                flags+=" -DCI=true"
                ;;
            webflow|swf)
                task+="testWebflow "
                category+="WEBFLOW,"
                ;;
            oracle)
                task+="testOracle "
                category+="ORACLE,"
                flags+=" -DCI=true"
                ;;
            redis)
                task+="testRedis "
                category+="REDIS,"
                flags+=" -DCI=true"
                ;;
            activemq|amq)
                task+="testActiveMQ "
                category+="ActiveMQ,"
                flags+=" -DCI=true"
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

category=`echo $category | sed 's/,$//'`

if [[ -z "$task" || -z "$category" ]]
then
  printHelp
  exit 1
fi


cmdstring="\033[1m$gradleCmd \e[32m$task\e[39m-DtestCategoryType=\e[33m$category\e[36m$tests\e[39m $flags ${coverage}${debug}${parallel}\e[39m"
printf "$cmdstring \e[0m\n"

cmd="$gradleCmd $task -DtestCategoryType=$category $tests $flags ${coverage} ${debug} ${parallel}"
eval "$cmd"

