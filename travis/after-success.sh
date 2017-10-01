#!/bin/bash

if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$PUBLISH_SNAPSHOTS" == "false" ]; then
	echo -e "Running coveralls test coverage under Travis job ${TRAVIS_JOB_NUMBER}"
  	sudo ./gradlew jacocoTestReport coveralls --parallel
fi 
