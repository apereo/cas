#!/bin/bash
source ./ci/functions.sh

runBuild=false
echo "Reviewing changes that might affect the Gradle build..."
currentChangeSetAffectsDependencies
retval=$?
if [ "$retval" == 0 ]
then
    echo "Found changes that affect project dependencies."
    runBuild=true
else
    echo "Changes do NOT affect project dependencies."
    runBuild=false
fi

if [ "$runBuild" = false ]; then
    exit 0
fi

echo -e "***********************************************"
echo -e "Build started at `date`"
echo -e "***********************************************"

echo -e "Installing npm...\n"
npm install npm@latest -g
npm -v

echo -e "Installing node...\n"
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.34.0/install.sh | bash
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"
nvm install 12.16.1
node -v

echo -e "Installing renovate-bot...\n"
npm install -g renovate

waitloop="while sleep 9m; do echo -e '\n=====[ Build is still running ]====='; done &"
eval $waitloop

node --max-old-space-size=4096 renovate --labels=Bot --git-author=renovatebot@apereo.org --git-fs=https --token=${GH_TOKEN} apereo/cas

echo -e "***************************************************************************************"
echo -e "Build finished at `date` with exit code $retVal"
echo -e "***************************************************************************************"
