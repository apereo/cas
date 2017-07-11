#!/bin/bash

sudo mkdir -p /etc/cas/config /etc/cas/saml /etc/cas/services
sudo unzip -j -o ./etc/jce8.zip *.jar -d `jdk_switcher home oraclejdk8`/jre/lib/security
sudo ls `jdk_switcher home oraclejdk8`/jre/lib/security
sudo cp ./etc/java.security `jdk_switcher home oraclejdk8`/jre/lib/security
chmod -R 777 ./gradlew
sudo curl -sL https://deb.nodesource.com/setup_6.x | sudo -E bash -
sudo apt-get update
sudo apt-get install -y nodejs
