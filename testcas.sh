#!/bin/bash

RED="\e[31m"
GREEN="\e[32m"
CYAN="\e[36m"
ENDCOLOR="\e[0m"

clear
find ./ci/tests -type f -name "*.sh" -exec chmod +x {} \;

dockerPlatform="unknown"
type docker &> /dev/null
if [[ $? -ne 0 ]] ; then
  echo "Docker server is not available."
else
  dockerPlatform=$(docker version --format '{{json .Server.Os}}')
  printf "Docker server platform is ${GREEN}%s${ENDCOLOR}\n" "$dockerPlatform."
fi

function isDockerOnLinux() {
  if [[ $dockerPlatform =~ "linux" ]]; then
    return 0
  fi
  printf "${RED}Docker server is not available for the linux platform.${ENDCOLOR}"
  return 1
}

function isDockerOnWindows() {
  if [[ $dockerPlatform =~ "windows" ]]; then
    return 0
  fi
  printf "${RED}Docker server is not available for the windows platform.${ENDCOLOR}"
  return 1
}

printHelp() {
    printf "\nUsage: ${CYAN}./testcas.sh${ENDCOLOR} --category [category1,category2,...] [--help] [--test TestClass]\n\t[--ignore-failures] [--no-watch] [--no-wrapper] [--no-retry] [--debug] [--no-parallel]\n\t[--dry-run][--info] [--with-coverage] [--no-build-cache] \n"
    printf "\nTo see what test categories are available, use:\n"
    printf "\t${GREEN}./gradlew -q testCategories${ENDCOLOR}\n"
    echo -e "\nPlease see the test script for details."
}

task=""
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
        flags+=" --no-watch-fs "
        shift
        ;;
    --test)
        tests="--tests \"$2\" "
        shift 2
        ;;
    --no-retry)
        flags+=" -DskipTestRetry=true "
        shift
        ;;
    --ignore-failures)
        flags+=" -DignoreTestFailures=true "
        shift
        ;;
    --no-build-cache)
        flags+=" --no-build-cache "
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
            jdbcauthentication|jdbcauthn)
                task+="testJDBCAuthentication "
                ;;
            oauth)
                task+="testOAuth "
                ;;
            oidc)
                task+="testOIDC "
                ;;
            mfa)
                task+="testMFA "
                ;;
            mfaprovider|duo|gauth|webauthn|authy|fido|u2f|swivel|acceptto)
                task+="testMFAProvider "
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
            restfulapiauthentication|restfulauthn|restauthn)
                task+="testRestfulApiAuthentication "
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
                isDockerOnLinux && ./ci/tests/cosmosdb/run-cosmosdb-server.sh
                task+="testCosmosDb "
                ;;
            simple|unit)
                task+="testSimple "
                ;;
            mssql|mssqlserver)
                isDockerOnLinux && ./ci/tests/mssqlserver/run-mssql-server.sh
                task+="testMsSqlServer "
                ;;
            influx|influxdb)
                isDockerOnLinux && ./ci/tests/influxdb/run-influxdb-server.sh
                task+="testInfluxDb "
                ;;
            memcached|memcache|kryo)
                isDockerOnLinux && ./ci/tests/memcached/run-memcached-server.sh
                task+="testMemcached "
                ;;
            ehcache)
                isDockerOnLinux && ./ci/tests/ehcache/run-terracotta-server.sh
                task+="testEhcache "
                ;;
            ldap|ad|activedirectory)
                isDockerOnLinux && ./ci/tests/ldap/run-ldap-server.sh
                isDockerOnLinux && ./ci/tests/ldap/run-ad-server.sh true
                task+="testLdap "
                ;;
            couchbase)
                isDockerOnLinux && ./ci/tests/couchbase/run-couchbase-server.sh
                task+="testCouchbase "
                ;;
            mongo|mongodb)
                isDockerOnLinux && ./ci/tests/mongodb/run-mongodb-server.sh
                task+="testMongoDb "
                ;;
            couchdb)
                isDockerOnLinux && ./ci/tests/couchdb/run-couchdb-server.sh
                task+="testCouchDb "
                ;;
            mysql)
                isDockerOnLinux && ./ci/tests/mysql/run-mysql-server.sh
                task+="testMySQL "
                ;;
            maria|mariadb)
                isDockerOnLinux && ./ci/tests/mariadb/run-mariadb-server.sh
                task+="testMariaDb "
                ;;
            postgres|pg|postgresql)
                isDockerOnLinux && ./ci/tests/postgres/run-postgres-server.sh
                task+="testPostgres "
                ;;
            cassandra)
                isDockerOnLinux && ./ci/tests/cassandra/run-cassandra-server.sh
                task+="testCassandra "
                ;;
            kafka)
                isDockerOnLinux && ./ci/tests/kafka/run-kafka-server.sh
                task+="testKafka "
                ;;
            aws|amz|amazonwebservices)
                isDockerOnLinux && ./ci/tests/aws/run-aws-server.sh
                task+="testAmazonWebServices "
                ;;
            radius)
                isDockerOnLinux && ./ci/tests/radius/run-radius-server.sh
                task+="testRadius "
                ;;
            mail|email)
                isDockerOnLinux && ./ci/tests/mail/run-mail-server.sh
                task+="testMail "
                ;;
            zoo|zookeeper)
                isDockerOnLinux && ./ci/tests/zookeeper/run-zookeeper-server.sh
                task+="testZooKeeper "
                ;;
            dynamodb|dynamo)
                isDockerOnLinux && ./ci/tests/dynamodb/run-dynamodb-server.sh
                task+="testDynamoDb "
                ;;
            oracle)
                isDockerOnLinux && ./ci/tests/oracle/run-oracle-server.sh
                task+="testOracle "
                ;;
            redis)
                isDockerOnLinux && ./ci/tests/redis/run-redis-server.sh
                task+="testRedis "
                ;;
            activemq|amq|jms)
                isDockerOnLinux && ./ci/tests/activemq/run-activemq-server.sh
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
