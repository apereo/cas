#!/bin/bash
source ./ci/functions.sh

runBuild=false
echo "Reviewing changes that might affect the Gradle build..."

casVersion=$(./gradlew casVersion --no-daemon -q)
echo "Current CAS version is $casVersion"

gradle="./gradlew $@"
gradleBuild=""
gradleBuildOptions="--build-cache --configure-on-demand --no-daemon  \
                    -Dorg.gradle.internal.http.socketTimeout=180000 \
                    -Dorg.gradle.internal.http.connectionTimeout=180000 "

echo -e "***********************************************"
echo -e "Gradle build started at `date`"
echo -e "***********************************************"

if [[ "$casVersion" == *"-SNAPSHOT" ]]; then
    currentChangeSetAffectsSnapshots
    retval=$?
    if [ "$retval" == 0 ]
    then
        echo "Found changes that require snapshots to be published."
        runBuild=true
    else
        echo "Changes do NOT affect project snapshots."
        runBuild=false
    fi
else
    runBuild=false
fi

if [ "$runBuild" = false ]; then
    echo -e "Gradle build will not run under Travis job ${TRAVIS_JOB_NUMBER}"
    exit 0
fi


echo -e "The build will deploy SNAPSHOT artifacts to Sonatype under Travis job ${TRAVIS_JOB_NUMBER}"
gradleBuild="$gradleBuild assemble publish -x test -x javadoc -x check \
        -DpublishSnapshots=true -DsonatypeUsername=${SONATYPE_USER} \
        -DsonatypePassword=${SONATYPE_PWD} --parallel "


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
        echo "Gradle build finished successfully."
        exit 0
    else
        echo "Gradle build did NOT finish successfully."
        exit $retVal
    fi
fi
