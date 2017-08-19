#!/bin/bash

if [ "$PUBLISH_SNAPSHOTS" == "false" ]; then
  echo -e "Generating javadocs will be skipped since Travis CI build indicates that we are not publishing snapshots.\n"
  sudo ./gradlew clean checkstyleMain bootRepackage --parallel -x test -x javadoc --stacktrace --build-cache
else
  echo -e "Travis CI build indicates that tests should should run.\n"
  sudo ./gradlew bootRepackage --parallel -x test --stacktrace --build-cache -x check
fi
