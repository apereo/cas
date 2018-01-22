#!/bin/bash

if [ "$TRAVIS_PULL_REQUEST" != "false" ] && [ "$PUBLISH_SNAPSHOTS" == "true" ]; then
    echo -e "Skipping build since this is a pull request and we are not publishing snapshots.\n"
    exit 0
fi

gradle="sudo ./gradlew $@"

gradleBuildOptions="--stacktrace --parallel -DskipNpmCache=true"
gradleBuild="assemble"

if [ "$PUBLISH_SNAPSHOTS" == "false" ]; then
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
else
    echo -e "The build is publishing snapshots; Skipping tests and checks...\n"
    gradleBuild="$gradleBuild -x test -x check -x javadoc"
fi

tasks="$gradle $gradleBuildOptions $gradleBuild"
echo $tasks
eval $tasks
retVal=$?
echo -e "******************************************************************"
echo -e "Gradle build finished at `date` with exit code $retVal"
echo -e "******************************************************************"
if [ $retVal == 0 ]; then
    echo "Gradle build finished successfully."
else
    echo "Gradle build did NOT finish successfully."
    exit $retVal
fi
