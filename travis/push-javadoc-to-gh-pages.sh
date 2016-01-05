#!/bin/bash
#
# Licensed to Apereo under one or more contributor license
# agreements. See the NOTICE file distributed with this work
# for additional information regarding copyright ownership.
# Apereo licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file
# except in compliance with the License.  You may obtain a
# copy of the License at the following location:
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

invokeDoc=false

# Only invoke the javadoc deployment process
# for the first job in the build matrix, so as
# to avoid multiple deployments.

if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "4.1.x" ]; then
  case "${TRAVIS_JOB_NUMBER}" in
       *\.1) 
        echo -e "Invoking auto-doc deployment for Travis job ${TRAVIS_JOB_NUMBER}"
        invokeDoc=true;
  esac
fi

echo -e "Staring with project docs...\n"

if [ "$invokeDoc" == true ]; then

  echo -e "Started to publish latest Javadoc to gh-pages...\n"
  echo -e "Invoking Maven to generate the project site...\n"
  mvn -T 20 site site:stage -q -ff -B -P nocheck -Dversions.skip=false
  echo -e "Copying the generated docs over...\n"
  cp -R target/staging $HOME/javadoc-latest

  echo -e "Copying project documentation over to $HOME/docs-latest...\n"
  cp -R cas-server-documentation $HOME/docs-latest

  cd $HOME
  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "travis-ci"
  echo -e "Cloning the gh-pages branch...\n"
  git clone --depth 10 --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/Jasig/cas gh-pages > /dev/null
  cd gh-pages

  echo -e "Staring to move project documentation over...\n"

  echo -e "Removing previous documentation from development...\n"
  git rm -rf ./4.1.x > /dev/null

  echo -e "Creating 4.1.x directory...\n"
  test -d "./4.1.x" || mkdir -m777 -v ./4.1.x

  echo -e "Copying new docs from $HOME/docs-latest over to 4.1.x...\n"
  cp -Rf $HOME/docs-latest/* ./4.1.x
  echo -e "Copied project documentation...\n"

  echo -e "Staring to move project Javadocs over...\n"

  echo -e "Removing previous Javadocs from /$TRAVIS_BRANCH/javadocs...\n"
  git rm -rf ./"$TRAVIS_BRANCH"/javadocs > /dev/null

  echo -e "Creating Javadocs directory at /$TRAVIS_BRANCH/javadocs...\n"
  test -d "./$TRAVIS_BRANCH/javadocs" || mkdir -m777 -v ./"$TRAVIS_BRANCH"/javadocs

  echo -e "Copying new Javadocs...\n"
  cp -Rf $HOME/javadoc-latest/* ./"$TRAVIS_BRANCH"/javadocs
  echo -e "Copied project Javadocs...\n"


  echo -e "Adding changes to the git index...\n"
  git add -f . > /dev/null

  echo -e "Committing changes...\n"
  git commit -m "Published documentation to [gh-pages]. Build $TRAVIS_BUILD_NUMBER" > /dev/null

  echo -e "Pushing upstream to origin...\n"
  git push -fq origin gh-pages > /dev/null

  echo -e "Successfully published documenetation to [gh-pages] $TRAVIS_BRANCH branch.\n"

fi
