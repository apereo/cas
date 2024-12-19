#!/bin/bash

#echo "Dumping Strapi configuration"
#echo "=============================="

#strapi configuration:dump --file config.json --pretty
#sed -i'.bak' 's|\\"cas\\":{\\"icon\":\\"book\\",\\"enabled\\":false|\\"cas\\":{\\"icon\":\\"book\\",\\"enabled\\":true|g' config.json
#sed -i'.bak' -E 's|cas(.*)\\"secret\\":\\"\\"|cas\1\\"secret\\":\\"strapisecret\\"|g' config.json
#sed -i'.bak' -E 's|cas(.*)\\"key\\":\\"\\"|cas\1\\"key\\":\\"strapi\\"|g' config.json
#sed -i'.bak' 's|subdomain\\":\\"my.subdomain.com/cas|subdomain\\":\\"localhost:8443/cas|g' config.json

#echo "==========================="
#echo "Strapi Configuration"
#echo "==========================="
#cat config.json

export PATH=$PATH:/srv/app/node_modules/.bin

echo -e "\nRestoring modified Strapi configuration..."
strapi configuration:restore --file config.json

echo "==========================="
echo "Running Strapi $(strapi --version)"
echo "==========================="

export NODE_TLS_REJECT_UNAUTHORIZED=0
export DATABASE_SSL_REJECT_UNAUTHORIZED=false
strapi build
strapi start
