#!/bin/bash

gradle="sudo ./gradlew"
gradleOptions="--stacktrace --parallel --build-cache --max-workers=8 --configure-on-demand --no-daemon"
gradleBuild="checkstyleMain bootRepackage install"
gradleTest=""

if [ "$PUBLISH_SNAPSHOTS" == "false" ]; then
    echo -e "The build will aggregate javadocs from all modules into one JAR file.\n"
    gradleBuild="$gradleBuild aggregateJavadocsIntoJar"
fi

if [ "$PUBLISH_SNAPSHOTS" == "false" ] && [ "$TRAVIS_COMMIT_MESSAGE" != "[skip tests]" ]; then
    echo -e "The build indicates that tests along with coveralls test coverage should run.\n"
    gradleTest="checkstyleTest test coveralls"
fi

tasks="$gradle $gradleOptions $gradleBuild $gradleTest"
echo $tasks
eval $tasks
