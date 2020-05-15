#!/bin/bash

echo -e "***********************************************"
echo -e "Build started at `date`"
echo -e "***********************************************"

echo -e "Installing renovate-bot...\n"
npm install -g renovate

waitloop="while sleep 9m; do echo -e '\n=====[ Build is still running ]====='; done &"
eval $waitloop

node --max-old-space-size=4096 renovate --labels=Bot --git-author=renovatebot@apereo.org --git-fs=https --token=${RENOVATE_TOKEN} apereo/cas

echo -e "***************************************************************************************"
echo -e "Build finished at `date` with exit code $retVal"
echo -e "***************************************************************************************"
