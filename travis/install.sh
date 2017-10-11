#!/bin/bash

if [ "$TRAVIS_PULL_REQUEST" != "false" ] && [ "$PUBLISH_SNAPSHOTS" == "true" ]; then
    echo -e "Skipping build since this is a pull request and we are not publishing snapshots.\n"
    exit 0
fi

if [ "$PUBLISH_SNAPSHOTS" == "false" ]; then

    gradle="sudo ./gradlew"
    
    gradleBuildOptions="--stacktrace --parallel --configure-on-demand --max-workers=8"
    gradleBuild="bootRepackage install"

    echo -e "The build will aggregate javadocs from all modules into one JAR file.\n"
    gradleBuild="$gradleBuild checkstyleMain"
    if [[ "${TRAVIS_COMMIT_MESSAGE}" == *"[skip tests]"* ]]; then
        echo -e "The build commit message indicates that tests should be skipped.\n"
        gradleBuild="$gradleBuild -x test"
    else
        echo -e "The build indicates that tests along with coveralls test coverage should run.\n"
        gradleBuild="$gradleBuild checkstyleTest test coveralls"
        if [[ "${TRAVIS_COMMIT_MESSAGE}" == *"[show streams]"* ]]; then
            gradleBuild="$gradleBuild -DshowStandardStreams=true"
        fi
    fi

    tasks="$gradle $gradleBuildOptions $gradleBuild"
    echo $tasks
    eval $tasks
    retVal=$?
    echo -e "Gradle build finished at `date` with exit code $retVal\n"
    
    if [ $retVal == 0 ]; then
        echo "Gradle build finished successfully."
    else
        echo "Gradle build did NOT finished successfully."
        exit $retVal
    fi
else
    echo "Gradle build is publishing snapshots; Skipping the install phase for now..."
    exit 0
fi
