#!/bin/bash

echo "Accessing CAS SAML2 identity provider metadata"
curl -k -I https://localhost:8443/cas/idp/metadata
