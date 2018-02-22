#!/bin/bash

gradle="sudo ./gradlew $@"
gradleBuild=""
gradleBuildOptions="--stacktrace --build-cache --configure-on-demand -DskipNestedConfigMetadataGen=true --parallel "


if [ "$MATRIX_JOB_TYPE" == "BUILD" ] || [ "$MATRIX_JOB_TYPE" == "SNAPSHOT" ]; then
    gradleBuild="$gradleBuild assemble -x test -x javadoc -x check -DskipNpmLint=true "
elif [ "$MATRIX_JOB_TYPE" == "STYLE" ]; then
     gradleBuild="$gradleBuild checkstyleMain checkstyleTest -x test -x javadoc "
elif [ "$MATRIX_JOB_TYPE" == "JAVADOC" ]; then
     gradleBuild="$gradleBuild javadoc -x test -x check -DskipNpmLint=true "
elif [ "$MATRIX_JOB_TYPE" == "TEST" ]; then
    gradleBuild="$gradleBuild test coveralls -x javadoc -x check -DskipNpmLint=true "
fi

if [[ "${TRAVIS_COMMIT_MESSAGE}" == *"[show streams]"* ]]; then
    gradleBuild="$gradleBuild -DshowStandardStreams=true"
fi

tasks="$gradle $gradleBuildOptions $gradleBuild"
echo $tasks

waitRetVal=-1
if [ "$MATRIX_JOB_TYPE" == "TEST" ]; then
    waitloop="while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &"
    eval $waitloop
    waitRetVal=$?
fi

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
