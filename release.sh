#!/bin/bash

GREEN=$(tput setaf 2)
YELLOW=$(tput setaf 3)
NORMAL=$(tput sgr0)
TIMEOUT=640000

function build {
    ./gradlew clean --parallel
    echo -e "\n${GREEN}Building CAS. Please be patient as this might take a while...${NORMAL}\n"
    ./gradlew assemble -x test -x check --no-watch-fs -DpublishReleases=true -DrepositoryUsername="$1" -DrepositoryPassword="$2"
}

function publish {
    echo -e "\n${GREEN}Publishing CAS. Please be patient as this might take a while...${NORMAL}\n"
    ./gradlew publishToSonatype closeAndReleaseStagingRepository --no-watch-fs -DpublishReleases=true -DrepositoryUsername="$1" -DrepositoryPassword="$2" \
      -DpublishReleases=true -DrepositoryUsername="$1" -DrepositoryPassword="$2" \
      -Dorg.gradle.internal.http.socketTimeout="${TIMEOUT}" \
      -Dorg.gradle.internal.http.connectionTimeout="${TIMEOUT}"  \
      -Dorg.gradle.internal.publish.checksums.insecure=true
}

function instructions {
    echo -e "\n${YELLOW}Done!\nThe release should be automatically closed and published on Sonatype.\nThank you!${NORMAL}"
}

clear
java -version

casVersion=(`cat ./gradle.properties | grep "version" | cut -d= -f2`)
if [[ "${casVersion}" == v* ]] ;
then
    echo "CAS version ${} is incorrect and likely a tag."
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

read -s -p "If you are ready, press ENTER to continue..." anykey
clear

read -s -p "Sonatype Username: " username
echo
read -s -p "Sonatype Password: " password
echo

clear

echo "1) Clean, Build and Publish"
echo "2) Publish"
read -p "Choose (1, 2, etc): " selection
echo

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
        echo "Unable to recognize selection"
        ;;
esac
exit 0
