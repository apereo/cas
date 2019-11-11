#!/bin/bash
source ./ci/functions.sh

runBuild=false
echo "Reviewing changes that might affect the Gradle build..."
currentChangeSetAffectsBuild
retval=$?
if [ "$retval" == 0 ]
then
    echo "Found changes that require the build to run Groovy checks"
    runBuild=true
else
    echo "Changes do NOT affect project analysis."
    runBuild=false
fi

if [ "$runBuild" = false ]; then
    exit 0
fi

echo -e "Installing Groovy...\n"
groovyVersion=3.0.0-rc-1
wget https://dl.bintray.com/groovy/maven/apache-groovy-binary-${groovyVersion}.zip -O ./groovy.zip
unzip ./groovy.zip -d $PWD/.groovy
export PATH=$PWD/.groovy/groovy-${groovyVersion}/bin:$PATH
groovy --version

echo -e "***********************************************"
echo -e "Build started at `date`"
echo -e "***********************************************"

waitloop="while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &"
eval $waitloop

clear

echo -e "Checking for duplicate test configuration"
groovy ./ci/groovy/CheckDuplicateTestConfiguration.groovy
retVal=$?
if [ $retVal != 0 ]; then
    echo -e "Groovy analysis has found duplicate test configurations."
    exit $retVal
fi

echo -e "Checking for missing utility class annotation"
groovy ./ci/groovy/CheckMissingUtilityClassAnnotation.groovy
retVal=$?
if [ $retVal != 0 ]; then
    echo -e "Groovy analysis has found issues with utility classes."
    exit $retVal
fi

echo -e "Checking tests suite classes"
groovy ./ci/groovy/CheckMissingTestsSuiteClassAnnotation.groovy
retVal=$?
if [ $retVal != 0 ]; then
    echo -e "Groovy analysis has found issues with test suites."
    exit $retVal
fi

echo -e "***************************************************************************************"
echo -e "Build finished at `date`"
echo -e "***************************************************************************************"
