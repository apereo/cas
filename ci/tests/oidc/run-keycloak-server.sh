#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Running Keycloak docker image..."
docker stop keycloak || true && docker rm keycloak || true

openssl req -newkey rsa:2048 -nodes \
  -subj "/C=PE/ST=Lima/L=Lima/O=Acme Inc. /OU=IT Department/CN=acme.com" \
  -keyout "$PWD"/ci/tests/oidc/keycloak/server.key -x509 -days 3650 \
  -out "$PWD"/ci/tests/oidc/keycloak/server.crt
chmod 755 "$PWD"/ci/tests/oidc/keycloak/server.key
docker run -d --rm --name keycloak \
  -p 8989:8443 -p 8988:8080 \
  -e KC_HTTPS_CERTIFICATE_FILE=/opt/keycloak/conf/server.crt \
  -e KC_HTTPS_CERTIFICATE_KEY_FILE=/opt/keycloak/conf/server.key \
  -v $PWD/ci/tests/oidc/keycloak/server.crt:/opt/keycloak/conf/server.crt \
  -v $PWD/ci/tests/oidc/keycloak/server.key:/opt/keycloak/conf/server.key \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  -v "$PWD"/ci/tests/oidc/keycloak/import:/opt/keycloak/data/import:ro \
  quay.io/keycloak/keycloak:latest \
  start-dev --import-realm
echo "Waiting for Keycloak docker container to prepare..."
sleep 15
docker ps | grep "keycloak"
retVal=$?
if [ $retVal == 0 ]; then
    echo "Keycloak docker container is running."
    docker logs -f keycloak &
else
    echo "Keycloak docker container failed to start."
fi
exit $retVal
