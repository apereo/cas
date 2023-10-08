#!/bin/bash

GREEN=$(tput setaf 2)
YELLOW=$(tput setaf 3)
RED="\e[31m"
NORMAL=$(tput sgr0)
TIMEOUT=640000

function printgreen() {
  printf "✅ ${GREEN}$1${ENDCOLOR}\n"
}

function printred() {
  printf "🔴 ${RED}$1${ENDCOLOR}\n"
}

function build {
    ./gradlew clean --parallel --no-configuration-cache
    printgreen "Building CAS. Please be patient as this might take a while..."
    ./gradlew assemble -x test -x check --parallel --no-watch-fs --no-configuration-cache \
        -DskipAot=true -DpublishReleases=true -DrepositoryUsername="$1" -DrepositoryPassword="$2"
    if [ $? -ne 0 ]; then
        printred "Building CAS faild."
        exit 1
    fi
}

function publish {
    printgreen "Publishing CAS. Please be patient as this might take a while..."
    ./gradlew publishToSonatype closeAndReleaseStagingRepository \
      --no-parallel --no-watch-fs --no-configuration-cache -DskipAot=true -DpublishReleases=true \
      -DrepositoryUsername="$1" -DrepositoryPassword="$2" \
      -DpublishReleases=true -DrepositoryUsername="$1" -DrepositoryPassword="$2" \
      -Dorg.gradle.internal.http.socketTimeout="${TIMEOUT}" \
      -Dorg.gradle.internal.http.connectionTimeout="${TIMEOUT}"  \
      -Dorg.gradle.internal.publish.checksums.insecure=true
    if [ $? -ne 0 ]; then
        printred "Publishing CAS faild."
        exit 1
    fi
}

function instructions {
    printgreen "Done! The release is now automatically closed and published on Sonatype. Thank you!"
}

clear
java -version

casVersion=(`cat ./gradle.properties | grep "version" | cut -d= -f2`)
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
echo -e "Make sure the following criteria is met:\n"
echo -e "\t- Your Sonatype account (username/password) must be authorized to publish releases to 'org.apereo'."
echo -e "\t- Your PGP signatures must be configured via '~/.gradle/gradle.properties' to sign the release artifacts:"
echo -e "\t\tsigning.keyId=YOUR_KEY_ID"
echo -e "\t\tsigning.password=YOUR_KEY_PASSWORD"
echo -e "\t\tsigning.secretKeyRingFile=/path/to/.gnupg/secring.gpg"
echo -e "\t- Disable the Gradle daemon and parallel processing via '~/.gradle/gradle.properties':"
echo -e "\t\torg.gradle.daemon=false"
echo -e "\t\torg.gradle.parallel=false"
echo -e "\nFor more information, please visit\n\thttps://apereo.github.io/cas/developer/Release-Process.html\n"

if [[ -z $SONATYPE_USERNAME && -z $SONATYPE_PASSWORD ]]; then
  read -s -p "If you are ready, press ENTER to continue..." anykey
  echo
  read -s -p "Sonatype Username: " username
  echo
  read -s -p "Sonatype Password: " password
  echo
  echo "1) Clean, Build and Publish"
  echo "2) Publish"
  read -p "Choose (1, 2, etc): " selection
  echo
  clear
else
  printgreen "Sonatype username and password are defined. Starting the CAS release process..."
  selection="1"
  username="$SONATYPE_USERNAME"
  password="$SONATYPE_PASSWORD"
  clear
fi

case "$selection" in
    1)
        build ${username} ${password}
        publish ${username} ${password}
        instructions
        ;;
    2)
        publish ${username} ${password}
        instructions
        ;;
    *)
        printred "Unable to recognize selection"
        ;;
esac
exit 0
