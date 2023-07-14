#!/bin/bash

echo "Accessing CAS SAML2 identity provider metadata"
curl -k -I --connect-timeout 10 https://localhost:8443/cas/idp/metadata

export ACS_URL=https://localhost:9859/post
export SIGN_AUTHN_REQUESTS=false
chmod +x "${PWD}/ci/tests/saml2/run-samlsp-server.sh"
"${PWD}/ci/tests/saml2/run-samlsp-server.sh"
