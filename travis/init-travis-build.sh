#!/bin/bash
echo -e "Building branch: ${TRAVIS_BRANCH}"
echo -e "Build directory: ${TRAVIS_BUILD_DIR}"
echo -e "Build id: ${TRAVIS_BUILD_ID}"
echo -e "Builder number: ${TRAVIS_BUILD_NUMBER}"
echo -e "Job id: ${TRAVIS_JOB_ID}"
echo -e "Job number: ${TRAVIS_JOB_NUMBER}"
echo -e "Repo slug: ${TRAVIS_REPO_SLUG}"
echo -e "OS name: ${TRAVIS_OS_NAME}"

if [ "$TRAVIS_SECURE_ENV_VARS" == "false" ]
then
  echo -e "Secure environment variables are NOT available...\n"
else
  echo -e "Secure environment variables are available...\n"
  #echo -e "GH_TOKEN -> ${GH_TOKEN}"
fi
