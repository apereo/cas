#!/bin/bash

RED="\e[31m"
GREEN="\e[32m"
YELLOW="\e[33m"
ENDCOLOR="\e[0m"

casVersion=(`cat ./gradle.properties | grep "version" | cut -d= -f2`)

function printgreen() {
  printf "☘️  ${GREEN}$1${ENDCOLOR}\n"
}

function printyellow() {
  printf "⚠️  ${YELLOW}$1${ENDCOLOR}\n"
}

function printred() {
  printf "🚨  ${RED}$1${ENDCOLOR}\n"
}

function clean {
  ./gradlew clean --parallel --no-configuration-cache --no-daemon
}

function snapshot() {
  if [[ "${casVersion}" != *SNAPSHOT* ]] ;
  then
      printred "CAS version ${casVersion} MUST be a SNAPSHOT version"
      exit 1
  fi
  printgreen "Publishing CAS SNAPSHOT artifacts. This might take a while..."
  ./gradlew assemble publishAggregationToCentralPortalSnapshots \
    -x test -x javadoc -x check --no-daemon --parallel \
    -DskipAot=true -DpublishSnapshots=true \
    --no-configuration-cache --configure-on-demand \
    -DrepositoryUsername="$1" -DrepositoryPassword="$2"
  if [ $? -ne 0 ]; then
      printred "Publishing CAS SNAPSHOTs failed."
      exit 1
  fi
}

function publish {
    if [[ "${casVersion}" == *SNAPSHOT* ]]; then
        printred "CAS version ${casVersion} cannot be a SNAPSHOT version"
        exit 1
    fi
    printgreen "Publishing CAS releases. This might take a while..."
    ./gradlew assemble publishAggregationToCentralPortal \
      --parallel --no-daemon --no-configuration-cache -x test -x check \
      -DskipAot=true -DpublishReleases=true --stacktrace \
      -DrepositoryUsername="$1" -DrepositoryPassword="$2"
    if [ $? -ne 0 ]; then
        printred "Publishing Apereo CAS failed."
        exit 1
    fi

    if [[ "$CI" == "true" ]]; then
      git config --global user.email "cas@apereo.org"
      git config --global user.name "Apereo CAS"
    fi

    printgreen "Tagging the source tree for CAS version: ${casVersion}"
    releaseTag="v${casVersion}"
    if [[ $(git tag -l "${releaseTag}") ]]; then
        printyellow "Tag ${releaseTag} already exists. Deleting it and re-creating it."
        git tag -d "${releaseTag}" && git push --delete origin "${releaseTag}"
    fi
    git tag "${releaseTag}" -m "Tagging CAS ${releaseTag} release" && git push origin "${releaseTag}"
    if [ $? -ne 0 ]; then
        printred "Tagging the source tree for CAS version ${casVersion} failed."
        exit 1
    fi
    printgreen "Deleting previous GitHub Release for ${releaseTag}, if any"
    gh release delete "${releaseTag}" --cleanup-tag -y

    printgreen "Creating GitHub Release for: ${releaseTag}"
    gh release create "${releaseTag}" \
      --title "${releaseTag}" \
      ./support/cas-server-support-shell/build/libs/cas-server-support-shell-${casVersion}.jar
    if [ $? -ne 0 ]; then
        printred "Creating GitHub Release for CAS version ${casVersion} failed."
        exit 1
    fi
}

function finished {
    printgreen "Done! The release is now automatically published. There is nothing more for you to do. Thank you!"
}

if [[ "$CI" == "true" ]]; then
  printgreen "Running in CI mode..."
else
  git diff --quiet
  if [ $? -ne 0 ]; then
    printred "Git repository has modified or untracked files. Commit or discard all changes and try again."
    git status && git diff
    exit 1
  fi
fi

if [[ "${casVersion}" == v* ]]; then
    printred "CAS version ${casVersion} is incorrect and likely a tag."
    exit 1
fi

echo -e "\n"
echo "***************************************************************"
printgreen "Welcome to the release process for Apereo CAS ${casVersion}"
echo -n $(java -version)
echo -e "***************************************************************\n"

username="$REPOSITORY_USER"
password="$REPOSITORY_PWD"

if [[ -z $username || -z $password ]]; then
  printred "Repository username and password are missing."
  printred "Make sure the following environment variables are defined: REPOSITORY_USER and REPOSITORY_PWD"
  exit 1
fi

if [[ "${casVersion}" == *SNAPSHOT* ]]; then
  selection="2"
else
  selection="1"
fi

case "$selection" in
    1)
        clean
        publish ${username} ${password}
        finished
        ;;
    2)
        snapshot ${username} ${password}
        finished
        ;;
    *)
        printred "Unable to recognize selection"
        exit 1
        ;;
esac
exit 0
