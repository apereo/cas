#!/bin/bash
"${PWD}/ci/tests/ldap/run-ldap-server.sh" || exit 1
"${PWD}/ci/tests/ldap/add-ldap-entries.sh" "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/gauth.ldif" || exit 1
