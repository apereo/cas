#!/bin/bash

if [ "$PUBLISH_SNAPSHOTS" == "false" ]; then
	echo -e "Running coveralls test coverage under Travis job ${TRAVIS_JOB_NUMBER}"
  	sudo ./gradlew coveralls --parallel --offline 
fi 
