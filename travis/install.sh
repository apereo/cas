#!/bin/bash

branchName="master"

gradle="sudo ./gradlew"
gradleOptions="--stacktrace --parallel --build-cache --max-workers=8 --configure-on-demand --no-daemon"
gradleBuild="bootRepackage"

if [ "$PUBLISH_SNAPSHOTS" == "false" ]; then
    echo -e "The build will aggregate javadocs from all modules into one JAR file.\n"
    gradleBuild="$gradleBuild checkstyleMain aggregateJavadocsIntoJar"
else
    gradleBuild="$gradleBuild -x check aggregateJavadocsIntoJar"
    if [[ "${TRAVIS_COMMIT_MESSAGE}" == *"[skip tests]"* ]]; then
        echo -e "The build indicates that tests should be skipped.\n"
        gradleBuild="$gradleBuild -x test"
    else
        echo -e "The build indicates that tests along with coveralls test coverage should run.\n"
        gradleBuild="$gradleBuild checkstyleTest test coveralls"
    fi
fi

tasks="$gradle $gradleOptions $gradleBuild"
echo $tasks
eval $tasks
retVal=$?
echo -e "Gradle build finished at `date` with exit code $retVal\n"

if [ ! $? -eq 0 ]; then
    echo -e "Gradle build finished with an error exit code\n"
    exit $retVal
else
    if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "$branchName" ] && [ "$PUBLISH_SNAPSHOTS" == "true" ]; then
        if [[ "${TRAVIS_COMMIT_MESSAGE}" == *"[skip snapshots]"* ]]; then
             echo -e "The build will skip deploying SNAPSHOT artifacts to Sonatype under Travis job ${TRAVIS_JOB_NUMBER}"
        else
            echo -e "The build will deploy SNAPSHOT artifacts to Sonatype under Travis job ${TRAVIS_JOB_NUMBER}"
            gradleUpload="uploadArchives -x test -x check -x javadoc --offline -DpublishSnapshots=true -DsonatypeUsername=${SONATYPE_USER} -DsonatypePassword=${SONATYPE_PWD}"
            upload="$gradle $gradleOptions $gradleUpload"
            echo $upload
            eval $upload
            echo -e "Deploying snapshots to sonatype finished at `date` \n"
        fi
    fi
fi
