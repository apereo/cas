#!/bin/bash

RED="\e[31m"
GREEN="\e[32m"
YELLOW="\e[33m"
CYAN="\e[36m"
ENDCOLOR="\e[0m"

clear

hasDocker() {
  type docker &> /dev/null
  if [[ $? -ne 0 ]] ; then
    echo "Docker is not available."
    return 1
  fi
  dockerserver=$(docker version --format '{{json .Server.Os}}')
  echo -n "Docker server is ${dockerserver}."
  if [[ $dockerserver =~ "linux" ]]; then
    return 0
  fi
  echo "Docker server is not linux."
  return 1
}

printHelp() {
    hasDocker
    printf "\nUsage: ${CYAN}./testcas.sh${ENDCOLOR} --category [category1,category2,...] [--help] [--test TestClass]\n\t[--ignore-failures] [--no-watch] [--no-wrapper] [--no-retry] [--debug] [--no-parallel]\n\t[--dry-run][--info] [--with-coverage] [--no-build-cache] \n"
    printf "\nTo see what test categories are available, use:\n"
    printf "\t${GREEN}./gradlew -q testCategories${ENDCOLOR}\n"
    echo -e "\nPlease see the test script for details."
}

task="cleanTest "
parallel="--parallel "
dryRun=""
info=""
gradleCmd="./gradlew"
flags="--no-daemon --configure-on-demand --build-cache -x javadoc -x check -DskipNestedConfigMetadataGen=true -Dverbose=true "
coverageTask=""

