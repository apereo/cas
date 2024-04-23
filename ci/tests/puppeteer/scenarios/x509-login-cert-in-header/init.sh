#!/bin/bash
cd "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}"
echo "Renew client certificate ..."

openssl x509 -x509toreq -in cert.pem -out cert.csr -signkey key.pem
openssl x509 -req -days 3650 -in cert.csr -out cert.pem.new -signkey key.pem
mv cert.pem.new cert.pem
rm cert.csr
