#!/bin/bash
#
# Licensed to Jasig under one or more contributor license
# agreements. See the NOTICE file distributed with this work
# for additional information regarding copyright ownership.
# Jasig licenses this file to you under the Apache License,
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

docsDirectoryForTheRelease="4.0.0"
echo -e "Preparing Javadoc for 4.0.x branch release...\n"

if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "4.0.x" ]; then

  echo -e "Start to publish lastest Javadoc to gh-pages...\n"

  cp -R target/staging $HOME/javadoc-latest

  cd $HOME
  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "travis-ci"
  echo -e "Cloning the gh-pages branch...\n"
  git clone --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/Jasig/cas gh-pages > /dev/null

  cd gh-pages
  echo -e "Removing $docsDirectoryForTheRelease javadocs...\n"
  git rm -rf ./$docsDirectoryForTheRelease/javadocs

  echo -e "Copying new javadocs to $docsDirectoryForTheRelease/javadocs...\n"
  cp -Rf $HOME/javadoc-latest ./$docsDirectoryForTheRelease/javadocs
  echo -e "Adding changes to the git index...\n"
  git add -f .
  echo -e "Committing changes...\n"
  git commit -m "Lastest javadoc on successful travis build $TRAVIS_BUILD_NUMBER auto-pushed to gh-pages"
  echo -e "Pushing upstream to origin...\n"
  git push -fq origin gh-pages > /dev/null

  echo -e "Done magic with auto publishment to gh-pages.\n"
  
fi
