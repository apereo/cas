#!/bin/sh
#

groovy build/Anchor.groovy -d current
jekyll build --safe

# mvn site site:stage --file /cas-server/pom.xml
# cp -r /cas-server/target/staging/* current/javadocs