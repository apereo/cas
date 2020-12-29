#!/bin/bash

branchVersion="$1"

function validateProjectDocumentation {
  HTML_PROOFER_IMAGE=hdeadman/html-proofer:latest
  DOCS_FOLDER=$PWD/gh-pages/"$branchVersion"
  DOCS_OUTPUT=/tmp/build/out
  HTML_PROOFER_SCRIPT=$PWD/ci/docs/html-proofer-docs.rb
  
  echo "Running html-proof image: ${HTML_PROOFER_IMAGE} on ${DOCS_FOLDER} with output ${DOCS_OUTPUT} using ${HTML_PROOFER_SCRIPT}"
  docker run --name="html-proofer" --rm \
      --workdir /root \
      -v ${DOCS_FOLDER}:/root/docs \
      -v ${DOCS_OUTPUT}:/root/out \
      -v ${HTML_PROOFER_SCRIPT}:/root/html-proofer-docs.rb \
      --entrypoint /usr/local/bin/ruby \
       ${HTML_PROOFER_IMAGE} \
       /root/html-proofer-docs.rb
  retVal=$?
  if [[ ${retVal} -eq 0 ]]; then
      echo "HTML Proofer found no bad links."
      return 0
  else
      echo "HTML Proofer found bad links."
      return 1
  fi
}

[[ -d $PWD/docs-latest ]] && rm -Rf $PWD/docs-latest
[[ -d $PWD/docs-includes ]] && rm -Rf $PWD/docs-includes

echo -e "Copying project documentation over to $PWD/docs-latest...\n"
chmod -R 777 docs/cas-server-documentation
cp -R docs/cas-server-documentation/ $PWD/docs-latest
mv $PWD/docs-latest/_includes $PWD/docs-includes

echo -e "Cloning the repository to push documentation...\n"
[[ -d $PWD/gh-pages ]] && rm -Rf $PWD/gh-pages

git clone --single-branch --depth 1 --branch gh-pages --quiet \
  https://${GH_PAGES_TOKEN}@github.com/apereo/cas $PWD/gh-pages

echo -e "Removing previous documentation from $branchVersion...\n"
rm -Rf $PWD/gh-pages/"$branchVersion" > /dev/null

echo -e "Creating $branchVersion directory...\n"
mkdir -p "$PWD/gh-pages/$branchVersion"
mkdir -p "$PWD/gh-pages/_includes/$branchVersion"

echo -e "Copying new docs to $branchVersion...\n"
cp -Rf $PWD/docs-latest/* "$PWD/gh-pages/$branchVersion"
cp -Rf $PWD/docs-includes/* "$PWD/gh-pages/_includes/$branchVersion"

rm -Rf "$PWD/gh-pages/$branchVersion/build"
echo -e "Copied project documentation...\n"

rm -Rf $PWD/docs-latest
rm -Rf $PWD/docs-includes

validateProjectDocumentation
retVal=$?
if [[ ${retVal} -eq 1 ]]; then
    echo -e "Failed to validate documentation.\n"
#    exit ${retVal}
fi

cd $PWD/gh-pages

git config user.email "cas@apereo.org"
git config user.name "CAS"

echo -e "Configuring tracking branches for repository...\n"
git branch -u origin/gh-pages

echo -e "Adding changes to the git index...\n"
git add -f .

echo -e "Committing changes...\n"
git commit -m "Published docs [gh-pages]. "

echo -e "Pushing upstream to origin/gh-pages...\n"
#git push -fq origin --all
#retVal=$?
#rm -Rf $PWD/gh-pages
#if [[ ${retVal} -eq 0 ]]; then
#    echo -e "Successfully published documentation.\n"
#    exit 0
#else
#    echo -e "Failed to publish documentation.\n"
#    exit ${retVal}
#fi
