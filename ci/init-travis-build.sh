#!/bin/bash

echo -e "Building branch: ${TRAVIS_BRANCH}"
echo -e "Build directory: ${TRAVIS_BUILD_DIR}"
echo -e "Build id: ${TRAVIS_BUILD_ID}"
echo -e "Builder number: ${TRAVIS_BUILD_NUMBER}"
echo -e "Job id: ${TRAVIS_JOB_ID}"
echo -e "Job number: ${TRAVIS_JOB_NUMBER}"
echo -e "Repo slug: ${TRAVIS_REPO_SLUG}"
echo -e "OS name: ${TRAVIS_OS_NAME}"
echo -e "Pull Request: ${TRAVIS_PULL_REQUEST}"
echo -e "Commit Message: ${TRAVIS_COMMIT_MESSAGE}"
echo -e "Publish Snapshots: ${PUBLISH_SNAPSHOTS}"

if [ "$TRAVIS_SECURE_ENV_VARS" == "false" ]
then
  echo -e "Secure environment variables are NOT available...\n"
else
  echo -e "Secure environment variables are available...\n"
  #echo -e "GH_TOKEN -> ${GH_TOKEN}"
fi

if [ "$TRAVIS_PULL_REQUEST" != "false" ] && [ "$PUBLISH_SNAPSHOTS" == "true" ]; then
    echo -e "Skipping build since this is a pull request and we are not publishing snapshots.\n"
    exit 0
fi

echo -e "Setting build environment...\n"
sudo mkdir -p /etc/cas/config /etc/cas/saml /etc/cas/services

echo -e "Configuring Oracle JDK8 JCE...\n"
sudo unzip -j -o ./etc/jce8.zip *.jar -d $JAVA_HOME/jre/lib/security
sudo cp ./etc/java.security $JAVA_HOME/jre/lib/security

echo -e "Configuring Gradle wrapper...\n"
chmod -R 777 ./gradlew

# echo -e "Installing NodeJS...\n"
# sudo curl -sL https://deb.nodesource.com/setup_7.x | sudo -E bash - &>/dev/null
# sudo apt-get update  -qq > /dev/null
# sudo apt-get install -y nodejs -qq > /dev/null

echo -e "Installing NPM...\n"
sudo ./gradlew npmInstall --stacktrace -q

echo -e "Configured build environment\n"
