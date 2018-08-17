#!/bin/bash
source ./ci/functions.sh

runBuild=false
echo "Reviewing changes that might affect the Gradle build..."
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

if [ "$runBuild" = false ]; then
    exit 0
fi

prepCommand="echo 'Running command...'; "
gradle="./gradlew $@"
gradleBuild=""
gradleBuildOptions="--stacktrace --build-cache --configure-on-demand --no-daemon "

echo -e "***********************************************"
echo -e "Gradle build started at `date`"
echo -e "***********************************************"

echo -e "The build will deploy SNAPSHOT artifacts to Sonatype under Travis job ${TRAVIS_JOB_NUMBER}"

echo -e "Installing NPM...\n"
./gradlew npmInstall --stacktrace -q

gradleBuild="$gradleBuild assemble uploadArchives -x test -x javadoc -x check \
        -DskipNpmLint=true -DskipNestedConfigMetadataGen=true \
        -DpublishSnapshots=true -DsonatypeUsername=${SONATYPE_USER} \
        -DsonatypePassword=${SONATYPE_PWD} --parallel "

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
        exit 0
    else
        echo "Gradle build did NOT finish successfully."
        exit $retVal
    fi
fi
