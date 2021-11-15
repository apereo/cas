#!/bin/sh

# This script documents how the certificate for this test was created but it is not executed for each test.
# The certificate must contain an e-mail subject alt name that matches a user in the Samba AD instance used by tests.

rm cert.pem
rm key.pem
rm truststore.jks
rm truststore.p12
CONFIG="
[req]
distinguished_name=dn
[ dn ]
[ ext ]
basicConstraints=CRITICAL,CA:FALSE
"
openssl req -x509 -config <(echo "$CONFIG") -nodes -newkey rsa:2048 -keyout key.pem -out cert.pem -days 3650 \
  -subj "/C=US/ST=Virginia/L=Fairfax/O=Some Company, Inc./OU=IT/CN=CAS User" \
  -addext 'basicConstraints=critical,CA:FALSE' \
  -addext 'subjectAltName=otherName:msUPN;UTF8:1234567890@college.edu,email:1234567890@college.edu'
keytool -importcert -keystore truststore.jks -storetype JKS -storepass changeit -alias casuser -file cert.pem -noprompt
keytool -importcert -keystore truststore.p12 -storetype PKCS12 -storepass changeit -alias casuser -file cert.pem -noprompt
