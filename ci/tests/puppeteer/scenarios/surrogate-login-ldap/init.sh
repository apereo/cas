#!/bin/bash

${PWD}/ci/tests/httpbin/run-httpbin-server.sh
sleep 2

${PWD}/ci/tests/ldap/run-ldap-server.sh
sleep 5

ldif=$(cat ${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/ldap.ldif)
echo -e "Importing LDIF:\n${ldif}"

docker exec ldap-server bash -c "echo '${ldif}' > ./users.ldif"
docker exec -it ldap-server ls
docker exec ldap-server bash -c "cat ./users.ldif"

echo -e "Adding entry..."
docker exec ldap-server bash -c "ldapadd -x -D 'cn=Directory Manager' -w password -H ldap:// -f ./users.ldif"
echo -e "\n\nReady!"
