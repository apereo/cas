#!/bin/bash

export AUTHN_CONTEXT=https://refeds.org/profile/mfa
chmod +x "${PWD}/ci/tests/saml2/run-samlsp-server.sh"
"${PWD}/ci/tests/saml2/run-samlsp-server.sh"
