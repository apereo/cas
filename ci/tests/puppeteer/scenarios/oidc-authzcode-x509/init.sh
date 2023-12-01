#!/bin/bash
cd "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}"
echo "Renew trusted certificate in the keystore..."

openssl x509 -x509toreq -in cert.pem -out cert.csr -signkey key.pem
openssl x509 -req -days 3650 -in cert.csr -out cert.pem.new -signkey key.pem
mv cert.pem.new cert.pem
rm cert.csr
#openssl x509 -in cert.pem -text
keytool -delete -alias "cas" -keystore ./truststore.jks -storepass changeit
keytool -importcert -file cert.pem -keystore ./truststore.jks -alias "cas" --storepass changeit -noprompt
