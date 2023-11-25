#!/bin/bash

RED="\e[31m"
GREEN="\e[32m"
YELLOW="\e[33m"
ENDCOLOR="\e[0m"

casVersion=(`cat ./gradle.properties | grep "version" | cut -d= -f2`)

function printgreen() {
  printf "✅ ${GREEN}$1${ENDCOLOR}\n"
}

function printred() {
  printf "🔴 ${RED}$1${ENDCOLOR}\n"
}

function clean {
  ./gradlew clean --parallel --no-configuration-cache
}

function build {
    printgreen "Building CAS. Please be patient as this might take a while..."
    ./gradlew assemble -x test -x check --parallel --no-watch-fs --no-configuration-cache \
        -DskipAot=true -DpublishReleases=true -DrepositoryUsername="$1" -DrepositoryPassword="$2"
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
  printgreen "Publishing CAS SNAPSHOT artifacts. Please be patient as this might take a while..."
  ./gradlew build publish -x test -x javadoc -x check --no-daemon --parallel \
    -DskipAot=true -DpublishSnapshots=true --build-cache --no-configuration-cache --configure-on-demand \
    -Dorg.gradle.internal.http.socketTimeout=640000 \
    -Dorg.gradle.internal.http.connectionTimeout=640000 \
    -Dorg.gradle.internal.publish.checksums.insecure=true \
    -Dorg.gradle.internal.remote.repository.deploy.max.attempts=5 \
    -Dorg.gradle.internal.remote.repository.deploy.initial.backoff=5000 \
    -Dorg.gradle.internal.repository.max.tentatives=10 \
    -Dorg.gradle.internal.repository.initial.backoff=1000 \
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
    printgreen "Publishing CAS releases. Please be patient as this might take a while..."
    ./gradlew publishToSonatype closeAndReleaseStagingRepository \
      --no-parallel --no-watch-fs --no-configuration-cache -DskipAot=true -DpublishReleases=true \
      -DrepositoryUsername="$1" -DrepositoryPassword="$2" -DpublishReleases=true \
      -Dorg.gradle.internal.http.socketTimeout=640000 \
      -Dorg.gradle.internal.http.connectionTimeout=640000  \
      -Dorg.gradle.internal.publish.checksums.insecure=true \
      -Dorg.gradle.internal.remote.repository.deploy.max.attempts=5 \
      -Dorg.gradle.internal.remote.repository.deploy.initial.backoff=5000 \
      -Dorg.gradle.internal.repository.max.tentatives=10 \
      -Dorg.gradle.internal.repository.initial.backoff=1000
    if [ $? -ne 0 ]; then
        printred "Publishing CAS failed."
        exit 1
    fi
}

function finished {
    printgreen "Done! The release is now automatically published on Sonatype. There is nothing more for you to do. Thank you!"
}

clear
java -version

if [[ "${casVersion}" == v* ]] ;
then
    printred "CAS version ${casVersion} is incorrect and likely a tag."
    exit 1
fi

echo -e "\n"
echo "***************************************************************"
echo "Welcome to the release process for Apereo CAS ${casVersion}"
echo "***************************************************************"
echo -e "\n"
echo -e "Make sure the following criteria is met for non-SNAPSHOT versions:\n"
echo -e "\t- Your Sonatype account (username/password) must be authorized to publish releases to 'org.apereo'."
echo -e "\t- Your PGP signatures must be configured via '~/.gradle/gradle.properties' to sign the release artifacts:"
echo -e "\t\tsigning.keyId=YOUR_KEY_ID"
echo -e "\t\tsigning.password=YOUR_KEY_PASSWORD"
echo -e "\t\tsigning.secretKeyRingFile=/path/to/.gnupg/secring.gpg"
echo -e "\t- Disable the Gradle daemon and parallel processing via '~/.gradle/gradle.properties':"
echo -e "\t\torg.gradle.daemon=false"
echo -e "\t\torg.gradle.parallel=false"
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
