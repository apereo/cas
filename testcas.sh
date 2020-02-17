#!/bin/bash

clear

printHelp() {
    echo -e "Usage: ./testcas.sh --category [category1,category2,...] [--help] [--debug] [--coverage]\n"
    echo -e "Available test categories are:\n"
    echo -e "\t - simple"
    echo -e "\t - memcached"
    echo -e "\t - cassandra"
    echo -e "\t - groovy"
    echo -e "\t - ldap"
    echo -e "\t - rest"
    echo -e "\t - mfa"
    echo -e "\t - jdbc"
    echo -e "\t - mssql"
    echo -e "\t - oracle"
    echo -e "\t - radius"
    echo -e "\t - couchdb"
    echo -e "\t - mariadb"
    echo -e "\t - files"
    echo -e "\t - postgres"
    echo -e "\t - dynamodb"
    echo -e "\t - couchbase"
    echo -e "\t - uma"
    echo -e "\t - saml"
    echo -e "\t - mail"
    echo -e "\t - aws"
    echo -e "\t - activemq"
    echo -e "\t - oauth"
    echo -e "\t - oidc"
    echo -e "\t - redis"
    echo -e "\t - webflow"
    echo -e "\t - mongo"
    echo -e "\t - ignite"
    echo -e "\t - influxdb"
    echo -e "\t - zookeeper"
    echo -e "\t - mysql"
    echo -e "\nPlease see the test script for more available categories.\n"
}

parallel="--parallel "

while (( "$#" )); do
    case "$1" in
    --coverage)
        coverage="jacocoRootReport "
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
                ;;
            influx|influxdb)
                task+="testInfluxDb "
                category+="InfluxDb,"
                ;;
            ldap|ad|activedirectory)
                task+="testLdap "
                category+="LDAP,"
                ;;
            couchbase)
                task+="testCouchbase "
                category+="COUCHBASE,"
                ;;
            mongo|mongodb)
                task+="testMongoDb "
                category+="MONGODB,"
                ;;
            couchdb)
                task+="testCouchDb "
                category+="COUCHDB,"
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
                ;;
            jdbc|jpa|database|hibernate|rdbms|hsql)
                task+="testJDBC "
                category+="JDBC,"
                ;;
            postgres|pg|postgresql)
                task+="testPostgres "
                category+="POSTGRES,"
                ;;
            cassandra)
                task+="testCassandra "
                category+="CASSANDRA,"
                ;;
            oauth)
                task+="testOAuth "
                category+="OAUTH,"
                ;;
            aws)
                task+="testAWS "
                category+="AmazonWebServices,"
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
                ;;
            mail|email)
                task+="testMail "
                category+="MAIL,"
                ;;
            zoo|zookeeper)
                task+="testZooKeeper "
                category+="ZOOKEEPER,"
                ;;
            dynamodb|dynamo)
                task+="testDynamoDb "
                category+="DYNAMODB,"
                ;;
            webflow|swf)
                task+="testWebflow "
                category+="WEBFLOW,"
                ;;
            oracle)
                task+="testOracle "
                category+="ORACLE,"
                ;;
            redis)
                task+="testRedis "
                category+="REDIS,"
                ;;
            activemq|amq)
                task+="testActiveMQ "
                category+="ActiveMQ,"
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

flags="--build-cache -x javadoc -x check -DignoreTestFailures=false -DskipNestedConfigMetadataGen=true \
-DskipGradleLint=true -DshowStandardStreams=true --no-daemon --configure-on-demand"

cmdstring="\033[1m./gradlew jacocoRootReport \e[32m$task\e[39m-DtestCategoryType=\e[33m$category\e[36m$tests\e[39m $flags ${coverage}${debug}${parallel}\e[39m"
printf "$cmdstring \e[0m\n"

cmd="./gradlew $task -DtestCategoryType=$category $tests $flags ${coverage} ${debug} ${parallel}"
eval "$cmd"

