#!/bin/bash

${PWD}/ci/tests/ldap/run-ldap-server.sh
${PWD}/ci/tests/mail/run-mail-server.sh

ldif=$(cat ${PWD}/ci/tests/puppeteer/scenarios/passwordless-mfa-login-duo-with-ldap/ldap-passwordless.ldif)
echo -e "Importing LDIF:\n${ldif}"

docker exec ldap-server bash -c "echo '${ldif}' > ./users.ldif"
docker exec -it ldap-server ls
docker exec ldap-server bash -c "cat ./users.ldif"

echo -e "Adding entry..."
docker exec -it ldap-server bash -c "ldapadd -x -D 'cn=Directory Manager' -w password -H ldap:// -f ./users.ldif"
echo -e "\n\nReady!"
