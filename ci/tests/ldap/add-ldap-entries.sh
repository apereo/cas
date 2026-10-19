#!/bin/bash
# Adds the entries of an LDIF file to the ldap-server container started by run-ldap-server.sh,
# waiting for the server to answer first. Entries that already exist are skipped.
ldif="$1"
if [[ ! -f "${ldif}" ]]; then
  echo "LDIF file ${ldif} cannot be found"
  exit 1
fi
for attempt in {1..30}; do
  docker exec ldap-server ldapsearch -x -D "cn=Directory Manager" -w password -H ldap:// -b "dc=example,dc=org" -s base dn >/dev/null 2>&1 && break
  echo "Waiting for the LDAP server, attempt ${attempt}"
  sleep 2
done
echo "Adding LDAP entries from ${ldif}"
docker exec -i ldap-server ldapadd -c -x -D "cn=Directory Manager" -w password -H ldap:// < "${ldif}"
result=$?
if [[ ${result} -ne 0 && ${result} -ne 68 ]]; then
  echo "Failed to add LDAP entries from ${ldif}: ${result}"
  exit 1
fi
