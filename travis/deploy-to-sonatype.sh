#!/bin/bash
# Only invoke the deployment to Sonatype when it's not a PR and only for master
if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "master" ]; then
  mvn -T 10 deploy --settings ./travis/settings.xml -P nocheck -Dlog4j.configuration=file:./travis/log4j.xml
  echo -e "Successfully deployed SNAPSHOT artifacts to Sonatype under Travis job ${TRAVIS_JOB_NUMBER}"
fi 
