#!/bin/bash

if [ "$TRAVIS_COMMIT_MESSAGE" == "[skip tests]" ]
then
  echo -e "Travis CI build indicates that tests should be skipped.\n"
else
  sudo ./gradlew checkstyleTest test --stacktrace --parallel --build-cache --offline
fi
