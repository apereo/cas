#!/bin/bash

RED="\e[31m"
GREEN="\e[32m"
YELLOW="\e[33m"
ENDCOLOR="\e[0m"

function printred() {
  printf "🔥 ${RED}$1${ENDCOLOR}\n"
}
function printgreen() {
  printf "🍀 ${GREEN}$1${ENDCOLOR}\n"
}
function printyellow() {
  printf "⚠️  ${YELLOW}$1${ENDCOLOR}\n"
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


GRADLE_BUILD_OPTIONS="-q --no-daemon -x check -x test -x javadoc --configure-on-demand --max-workers=8 --no-configuration-cache "

REPOSITORY_NAME="apereo/cas"
REPOSITORY_ADDR="https://${GH_PAGES_TOKEN}@github.com/${REPOSITORY_NAME}"

branchVersion="master"
propFilter=".+"
generateData=true
audit=true
proofRead=true
actuators=true
thirdParty=true
serviceProps=true
publishDocs=true
buildDocs=true
clone=true
buildFeatures=true
shellCommands=true
dependencyVersions=true
userinterface=true

serve=false


while (("$#")); do
  case "$1" in
  --reset)
    printgreen "Resetting local build to allow forceful creation of documentation binary artifacts..."
    ./gradlew :api:cas-server-core-api-configuration-model:clean :docs:cas-server-documentation-processor:clean $GRADLE_BUILD_OPTIONS
    printgreen "Build completed. Documentation binary artifacts and configuration catalog will be rebuilt on the next attempt."
    shift 1
    ;;
  --local)
    propFilter=$2
    if [[ -z "$propFilter" ]]; then
      propFilter="-none-"
    fi
    shift 2
    printgreen "Generating documentation for property filter: ${propFilter}"
    serve=true
    proofRead=false
    audit=false
    actuators=false
    thirdParty=false
    serviceProps=false
    publishDocs=false
    buildDocs=true
    buildFeatures=false
    shellCommands=false
    dependencyVersions=false
    userinterface=false
    ;;
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
  --actuators|--act)
    actuators=$2
    shift 2
    ;;
  --thirdParty|--thirdparty|--tp)
    thirdParty=$2
    shift 2
    ;;
  --serviceProperties|--sp)
    serviceProps=$2
    shift 2
    ;;
  --shell)
    shellCommands=$2
    shift 2
    ;;
  --audit|--aud)
    audit=$2
    shift 2
    ;;
  --versions)
    dependencyVersions=$2
    shift 2
    ;;
  --ui|--themes|--userinterface)
    userinterface=$2
    shift 2
    ;;
  --features|--feat|--ft)
    buildFeatures=$2
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
  printyellow "No GitHub token is defined to publish documentation."
fi

if [[ "${CI}" == "true" ]]; then
  echo "Configuring git settings..."
  git config --global http.postbuffer 524288000
  git config --global credential.helper "cache --timeout=86400"
  git config --global pack.threads "8"
fi

echo "-------------------------------------------------------"
printgreen "Branch: \t\t${branchVersion}"
printgreen "Build: \t\t${buildDocs}"
printgreen "Serve: \t\t${serve}"
printgreen "Generate Data: \t${generateData}"
printgreen "Validate: \t\t${proofRead}"
printgreen "Publish: \t\t${publishDocs}"
printgreen "Filter: \t\t${propFilter}"
printgreen "Actuators: \t\t${actuators}"
printgreen "Third Party: \t${thirdParty}"
printgreen "Dependency Versions: ${dependencyVersions}"
printgreen "Service Properties: \t${serviceProps}"
printgreen "Features: \t\t${buildFeatures}"
printgreen "Shell: \t\t${shellCommands}"
printgreen "Audit: \t\t${audit}"
printgreen "UI: \t\t\t${userinterface}"
printgreen "Ruby Version: \t$(ruby -v)"
echo "-------------------------------------------------------"

