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

if test "$1" == ""; then
    if [ "$TRAVIS_COMMIT_RANGE" == "" -o "$TRAVIS_PULL_REQUEST" == "false" ]
    then
        echo -n "Using fallback for commit range (last commit): "
        RANGE='HEAD^..HEAD'
    else
        echo -n "Using \$TRAVIS_COMMIT_RANGE for commit range: "
        # Range is given on the form of 515744f1079f...23be2b8db4d7.
        # Therefore, we need to adjust it to Git commit range format. 
        RANGE=`echo $TRAVIS_COMMIT_RANGE | sed 's/\.\.\./../'`
    fi
else
    echo -n "Using command line parameter for commit range: "
    RANGE=$1
fi
echo $RANGE

for sha in `git log --format=oneline "$RANGE" | cut '-d ' -f1`
do
    echo -e "Checking commit message for SHA: $sha..."
    git rev-list --no-merges --format=%B --max-count=1 $sha|awk '
    NR == 2 && !/^(CAS-|CHECKSTYLE|JAVADOCS|NOJIRA|\[maven-release-plugin\])/ {
        print "Commit message does not comply with commit guidelines."
        print "Message:"
        print $0
        exit 1
    }
    '
    EXITCODE=$?
    if [ $EXITCODE -ne 0 ]; then
        echo -e "\nTravis-CI build has failed."
        echo
        echo "Commit message for $sha is not following commit guidelines. Please see:"
        echo "http://jasig.github.io/cas/developer/Contributor-Guidelines.html"
        exit $EXITCODE
    else
        echo "OK."
    fi
done
