#!/bin/bash

waitloop="while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &"
eval $waitloop
echo -e "Installing Groovy...\n"
groovyVersion=3.0.4
wget https://dl.bintray.com/groovy/maven/apache-groovy-binary-${groovyVersion}.zip -O ./groovy.zip
unzip ./groovy.zip -d $PWD/.groovy
export PATH=$PWD/.groovy/groovy-${groovyVersion}/bin:$PATH
groovy --version

echo -e "***********************************************"
echo -e "Build started at `date`"
echo -e "***********************************************"

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

echo -e "Checking for duplicate test configuration in inheritance hierarchy"
groovy ./ci/groovy/CheckRedundantTestConfigurationInheritance.groovy
retVal=$?
if [ $retVal != 0 ]; then
    echo -e "Groovy analysis has found issues with test configuration classes."
    exit $retVal
fi

echo -e "Checking for duplicate configurations in Gradle build files"
groovy ./ci/groovy/CheckDuplicateGradleConfiguration.groovy
retVal=$?
if [ $retVal != 0 ]; then
    echo -e "Groovy analysis has found issues with Gradle configurations."
    exit $retVal
fi

echo -e "Groovy analysis has successfully examined the codebase."
echo -e "***************************************************************************************"
echo -e "Build finished at `date`"
echo -e "***************************************************************************************"
