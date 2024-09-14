#!/usr/bin/env bash

docker stop strapi-server || true && docker rm strapi-server || true

echo "Building Strapi docker container"
docker build ci/tests/puppeteer/scenarios/oidc-login-strapi/strapi -t cas/strapi:latest

docker run -d -p 1337:1337 \
  --add-host host.docker.internal:host-gateway \
  --add-host localhost:host-gateway \
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
