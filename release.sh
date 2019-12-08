#!/bin/bash

GREEN=$(tput setaf 2)
YELLOW=$(tput setaf 3)
NORMAL=$(tput sgr0)

clear
java -version

timeout=640000
casVersion=(`cat ./gradle.properties | grep "version" | cut -d= -f2`)

echo "***************************************************************"
echo "Welcome to the release process for Apereo CAS ${casVersion}"
echo "***************************************************************"

echo -e "Make sure the following criteria is met:\n"
echo -e "\t- Your Sonatype account (username/password) must be authorized to publish releases to 'org.apereo'."
echo -e "\t- Your PGP signatures must be configured to sign the release artifacts apriori."
echo -e "\t\tsigning.keyId=YOUR_KEY_ID"
echo -e "\t\tsigning.password=YOUR_KEY_PASSWORD"
echo -e "\t\tsigning.secretKeyRingFile=/path/to/.gnupg/secring.gpg"
echo -e "\t- Disable the Gradle daemon and parallel processing via properties '~/.gradle/gradle.properties'."
echo -e "\t\torg.gradle.daemon=false"
echo -e "\t\torg.gradle.parallel=false"
echo -e "\nFor more information, please visit https://apereo.github.io/cas/developer/Release-Process.html\n"

read -s -p "If you are ready, press ENTER to continue..." anykey
clear

read -s -p "Sonatype Username: " username
echo
read -s -p "Sonatype Password: " password
echo

./gradlew clean --parallel
clear

echo -e "\n${GREEN}Building CAS. Please be patient as this might take a while...${NORMAL}\n"
./gradlew assemble -x test -x check -DpublishReleases=true \
-DsonatypeUsername="${username}" -DsonatypePassword="${password}"

echo -e "\n${GREEN}Publishing CAS. Please be patient as this might take a while...${NORMAL}\n"
./gradlew publish -DpublishReleases=true -DsonatypeUsername="${username}" -DsonatypePassword="${password}" \
-Dorg.gradle.internal.http.socketTimeout="${timeout}" -Dorg.gradle.internal.http.connectionTimeout="${timeout}"

echo -e "\n${YELLOW}Done! You may now finalize the release process via Sonatype:"
echo -e "\t\tLog into https://oss.sonatype.org"
echo -e "\t\tClick on 'Staged Repositories' and find the CAS release."
echo -e "\t\t'Close' the repository via the toolbar button and ensure all checks pass."
echo -e "\t\t'Release' the repository via the toolbar button when the repository is successfully closed."

echo -e "\nThank you!${NORMAL}"
exit 0
