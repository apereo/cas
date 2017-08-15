#!/bin/bash

if [ "$TRAVIS_COMMIT_MESSAGE" == "[skip tests]" ] || [ "$PUBLISH_SNAPSHOTS" == "false" ]; then
  echo -e "Travis CI build indicates that tests should be skipped.\n"
  echo -e "Commit message is $TRAVIS_COMMIT_MESSAGE and snapshot publish status is $PUBLISH_SNAPSHOTS\n"
else
  sudo ./gradlew checkstyleTest test --stacktrace --parallel --build-cache
fi
