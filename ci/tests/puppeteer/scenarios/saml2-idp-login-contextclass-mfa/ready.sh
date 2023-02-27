#!/bin/bash

export AUTHN_CONTEXT=https://refeds.org/profile/mfa
export SIGN_AUTHN_REQUESTS=false
chmod +x "${PWD}/ci/tests/saml2/run-samlsp-server.sh"
"${PWD}/ci/tests/saml2/run-samlsp-server.sh"
