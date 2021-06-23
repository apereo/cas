#!/bin/sh
rm cert.pem
rm key.pem
rm truststore.jks
rm truststore.p12
# -addext 'basicConstraints=CA:FALSE'
openssl req -x509 -nodes -newkey rsa:2048 -keyout key.pem -out cert.pem -days 3650 -subj "/C=US/ST=Virginia/L=Fairfax/O=Some Company, Inc./OU=IT/CN=CAS User" -addext 'extendedKeyUsage=1.3.6.1.4.1.311.10.3.4.1'  -addext 'subjectAltName=otherName:msUPN;UTF8:1234567890@edu'
keytool -importcert -keystore truststore.jks -storetype JKS -storepass changeit -alias casuser -file cert.pem -noprompt
keytool -importcert -keystore truststore.p12 -storetype PKCS12 -storepass changeit -alias casuser -file cert.pem -noprompt