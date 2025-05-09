#!/bin/bash

#java -cp "/path/to/h2-2.2.224.jar" org.h2.tools.Shell \
#-url jdbc:h2:file:./ci/tests/keycloak/data/data/h2/keycloakdb -user sa -password password \
#-sql "SELECT CC.\"VALUE\" FROM COMPONENT_CONFIG CC INNER JOIN COMPONENT C INNER JOIN REALM R WHERE CC.COMPONENT_ID = C.ID AND R.ID = C.REALM_ID AND R.NAME='cas' AND C.NAME = 'rsa-generated' AND CC.NAME='privateKey';" \
#| head -n +2

GREEN="\e[32m"
ENDCOLOR="\e[0m"

function printgreen() {
  printf "🍀 ${GREEN}$1${ENDCOLOR}\n"
}


# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &
export DOCKER_IMAGE="quay.io/keycloak/keycloak:latest"
printgreen "Running Keycloak docker container..."
docker stop keycloak || true && docker rm keycloak || true

rm -f "$PWD"/ci/tests/keycloak/data/server.crt
rm -f "$PWD"/ci/tests/keycloak/data/server.key

openssl req -newkey rsa:2048 -nodes \
  -subj "/C=PE/ST=Lima/L=Lima/O=Acme Inc. /OU=IT Department/CN=acme.com" \
  -keyout "$PWD"/ci/tests/keycloak/data/server.key -x509 -days 3650 \
  -out "$PWD"/ci/tests/keycloak/data/server.crt
chmod 755 "$PWD"/ci/tests/keycloak/data/server.key

#  -v "$PWD"/ci/tests/keycloak/data/data/h2:/opt/keycloak/data/h2 \

docker run -d --rm --name keycloak \
  -p 8989:8443 -p 8988:8080 \
  -e KC_HTTPS_CERTIFICATE_FILE=/opt/keycloak/conf/server.crt \
  -e KC_HTTPS_CERTIFICATE_KEY_FILE=/opt/keycloak/conf/server.key \
  -v $PWD/ci/tests/keycloak/data/server.crt:/opt/keycloak/conf/server.crt \
  -v $PWD/ci/tests/keycloak/data/server.key:/opt/keycloak/conf/server.key \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  -e KC_DB=dev-file \
  -e KC_HEALTH_ENABLED=true \
  -e KC_METRICS_ENABLED=true \
  -v "$PWD"/ci/tests/keycloak/data/import:/opt/keycloak/data/import:ro \
  ${DOCKER_IMAGE} \
  start-dev --import-realm --log-level=INFO
printgreen "Waiting for Keycloak docker container to prepare..."
sleep 15
docker ps | grep "keycloak"
retVal=$?
if [ $retVal == 0 ]; then
    printgreen "Keycloak docker container is running."
    docker logs -f keycloak &
else
    echo "Keycloak docker container failed to start."
fi
exit $retVal
