#!/usr/bin/env bash
set -e
set -m
SCENARIO="oidc-login-strapi"
STRAPI_FOLDER=${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/strapi
PROJECT=getstarted
if [[ ! -d $STRAPI_FOLDER/$PROJECT ]] ; then
  mkdir -p $STRAPI_FOLDER
  cd $STRAPI_FOLDER
  # run this command if you don't have yarn installed, should be installed on CI server already
  #npm install -g yarn
  yarn create strapi-app $PROJECT --quickstart --no-run
  cd -
fi

# copy server.js with URL for strapi defined
cp ${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/strapi-custom/server.js $STRAPI_FOLDER/$PROJECT/config/server.js

# install node modules for strapi app
cd $STRAPI_FOLDER/$PROJECT
yarn install

STRAPI_CMD=./node_modules/.bin/strapi

# run without SSL verification in order to call CAS via https and not worry about SSL trust
yarn build --no-optimization

# This could probably be done in javascript as part of user-permissions plugin extension to modify
# the bootstrap config before it goes in database, but doing this until that process better documented
echo Dumping strapi configuration
$STRAPI_CMD configuration:dump --file config.json --pretty
sed -i 's|\\"cas\\":{\\"enabled\\":false|\\"cas\\":{\\"enabled\\":true|g' config.json
sed -Ei 's|cas(.*)\\"secret\\":\\"\\"|cas\1\\"secret\\":\\"strapisecret\\"|g' config.json
sed -Ei 's|cas(.*)\\"key\\":\\"\\"|cas\1\\"key\\":\\"strapi\\"|g' config.json
sed -i 's|subdomain\\":\\"my.subdomain.com/cas|subdomain\\":\\"localhost:8443/cas|g' config.json
# this next line is fixing a bug where default redirect url contains // instead of /
sed -i 's|1337//api|1337/api|g' config.json
echo Restoring modified strapi configuration
$STRAPI_CMD configuration:restore --file config.json

NODE_TLS_REJECT_UNAUTHORIZED=0 $STRAPI_CMD start &
pid=$!
echo "Waiting for Strapi start up in background $pid"
until curl -k -L --output /dev/null --silent --fail http://localhost:1337; do
  echo -n '.'
  sleep 1
done

if [[ "$SKIP_REGISTRATION" != "true" ]]; then
  $STRAPI_FOLDER/$PROJECT/../../register-strapi-admin.sh
fi

echo "Strapi Ready - PID: $pid"
echo $pid > $STRAPI_FOLDER/strapi.pid
# let strapi keep running
disown -h %1
