#!/bin/bash

# Every variation gets a freshly provisioned Samba AD: the script changes the password of mustchangepassword, and
# run-ad-server.sh keeps a container that is already running (returning with exit 0, which ends run.sh when sourced).
# Running the bootstrap as a child process keeps its exit away from run.sh as well.
docker rm -f samba >/dev/null 2>&1
bash "${PWD}/ci/tests/ldap/run-ad-server.sh"
