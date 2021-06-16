#!/bin/bash

keystore="$PWD"/ci/tests/puppeteer/overlay/thekeystore
truststore=$PWD/ci/tests/puppeteer/scenarios/oidc-authzcode-pac4j-delegated-login/truststore

echo -e "Removing previous truststore, if exists"
rm -rf $truststore
echo -e "Creating truststore $truststore with cert from keystore $keystore"
keytool -keystore $keystore -exportcert -file cacert -alias cas -storepass changeit
keytool -keystore $truststore -storepass changeit -noprompt -importcert -alias localhost -file cacert
rm -rf cacert

keytool -keystore $truststore -storepass changeit -list
echo "Truststore for Pac4j created (to accept the selfsigned X509 cert from thekeystore)"