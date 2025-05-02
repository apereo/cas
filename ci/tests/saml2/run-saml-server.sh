#!/bin/bash

GREEN="\e[32m"
ENDCOLOR="\e[0m"

function printgreen() {
  printf "🍀 ${GREEN}$1${ENDCOLOR}\n"
}


export DOCKER_IMAGE="kenchan0130/simplesamlphp:latest"

tmp="${TMPDIR}"
if [[ -z "${tmp}" ]]; then
  tmp="/tmp"
fi
export TMPDIR=${tmp}
echo "Using temp directory: ${TMPDIR}"

if [[ -z "${SP_SLO_SERVICE}" ]]; then
  export SP_SLO_SERVICE="https://localhost:8443/cas/login?client_name=SAML2Client"
fi

if [[ -z "${SP_ACS_SERVICE}" ]]; then
  export SP_ACS_SERVICE="https://localhost:8443/cas/login?client_name=SAML2Client"
else
  echo -e "Found existing ACS service at ${SP_ACS_SERVICE}"
fi

if [[ -z "${SP_ENTITY_ID}" ]]; then
  export SP_ENTITY_ID="cas:apereo:pac4j:saml"
fi

docker stop simplesamlphp-idp || true && docker rm simplesamlphp-idp || true
printgreen "Running SAML2 IdP with SP entity id ${SP_ENTITY_ID} and SP ACS service ${SP_ACS_SERVICE} on port 9443"

echo "Creating private key and certificate for SP metadata"
openssl req -newkey rsa:3072 -new -x509 -days 365 \
  -nodes -out ${TMPDIR}/saml.crt -keyout ${TMPDIR}/saml.pem \
  -subj "/C=PE/ST=Lima/L=Lima/O=Acme Inc. /OU=IT Department/CN=acme.com"
if [ $? -ne 0 ]; then
  echo "Failed to create private key and certificate for SP metadata"
  exit 1
fi
echo "SP certificate..."
chmod 777 "${TMPDIR}"/saml.crt
cat "${TMPDIR}"/saml.crt

echo "SP private key..."
chmod 777 ${TMPDIR}/saml.pem
cat "${TMPDIR}"/saml.pem

if [[ -z "${IDP_ENTITYID}" ]]; then
  export IDP_ENTITYID="https://cas.apereo.org/saml/idp"
fi

echo -e "Using IDP entity id: ${IDP_ENTITYID}"
echo -e "Using IDP signing certificate:\n$IDP_SIGNING_CERTIFICATE"
echo -e "Using IDP encryption certificate:\n$IDP_ENCRYPTION_CERTIFICATE"
echo -e "SP passive authentication enabled: ${SP_PASSIVE_AUTHN}"
echo -e "Using SP SLO service: ${SP_SLO_SERVICE}"
echo -e "Using SP entity id: ${SP_ENTITY_ID}"
echo -e "Redirect binding disabled: ${DISABLE_REDIRECT_BINDING}"

docker run -d --rm --name=simplesamlphp-idp -p 9443:8080 \
  -e SIMPLESAMLPHP_SP_ENTITY_ID="${SP_ENTITY_ID}" \
  -e SIMPLESAMLPHP_SP_ASSERTION_CONSUMER_SERVICE="${SP_ACS_SERVICE}" \
  -e SIMPLESAMLPHP_SP_SINGLE_LOGOUT_SERVICE="${SP_SLO_SERVICE}" \
  -e IDP_ENCRYPTION_CERTIFICATE="${IDP_ENCRYPTION_CERTIFICATE}" \
  -e IDP_SIGNING_CERTIFICATE="${IDP_SIGNING_CERTIFICATE}" \
  -e IDP_ENTITYID="${IDP_ENTITYID}" \
  -e SP_PASSIVE_AUTHN="${SP_PASSIVE_AUTHN}" \
  -e DISABLE_REDIRECT_BINDING="${DISABLE_REDIRECT_BINDING}" \
  -v $TMPDIR/saml.crt:/var/www/simplesamlphp/cert/saml.crt \
  -v $TMPDIR/saml.pem:/var/www/simplesamlphp/cert/saml.pem \
  -v $PWD/ci/tests/saml2/saml20-idp-remote.php:/var/www/simplesamlphp/metadata/saml20-idp-remote.php \
  -v $PWD/ci/tests/saml2/saml20-idp-hosted.php:/var/www/simplesamlphp/metadata/saml20-idp-hosted.php \
  -v $PWD/ci/tests/saml2/authsources.php:/var/www/simplesamlphp/config/authsources.php \
  -v $PWD/ci/tests/saml2/config.php:/var/www/simplesamlphp/config/config.php \
  -v $PWD/ci/tests/saml2/php.ini-production:/usr/local/etc/php/php.ini \
  ${DOCKER_IMAGE}
#docker logs -f simplesamlphp-idp &

chmod +x ${PWD}/ci/tests/httpbin/run-httpbin-server.sh
${PWD}/ci/tests/httpbin/run-httpbin-server.sh
