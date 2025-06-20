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

function build {
    printgreen "Creating OpenRewrite recipe for ${casVersion}..."
    ./gradlew createOpenRewriteRecipe --no-daemon
        git diff --quiet
        git status
        git add "**/rewrite/*.yml" && git commit -m "Generated OpenRewrite recipe for ${casVersion}"

    printgreen "Building CAS. Please be patient as this might take a while..."
    ./gradlew assemble -x test -x check --no-daemon --parallel \
        --no-watch-fs --no-configuration-cache \
        -DskipAot=true -DpublishReleases=true \
        -DrepositoryUsername="$1" -DrepositoryPassword="$2"
    if [ $? -ne 0 ]; then
        printred "Building CAS failed."
        exit 1
    fi
}

function snapshot() {
  if [[ "${casVersion}" != *SNAPSHOT* ]] ;
  then
      printred "CAS version ${casVersion} MUST be a SNAPSHOT version"
      exit 1
  fi
  printgreen "Publishing CAS SNAPSHOT artifacts. This might take a while..."
  ./gradlew build publish -x test -x javadoc -x check --no-daemon --parallel \
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
    ./gradlew publishToSonatype closeAndReleaseStagingRepository \
      --no-build-cache --no-daemon --no-parallel --no-watch-fs --no-configuration-cache \
      -DskipAot=true -DpublishReleases=true \
      -DrepositoryUsername="$1" -DrepositoryPassword="$2" -DpublishReleases=true \
      -Dorg.gradle.internal.http.socketTimeout=640000 \
      -Dorg.gradle.internal.http.connectionTimeout=640000 \
      -Dorg.gradle.internal.publish.checksums.insecure=true \
      -Dorg.gradle.internal.network.retry.max.attempts=5 \
      -Dorg.gradle.internal.network.retry.initial.backOff=5000
    if [ $? -ne 0 ]; then
        printred "Publishing CAS failed."
        exit 1
    fi

    createTag
}

function createTag {
  printgreen "Tagging the source tree for CAS version: ${casVersion}"
  read -p "CAS version to release (Leave blank for ${casVersion}): " releaseVersion
  if [[ -z "${releaseVersion}" ]]; then
    releaseVersion="${casVersion}"
  fi

  releaseTag="v${releaseVersion}"
  if [[ $(git tag -l "${releaseTag}") ]]; then
    git tag -d "${releaseTag}" && git push --delete origin "${releaseTag}"
  fi
  git tag "${releaseTag}" -m "Tagging CAS ${releaseTag} release" && git push origin "${releaseTag}"

  read -p "Current tag: ${releaseTag}. Enter the previous release tag (i.e. vA.B.C): " previousTag
  previousTagCommit=$(git rev-list -n 1 "$previousTag")
  currentCommit=$(git log -1 --format="%H")
  echo "Parsing the commit log between ${previousTagCommit} and ${currentCommit}..."
  git shortlog -sen "${previousTagCommit}".."${currentCommit}"
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
echo -e "Make sure the following criteria is met for non-SNAPSHOT versions:\n"
echo -e "\t- Your Sonatype account (username/password) must be authorized to publish releases to 'org.apereo'."
echo -e "\t- Your PGP signatures must be configured via '~/.gradle/gradle.properties' to sign the release artifacts:"
echo -e "\t\tsigning.keyId=YOUR_KEY_ID"
echo -e "\t\tsigning.password=YOUR_KEY_PASSWORD"
echo -e "\t\tsigning.secretKeyRingFile=/path/to/.gnupg/secring.gpg"
echo -e "\nFor more information, please visit\n\thttps://apereo.github.io/cas/developer/Release-Process.html\n"

if [[ -z $REPOSITORY_USER && -z $REPOSITORY_PWD ]]; then
  read -s -p "If you are ready, press ENTER to continue..." anykey
  echo
  read -s -p "Repository Username: " username
  echo
  read -s -p "Repository Password: " password
  echo
  echo "1) Clean, Build and Publish"
  echo "2) Publish"
  echo "3) Publish SNAPSHOTs"
  read -p "Choose (1, 2, 3, etc): " selection
  echo
  clear
else
  printgreen "Repository username and password are predefined."
  username="$REPOSITORY_USER"
  password="$REPOSITORY_PWD"
  if [[ "${casVersion}" == *SNAPSHOT* ]]; then
    selection="3"
  else
    selection="1"
  fi
fi

if [[ -z $username || -z $password ]]; then
  printred "Repository username and password are missing."
  printred "Make sure the following environment variables are defined: REPOSITORY_USER and REPOSITORY_PWD"
  exit 1
fi

case "$selection" in
    1)
        clean
        build ${username} ${password}
        publish ${username} ${password}
        finished
        ;;
    2)
        publish ${username} ${password}
        finished
        ;;
    3)
        snapshot ${username} ${password}
        finished
        ;;
    *)
        printred "Unable to recognize selection"
        ;;
esac
exit 0
