#!/bin/bash
invokeDoc=false

casBranch="5.2.x"
branchVersion="5.2.x"

# Only invoke the javadoc deployment process
# for the first job in the build matrix, so as
# to avoid multiple deployments.

if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$PUBLISH_SNAPSHOTS" == "false" ] && [ "$TRAVIS_BRANCH" == "$casBranch" ]; then
  case "${TRAVIS_JOB_NUMBER}" in
       *\.1)
        echo -e "Invoking auto-doc deployment for Travis job ${TRAVIS_JOB_NUMBER}"
        invokeDoc=true;;
  esac
fi

if [ "$invokeDoc" == true ]; then
  echo -e "Copying project documentation over to $HOME/docs-latest...\n"
  cp -R docs/cas-server-documentation $HOME/docs-latest
fi

if [[ "$invokeDoc" == true ]]; then

  cd $HOME
  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "travis-ci"
  git config --global pack.threads "24"
  
  echo -e "Cloning the repository to push documentation...\n"
  git clone --single-branch --depth 1 --branch gh-pages --quiet https://${GH_TOKEN}@github.com/apereo/cas gh-pages > /dev/null
  cd gh-pages
  # git gc --aggressive --prune=now
  
  echo -e "Configuring tracking branches for repository...\n"
  for branch in `git branch -a | grep remotes | grep -v HEAD | grep -v $casBranch`; do
     git branch --track ${branch##*/} $branch
  done

  echo -e "Switching to gh-pages branch\n"
  git checkout gh-pages

  echo -e "\nStaring to move project documentation over...\n"

  if [ "$invokeDoc" == true ]; then
    echo -e "Removing previous documentation from $branchVersion...\n"
    git rm -rf ./"$branchVersion" > /dev/null

    echo -e "Creating $branchVersion directory...\n"
    test -d "./$branchVersion" || mkdir -m777 -v "./$branchVersion"

    echo -e "Copying new docs from $HOME/docs-latest over to $branchVersion...\n"
    cp -Rf $HOME/docs-latest/* "./$branchVersion"
    echo -e "Copied project documentation...\n"
  fi

  echo -e "Adding changes to the git index...\n"
  git add -f . > /dev/null

  echo -e "Committing changes...\n"
  git commit -m "Published documentation from $TRAVIS_BRANCH to [gh-pages]. Build $TRAVIS_BUILD_NUMBER " > /dev/null
  
  # echo -e "Before: Calculating git repository disk usage:\n"
  # du -sh .git/

  # echo -e "Before: Counting git objects in the repository:\n"
  # git count-objects -vH
  
  # echo -e "\nCleaning up repository...\n"
  # rm -rf .git/refs/original/
  # rm -Rf .git/logs/
  # git reflog expire --expire=now --all
  
  # echo -e "\nRunning garbage collection on the git repository...\n"
  # git gc --prune=now
  # git repack -a -d --depth=500000 --window=500
  
  # echo -e "After: Calculating git repository disk usage:\n"
  # du -sh .git/
  
  # echo -e "After: Counting git objects in the repository:\n"
  # git count-objects -vH
  
  echo -e "Pushing upstream to origin/gh-pages...\n"
  git push -fq origin --all > /dev/null

  echo -e "Successfully published documentation.\n"
fi
