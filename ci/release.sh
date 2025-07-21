#!/bin/bash

RED="\e[31m"
GREEN="\e[32m"
YELLOW="\e[33m"
ENDCOLOR="\e[0m"

casVersion=(`cat ./gradle.properties | grep "version" | cut -d= -f2`)

function printgreen() {
  printf "☘️  ${GREEN}$1${ENDCOLOR}\n"
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
    -DskipAot=true -DpublishSnapshots=true --no-build-cache \
    --no-configuration-cache --configure-on-demand \
    -Dorg.gradle.internal.http.socketTimeout=640000 \
    -Dorg.gradle.internal.http.connectionTimeout=640000 \
    -Dorg.gradle.internal.publish.checksums.insecure=true \
    -Dorg.gradle.internal.network.retry.max.attempts=5 \
    -Dorg.gradle.internal.network.retry.initial.backOff=5000 \
    -DrepositoryUsername="$1" -DrepositoryPassword="$2"
  if [ $? -ne 0 ]; then
      printred "Publishing CAS SNAPSHOTs failed."
      exit 1
  fi
}

function publish {
    if [[ "${casVersion}" == *SNAPSHOT* ]] ;
    then
        printred "CAS version ${casVersion} cannot be a SNAPSHOT version"
        exit 1
    fi
    printgreen "Publishing CAS releases. This might take a while..."
    ./gradlew assemble publishAggregationToCentralPortal \
      --parallel --no-daemon --no-configuration-cache -x test -x check \
      -DskipAot=true -DpublishReleases=true --stacktrace \
      -DrepositoryUsername="$1" -DrepositoryPassword="$2" \
      -Dorg.gradle.internal.http.socketTimeout=640000 \
      -Dorg.gradle.internal.http.connectionTimeout=640000 \
      -Dorg.gradle.internal.publish.checksums.insecure=true \
      -Dorg.gradle.internal.network.retry.max.attempts=5 \
      -Dorg.gradle.internal.network.retry.initial.backOff=5000
    if [ $? -ne 0 ]; then
        printred "Publishing Apereo CAS failed."
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

if [[ "${casVersion}" == v* ]] ;
then
    printred "CAS version ${casVersion} is incorrect and likely a tag."
    exit 1
fi

echo -e "\n"
echo "***************************************************************"
printgreen "Welcome to the release process for Apereo CAS ${casVersion}"
echo -n $(java -version)
echo "***************************************************************"

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
