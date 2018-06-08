#!/bin/bash

branchName="master"

prepCommand="echo 'Running command...'; "
gradle="sudo ./gradlew $@"
gradleBuild=""
gradleBuildOptions="--stacktrace --build-cache --configure-on-demand --no-daemon --parallel "

echo -e "***********************************************"
echo -e "Gradle build started at `date`"
echo -e "***********************************************"

if [ "$MATRIX_JOB_TYPE" == "BUILD" ]; then
    gradleBuild="$gradleBuild build -x test -x javadoc -x check -DskipNpmLint=true \
    -DenableIncremental=true -DskipNestedConfigMetadataGen=true "
elif [ "$MATRIX_JOB_TYPE" == "SNAPSHOT" ]; then
    if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "$branchName" ]; then
        if [[ "${TRAVIS_COMMIT_MESSAGE}" == *"[skip snapshots]"* ]]; then
            echo -e "The build will skip deploying SNAPSHOT artifacts to Sonatype under Travis job ${TRAVIS_JOB_NUMBER}"
            gradleBuild=""
        else
            echo -e "The build will deploy SNAPSHOT artifacts to Sonatype under Travis job ${TRAVIS_JOB_NUMBER}"
            gradleBuild="$gradleBuild assemble uploadArchives -x test -x javadoc -x check \
                -DenableIncremental=true -DskipNpmLint=true -DskipNestedConfigMetadataGen=true  
                -DpublishSnapshots=true -DsonatypeUsername=${SONATYPE_USER} \
                -DsonatypePassword=${SONATYPE_PWD}"
        fi
    else
        echo -e "*******************************************************************************************************"
        echo -e "Skipping SNAPSHOTs since the change-set is a pull request or not targeted at branch $branchName.\n"
        echo -e "*******************************************************************************************************"
    fi
elif [ "$MATRIX_JOB_TYPE" == "CFGMETADATA" ]; then
     gradleBuild="$gradleBuild :api:cas-server-core-api-configuration-model:build -x check -x test -x javadoc \
     -DskipGradleLint=true -DskipSass=true \
     -DskipNodeModulesCleanUp=true -DskipNpmCache=true  "
elif [ "$MATRIX_JOB_TYPE" == "STYLE" ]; then
     gradleBuild="$gradleBuild check -x test -x javadoc -DenableIncremental=true \
     -DskipGradleLint=true -DskipSass=true -DskipNestedConfigMetadataGen=true \
     -DskipNodeModulesCleanUp=true -DskipNpmCache=true "
elif [ "$MATRIX_JOB_TYPE" == "JAVADOC" ]; then
     gradleBuild="$gradleBuild javadoc -x test -x check -DskipNpmLint=true \
     -DskipGradleLint=true -DskipSass=true -DenableIncremental=true -DskipNestedConfigMetadataGen=true \
     -DskipNodeModulesCleanUp=true -DskipNpmCache=true "
