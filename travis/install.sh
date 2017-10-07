#!/bin/bash

if [ "$PUBLISH_SNAPSHOTS" == "false" ]; then
  echo -e "Generating javadocs will be skipped since Travis CI build indicates that we are not publishing snapshots.\n"
  sudo ./gradlew checkstyleMain bootRepackage --parallel -x test -x javadoc --stacktrace --build-cache --max-workers=8 --configure-on-demand
else
  echo -e "Travis CI build indicates that tests should not run since we are publishing snapshots.\n"
  sudo ./gradlew javadoc bootRepackage --parallel -x test --stacktrace --build-cache -x check --max-workers=8 --configure-on-demand
fi
