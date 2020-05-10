#!/bin/bash
source ./ci/functions.sh

gradle="./gradlew $@"
gradleBuild=""
gradleBuildOptions="--build-cache --configure-on-demand --no-daemon -DtestCategoryType=SIMPLE "

echo -e "***********************************************"
echo -e "Gradle build started at `date`"
echo -e "***********************************************"

gradleBuild="$gradleBuild assemble test jacocoRootReport --parallel -x javadoc -x check \
    -DskipNestedConfigMetadataGen=true -DshowStandardStreams=true "

if [[ "${TRAVIS_COMMIT_MESSAGE}" == *"[rerun tasks]"* ]]; then
    gradleBuild="$gradleBuild --rerun-tasks "
fi

if [[ "${TRAVIS_COMMIT_MESSAGE}" == *"[refresh dependencies]"* ]]; then
    gradleBuild="$gradleBuild --refresh-dependencies "
fi

if [ -z "$gradleBuild" ]; then
    echo "Gradle build will be ignored since no commands are specified to run."
else
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
        bash <(curl -s https://codecov.io/bash) -F Simple
        echo "Gradle build finished successfully."
    else
        echo "Gradle build did NOT finish successfully."
        exit $retVal
    fi
fi
