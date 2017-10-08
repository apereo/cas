#!/bin/bash

gradle="sudo ./gradlew"
gradleOptions="--stacktrace --parallel --build-cache --max-workers=8 --configure-on-demand --no-daemon"
gradleBuild="checkstyleMain bootRepackage javadoc install"
gradleTest=""

if [ "$PUBLISH_SNAPSHOTS" == "false" ] && [ "$TRAVIS_COMMIT_MESSAGE" != "[skip tests]" ]; then
    echo -e "Travis CI build indicates that tests along with coveralls test coverage should run.\n"
    gradleTest="checkstyleTest test coveralls"
fi

tasks="$gradle $gradleOptions $gradleBuild $gradleTest"
echo $tasks
eval $tasks
