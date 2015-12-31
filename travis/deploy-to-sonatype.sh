#!/bin/bash
# Only invoke the deployment to Sonatype when it's not a PR and only for master
if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "4.2.x" ]; then
  ./gradlew uploadArchives -q -DpublishSnapshots=true -DsonatypeUsername=$SONATYPE_USER -DsonatypePassword=$SONATYPE_PWD
  echo -e "Successfully deployed SNAPSHOT artifacts to Sonatype under Travis job ${TRAVIS_JOB_NUMBER}"
fi
