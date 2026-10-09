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

phaseStartedAt=$SECONDS
phaseTimings=()

function recordPhase() {
  phaseTimings+=("$1 $((SECONDS - phaseStartedAt))s")
  phaseStartedAt=$SECONDS
}

function reportPhases() {
  recordPhase "remaining"
  local summary
  summary=$(printf '%s, ' "${phaseTimings[@]}")
  summary="${summary%, }, total ${SECONDS}s"
  printgreen "Documentation build timing: ${summary}"
  if [[ "$CI" == "true" ]]; then
    echo "::notice title=Documentation build timing::${summary}"
  fi
}

function publishConfigurationMetadata() {
  local metadataFile="$1"
  local casVersion="$2"

  if [[ "$uploadMetadata" != "true" ]]; then
    printyellow "Configuration metadata upload is turned off; skipping configuration metadata publication."
    return 0
  fi
  if [[ -z "${CAS_MODULE_METADATA_MONGODB_URL:-}" ]]; then
    printyellow "MongoDB settings are not defined; skipping configuration metadata publication."
    return 0
  fi
  if [[ "${GITHUB_EVENT_NAME:-}" == "pull_request" ]]; then
    printyellow "Skipping configuration metadata publication for a pull request."
    return 0
  fi
  if [[ "$thirdParty" != "true" ]]; then
    printyellow "Third-party configuration metadata is disabled; skipping incomplete upload & publication."
    return 0
  fi

  local releaseVersion="${casVersion%%-*}"
  if [[ ! "$releaseVersion" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    printred "Unable to determine configuration metadata collection from CAS version $casVersion"
    return 1
  fi
  local patchVersion="${releaseVersion##*.}"
  if [[ "$patchVersion" != "0" ]]; then
    printyellow "CAS $casVersion is a patch or security release; skipping configuration metadata publication."
    return 0
  fi

  if [[ ! -s "$metadataFile" ]]; then
    printred "Combined configuration metadata file does not exist or is empty: $metadataFile"
    return 1
  fi

  local versionNumbers="$releaseVersion"
  versionNumbers="${versionNumbers//./}"
  local collectionName="casconfig${versionNumbers}"
  local mongoImportExecutable="${CAS_MODULE_METADATA_MONGOIMPORT_EXECUTABLE:-mongoimport}"
  if ! command -v "$mongoImportExecutable" >/dev/null 2>&1; then
    printred "MongoDB import executable is not available: $mongoImportExecutable"
    return 1
  fi

  printgreen "Uploading combined configuration metadata for CAS $casVersion to $collectionName..."
  "$mongoImportExecutable" \
    --uri "$CAS_MODULE_METADATA_MONGODB_URL" \
    --collection "$collectionName" \
    --file "$metadataFile" \
    --type json \
    --jsonArray \
    --drop
  local result=$?
  if [[ $result -ne 0 ]]; then
    printred "Failed to upload combined configuration metadata to MongoDB collection $collectionName"
    return $result
  fi
  printgreen "Uploaded combined configuration metadata to MongoDB collection $collectionName"
}

# Returns 0 when all links pass, 3 when internal links, images or scripts are broken,
# 4 when only external links failed, and 1 when the proofer itself could not run.
function validateProjectDocumentation() {
  local proofLog="$PWD/html-proofer.log"
  DOCS_PROOF_EXTERNAL="$proofExternal" \
    BUNDLE_GEMFILE="${BUNDLE_GEMFILE:-$PWD/gh-pages/Gemfile}" \
    bundle exec ruby "$PWD/ci/docs/proof.rb" 2>&1 | tee "$proofLog"

  local result=${PIPESTATUS[0]}
  if [[ ${result} -eq 0 ]]; then
    printgreen "HTML Proofer found no bad links."
    return 0
  fi

  local level="error" title="HTML Proofer failures"
  case ${result} in
  3) printred "HTML Proofer found broken internal links, images or scripts." ;;
  4)
    printyellow "HTML Proofer found broken external links only."
    level="warning"
    title="External link failures"
    ;;
  *)
    printred "HTML Proofer failed to run (exit code ${result})."
    result=1
    ;;
  esac
  if [[ "$CI" == "true" ]]; then
    local failures
    failures=$(grep -A 3 '^\* ' "$proofLog" | grep -v '^--$' | head -60 | sed ':a;N;$!ba;s/%/%25/g;s/\n/%0A/g')
    echo "::${level} title=${title}::${failures:-see the job log}"
  fi
  return ${result}
}


GRADLE_BUILD_OPTIONS="-q -x check -x test -x javadoc --configure-on-demand --max-workers=8  "

