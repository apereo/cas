#!/bin/bash

RED="\e[31m"
GREEN="\e[32m"
CYAN="\e[36m"
ENDCOLOR="\e[0m"

#clear
find ./ci/tests -type f -name "*.sh" -exec chmod +x {} \;

dockerPlatform="unknown"
docker ps &> /dev/null
if [[ $? -ne 0 ]] ; then
  printf "\n${RED}Docker engine is not available.${ENDCOLOR}"
else
  dockerPlatform=$(docker version --format '{{json .Server.Os}}')
  printf "\nDocker engine platform is ${GREEN}%s${ENDCOLOR}\n" "$dockerPlatform."
fi

function isDockerOnLinux() {
  if [[ $dockerPlatform =~ "linux" ]]; then
    return 0
  fi
  printf "${RED}Docker engine is not available for the linux platform.%n${ENDCOLOR}"
  return 1
}

function isDockerOnWindows() {
  if [[ $dockerPlatform =~ "windows" ]]; then
    return 0
  fi
  printf "${RED}Docker engine is not available for the windows platform.%n${ENDCOLOR}"
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
flags="--no-daemon --configure-on-demand --build-cache -x javadoc -x check -Dverbose=true "
coverageTask=""

while (( "$#" )); do
    case "$1" in
    --max-workers)
        parallel="${parallel} --max-workers=8"
        shift
        ;;
    --no-cache)
        parallel="--no-configuration-cache "
        shift
        ;;
    --no-parallel)
        parallel="--no-parallel "
        shift
        ;;
    --pts)
        printf "Running tests with predictive test selection mode: ${GREEN}$2${ENDCOLOR}\n"
        flags+=" -Dpts.mode=$2 "
        shift 2
        ;;
    --no-pts)
        flags+=" -DPTS_ENABLED=false "
        shift
        ;;
    --with-coverage)
        currentDir=`pwd`
        case "${currentDir}" in
            *api*|*core*|*support*|*webapp*)
                coverageTask="jacocoTestReport"
                ;;
            *)
                coverageTask="jacocoRootReport"
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
    --events)
        flags+=" -DtestLoggingEvents=$2 "
        shift 2
        ;;
    --offline)
        flags+=" --offline "
        shift
        ;;
    --no-watch)
        flags+=" --no-watch-fs "
        shift
        ;;
    --test|--tests)
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
    --no-config-cache)
        flags+=" --no-configuration-cache "
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
            grouper)
                task+="testGrouper "
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
            authnpolicy|authpolicy|authenticationpolicy)
                task+="testAuthenticationPolicy "
                ;;
            authz|heimdall|authorization)
                isDockerOnLinux && ./ci/tests/mysql/run-mysql-server.sh
                task+="testAuthorization "
                ;;
            auth|authn|authentication)
                task+="testAuthentication "
                ;;
            tickets|ticketing)
                task+="testTickets "
                ;;
            syncope)
                isDockerOnLinux && ./ci/tests/syncope/run-syncope-server.sh
                task+="testSyncope "
                ;;
            native|graal|graalvm)
                task+="testNative "
                ;;
            delegation)
                task+="testDelegation "
                ;;
            cookie)
                task+="testCookie "
                ;;
            consent)
                task+="testConsent "
                ;;
            duo|duosecurity)
                task+="testDuoSecurity "
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
            cipher)
                task+="testCipher "
                ;;
            elastic)
                isDockerOnLinux && ./ci/tests/elastic/run-elastic-apm.sh
                task+="testElastic "
                ;;
            gcp|googlecloud|gcloud)
                isDockerOnLinux && ./ci/tests/gcp/run-gcp-server.sh
                task+="testGCP "
                ;;
            web)
                isDockerOnLinux && ./ci/tests/httpbin/run-httpbin-server.sh
                task+="testWeb "
                ;;
            scim)
                isDockerOnLinux && ./ci/tests/scim/run-scim-server.sh
                task+="testSCIM "
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
            password-ops|pswd|pswd-ops|psw|passwordops|ppolicy)
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
            geolocation|geo)
                task+="testGeoLocation "
                ;;
            git|scm)
                task+="testGit "
                ;;
            groovy|script)
                task+="testGroovy "
                ;;
            groovyauthentication)
                task+="testGroovyAuthentication "
                ;;
            groovymfa)
                task+="testGroovyMfa "
                ;;
            groovyservices)
                task+="testGroovyServices "
                ;;
            hibernate)
                task+="testHibernate "
                ;;
            jdbcmfa)
                task+="testJDBCMFA "
                ;;
            jdbc|jpa|database|db|rdbms|hsql)
                task+="testJDBC "
                ;;
            jdbcauthentication|jdbcauthn)
                task+="testJDBCAuthentication "
                ;;
            oauthtoken)
                task+="testOAuthToken "
                ;;
            oauthweb)
                task+="testOAuthWeb "
                ;;
            oauth)
                task+="testOAuth "
                ;;
            oidcservices)
                isDockerOnLinux && ./ci/tests/mail/run-mail-server.sh
                task+="testOIDCServices "
                ;;
            oidcauthentication|oidcauthn)
                isDockerOnLinux && ./ci/tests/mail/run-mail-server.sh
                task+="testOIDCAuthentication "
                ;;
            oidcattributes|oidcclaims|oidcattrs)
                isDockerOnLinux && ./ci/tests/mail/run-mail-server.sh
                task+="testOIDCAttributes "
                ;;
            oidcweb)
                isDockerOnLinux && ./ci/tests/mail/run-mail-server.sh
                task+="testOIDCWeb "
                ;;
            oidc)
                isDockerOnLinux && ./ci/tests/mail/run-mail-server.sh
                task+="testOIDC "
                ;;
            mfa)
                task+="testMFA "
                ;;
            mfaprovider|gauth|webauthn|fido)
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
            saml1)
                task+="testSAML1 "
                ;;
            saml2web|samlweb)
                task+="testSAML2Web "
                ;;
            saml2)
                task+="testSAML2 "
                ;;
            samlresponse)
                task+="testSAMLResponse "
                ;;
            samlattributes|samlattrs)
                task+="testSAMLAttributes "
                ;;
            saml)
                task+="testSAML "
                ;;
            samllogout)
                task+="testSAMLLogout "
                ;;
            jmx)
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
            webflowaccountactions)
                task+="testWebflowAccountActions "
                ;;
            webflowserviceactions)
                task+="testWebflowServiceActions "
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
            spnego)
                task+="testSpnego "
                ;;
            azure|cosmosdb)
                isDockerOnLinux && ./ci/tests/cosmosdb/run-cosmosdb-server.sh
                task+="testAzure "
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
            activedirectory|ad)
                isDockerOnLinux && ./ci/tests/ldap/run-ad-server.sh true
                task+="testActiveDirectory "
                ;;
            ldapservices)
                isDockerOnLinux && ./ci/tests/ldap/run-ldap-server.sh
                task+="testLdapServices "
                ;;
            ldaprepository|ldaprepo)
                isDockerOnLinux && ./ci/tests/ldap/run-ldap-server.sh
                task+="testLdapRepository "
                ;;
            ldapauthentication|ldapauthn)
                isDockerOnLinux && ./ci/tests/ldap/run-ldap-server.sh
                task+="testLdapAuthentication "
                ;;
            ldapattributes|ldapattrs)
                isDockerOnLinux && ./ci/tests/ldap/run-ldap-server.sh
                task+="testLdapAttributes "
                ;;
            ldap)
                isDockerOnLinux && ./ci/tests/ldap/run-ldap-server.sh
                task+="testLdap "
                ;;
            mongodbmfa)
                isDockerOnLinux && ./ci/tests/mongodb/run-mongodb-server.sh
                task+="testMongoDbMFA "
                ;;
            mongo|mongodb)
                isDockerOnLinux && ./ci/tests/mongodb/run-mongodb-server.sh
                task+="testMongoDb "
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
            activemq|amq|jms|rabbitmq|artemis|amqp)
                isDockerOnLinux && ./ci/tests/rabbitmq/run-rabbitmq-server.sh
                task+="testAMQP "
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

if [[ -n "$coverageTask" ]]; then
  task=""
  printf "${GREEN}Running code coverage task [${coverageTask}] will disable all other task executions. Make sure all test tasks that generate code coverage data have already executed.${ENDCOLOR}\n"
fi

if [[ -z "$task" ]] && [[ -z "$coverageTask" ]]; then
  printHelp
  exit 1
fi

cmd="$gradleCmd ${GREEN}$task $tests${ENDCOLOR}${flags}${debug}${dryRun}${info}${parallel}${GREEN}$coverageTask${ENDCOLOR}"
printf "${cmd} %n"
echo
cmd="$gradleCmd $task $tests $flags ${debug} ${parallel} ${dryRun} ${info} ${coverageTask}"
eval "$cmd"
retVal=$?
echo -e "***************************************************************************************"
printf "${CYAN}Gradle build finished at `date` with exit code $retVal ${ENDCOLOR}%n"
echo -e "***************************************************************************************"

if [ $retVal == 0 ]; then
    printf "${GREEN}Gradle build finished successfully.${ENDCOLOR}%n"
    exit 0
else
    printf "${RED}Gradle build did NOT finish successfully.${ENDCOLOR}%n"
    exit $retVal
fi
