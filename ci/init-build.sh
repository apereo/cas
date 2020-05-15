#!/bin/bash


echo -e "SHA: ${GITHUB_SHA}"
echo -e "Branch: ${GITHUB_REF}"
echo -e "Head Branch: ${GITHUB_HEAD_REF}"
echo -e "Base Branch: ${GITHUB_BASE_REF}"
echo -e "SHA: ${GITHUB_SHA}"
echo -e "SHA: ${GITHUB_SHA}"
echo -e "Workflow: ${GITHUB_WORKFLOW}"
echo -e "Run ID: ${GITHUB_RUN_ID}"
echo -e "Run Number: ${GITHUB_RUN_NUMBER}"
echo -e "Action: ${GITHUB_ACTION}"
echo -e "Actor: ${GITHUB_ACTOR}"
echo -e "Repository: ${GITHUB_REPOSITORY}"
echo -e "Event: ${GITHUB_EVENT_NAME}"
echo -e "Workspace: ${GITHUB_WORKSPACE}"
echo -e "User: ${USER}"
echo -e "User HOME directory: ${HOME}"
echo -e "************************************"

echo -e "Setting build environment...\n"
sudo mkdir -p /etc/cas/config /etc/cas/saml /etc/cas/services

echo -e "Configuring Gradle wrapper...\n"
mkdir -p ~/.gradle && echo "org.gradle.daemon=false" >> ~/.gradle/gradle.properties
chmod -R 777 ./gradlew
echo "Gradle Home directory:"
./gradlew gradleHome --no-daemon --version
echo -e "Configured build environment\n"
