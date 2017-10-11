#!/bin/bash

branchName="master"
gradle="sudo ./gradlew"
gradleUpload="assemble uploadArchives -x test -x check"
gradleUploadOptions="--stacktrace -DpublishSnapshots=true -DsonatypeUsername=${SONATYPE_USER} -DsonatypePassword=${SONATYPE_PWD}"

if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "$branchName" ] && [ "$PUBLISH_SNAPSHOTS" == "true" ]; then
    if [[ "${TRAVIS_COMMIT_MESSAGE}" == *"[skip snapshots]"* ]]; then
         echo -e "The build will skip deploying snapshot artifacts to Sonatype under Travis job ${TRAVIS_JOB_NUMBER}"
    else
        echo -e "The build will deploy snapshot artifacts to Sonatype under Travis job ${TRAVIS_JOB_NUMBER}"
        upload="$gradle $gradleUploadOptions $gradleUpload"
        echo $upload
        eval $upload
        retVal=$?
        echo -e "Deploying snapshots to sonatype finished at `date` \n"
    fi
fi