REPOSITORY_NAME="apereo/cas"
REPOSITORY_ADDR="https://${GH_PAGES_TOKEN}@github.com/${REPOSITORY_NAME}"

branchVersion="master"
propFilter=".+"
generateData=true
audit=true
proofRead=true
proofExternal=true
externalLinkFailures=false
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
uploadMetadata=true

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
      propFilter="nothing"
      shift 1
    else 
      shift 2
    fi
    printgreen "Generating documentation for property filter: ${propFilter}"
    serve=true
    proofRead=false
    audit=true
    actuators=true
    thirdParty=true
    serviceProps=true
    publishDocs=false
    buildDocs=true
    buildFeatures=true
    shellCommands=true
    dependencyVersions=true
    userinterface=true
    uploadMetadata=false
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
  --proof-external)
    proofExternal=$2
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
  --skip-upload)
    uploadMetadata=false
    shift 1
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
  printgreen "Configuring git settings..."
  git config --global http.postbuffer 524288000
  git config --global credential.helper "cache --timeout=86400"
  git config --global pack.threads "$(nproc 2>/dev/null || sysctl -n hw.ncpu)"
  git config --global init.defaultBranch master
  git config --global protocol.version 2
fi

echo "-------------------------------------------------------"
printgreen "Branch: \t\t${branchVersion}"
printgreen "Build: \t\t${buildDocs}"
printgreen "Serve: \t\t${serve}"
printgreen "Generate Data: \t${generateData}"
printgreen "Validate: \t\t${proofRead}"
printgreen "External Links: \t${proofExternal}"
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
printgreen "Upload(s): \t\t${uploadMetadata}"
printgreen "Ruby Version: \t$(ruby -v)"
echo "-------------------------------------------------------"

trap reportPhases EXIT
recordPhase "setup"

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

