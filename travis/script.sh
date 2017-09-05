#!/bin/bash

if [ "$TRAVIS_COMMIT_MESSAGE" == "[skip tests]" ]; then
  echo -e "Travis CI build indicates that tests should be skipped because the commit message says so. Commit message is [$TRAVIS_COMMIT_MESSAGE].\n"
else
  echo -e "Travis CI build indicates that tests should should run.\n"
  sudo ./gradlew checkstyleTest test --stacktrace --parallel --build-cache
fi
