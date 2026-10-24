#!/bin/bash

# Starts an MIT Kerberos KDC for the EXAMPLE.ORG realm on port 10088 and obtains a ticket for casuser.
# The realm has the CAS service principal HTTP/localhost (password Kerb3r0s) and the user casuser (password Mellon).
# The ticket cache is written to ${KERBEROS_TICKET_CACHE_DIR}/casuser.ccache so a browser can sign in with SPNEGO.
# This script is sourced by the puppeteer runner, so it returns instead of exiting.

RED="\e[31m"
GREEN="\e[32m"
ENDCOLOR="\e[0m"

function printgreen() {
  printf "🍀 ${GREEN}$1${ENDCOLOR}\n"
}

function printred() {
  printf "🚨  ${RED}$1${ENDCOLOR}\n"
}

export KERBEROS_TICKET_CACHE_DIR="${TMPDIR:-/tmp}/cas-kerberos"
mkdir -p "${KERBEROS_TICKET_CACHE_DIR}"
rm -f "${KERBEROS_TICKET_CACHE_DIR}/casuser.ccache"

printgreen "Running Kerberos KDC docker container..."
docker rm -f kerberos-kdc >/dev/null 2>&1 || true
docker run --rm --detach --name kerberos-kdc \
  -p 10088:88 -p 10088:88/udp \
  -v "${PWD}/ci/tests/kerberos:/kerberos:ro" \
  -v "${KERBEROS_TICKET_CACHE_DIR}:/tickets" \
  alpine:3.20 sh /kerberos/start-kdc.sh

kerberosTicketCreated=false
for _ in $(seq 1 60); do
  if docker exec -e KRB5_CONFIG=/kerberos/krb5.conf kerberos-kdc \
    sh -c "kinit -kt /var/lib/krb5kdc/casuser.keytab -c FILE:/tickets/casuser.ccache casuser && chmod 644 /tickets/casuser.ccache" >/dev/null 2>&1; then
    kerberosTicketCreated=true
    break
  fi
  sleep 2
done

if [[ "${kerberosTicketCreated}" != "true" ]]; then
  printred "Unable to obtain a Kerberos ticket for casuser"
  docker logs kerberos-kdc
  return 1 2>/dev/null || exit 1
fi
docker exec -e KRB5_CONFIG=/kerberos/krb5.conf kerberos-kdc klist -c FILE:/tickets/casuser.ccache
printgreen "Kerberos ticket cache for casuser is at ${KERBEROS_TICKET_CACHE_DIR}/casuser.ccache"
