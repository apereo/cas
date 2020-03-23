#!/bin/bash
source ./ci/functions.sh

runBuild=false
echo "Reviewing changes that might affect the Gradle build..."
currentChangeSetAffectsTests
retval=$?
if [ "$retval" == 0 ]
then
    echo "Found changes that require the build to run test cases."
    runBuild=true
else
    echo "Changes do NOT affect project test cases."
    runBuild=false
fi

if [ "$runBuild" = false ]; then
    exit 0
fi


gradle="./gradlew $@"
gradleBuild=""
gradleBuildOptions="--build-cache --configure-on-demand --no-daemon -DtestCategoryType=COUCHDB "

echo -e "***********************************************"
echo -e "Gradle build started at `date`"
echo -e "***********************************************"

gradleBuild="$gradleBuild testCouchDb jacocoRootReport -x test -x javadoc -x check \
    --parallel \
    -DskipNestedConfigMetadataGen=true "

if [[ "${TRAVIS_COMMIT_MESSAGE}" == *"[show streams]"* ]]; then
    gradleBuild="$gradleBuild -DshowStandardStreams=true "
fi

if [[ "${TRAVIS_COMMIT_MESSAGE}" == *"[rerun tasks]"* ]]; then
    gradleBuild="$gradleBuild --rerun-tasks "
fi

if [[ "${TRAVIS_COMMIT_MESSAGE}" == *"[refresh dependencies]"* ]]; then
    gradleBuild="$gradleBuild --refresh-dependencies "
fi

if [ -z "$gradleBuild" ]; then
    echo "Gradle build will be ignored since no commands are specified to run."
else
    ./ci/tests/couchdb/run-couchdb-server.sh
    retVal=$?
    if [ $retVal == 0 ]; then
        echo "CouchDb initialization finished successfully."
    else
        echo "CouchDb initialization did NOT finish successfully."
        exit $retVal
    fi

    tasks="$gradle $gradleBuildOptions $gradleBuild"
    echo -e "***************************************************************************************"

    echo $tasks
    echo -e "***************************************************************************************"

    waitloop="while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &"
    eval $waitloop
    waitRetVal=$?


    eval $tasks
    retVal=$?

    echo -e "***************************************************************************************"
    echo -e "Gradle build finished at `date` with exit code $retVal"
    echo -e "***************************************************************************************"

    if [ $retVal == 0 ]; then
        echo "Uploading test coverage results..."
        bash <(curl -s https://codecov.io/bash) -F CouchDb
        echo "Gradle build finished successfully."
    else
        echo "Gradle build did NOT finish successfully."
        exit $retVal
    fi
fi
