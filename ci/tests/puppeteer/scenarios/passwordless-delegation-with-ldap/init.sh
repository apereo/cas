#!/bin/bash

echo -e "Removing previous SAML metadata directory, if any"
rm -Rf "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-md"
echo -e "Creating SAML metadata directory"
mkdir "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-md"
chmod +x "${PWD}/ci/tests/saml2/run-saml-server.sh"
"${PWD}/ci/tests/saml2/run-saml-server.sh"

${PWD}/ci/tests/ldap/run-ldap-server.sh

ldif=$(cat ${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/ldap-passwordless.ldif)
echo -e "Importing LDIF:\n${ldif}"

docker exec ldap-server bash -c "echo '${ldif}' > ./users.ldif"
docker exec -it ldap-server ls
docker exec ldap-server bash -c "cat ./users.ldif"

echo -e "Adding entry..."
docker exec ldap-server bash -c "ldapadd -x -D 'cn=Directory Manager' -w password -H ldap:// -f ./users.ldif"
echo -e "\n\nReady!"
