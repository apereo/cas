#!/bin/bash
RED="\e[31m"
GREEN="\e[32m"
YELLOW="\e[33m"
ENDCOLOR="\e[0m"
function printred() {
  printf "${RED}$1${ENDCOLOR}\n"
}
function printgreen() {
  printf "${GREEN}$1${ENDCOLOR}\n"
}
function printyellow() {
  printf "${YELLOW}$1${ENDCOLOR}\n"
}

function validateProjectDocumentation() {
  ruby $PWD/ci/docs/proof.rb

  retVal=$?
  if [[ ${retVal} -eq 0 ]]; then
    printgreen "HTML Proofer found no bad links."
    return 0
  else
    printred "HTML Proofer found bad links."
    return 1
  fi
}

clear

REPOSITORY_NAME="apereo/cas"
REPOSITORY_ADDR="https://${GH_PAGES_TOKEN}@github.com/${REPOSITORY_NAME}"

branchVersion="master"
propFilter=".+"
generateData=true
proofRead=true
actuators=true
thirdParty=true
serviceProps=true
publishDocs=true
buildDocs=true
serve=false
clone=true

while (("$#")); do
  case "$1" in
  --branch)
    branchVersion=$2
    shift 2
    ;;
  --generate-data|--data)
    generateData=$2
    shift 2
    ;;
  --proof-read|--validate)
    proofRead=$2
    shift 2
    ;;
  --publish)
    publishDocs=$2
    shift 2
    ;;
  --skip-clone)
    clone=false
    shift;
    ;;
  --build)
    buildDocs=$2
    shift 2
    ;;
  --serve)
    serve=$2
    shift 2
    ;;
  --filter)
    propFilter=$2
    shift 2
    ;;
  --actuators)
    actuators=$2
    shift 2
    ;;
  --thirdParty|--thirdparty)
    thirdParty=$2
    shift 2
    ;;
  --serviceProperties)
    serviceProps=$2
    shift 2
    ;;
  *)
    shift
    ;;
  esac
done

if [[ $branchVersion == "master" ]]; then
  branchVersion="development"
fi

if [ -z "$GH_PAGES_TOKEN" ] && [ "${GITHUB_REPOSITORY}" != "${REPOSITORY_NAME}" ]; then
  publishDocs=false
  printyellow "\nNo GitHub token is defined to publish documentation."
fi

echo "-------------------------------------------------------"
printgreen "Branch: \t${branchVersion}"
printgreen "Build: \t\t${buildDocs}"
printgreen "Serve: \t\t${serve}"
printgreen "Generate Data: \t${generateData}"
printgreen "Validate: \t${proofRead}"
printgreen "Publish: \t${publishDocs}"
printgreen "Filter: \t${propFilter}"
printgreen "Actuators: \t${actuators}"
printgreen "Third Party: \t${thirdParty}"
printgreen "Ruby Version: \t$(ruby -v)"
echo "-------------------------------------------------------"

