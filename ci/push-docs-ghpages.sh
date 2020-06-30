#!/bin/bash


branchVersion="6.2.x"

echo -e "Copying project documentation over to $HOME/docs-latest...\n"
cp -R docs/cas-server-documentation $HOME/docs-latest

cd $HOME
git config --global user.email "travis@travis-ci.org"
git config --global user.name "travis-ci"
git config --global pack.threads "8"

echo -e "Cloning the repository to push documentation...\n"

git clone --single-branch --depth 1 --branch gh-pages --quiet https://${GH_PAGES_TOKEN}@github.com/apereo/cas gh-pages
cd gh-pages

echo -e "Switching to gh-pages branch\n"
git checkout gh-pages > /dev/null

echo -e "Configuring tracking branches for repository...\n"
git branch -u origin/gh-pages

echo -e "Removing previous documentation from $branchVersion...\n"
git rm -rf ./"$branchVersion" > /dev/null

echo -e "Creating $branchVersion directory...\n"
test -d "./$branchVersion" || mkdir -m777 -v "./$branchVersion"

echo -e "Copying new docs from $HOME/docs-latest over to $branchVersion...\n"
cp -Rf $HOME/docs-latest/* "./$branchVersion"
echo -e "Copied project documentation...\n"

echo -e "Adding changes to the git index...\n"
git add -f . > /dev/null

echo -e "Committing changes...\n"
git commit -m "Published docs [gh-pages]. " > /dev/null

echo -e "Pushing upstream to origin/gh-pages...\n"
git push -fq origin --all
retVal=$?
if [[ ${retVal} -eq 0 ]]; then
    echo -e "Successfully published documentation.\n"
    exit 0
else
    echo -e "Failed to publish documentation.\n"
    exit ${retVal}
fi
