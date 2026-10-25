#!/bin/bash
metadataDirectory="${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-md"
mkdir -p "${metadataDirectory}"

echo "Fetching CAS SAML2 identity provider metadata, kept in git"
curl -k -fsS --connect-timeout 10 https://localhost:8443/cas/idp/metadata > "${metadataDirectory}/idp-metadata.xml" || exit 1
curl -k -fsS --connect-timeout 10 https://localhost:8443/cas/idp/metadata/signingCertificate > "${metadataDirectory}/idp-signing.crt" || exit 1
curl -k -fsS --connect-timeout 10 https://localhost:8443/cas/idp/metadata/encryptionCertificate > "${metadataDirectory}/idp-encryption.crt" || exit 1

export IDP_SIGNING_CERTIFICATE=$(sed '/-----/d' "${metadataDirectory}/idp-signing.crt")
export IDP_ENCRYPTION_CERTIFICATE=$(sed '/-----/d' "${metadataDirectory}/idp-encryption.crt")
"${PWD}/ci/tests/saml2/run-saml-server.sh" || exit 1

METADATA_URL="http://localhost:9443/simplesaml/module.php/saml/sp/metadata.php/default-sp"
seed="/tmp/cas-saml-metadata-git/seed"
echo "Committing SAML2 service provider metadata from ${METADATA_URL} to the git repository by hand"
git -C "${seed}" pull --quiet --ff-only origin master || exit 1
mkdir -p "${seed}/sp-metadata"
curl -k -fsS "${METADATA_URL}" > "${seed}/sp-metadata/SimpleSAMLphp.xml" || exit 1
git -C "${seed}" add sp-metadata
git -C "${seed}" -c user.name=CAS -c user.email=cas@apereo.org -c commit.gpgsign=false commit --quiet -m "Add SimpleSAMLphp" || exit 1
git -C "${seed}" push --quiet origin master || exit 1
echo "Pushed SAML2 service provider metadata"
