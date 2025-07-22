#!/bin/bash

RED="\e[31m"
GREEN="\e[32m"
YELLOW="\e[33m"
ENDCOLOR="\e[0m"

casVersion=(`cat ./gradle.properties | grep "version" | cut -d= -f2`)

function printgreen() {
  printf "☘️  ${GREEN}$1${ENDCOLOR}\n"
}

function printyellow() {
  printf "⚠️  ${YELLOW}$1${ENDCOLOR}\n"
}

function printred() {
  printf "🚨  ${RED}$1${ENDCOLOR}\n"
}

function clean {
  ./gradlew clean --parallel --no-configuration-cache --no-daemon
}

function snapshot() {
  if [[ "${casVersion}" != *SNAPSHOT* ]] ;
  then
      printred "CAS version ${casVersion} MUST be a SNAPSHOT version"
      exit 1
  fi
  printgreen "Publishing CAS SNAPSHOT artifacts. This might take a while..."
  ./gradlew assemble publish \
    -x test -x javadoc -x check --no-daemon --parallel \
    -DskipAot=true -DpublishSnapshots=true --stacktrace \
    --no-configuration-cache --configure-on-demand \
    -DrepositoryUsername="$REPOSITORY_USER" -DrepositoryPassword="$REPOSITORY_PWD"
  if [ $? -ne 0 ]; then
      printred "Publishing CAS SNAPSHOTs failed."
      exit 1
  fi
}

function publish {
    if [[ "${casVersion}" == *SNAPSHOT* ]]; then
        printred "CAS version ${casVersion} cannot be a SNAPSHOT version"
        exit 1
    fi
    printgreen "Publishing CAS releases. This might take a while..."
    ./gradlew assemble publishAggregationToCentralPortal \
      --parallel --no-daemon --no-configuration-cache -x test -x check \
      -DskipAot=true -DpublishReleases=true --stacktrace \
      -DrepositoryUsername="$REPOSITORY_USER" -DrepositoryPassword="$REPOSITORY_PWD"
    if [ $? -ne 0 ]; then
        printred "Publishing Apereo CAS failed."
        exit 1
    fi

    if [[ "$CI" == "true" ]]; then
        git config --global user.email "cas@apereo.org"
        git config --global user.name "Apereo CAS"
    fi

    releaseTag="v${casVersion}"

    printgreen "Removing previous source tree tag ${releaseTag}, if any"
    git tag -d "${releaseTag}" 2>/dev/null
    git push --delete origin "${releaseTag}" 2>/dev/null

    printgreen "Deleting previous GitHub Release for ${releaseTag}, if any"
    gh release delete "${releaseTag}" --cleanup-tag -y --repo "apereo/cas" 2>/dev/null

    printgreen "Tagging the source tree for CAS version ${casVersion}"
    git tag "${releaseTag}" -m "Tagging CAS ${releaseTag} release" && git push origin "${releaseTag}"
    if [ $? -ne 0 ]; then
        printred "Tagging the source tree for CAS version ${casVersion} failed."
        exit 1
    fi

    printgreen "Creating GitHub Release for ${releaseTag}"

    releaseFlags=""
    if [[ "${casVersion}" == *-RC* ]]; then
        releaseFlags="--prerelease"
        printgreen "This is a release candidate, and will be marked as pre-release on GitHub."
    else
        releaseFlags="--latest"
        printgreen "This is a final release, and will be marked as latest on GitHub."
    fi

    currentBranch=$(git branch --show-current)
    if [[ ${currentBranch} == "master" ]]; then
        documentationBranch="development"
    else
        documentationBranch=${currentBranch}
    fi

    changelog=""
    if [[ $casVersion =~ -RC([0-9]+)$ ]]; then
      rc_max=${BASH_REMATCH[1]}

      links=()
      for (( i=1; i<=rc_max; i++ )); do
        links+=( "[RC$i](https://apereo.github.io/cas/${documentationBranch}/release_notes/RC$i.html)" )
      done
      changelog=$(printf '%s\n' "${links[*]// /,}")
    fi

    if [[ -n "${changelog}" ]]; then
      changelog="- Changelog: ${changelog}"
    fi
    
    currentCommit=$(git rev-parse HEAD)
    printgreen "Current commit is ${currentCommit}"

    previousTag=$(git describe --tags --abbrev=0 "${releaseTag}^")
    echo "Looking at commits in range: $previousTag..$releaseTag" >&2

    contributors=$(gh api repos/apereo/cas/compare/$previousTag...$releaseTag \
      --jq '.commits[].author.login // .commits[].commit.author.name' \
      | sort -u \
      | sed 's/.*/- @&/')

    notes='
# :star: Release Notes

- [Documentation](https://apereo.github.io/cas/${documentationBranch})
- [Commit Log](https://github.com/apereo/cas/commits/${currentCommit})
- [Maintenance Policy](https://apereo.github.io/cas/developer/Maintenance-Policy.html)
- [Release Policy](https://apereo.github.io/cas/developer/Release-Policy.html)
- [Release Schedule](https://github.com/apereo/cas/milestones)
- Changelog: ${changelog}

# :couple: Contributions

Special thanks to the following individuals for their excellent contributions:	

${contributors}
    '

    releaseNotes=$(eval "cat <<EOF $notes")

    gh release create "${releaseTag}" --notes "${releaseNotes}" \
      --title "${releaseTag}" --draft --verify-tag --repo "apereo/cas" ${releaseFlags} \
      "./support/cas-server-support-shell/build/libs/cas-server-support-shell-${casVersion}.jar#CAS Command-line Shell" 
    if [ $? -ne 0 ]; then
        printred "Creating GitHub Release for CAS version ${casVersion} failed."
        exit 1
    fi
}

function finished {
    printgreen "Done! The release is now automatically published. There is nothing more for you to do. Thank you!"
}

if [[ "${casVersion}" == v* ]]; then
    printred "CAS version ${casVersion} is incorrect and likely a tag."
    exit 1
fi

echo -e "\n"
echo "***************************************************************"
printgreen "Welcome to the release process for Apereo CAS ${casVersion}"
echo -n $(java -version)
echo -e "***************************************************************\n"

if [[ -z $REPOSITORY_USER || -z $REPOSITORY_PWD ]]; then
  printred "Repository username and password are missing."
  printred "Make sure the following environment variables are defined: REPOSITORY_USER and REPOSITORY_PWD"
  exit 1
fi

if [[ "${casVersion}" == *SNAPSHOT* ]]; then
  selection="2"
else
  selection="1"
fi

case "$selection" in
    1)
        clean
        publish
        finished
        ;;
    2)
        snapshot
        finished
        ;;
    *)
        printred "Unable to recognize selection"
        exit 1
        ;;
esac
exit 0
