#!/bin/bash
echo "Accessing CAS SAML2 identity provider metadata"
curl -k -I --connect-timeout 10 https://localhost:8443/cas/idp/metadata

