#!/bin/bash

prepCommand="echo 'Running command...'; "
gradle="./gradlew $@"
gradleBuild=""
gradleBuildOptions="--stacktrace --build-cache --configure-on-demand --no-daemon "

echo -e "***********************************************"
echo -e "Gradle build started at `date`"
echo -e "***********************************************"

runAnalysis=false

if [ "$TRAVIS_PULL_REQUEST" == "true" ]; then
    echo "Reviewing changes that might affect the Gradle build in this pull request..."

    results=`git diff --name-only HEAD~1`
    for i in "$results"
        do
            :
            if [[ "$i" =~ gradle ]]; then
                echo "Changes affect Gradle build descriptors. Dependency analysis will run."
                runAnalysis=true
                break
            fi
    done
else
    echo "Dependency analysis will run against branch $TRAVIS_BRANCH"
    runAnalysis=true
fi

if [ "$runAnalysis" = true ]; then
    echo "Running dependency analysis..."

    gradleBuild="$gradleBuild dependencyCheckAnalyze dependencyCheckUpdate -x javadoc -x check \
        -DskipNpmLint=true -DskipGradleLint=true --parallel -DskipSass=true -DskipNpmLint=true \
        -DskipNodeModulesCleanUp=true -DskipNpmCache=true -DskipNestedConfigMetadataGen=true "

    if [[ "${TRAVIS_COMMIT_MESSAGE}" == *"[show streams]"* ]]; then
        gradleBuild="$gradleBuild -DshowStandardStreams=true "
    fi

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