elif [ "$MATRIX_JOB_TYPE" == "TEST" ]; then
    if [ "$MATRIX_SERVER" == "NONE" ]; then
        gradleBuild="$gradleBuild test "
    elif [ "$MATRIX_SERVER" == "CASSANDRA" ]; then
        ./ci/run-cassandra-server.sh
        gradleBuild="$gradleBuild testCassandra  "
    elif [ "$MATRIX_SERVER" == "COUCHBASE" ]; then
        ./ci/run-couchbase-server.sh
        gradleBuild="$gradleBuild testCouchbase  "
    elif [ "$MATRIX_SERVER" == "COSMOSDB" ]; then
        gradleBuild="$gradleBuild testCosmosDb  "
    elif [ "$MATRIX_SERVER" == "DYNAMODB" ]; then
        ./ci/run-dynamodb-server.sh
        gradleBuild="$gradleBuild testDynamoDb  "
    elif [ "$MATRIX_SERVER" == "FILESYSTEM" ]; then
        gradleBuild="$gradleBuild testFileSystem  "
    elif [ "$MATRIX_SERVER" == "IGNITE" ]; then
        gradleBuild="$gradleBuild testIgnite  "
    elif [ "$MATRIX_SERVER" == "INFLUXDB" ]; then
        ./ci/run-influxdb-server.sh
        gradleBuild="$gradleBuild testInfluxDb  "
    elif [ "$MATRIX_SERVER" == "LDAP" ]; then
        ./ci/run-ldap-server.sh
        gradleBuild="$gradleBuild testLdap  "
    elif [ "$MATRIX_SERVER" == "MAIL" ]; then
        ./ci/run-mail-server.sh
        gradleBuild="$gradleBuild testMail  "
    elif [ "$MATRIX_SERVER" == "COUCHDB" ]; then
        ./ci/run-couchdb-server.sh
        gradleBuild="$gradleBuild testCouchDb "
    elif [ "$MATRIX_SERVER" == "MEMCACHED" ]; then
        ./ci/run-memcached-server.sh
        gradleBuild="$gradleBuild testMemcached "
    elif [ "$MATRIX_SERVER" == "MYSQL" ]; then
        ./ci/run-mysql-server.sh
        gradleBuild="$gradleBuild testMySQL "
    elif [ "$MATRIX_SERVER" == "MSSQLSERVER" ]; then
        ./ci/run-mssql-server.sh
        gradleBuild="$gradleBuild testMsSqlServer "
    elif [ "$MATRIX_SERVER" == "MONGODB" ]; then
        gradleBuild="$gradleBuild testMongoDb  "
    elif [ "$MATRIX_SERVER" == "REDIS" ]; then
        gradleBuild="$gradleBuild testRedis  "
    elif [ "$MATRIX_SERVER" == "ALL" ]; then
        gradleBuild="$gradleBuild testAll  "
    fi
    gradleBuild="$gradleBuild coveralls -x javadoc -x check \
    -DskipNpmLint=true -DskipGradleLint=true -DskipSass=true -DskipNpmLint=true \
    -DskipNodeModulesCleanUp=true -DskipNpmCache=true -DskipNestedConfigMetadataGen=true "
elif [ "$MATRIX_JOB_TYPE" == "DEPANALYZE" ]; then
    gradleBuild="$gradleBuild dependencyCheckAnalyze dependencyCheckUpdate -x javadoc -x check \
    -DskipNpmLint=true -DskipGradleLint=true -DskipSass=true -DskipNpmLint=true \
    -DskipNodeModulesCleanUp=true -DskipNpmCache=true -DskipNestedConfigMetadataGen=true "
elif [ "$MATRIX_JOB_TYPE" == "DEPUPDATE" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "$branchName" ]; then
    gradleBuild="$gradleBuild dependencyUpdates -Drevision=release -x javadoc -x check  \
    -DskipNpmLint=true -DskipGradleLint=true -DskipSass=true -DskipNestedConfigMetadataGen=true \
    -DskipNodeModulesCleanUp=true -DskipNpmCache=true "
fi

if [[ "${TRAVIS_COMMIT_MESSAGE}" == *"[show streams]"* ]]; then
    gradleBuild="$gradleBuild -DshowStandardStreams=true "
fi

if [ -z "$gradleBuild" ]; then
    echo "Gradle build will be ignored since no commands are specified to run."
else
    tasks="$gradle $gradleBuildOptions $gradleBuild"
    echo -e "***************************************************************************************"
    echo $prepCommand
    echo $tasks
    echo -e "***************************************************************************************"

    waitloop="while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &"
    eval $waitloop
    waitRetVal=$?

    eval $prepCommand
    eval $tasks
    retVal=$?

    echo -e "***************************************************************************************"
    echo -e "Gradle build finished at `date` with exit code $retVal"
    echo -e "***************************************************************************************"

    if [ $retVal == 0 ]; then
        echo "Gradle build finished successfully."
    else
        echo "Gradle build did NOT finish successfully."
        exit $retVal
    fi
fi