while (( "$#" )); do
    case "$1" in
    --no-parallel)
        parallel="--no-parallel "
        shift
        ;;
    --with-coverage)
        currentDir=`pwd`
        case "${currentDir}" in
            *api*|*core*|*support*|*webapp*)
                coverageTask="jacocoTestReport "
                ;;
            *)
                coverageTask="jacocoRootReport "
                ;;
        esac
        shift
        ;;
    --info)
        info="--info "
        shift
        ;;
    --dry-run)
        dryRun="--dry-run "
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
        debug=" --debug-jvm "
        parallel=""
        shift
        ;;
    --no-watch)
        flags+=" --no-watch-fs"
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
    --no-build-cache)
        flags+=" --no-build-cache"
        shift
        ;;
    --category)
        category="$2"
        for item in $(echo "$category" | sed "s/,/ /g")
        do
            categoryItem=$(echo "${item}" | awk '{print tolower($0)}')
            
            case "${categoryItem}" in
            test|simple|run|basic|unit|unittests)
                task+="testSimple "
                ;;
            webapp)
                task+="testWebApp "
                ;;
            throttle|throttling|bucket4j|authenticationthrottling)
                task+="testAuthenticationThrottling "
                ;;
            authnhandler|authenticationhandler)
                task+="testAuthenticationHandler "
                ;;
            authnmetadata|authenticationmetadata)
                task+="testAuthenticationMetadata "
                ;;
            authnpolicy|authenticationpolicy)
                task+="testAuthenticationPolicy "
                ;;
            auth|authn|authentication)
                task+="testAuthentication "
                ;;
            tickets|ticketing)
                task+="testTickets "
                ;;
            delegation)
                task+="testDelegation "
                ;;
            cookie)
                task+="testCookie "
                ;;
            event|events)
                task+="testEvents "
                ;;
            impersonation|surrogate)
                task+="testImpersonation "
                ;;
            x509)
                task+="testX509 "
                ;;
            shell)
                task+="testSHELL "
                ;;
            web)
                task+="testWeb "
                ;;
            logout|slo)
                task+="testLogout "
                ;;
            cas)
                task+="testCAS "
                ;;
            metrics|stats)
                task+="testMetrics "
                ;;
            services|regsvc|registeredservice)
                task+="testRegisteredService "
                ;;
            actuator|endpoint|actuatorendpoint)
                task+="testActuatorEndpoint "
                ;;
            utility|utils|util)
                task+="testUtility "
                ;;
            wsfed|wsfederation)
                task+="testWSFederation "
                ;;
            attrs|attr|attributes)
                task+="testAttributes "
                ;;
            expiration-policy|exppolicy|expp|expirationpolicy)
                task+="testExpirationPolicy "
                ;;
            password-ops|pswd|pswd-ops|psw|passwordops)
                task+="testPasswordOps "
                ;;
            sms)
                task+="testSMS "
                ;;
            audit|audits)
                task+="testAudits "
                ;;
            uma)
                task+="testUMA "
                ;;
            filesystem|files|file|fsys)
                task+="testFileSystem "
                ;;
            config|casconfig|ccfg|cfg|cas-config|casconfiguration)
                task+="testCasConfiguration "
                ;;
            groovy|script)
                task+="testGroovy "
                ;;
            jdbc|jpa|database|db|hibernate|rdbms|hsql)
                task+="testJDBC "
                ;;
            oauth)
                task+="testOAuth "
                ;;
            oidc)
                task+="testOIDC "
                ;;
            mfa|duo|gauth|webauthn|authy|fido|u2f|swivel|acceptto)
                task+="testMFA "
                ;;
            mfatrigger)
                task+="testMFATrigger "
                ;;
            mfatrusteddevices|mfadevices|trusteddevices)
                task+="testMFATrustedDevices "
                ;;
            saml2sp|samlsp|samlserviceprovider)
                task+="testSAMLServiceProvider "
                ;;
            metadata|md|samlmetadata)
                task+="testSAMLMetadata "
                ;;
            saml|saml2)
                task+="testSAML "
                ;;
            jmx|jmx)
                task+="testJMX "
                ;;
            rest|restful|restapi|restfulapi)
                task+="testRestfulApi "
                ;;
            webflow-mfa-actions|swf-mfa_actions|webflowmfaactions)
                task+="testWebflowMfaActions "
                ;;
            webflowauthenticationactions|swfauthnactions|webflowauthnactions)
                task+="testWebflowAuthenticationActions "
                ;;
            webflowactions|swfactions|webflow-actions)
                task+="testWebflowActions "
                ;;
            webflowevents|webflow-events)
                task+="testWebflowEvents "
                ;;
            webflowconfig|swfcfg|webflowcfg|webflow-config)
                task+="testWebflowConfig "
                ;;
            webflowmfaconfig)
                task+="testWebflowMfaConfig "
                ;;
            webflow|swf)
                task+="testWebflow "
                ;;
            hz|hazelcast)
                task+="testHazelcast "
                ;;
            ignite)
                task+="testIgnite "
                ;;
            infinispan)
                task+="testInfinispan"
                ;;
            spnego)
                task+="testSpnego"
                ;;
            cosmosdb|cosmos)
                task+="testCosmosDb "
                ;;
            simple|unit)
                task+="testSimple "
                ;;
            mssql|mssqlserver)
                hasDocker && ./ci/tests/mssqlserver/run-mssql-server.sh
                task+="testMsSqlServer "
                ;;
            influx|influxdb)
                hasDocker && ./ci/tests/influxdb/run-influxdb-server.sh
                task+="testInfluxDb "
                ;;
            memcached|memcache|kryo)
                hasDocker && ./ci/tests/memcached/run-memcached-server.sh
                task+="testMemcached "
                ;;
            ehcache)
                hasDocker && ./ci/tests/ehcache/run-terracotta-server.sh
                task+="testEhcache "
                ;;
            ldap|ad|activedirectory)
                hasDocker && ./ci/tests/ldap/run-ldap-server.sh
                hasDocker && ./ci/tests/ldap/run-ad-server.sh true
                task+="testLdap "
                ;;
            couchbase)
                hasDocker && ./ci/tests/couchbase/run-couchbase-server.sh
                task+="testCouchbase "
                ;;
            mongo|mongodb)
                hasDocker && ./ci/tests/mongodb/run-mongodb-server.sh
                task+="testMongoDb "
                ;;
            couchdb)
                hasDocker && ./ci/tests/couchdb/run-couchdb-server.sh
                task+="testCouchDb "
                ;;
            mysql)
                hasDocker && ./ci/tests/mysql/run-mysql-server.sh
                task+="testMySQL "
                ;;
            maria|mariadb)
                hasDocker && ./ci/tests/mariadb/run-mariadb-server.sh
                task+="testMariaDb "
                ;;
            postgres|pg|postgresql)
                hasDocker && ./ci/tests/postgres/run-postgres-server.sh
                task+="testPostgres "
                ;;
            cassandra)
                hasDocker && ./ci/tests/cassandra/run-cassandra-server.sh
                task+="testCassandra "
                ;;
            kafka)
                hasDocker && ./ci/tests/kafka/run-kafka-server.sh
                task+="testKafka "
                ;;
            aws|amz|amazonwebservices)
                hasDocker && ./ci/tests/aws/run-aws-server.sh
                task+="testAmazonWebServices "
                ;;
            radius)
                hasDocker && ./ci/tests/radius/run-radius-server.sh
                task+="testRadius "
                ;;
            mail|email)
                hasDocker && ./ci/tests/mail/run-mail-server.sh
                task+="testMail "
                ;;
            zoo|zookeeper)
                hasDocker && ./ci/tests/zookeeper/run-zookeeper-server.sh
                task+="testZooKeeper "
                ;;
            dynamodb|dynamo)
                hasDocker && ./ci/tests/dynamodb/run-dynamodb-server.sh
                task+="testDynamoDb "
                ;;
            oracle)
                hasDocker && ./ci/tests/oracle/run-oracle-server.sh
                task+="testOracle "
                ;;
            redis)
                hasDocker && ./ci/tests/redis/run-redis-server.sh
                task+="testRedis "
                ;;
            activemq|amq|jms)
                hasDocker && ./ci/tests/activemq/run-activemq-server.sh
                task+="testJMS "
                ;;
            *)
                printf "${RED}Unable to recognize test category: ${item}${ENDCOLOR}\n"
                printHelp
                exit 1
                ;;
            esac
        done
        shift 2
        ;;
    *)
        printf "${RED}Unable to accept parameter: $1${ENDCOLOR}\n"
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

cmd="$gradleCmd ${GREEN}$task $tests${ENDCOLOR}${flags}${debug}${dryRun}${info}${parallel}${GREEN}$coverageTask${ENDCOLOR}"
printf "${cmd}\n"

cmd="$gradleCmd $task $tests $flags ${debug} ${parallel} ${dryRun} ${info} ${coverageTask}"
eval "$cmd"
retVal=$?
echo -e "***************************************************************************************"
printf "${CYAN}Gradle build finished at `date` with exit code $retVal ${ENDCOLOR}\n"
echo -e "***************************************************************************************"

if [ $retVal == 0 ]; then
    printf "${GREEN}Gradle build finished successfully.${ENDCOLOR}\n"
else
    printf "${RED}Gradle build did NOT finish successfully.${ENDCOLOR}"
    exit $retVal
fi
