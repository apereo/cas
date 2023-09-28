#!/usr/bin/env bash

docker stop strapi-server || true && docker rm strapi-server || true

echo "Building Strapi docker image"
docker build ci/tests/puppeteer/scenarios/oidc-login-strapi/strapi -t cas/strapi:latest

HOST_IP=$(ipconfig getifaddr en0)
echo "Host IP address: ${HOST_IP}"
HOST_URL="http://${HOST_IP}:1337"
echo "Host URL address: ${HOST_URL}"

docker run -d -p 1337:1337 \
  --network host \
  --name="strapi-server" cas/strapi:latest
docker logs -f strapi-server &
sleep 10
docker ps | grep "strapi-server"
retVal=$?
if [ $retVal == 0 ]; then
    echo "Strapi docker container is running."
else
    echo "Strapi docker container failed to start."
    exit $retVal
fi

echo "Waiting for Strapi start"
until curl -k -L --output /dev/null --silent --fail http://localhost:1337; do
  echo -n '.'
  sleep 1
done

DATA='{"email":"admin@example.org","password":"P@ssw0rd","firstname":"Strapi","lastname":"Admin"}'
echo $DATA > .admin.txt
cat .admin.txt
set +e
curl -X POST -H "Content-Type: application/json" http://localhost:1337/admin/register-admin --data @./.admin.txt
rm .admin.txt
echo -e "\nRegistration Complete: $?"
