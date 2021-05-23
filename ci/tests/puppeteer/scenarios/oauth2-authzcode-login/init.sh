#!/bin/bash
clientDir="./oauth2-client"
rm -Rf "${clientDir}" > /dev/null
git clone --depth 1 https://github.com/mmoayyed/oauth2-client-shell-v2.git "${clientDir}"
cd "${clientDir}" || exit
npm i
#npm run build
npm run serve &
echo "Started OAuth client application..."
