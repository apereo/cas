#!/bin/bash

clear
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
rm -Rf $PWD/gh-pages/_includes/"$branchVersion" > /dev/null
rm -Rf $PWD/gh-pages/_data/"$branchVersion" > /dev/null

echo -e "Creating $branchVersion directory...\n"
mkdir -p "$PWD/gh-pages/$branchVersion"
mkdir -p "$PWD/gh-pages/_includes/$branchVersion"
mkdir -p "$PWD/gh-pages/_data/$branchVersion"

echo -e "Copying new docs to $branchVersion...\n"
cp -Rf $PWD/docs-latest/* "$PWD/gh-pages/$branchVersion"
cp -Rf $PWD/docs-includes/* "$PWD/gh-pages/_includes/$branchVersion"
echo -e "Copied project documentation to $PWD/gh-pages/...\n"

echo -e "Generating documentation site data...\n"
rm -Rf $PWD/gh-pages/_data/"$branchVersion" > /dev/null
./gradlew :docs:cas-server-documentation-processor:build --no-daemon -x check -x test -x javadoc --configure-on-demand
if [ $? -eq 1 ]; then
  echo "Unable to build the documentation processor. Aborting..."
  exit 1
fi

docgen="docs/cas-server-documentation-processor/build/libs/casdocsgen.jar"
chmod +x ${docgen}
${docgen} "$PWD/gh-pages/_data" "$branchVersion" "$PWD"
rm -Rf docs/cas-server-documentation-processor/build
echo -e "Generated documentation data at $PWD/gh-pages/_data/$branchVersion...\n"

rm -Rf $PWD/docs-latest
rm -Rf $PWD/docs-includes

echo "Looking for badly named include fragments..."
ls $PWD/gh-pages/_includes/$branchVersion/*.md | grep -v '\-configuration.md$'
docsVal=$?
if [ $docsVal == 0 ]; then
 echo "Found include fragments whose name does not end in '-configuration.md'"
 exit 1
fi

echo "Looking for unused include fragments..."
res=0
files=$(ls $PWD/gh-pages/_includes/$branchVersion/*.md)
for f in $files; do
 fname=$(basename "$f")
#  echo "Looking for $fname in $PWD/gh-pages/$branchVersion";
 grep -r $fname "$PWD/gh-pages/$branchVersion" --include \*.md >/dev/null 2>&1
 docsVal=$?
 if [ $docsVal == 1 ]; then
   grep -r $fname "$PWD/gh-pages/_includes/$branchVersion" --include \*.md >/dev/null 2>&1
   docsVal=$?
 fi
 if [ $docsVal == 1 ]; then
   grep "fragment:keep" $f >/dev/null 2>&1
   docsVal=$?
   if [ $docsVal == 1 ]; then
      echo "$f is unused."
      rm "docs/cas-server-documentation/_includes/$fname"
      res=1
   fi
 fi
done

if [ $res == 1 ]; then
 echo "Found unused include fragments."
 exit 1
fi

echo "Validating documentation links..."
validateProjectDocumentation
retVal=$?
if [[ ${retVal} -eq 1 ]]; then
  echo -e "Failed to validate documentation.\n"
  exit ${retVal}
fi

pushd .
cd $PWD/gh-pages

echo -e "Installing documentation dependencies...\n"
bundle install --full-index
bundle update jekyll
bundle update github-pages
echo -e "\nBuilding documentation site...\n"
bundle exec jekyll build --incremental --profile 
retVal=$?
if [[ ${retVal} -eq 1 ]]; then
  echo -e "Failed to build documentation.\n"
  exit ${retVal}
fi

rm -Rf _site .jekyll-metadata .sass-cache "$branchVersion/build"

echo -e "\nConfiguring git repository settings...\n"
git config user.email "cas@apereo.org"
git config user.name "CAS"
git config core.fileMode false

echo -e "Configuring tracking branches for repository...\n"
git branch -u origin/gh-pages

echo -e "Adding changes to the git index...\n"
git add -f . 

echo -e "Committing changes...\n"
git commit -m "Published docs to [gh-pages] from $branchVersion. "

if [ -z "$GH_PAGES_TOKEN" ] && [ "${GITHUB_REPOSITORY}" != "apereo/cas" ]; then
  echo -e "\nNo GitHub token is defined to publish documentation."
  popd
  rm -Rf $PWD/gh-pages
  exit 0
fi

echo -e "Pushing upstream to origin/gh-pages...\n"
git push -fq origin --all
retVal=$?

popd
rm -Rf $PWD/gh-pages

if [[ ${retVal} -eq 0 ]]; then
   echo -e "Published documentation to $branchVersion.\n"
   exit 0
else
   echo -e "Failed to publish documentation.\n"
   exit ${retVal}
fi

