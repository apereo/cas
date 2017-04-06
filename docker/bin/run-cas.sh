#!/bin/bash
export JAVA_HOME=/opt/jre-home
export PATH=$PATH:$JAVA_HOME/bin:.
# echo "Use of this image/container constitutes acceptence of the Oracle Binary Code License Agreement for Java SE."
cd /cas-overlay/
echo -e "Executing build from directory:" && pwd
exec ./build.sh run