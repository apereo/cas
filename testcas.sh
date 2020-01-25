#!/bin/bash

clear

PARAMS=""
while (( "$#" )); do
    case "$1" in
    --coverage)
        coverage="jacocoRootReport"
        shift
        ;;
    --debug)
        debug="--debug-jvm"
        shift
        ;;
    --test)
        tests="--tests \"$2\" "
        shift 2
        ;;
    --category)
        case "$2" in
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
        shift 2
        ;;
    *)
        echo -e "Unable to accept parameter: $1"
        echo -e "Usage: ./testcas.sh --category [category] [--debug] [--coverage]"
        exit 1
        ;;
    esac
done

cmd="./gradlew $task $coverage $debug -DtestCategoryType=$category $tests \
--build-cache --parallel -x javadoc -x check -DignoreTestFailures=false -DskipNestedConfigMetadataGen=true \
-DskipGradleLint=true -DshowStandardStreams=true --no-daemon --configure-on-demand "


echo -e "$cmd\n"
eval "$cmd"

