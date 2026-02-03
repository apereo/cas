#!/bin/bash
echo "Renew client certificate ..."

openssl x509 -x509toreq -in ${SCENARIO_FOLDER}/cert.pem -out ${SCENARIO_FOLDER}/cert.csr -signkey ${SCENARIO_FOLDER}/key.pem
openssl x509 -req -days 3650 -in ${SCENARIO_FOLDER}/cert.csr -out ${SCENARIO_FOLDER}/cert.pem.new -signkey ${SCENARIO_FOLDER}/key.pem
mv ${SCENARIO_FOLDER}/cert.pem.new ${SCENARIO_FOLDER}/cert.pem
rm ${SCENARIO_FOLDER}/cert.csr
