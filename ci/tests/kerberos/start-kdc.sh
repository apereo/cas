#!/bin/sh
#
# Runs inside the KDC container: creates the EXAMPLE.ORG realm with the CAS service principal and a user,
# writes a keytab for the user so the host can obtain a ticket, then runs the KDC in the foreground.
#
set -e
if ! command -v krb5kdc >/dev/null 2>&1; then
    apk add --no-cache krb5 krb5-server >/dev/null
fi
export KRB5_CONFIG=/kerberos/krb5.conf
export KRB5_KDC_PROFILE=/kerberos/kdc.conf
mkdir -p /var/lib/krb5kdc
touch /var/lib/krb5kdc/kadm5.acl
kdb5_util create -s -r EXAMPLE.ORG -P "${KERBEROS_MASTER_PASSWORD:-M4st3rK3y}"
kadmin.local -q "addprinc -pw ${KERBEROS_SERVICE_PASSWORD:-Kerb3r0s} HTTP/localhost"
kadmin.local -q "addprinc -pw Mellon casuser"
kadmin.local -q "ktadd -k /var/lib/krb5kdc/casuser.keytab -norandkey casuser"
exec krb5kdc -n