cloneRepository=false
if [[ $clone == "true" ]]; then
  printgreen "Project documentation is instructed to be cloned"
  cloneRepository=true
elif [[ ! -d "$PWD/gh-pages" ]]; then
  printgreen "Project documentation directory does not exist, and will be cloned"
  cloneRepository=true
else
  printgreen "Project documentation will be reused from "$PWD/gh-pages""
  cloneRepository=false
fi

if [[ $cloneRepository == "true" ]]; then
  rm -Rf "$PWD/gh-pages"
  [[ -d $PWD/docs-latest ]] && rm -Rf "$PWD"/docs-latest
  [[ -d $PWD/docs-includes ]] && rm -Rf "$PWD"/docs-includes
  [[ -d $PWD/docs-includes-site ]] && rm -Rf "$PWD"/docs-includes-site

  printgreen "Copying project documentation over to $PWD/docs-latest.."
  chmod -R 777 docs/cas-server-documentation
  cp -R docs/cas-server-documentation/ "$PWD"/docs-latest
  mv "$PWD/docs-latest/_includes" "$PWD/docs-includes"
  mv "$PWD/docs-latest/_layouts" "$PWD/docs-layouts"
  mv "$PWD/docs-latest/_includes_site" "$PWD/docs-includes-site"

  printgreen "Cloning ${REPOSITORY_NAME}'s [gh-pages] branch..."
  [[ -d "$PWD/gh-pages" ]] && rm -Rf "$PWD/gh-pages"
  mkdir -p "$PWD/gh-pages"
  git clone --single-branch --depth 1 --branch gh-pages --quiet "${REPOSITORY_ADDR}" $PWD/gh-pages

  printgreen "Removing previous documentation from $branchVersion..."
  rm -Rf "$PWD/gh-pages/$branchVersion" >/dev/null
  rm -Rf "$PWD/gh-pages/_includes/$branchVersion" >/dev/null
  rm -Rf "$PWD/gh-pages/_layouts/$branchVersion" >/dev/null
  rm -Rf "$PWD/gh-pages/_data/$branchVersion" >/dev/null

  printgreen "Creating $branchVersion directory..."
  mkdir -p "$PWD/gh-pages/$branchVersion"
  mkdir -p "$PWD/gh-pages/_includes/$branchVersion"
  mkdir -p "$PWD/gh-pages/_includes"
  mkdir -p "$PWD/gh-pages/javascripts"
  mkdir -p "$PWD/gh-pages/stylesheets"
  mkdir -p "$PWD/gh-pages/_layouts"
  mkdir -p "$PWD/gh-pages/_data/$branchVersion"
  
  printgreen "Copying new docs to $branchVersion..."
  mv "$PWD/docs-latest/Gemfile" "$PWD/gh-pages"
  mv "$PWD/docs-latest/Support.md" "$PWD/gh-pages"
  mv "$PWD/docs-latest/404.md" "$PWD/gh-pages"
  mv "$PWD/docs-latest/_config.yml" "$PWD/gh-pages"
  rm -f "$PWD/gh-pages/Gemfile.lock"

  cp -Rf "$PWD"/docs-latest/* "$PWD/gh-pages/$branchVersion"
  if [[ $branchVersion == "development" ]]; then
    printgreen "Moving developer documentation into project documentation"
    mv "$PWD"/docs-latest/developer/* "$PWD/gh-pages/developer/"
  fi
  rm -Rf "$PWD/gh-pages/$branchVersion/developer"
  mv "$PWD"/docs-latest/javascripts/* "$PWD/gh-pages/javascripts/"
  mv "$PWD"/docs-latest/stylesheets/* "$PWD/gh-pages/stylesheets/"
  mv "$PWD"/docs-latest/_sass/* "$PWD/gh-pages/_sass/"
  cp -Rf "$PWD"/docs-includes/* "$PWD/gh-pages/_includes/$branchVersion"
  cp -Rf "$PWD"/docs-layouts/* "$PWD/gh-pages/_layouts"
  cp -Rf "$PWD"/docs-includes-site/* "$PWD/gh-pages/_includes"

  rm -Rf "$PWD/gh-pages/_data/$branchVersion" >/dev/null
  rm -Rf "$PWD/docs-latest"
  rm -Rf "$PWD/docs-includes"
  rm -Rf "$PWD/docs-layouts"
  rm -Rf "$PWD/docs-includes-site"
  printgreen "Copied project documentation to $PWD/gh-pages/..."
  # exit 1
fi

if [[ $generateData == "true" ]]; then
  docgen="docs/cas-server-documentation-processor/build/libs/casdocsgen.jar"
  printgreen "Generating documentation site data..."
  if [[ ! -f "$docgen" ]]; then
    ./gradlew :docs:cas-server-documentation-processor:jsonDependencies :docs:cas-server-documentation-processor:build $GRADLE_BUILD_OPTIONS
    if [ $? -eq 1 ]; then
      printred "Unable to build the documentation processor. Aborting..."
      exit 1
    fi
  fi
  chmod +x ${docgen}
  dataDir=$(echo "$branchVersion" | sed 's/\.//g')
  printgreen "Generating documentation data at $PWD/gh-pages/_data/$dataDir with filter $propFilter..."
  ${docgen} -d "$PWD/gh-pages/_data" -v "$dataDir" -r "$PWD" \
    -f "$propFilter" -a "$actuators" -tp "$thirdParty" \
    -sp "$serviceProps" -ft "$buildFeatures" -csh "$shellCommands" \
    -aud "$audit" -ver "$dependencyVersions" -ui "$userinterface"
  if [ $? -eq 1 ]; then
    printred "Unable to generate documentation data. Aborting..."
    exit 1
  fi
  
  printgreen "Generated documentation data at $PWD/gh-pages/_data/$dataDir..."

  casVersion=(`cat "$PWD"/gradle.properties | grep "version" | cut -d= -f2`)
  printgreen "CAS version is $casVersion"
  configurationCatalog="$PWD/api/cas-server-core-api-configuration-model/build/libs/cas-server-core-api-configuration-model-${casVersion}.jar"
  printgreen "Configuration catalog is at $configurationCatalog"
  rm -rf "$PWD/gh-pages/spring-configuration-metadata.json" >/dev/null 2>&1
  unzip -p $configurationCatalog META-INF/spring-configuration-metadata.json > $PWD/gh-pages/spring-configuration-metadata.json
  rm -rf "$PWD/gh-pages/assets/data/$branchVersion"/index.json >/dev/null 2>&1
  npm --prefix $PWD/ci/docs install
  printgreen "Creating configuration metadata index..."
  mkdir -p "$PWD/gh-pages/assets/data/$branchVersion"
  node $PWD/ci/docs/index.js $PWD/gh-pages/spring-configuration-metadata.json $PWD/gh-pages/_data/$branchVersion/third-party/config.yml "$PWD/gh-pages/assets/data/$branchVersion"/index.json
  rm -rf "$PWD/gh-pages/spring-configuration-metadata.json" >/dev/null 2>&1
  if [[ ! -e "$PWD/gh-pages/assets/data/$branchVersion"/index.json ]]; then
    printred "$PWD/gh-pages/assets/data/$branchVersion/index.json does not exist."
    exit 1
  fi

else
  printgreen "Skipping documentation data generation..."
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
        printred "$f is unused."
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

  if [[ "$CI" == "true" ]]; then
    printgreen "Moving jekyll artifacts into $PWD/gh-pages/ directory"
    mv "$PWD"/jekyll/.jekyll-cache "$PWD/gh-pages/"
    mv "$PWD"/jekyll/.jekyll-metadata "$PWD/gh-pages/"
    rm -Rf "$PWD"/jekyll
  fi

  cd "$PWD/gh-pages" || exit
  ruby --version

  printgreen "Installing documentation dependencies..."
  bundle config set force_ruby_platform true
  bundle install
  printgreen "Building documentation site for $branchVersion with data at $PWD/gh-pages/_data"
  echo -n "Starting at " && date
  jekyll --version

  if [[ ${serve} == "true" ]]; then
    bundle exec jekyll serve --profile --incremental --trace
  else
    bundle exec jekyll build --profile --incremental --trace
  fi
  retVal=$?

  echo -n "Ended at " && date
  if [[ ${retVal} -eq 1 ]]; then
    printred "Failed to build documentation."
    exit ${retVal}
  fi
  popd
  
  if [[ "$CI" == "true" ]]; then
    echo "Moving jekyll build artifacts into $PWD/jekyll"
    mkdir -p "$PWD/jekyll"
    mv "$PWD"/gh-pages/.jekyll-cache "$PWD"/jekyll/
    mv "$PWD"/gh-pages/.jekyll-metadata "$PWD"/jekyll/
    printgreen "Jekyll cache is now at $PWD/jekyll/"
    ls -al "$PWD/jekyll/"
  else
    printyellow "Deleting jekyll build directory"
    rm -Rf "$PWD"/jekyll/
  fi
fi

if [[ $proofRead == "true" ]]; then
  printgreen "Validating documentation links..."
  validateProjectDocumentation
  retVal=$?
  if [[ ${retVal} -eq 1 ]]; then
    printred "Failed to validate documentation."
    exit ${retVal}
  fi
fi

pushd .
cd "$PWD/gh-pages" || exit

if [[ $clone == "true" ]]; then
  rm -Rf .jekyll-cache .jekyll-metadata .sass-cache "$branchVersion/build"
  rm -Rf "$branchVersion/build"
  printgreen "Configuring git repository settings..."
  rm -Rf .git
  git init
  git config init.defaultBranch master
  git remote add origin "${REPOSITORY_ADDR}"
  git config user.email "cas@apereo.org"
  git config user.name "CAS"
  git config core.fileMode false

  printgreen "Checking out gh-pages branch..."
  git switch gh-pages 2>/dev/null || git switch -c gh-pages 2>/dev/null
  printgreen "Configuring tracking branches for repository..."
  git branch -u origin/gh-pages

  rm -Rf "./$branchVersion"
  mv "_site/$branchVersion" .
  touch "$branchVersion/.nojekyll"
  rm -Rf _site
  rm -Rf _data
fi

if [ -z "$GH_PAGES_TOKEN" ] && [ "${GITHUB_REPOSITORY}" != "${REPOSITORY_NAME}" ]; then
  printyellow "No GitHub token is defined to publish documentation. Skipping..."
  if [[ $clone == "true" ]]; then
    rm -Rf "$PWD/gh-pages"
    exit 0
  fi
elif [[ "${publishDocs}" == "true" ]]; then
  printgreen "Adding changes to the git index..."
  git add --all -f 2>/dev/null

  printgreen "Committing changes..."
  git commit -am "Published docs to [gh-pages] from $branchVersion." --quiet 2>/dev/null
  retVal=$?
  if [[ ${retVal} -eq 1 ]]; then
    printred "Failed to push documentation."
    exit ${retVal}
  fi
  git status

  echo "Pushing changes to upstream..."
  git push -fq origin gh-pages
  retVal=$?
  if [[ ${retVal} -eq 1 ]]; then
    printred "Failed to push documentation."
    exit ${retVal}
  fi
  printgreen "Pushed upstream to origin/gh-pages..."
  retVal=$?
else
  printyellow "Skipping documentation push to remote repository..."
fi

popd

if [[ $clone == "true" ]]; then
  rm -Rf "$PWD/gh-pages" || true
fi

if [[ ${retVal} -eq 0 ]]; then
  printgreen "Done processing documentation to $branchVersion."
  exit 0
else
  printred "Failed to process documentation."
  exit ${retVal}
fi
