#!/bin/bash
export JAVA_HOME=/opt/jre-home
export PATH=$PATH:$JAVA_HOME/bin:.
# echo "Use of this image/container constitutes acceptence of the Oracle Binary Code License Agreement for Java SE."
exec java -jar /cas-overlay/target/cas.war 