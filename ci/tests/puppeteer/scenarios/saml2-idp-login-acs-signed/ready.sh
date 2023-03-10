#!/bin/bash

export ACS_URL=https://httpbin.org/post
export SIGN_AUTHN_REQUESTS=true
chmod +x "${PWD}/ci/tests/saml2/run-samlsp-server.sh"
"${PWD}/ci/tests/saml2/run-samlsp-server.sh"
