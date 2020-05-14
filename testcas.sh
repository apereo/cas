#!/bin/bash

clear

printHelp() {
    echo -e "\nUsage: ./testcas.sh --category [category1,category2,...] [--help] [--ignore-failures] [--no-wrapper] [--no-retry] [--debug] [--coverage]\n"
    echo -e "Available test categories are:\n"
    echo -e "simple, memcached,cassandra,groovy,kafka,ldap,rest,mfa,jdbc,mssql,oracle,radius,couchdb,\
mariadb,files,postgres,dynamodb,couchbase,uma,saml,mail,aws,activemq,\
oauth,oidc,redis,webflow,mongo,ignite,influxdb,zookeeper,mysql,x509,shell"
    echo -e "\nPlease see the test script for details.\n"
}

parallel="--parallel "
gradleCmd="./gradlew"
flags="--build-cache -x javadoc -x check -DignoreTestFailures=false -DskipNestedConfigMetadataGen=true \
-DshowStandardStreams=true --no-daemon --configure-on-demand"

while (( "$#" )); do
    case "$1" in
    --coverage)
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
                ;;
            memcached|memcache|kryo)
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
            mssql)
                task+="testMsSqlServer "
                ;;
            ignite)
                task+="testIgnite "
                ;;
            influx|influxdb)
                task+="testInfluxDb "
                ;;
            ldap|ad|activedirectory)
                task+="testLdap "
                ;;
            couchbase)
                task+="testCouchbase "
                ;;
            mongo|mongodb)
                task+="testMongoDb "
                ;;
            couchdb)
                task+="testCouchDb "
                ;;
            rest|restful|restapi)
                task+="testRestful "
                ;;
            mysql)
                task+="testMySQL "
                ;;
            maria|mariadb)
                task+="testMariaDb "
                ;;
            jdbc|jpa|database|db|hibernate|rdbms|hsql)
                task+="testJDBC "
                ;;
            postgres|pg|postgresql)
                task+="testPostgres "
                ;;
            cassandra)
                task+="testCassandra "
                ;;
            kafka)
                task+="testKafka "
                ;;
            oauth)
                task+="testOAuth "
                ;;
            aws)
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
                task+="testRadius "
                ;;
            mail|email)
                task+="testMail "
                ;;
            zoo|zookeeper)
                task+="testZooKeeper "
                ;;
            dynamodb|dynamo)
                task+="testDynamoDb "
                ;;
            webflow|swf)
                task+="testWebflow "
                ;;
            oracle)
                task+="testOracle "
                ;;
            redis)
                task+="testRedis "
                ;;
            activemq|amq)
                task+="testActiveMQ "
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