rm -Rf "$PWD/docs-latest"
rm -Rf "$PWD/docs-includes"
rm -Rf "$PWD/docs-layouts"
rm -Rf "$PWD/docs-includes-site"

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
  git clone --single-branch --depth 1 --branch gh-pages --no-checkout \
    --filter=blob:none --no-tags --quiet "${REPOSITORY_ADDR}" "$PWD/gh-pages"
  printgreen "Checking out the shared site files and $branchVersion only..."
  git -C "$PWD/gh-pages" sparse-checkout set --no-cone \
    '/*' '!/*/' '/_includes/' '/_layouts/' '/stylesheets/' '/javascripts/' \
    '/images/' '/assets/' '/developer/' "/$branchVersion/"
  git -C "$PWD/gh-pages" checkout --quiet gh-pages

  printgreen "Removing previous documentation from $branchVersion..."
  rm -Rf "$PWD/gh-pages/$branchVersion" >/dev/null
  rm -Rf "$PWD/gh-pages/_includes/$branchVersion" >/dev/null
  rm -Rf "$PWD/gh-pages/_layouts/$branchVersion" >/dev/null
  rm -Rf "$PWD/gh-pages/_data/$branchVersion" >/dev/null
  rm -Rf "$PWD/gh-pages/_sass" >/dev/null

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
  [[ -f "$PWD/docs-latest/Gemfile.lock" ]] && mv "$PWD/docs-latest/Gemfile.lock" "$PWD/gh-pages"
  rm -Rf "$PWD/docs-latest/.bundle"
  rm -Rf "$PWD/gh-pages/_plugins"
  mv "$PWD/docs-latest/_plugins" "$PWD/gh-pages/_plugins"

  cp -Rf "$PWD"/docs-latest/* "$PWD/gh-pages/$branchVersion"
  if [[ $branchVersion == "development" ]]; then
    printgreen "Moving developer documentation into project documentation"
    mv "$PWD"/docs-latest/developer/* "$PWD/gh-pages/developer/"
  fi
  rm -Rf "$PWD/gh-pages/$branchVersion/developer"
  mv "$PWD"/docs-latest/javascripts/* "$PWD/gh-pages/javascripts/"
  mv "$PWD"/docs-latest/stylesheets/* "$PWD/gh-pages/stylesheets/"
  printgreen "Removing..."
  rm -Rf "$PWD"/gh-pages/stylesheets/*.scss

  rm -Rf "$PWD"/docs-latest/_sass
  rm -Rf "$PWD/gh-pages/_sass"
  printgreen "Copying..."
  cp -Rf "$PWD"/docs-includes/* "$PWD/gh-pages/_includes/$branchVersion" &
  cp -Rf "$PWD"/docs-layouts/* "$PWD/gh-pages/_layouts" &
  cp -Rf "$PWD"/docs-includes-site/* "$PWD/gh-pages/_includes" &
  wait

  rm -Rf "$PWD/gh-pages/_data/$branchVersion" >/dev/null
  rm -Rf "$PWD/docs-latest"
  rm -Rf "$PWD/docs-includes"
  rm -Rf "$PWD/docs-layouts"
  rm -Rf "$PWD/docs-includes-site"
  printgreen "Copied project documentation to $PWD/gh-pages/..."
  # exit 1
fi
recordPhase "clone"

if [[ $generateData == "true" ]]; then
  # The generator runs from Gradle's runtime classpath (an argument file) instead of a ~1 GB boot jar,
  # and its compile skips Error Prone/NullAway: the class files are the same, only the static analysis is dropped.
  docgen="docs/cas-server-documentation-processor/build/casdocsgen.args"
  printgreen "Generating documentation site data..."
  if [[ ! -f "$docgen" ]]; then
    ./gradlew :docs:cas-server-documentation-processor:jsonDependencies \
      :docs:cas-server-documentation-processor:docsGeneratorArguments \
      $GRADLE_BUILD_OPTIONS ${DOCS_GENERATOR_GRADLE_OPTIONS--DskipErrorProneCompiler=true}
    if [ $? -ne 0 ] || [[ ! -s "$docgen" ]]; then
      printred "Unable to build the documentation processor. Aborting..."
      exit 1
    fi
  fi
  recordPhase "gradle"
  dataDir=$(echo "$branchVersion" | sed 's/\.//g')
  printgreen "Generating documentation data at $PWD/gh-pages/_data/$dataDir with filter $propFilter..."
  java "@${docgen}" -d "$PWD/gh-pages/_data" -v "$dataDir" -r "$PWD" \
    -f "$propFilter" -a "$actuators" -tp "$thirdParty" \
    -sp "$serviceProps" -ft "$buildFeatures" -csh "$shellCommands" \
    -aud "$audit" -ver "$dependencyVersions" -ui "$userinterface"
  if [ $? -ne 0 ]; then
    printred "Unable to generate documentation data. Aborting..."
    exit 1
  fi

  printgreen "Generated documentation data at $PWD/gh-pages/_data/$dataDir..."

  casVersion=(`cat "$PWD"/gradle.properties | grep "version" | cut -d= -f2`)
  printgreen "CAS version is $casVersion"
  configurationCatalog="$PWD/api/cas-server-core-api-configuration-model/build/libs/cas-server-core-api-configuration-model-${casVersion}.jar"
  springConfigurationMetadata="$PWD/gh-pages/spring-configuration-metadata.json"
  thirdPartyConfigurationMetadata="$PWD/gh-pages/_data/$dataDir/third-party/config.yml"
  combinedConfigurationMetadata="$PWD/gh-pages/combined-configuration-properties.json"
  printgreen "Configuration catalog is at $configurationCatalog"
  rm -f "$springConfigurationMetadata" "$combinedConfigurationMetadata" >/dev/null 2>&1
  unzip -p "$configurationCatalog" META-INF/spring-configuration-metadata.json > "$springConfigurationMetadata"
  if [[ ! -s "$springConfigurationMetadata" ]]; then
    printred "CAS configuration metadata file does not exist or is empty: $springConfigurationMetadata"
    exit 1
  fi
  if [[ "$thirdParty" == "true" && ! -s "$thirdPartyConfigurationMetadata" ]]; then
    printred "Third-party configuration metadata file does not exist or is empty: $thirdPartyConfigurationMetadata"
    exit 1
  fi
  rm -rf "$PWD/gh-pages/assets/data/$branchVersion"/index.json >/dev/null 2>&1
  npm --prefix $PWD/ci/docs install
  printgreen "Creating configuration metadata index..."
  mkdir -p "$PWD/gh-pages/assets/data/$branchVersion"
  indexArguments=(
    "$springConfigurationMetadata"
    "$thirdPartyConfigurationMetadata"
    "$PWD/gh-pages/assets/data/$branchVersion/index.json"
  )
  if [[ "$thirdParty" == "true" ]]; then
    indexArguments+=("$combinedConfigurationMetadata")
  fi
  node "$PWD/ci/docs/index.js" "${indexArguments[@]}"
  if [[ $? -ne 0 ]]; then
    printred "Unable to create configuration metadata index. Aborting..."
    exit 1
  fi
  publishConfigurationMetadata "$combinedConfigurationMetadata" "$casVersion"
  if [[ $? -ne 0 ]]; then
    rm -f "$combinedConfigurationMetadata" >/dev/null 2>&1
    exit 1
  fi
  rm -f "$springConfigurationMetadata" "$combinedConfigurationMetadata" >/dev/null 2>&1
  if [[ ! -e "$PWD/gh-pages/assets/data/$branchVersion"/index.json ]]; then
    printred "$PWD/gh-pages/assets/data/$branchVersion/index.json does not exist."
  fi

else
  printgreen "Skipping documentation data generation..."
  rm -Rf "$PWD/gh-pages/_data"
fi
recordPhase "data"

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

recordPhase "fragments"

if [[ ${buildDocs} == "true" ]]; then
  pushd .

  if [[ "$CI" == "true" ]]; then
    if [[ -d "$PWD/jekyll/.jekyll-cache" ]]; then
      printgreen "Restoring the Jekyll cache into $PWD/gh-pages/"
      mv "$PWD/jekyll/.jekyll-cache" "$PWD/gh-pages/"
    fi
    rm -Rf "$PWD/jekyll"
  fi

  cd "$PWD/gh-pages" || exit
  ruby --version

  if bundle check >/dev/null 2>&1; then
    printgreen "Documentation dependencies are already installed"
  else
    printgreen "Installing documentation dependencies..."
    bundle install
  fi
  printgreen "Building documentation site for $branchVersion with data at $PWD/gh-pages/_data"
  echo -n "Starting at " && date
  bundle exec jekyll --version

  export RUBY_YJIT_ENABLE=1
  if [[ ${serve} == "true" ]]; then
    printgreen "Serving the documentation at http://localhost:4000/cas/${branchVersion}/"
    bundle exec jekyll serve --profile --incremental --trace
  else
    bundle exec jekyll build --trace
  fi
  retVal=$?

  echo -n "Ended at " && date
  if [[ ${retVal} -eq 1 ]]; then
    printred "Failed to build documentation."
    exit ${retVal}
  fi
  popd

  if [[ "$CI" == "true" ]]; then
    mkdir -p "$PWD/jekyll"
    if [[ -d "$PWD/gh-pages/.jekyll-cache" ]]; then
      mv "$PWD/gh-pages/.jekyll-cache" "$PWD/jekyll/"
      printgreen "Jekyll cache is now at $PWD/jekyll/ ($(du -sh "$PWD/jekyll/.jekyll-cache" | cut -f1))"
    fi
  else
    printyellow "Deleting Jekyll build directory"
    rm -Rf "$PWD"/jekyll/
  fi
fi

recordPhase "Jekyll"

if [[ $proofRead == "true" ]]; then
  printgreen "Validating documentation links..."
  validateProjectDocumentation
  retVal=$?
  if [[ ${retVal} -eq 4 ]]; then
    printyellow "Documentation will still be published; the job reports the external link failures at the end."
    externalLinkFailures=true
    retVal=0
  elif [[ ${retVal} -ne 0 ]]; then
    printred "Failed to validate documentation."
    exit ${retVal}
  fi
fi

recordPhase "proofread"

pushd .
cd "$PWD/gh-pages" || exit

if [[ $clone == "true" ]]; then
  rm -Rf .jekyll-cache .jekyll-metadata .sass-cache "$branchVersion/build" _plugins
  printgreen "Configuring git repository settings..."
  git config user.email "cas@apereo.org"
  git config user.name "CAS"
  git config core.fileMode false

  rm -Rf "./$branchVersion"
  mv "_site/$branchVersion" .
  touch "$branchVersion/.nojekyll"
  rm -Rf _site
  rm -Rf _data

  printgreen "Starting a new single-commit history on top of the cloned objects..."
  git checkout --quiet --orphan gh-pages-publish
fi

if [ -z "$GH_PAGES_TOKEN" ] && [ "${GITHUB_REPOSITORY}" != "${REPOSITORY_NAME}" ]; then
  printyellow "No GitHub token is defined to publish documentation. Skipping..."
  if [[ $clone == "true" ]]; then
    popd
    rm -Rf "$PWD/gh-pages"
    [[ $externalLinkFailures == "true" ]] && exit 4
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

  printgreen "Pushing changes to upstream..."
  git push -fq origin HEAD:gh-pages
  retVal=$?
  if [[ ${retVal} -eq 1 ]]; then
    printred "Failed to push documentation."
    exit ${retVal}
  fi
  printgreen "Pushed upstream to origin/gh-pages..."
  retVal=$?
  recordPhase "publish"
else
  printyellow "Skipping documentation push to remote repository..."
fi

popd

if [[ $clone == "true" ]]; then
  rm -Rf "$PWD/gh-pages" || true
fi

if [[ ${retVal} -eq 0 ]]; then
  printgreen "Done processing documentation to $branchVersion."
  if [[ $externalLinkFailures == "true" ]]; then
    printred "External link checks failed; see the HTML Proofer output above."
    exit 4
  fi
  exit 0
else
  printred "Failed to process documentation."
  exit ${retVal}
fi
