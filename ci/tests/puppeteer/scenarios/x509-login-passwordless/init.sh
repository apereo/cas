#!/bin/bash
echo "Renew trusted certificate in the keystore..."

openssl x509 -x509toreq -in ${SCENARIO_FOLDER}/cert.pem -out ${SCENARIO_FOLDER}/cert.csr -signkey ${SCENARIO_FOLDER}/key.pem
openssl x509 -req -days 3650 -in ${SCENARIO_FOLDER}/cert.csr -out ${SCENARIO_FOLDER}/cert.pem.new -signkey ${SCENARIO_FOLDER}/key.pem
mv ${SCENARIO_FOLDER}/cert.pem.new ${SCENARIO_FOLDER}/cert.pem
rm ${SCENARIO_FOLDER}/cert.csr
#openssl x509 -in ${SCENARIO_FOLDER}/cert.pem -text
keytool -delete -alias "cas" -keystore ${SCENARIO_FOLDER}/truststore.jks -storepass changeit
keytool -importcert -file ${SCENARIO_FOLDER}/cert.pem -keystore ${SCENARIO_FOLDER}/truststore.jks -alias "cas" --storepass changeit -noprompt