if [[ $clone == "true" ]]; then
  rm -Rf "$PWD/gh-pages"
  [[ -d $PWD/docs-latest ]] && rm -Rf "$PWD"/docs-latest
  [[ -d $PWD/docs-includes ]] && rm -Rf "$PWD"/docs-includes

  printgreen "Copying project documentation over to $PWD/docs-latest...\n"
  chmod -R 777 docs/cas-server-documentation
  cp -R docs/cas-server-documentation/ "$PWD"/docs-latest
  mv "$PWD/docs-latest/_includes" "$PWD/docs-includes"

  printgreen "Cloning ${REPOSITORY_NAME}'s [gh-pages] branch...\n"
  [[ -d "$PWD/gh-pages" ]] && rm -Rf "$PWD/gh-pages"
  git clone --single-branch --depth 1 --branch gh-pages --quiet "${REPOSITORY_ADDR}" $PWD/gh-pages

  printgreen "Removing previous documentation from $branchVersion...\n"
  rm -Rf "$PWD/gh-pages/$branchVersion" >/dev/null
  rm -Rf "$PWD/gh-pages/_includes/$branchVersion" >/dev/null
  rm -Rf "$PWD/gh-pages/_data/$branchVersion" >/dev/null

  printgreen "Creating $branchVersion directory...\n"
  mkdir -p "$PWD/gh-pages/$branchVersion"
  mkdir -p "$PWD/gh-pages/_includes/$branchVersion"
  mkdir -p "$PWD/gh-pages/_data/$branchVersion"

  printgreen "Copying new docs to $branchVersion...\n"
  mv "$PWD/docs-latest/Gemfile" "$PWD/gh-pages"
  mv "$PWD/docs-latest/_config.yml" "$PWD/gh-pages"
  rm -f "$PWD/gh-pages/Gemfile.lock"

  cp -Rf "$PWD"/docs-latest/* "$PWD/gh-pages/$branchVersion"
  cp -Rf "$PWD"/docs-includes/* "$PWD/gh-pages/_includes/$branchVersion"
  rm -Rf "$PWD/gh-pages/_data/$branchVersion" >/dev/null
  rm -Rf "$PWD/docs-latest"
  rm -Rf "$PWD/docs-includes"
  printgreen "Copied project documentation to $PWD/gh-pages/...\n"
fi

if [[ $generateData == "true" ]]; then
  docgen="docs/cas-server-documentation-processor/build/libs/casdocsgen.jar"
  printgreen "Generating documentation site data...\n"
  if [[ ! -f "$docgen" ]]; then
    ./gradlew :docs:cas-server-documentation-processor:build --no-daemon -x check -x test -x javadoc --configure-on-demand
    if [ $? -eq 1 ]; then
      echo "Unable to build the documentation processor. Aborting..."
      exit 1
    fi
  fi
  chmod +x ${docgen}
  dataDir=$(echo "$branchVersion" | sed 's/\.//g')
  printgreen "Generating documentation data at $PWD/gh-pages/_data/$dataDir with filter $propFilter...\n"
  ${docgen} -d "$PWD/gh-pages/_data" -v "$dataDir" -r "$PWD" -f "$propFilter" -a "$actuators" -tp "$thirdParty" -sp "$serviceProps"
  printgreen "Generated documentation data at $PWD/gh-pages/_data/$dataDir...\n"
else
  printgreen "Skipping documentation data generation...\n"
  rm -Rf "$PWD/gh-pages/_data"
fi

if [[ $proofRead == "true" ]]; then
  printgreen "Looking for badly named include fragments..."
  ls "$PWD"/gh-pages/_includes/$branchVersion/*.md | grep -v '\-configuration.md$'
  docsVal=$?
  if [ $docsVal == 0 ]; then
    printred "Found include fragments whose name does not end in '-configuration.md'"
    exit 1
  fi

  printgreen "Looking for unused include fragments..."
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
    printred "Found unused include fragments."
    exit 1
  fi

else
  printgreen "Skipping validation of documentation links..."
fi


if [[ ${buildDocs} == "true" ]]; then
  pushd .
  cd "$PWD/gh-pages"
  printgreen "Installing documentation dependencies...\n"
  bundle install
  printgreen "\nBuilding documentation site for $branchVersion with data at $PWD/gh-pages/_data"
  echo -n "Starting at " && date
  jekyll --version

  if [[ ${serve} == "true" ]]; then
    bundle exec jekyll serve --profile --incremental
  else
    bundle exec jekyll build --profile
  fi

  echo -n "Ended at " && date
  retVal=$?
  if [[ ${retVal} -eq 1 ]]; then
    printred "Failed to build documentation.\n"
    exit ${retVal}
  fi
  popd
fi

if [[ $proofRead == "true" ]]; then
  printgreen "Validating documentation links..."
  validateProjectDocumentation
  retVal=$?
  if [[ ${retVal} -eq 1 ]]; then
    printred "Failed to validate documentation.\n"
    exit ${retVal}
  fi
fi

pushd .
cd "$PWD/gh-pages"

if [[ $clone == "true" ]]; then
  rm -Rf .jekyll-cache .jekyll-metadata .sass-cache "$branchVersion/build"
  printgreen "\nConfiguring git repository settings...\n"
  rm -Rf .git
  git init
  git config init.defaultBranch master
  git remote add origin "${REPOSITORY_ADDR}"
  git config user.email "cas@apereo.org"
  git config user.name "CAS"
  git config core.fileMode false

  printgreen "Checking out gh-pages branch..."
  git switch gh-pages 2>/dev/null || git switch -c gh-pages 2>/dev/null
  printgreen "Configuring tracking branches for repository...\n"
  git branch -u origin/gh-pages

  rm -Rf "./$branchVersion"
  mv "_site/$branchVersion" .
  touch "$branchVersion/.nojekyll"
  rm -Rf _site
  rm -Rf _data
fi 

if [[ "${publishDocs}" == "true" ]]; then
  printgreen "Adding changes to the git index...\n"
  git add --all -f 2>/dev/null

  printgreen "Committing changes...\n"
  git commit -am "Published docs to [gh-pages] from $branchVersion." 2>/dev/null
  git status

  printgreen "Pushing changes to remote repository...\n"
  if [ -z "$GH_PAGES_TOKEN" ] && [ "${GITHUB_REPOSITORY}" != "${REPOSITORY_NAME}" ]; then
    printyellow "\nNo GitHub token is defined to publish documentation. Skipping"
    popd
    rm -Rf "$PWD/gh-pages"
    exit 0
  fi

  printgreen "Pushing upstream to origin/gh-pages...\n"
  git push -fq origin gh-pages
  retVal=$?
else
  printyellow "Skipping documentation push to remote repository...\n"
fi

popd

if [[ $clone == "true" ]]; then
  rm -Rf "$PWD/gh-pages"
fi

if [[ ${retVal} -eq 0 ]]; then
  printgreen "Done processing documentation to $branchVersion.\n"
  exit 0
else
  printred "Failed to process documentation.\n"
  exit ${retVal}
fi
