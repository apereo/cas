#!/bin/bash

if [ "$TRAVIS_COMMIT_MESSAGE" == "[skip tests]" ]; then
  echo -e "Travis CI build indicates that tests should be skipped because the commit message says so. Commit message is [$TRAVIS_COMMIT_MESSAGE].\n"
elif [ "$PUBLISH_SNAPSHOTS" == "true" ]; then
  echo -e "Travis CI build indicates that tests should be skipped because we are publishing snapshots. Tests should auto-run as part of Travis build matrix configuration.\n"
else
  echo -e "Travis CI build indicates that tests along with coveralls test coverage should run.\n"
  sudo ./gradlew checkstyleTest test coveralls --stacktrace --parallel --build-cache
fi
