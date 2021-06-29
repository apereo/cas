#!/bin/bash

clear
echo "Creating overlay work directory"
rm -Rf "$TMPDIR/cas" "$PWD"/ci/tests/oidc-conformance/overlay
mkdir "$PWD"/ci/tests/oidc-conformance/overlay

dname="${dname:-CN=cas.example.org,OU=Example,OU=Org,C=US}"
subjectAltName="${subjectAltName:-dns:example.org,dns:localhost,ip:127.0.0.1}"
keystore="$PWD"/ci/tests/oidc-conformance/overlay/thekeystore
echo -e "\nGenerating keystore ${keystore} for CAS with DN=${dname}, SAN=${subjectAltName} ..."
[ -f "${keystore}" ] && rm "${keystore}"
keytool -genkey -noprompt -alias cas -keyalg RSA -keypass changeit -storepass changeit \
  -keystore "${keystore}" -dname "${dname}" -ext SAN="${subjectAltName}"
[ -f "${keystore}" ] && echo "Created ${keystore}"

echo -e "\Building CAS with support for OpenID Connect..."
./gradlew :webapp:cas-server-webapp-tomcat:build -DskipNestedConfigMetadataGen=true -x check -x javadoc \
  --no-daemon --build-cache --configure-on-demand --parallel -PcasModules="oidc"
mv "$PWD"/webapp/cas-server-webapp-tomcat/build/libs/cas-server-webapp-tomcat-*-SNAPSHOT.war "$PWD"/cas.war

if [ $? -eq 1 ]; then
  echo "Unable to build or locate the CAS web application file. Aborting test..."
  exit 1
fi

echo -e "\nLaunching CAS..."
java -jar "$PWD"/cas.war \
  --cas.authn.attribute-repository.json[0].location="$PWD"/ci/tests/oidc-conformance/cas-config/attribute-repository.json \
  --cas.authn.oidc.core.issuer=https://localhost:8446/cas/oidc/ \
  --cas.authn.oidc.jwks.jwks-file=file:"$PWD"/ci/tests/oidc-conformance/cas-config/keystore.jwks \
  --cas.service-registry.core.init-from-json=true \
  --cas.authn.oidc.core.dynamic-client-registration-mode=OPEN \
  --cas.authn.oauth.user-profile-view-type=FLAT \
  --cas.server.name=https://localhost:8446 \
  --cas.server.prefix=https://localhost:8446/cas \
  --logging.level.org.apereo.cas=debug \
  --spring.profiles.active=none \
  --server.port=8446 \
  --server.ssl.key-store="$keystore" &

pid=$!
echo -e "\nWaiting for CAS under process id ${pid}"
until curl -k -L --output /dev/null --silent --fail https://localhost:8446/cas/login; do
  echo -n '.'
  sleep 5
done
echo -e "\n\nReady!"

echo "Launching OIDC conformance test suite..."
rm -Rf "$PWD"/ci/tests/oidc-conformance/conformance-suite
git clone --depth 1 https://gitlab.com/openid/conformance-suite.git "$PWD"/ci/tests/oidc-conformance/conformance-suite
rm -Rf ./conformance-suite/mongo
docker-compose -f "$PWD"/ci/tests/oidc-conformance/conformance-suite/docker-compose-dev.yml down
echo "Building OIDC conformance test suite..."
mvn --quiet -f ./ci/tests/oidc-conformance/conformance-suite/pom.xml clean package -DskipTests=true
echo "Launching OIDC conformance test suite..."
docker-compose -f "$PWD"/ci/tests/oidc-conformance/conformance-suite/docker-compose-dev.yml up -d

echo -e "\nWaiting for OIDC conformance test suite"
until curl -k -L --output /dev/null --silent --fail https://localhost:8443; do
  echo -n '.'
  sleep 1
done
echo -e "\n\nReady!"

echo -e "\nKilling process ${pid} ..."
kill -9 $pid
docker-compose -f "$PWD"/ci/tests/oidc-conformance/conformance-suite/docker-compose-dev.yml down
docker container stop $(docker container ls -aq) && docker container rm $(docker container ls -aq)
rm "$PWD"/cas.war
[ -f "${keystore}" ] && rm "${keystore}"
rm -Rf "$PWD"/ci/tests/oidc-conformance/overlay || true
rm -Rf "$PWD"/ci/tests/oidc-conformance/conformance-suite || true
