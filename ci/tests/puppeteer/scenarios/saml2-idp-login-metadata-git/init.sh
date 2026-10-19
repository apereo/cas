#!/bin/bash
echo -e "Removing previous SAML metadata directory"
rm -Rf "${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}/saml-md"

root="/tmp/cas-saml-metadata-git"
echo -e "Creating the git repository that holds SAML2 metadata at ${root}/remote.git"
rm -Rf "${root}"
mkdir -p "${root}"
git init --quiet --bare --initial-branch=master "${root}/remote.git" || exit 1
git clone --quiet "${root}/remote.git" "${root}/seed" || exit 1
echo "SAML2 metadata managed by CAS" > "${root}/seed/README.md"
git -C "${root}/seed" add README.md
git -C "${root}/seed" -c user.name=CAS -c user.email=cas@apereo.org -c commit.gpgsign=false commit --quiet -m "Initial commit" || exit 1
git -C "${root}/seed" push --quiet origin master || exit 1
