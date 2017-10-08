#!/bin/bash


gradle="sudo ./gradlew"
gradleOptions="--stacktrace --parallel --build-cache --max-workers=8 --configure-on-demand --no-daemon"
gradleBuild="checkstyleMain bootRepackage install"
gradleTest=""
gradleUpload=""

if [ "$PUBLISH_SNAPSHOTS" == "false" ]; then
    echo -e "The build will aggregate javadocs from all modules into one JAR file.\n"
    gradleBuild="$gradleBuild aggregateJavadocsIntoJar"
fi

if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$PUBLISH_SNAPSHOTS" == "true" ]; then
	echo -e "Th build will deploy SNAPSHOT artifacts to Sonatype under Travis job ${TRAVIS_JOB_NUMBER}"
	gradleUpload="uploadArchives -DpublishSnapshots=true -DsonatypeUsername=${SONATYPE_USER} -DsonatypePassword=${SONATYPE_PWD}"
fi

if [ "$PUBLISH_SNAPSHOTS" == "false" ] && [ "$TRAVIS_COMMIT_MESSAGE" != "[skip tests]" ]; then
    echo -e "The build indicates that tests along with coveralls test coverage should run.\n"
    gradleTest="checkstyleTest test coveralls"
fi

tasks="$gradle $gradleOptions $gradleBuild $gradleTest $gradleUpload"
echo $tasks
eval $tasks
