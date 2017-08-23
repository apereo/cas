#!/bin/bash

branchVersion="master"

if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$PUBLISH_SNAPSHOTS" == "true" ] && [ "$TRAVIS_BRANCH" == "$branchVersion" ]; then
	echo -e "Starting to deploy SNAPSHOT artifacts to Sonatype under Travis job ${TRAVIS_JOB_NUMBER}"
  	sudo ./gradlew aggregateJavadocsIntoJar uploadArchives --parallel -DpublishSnapshots=true -DsonatypeUsername=${SONATYPE_USER} -DsonatypePassword=${SONATYPE_PWD}
  	echo -e "Successfully deployed SNAPSHOT artifacts to Sonatype under Travis job ${TRAVIS_JOB_NUMBER}"
fi 
