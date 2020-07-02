#!/bin/bash

clear
echo -e "***********************************************"
echo -e "Build started at `date`"
echo -e "***********************************************"

echo -e "Checking for duplicate test configuration"
java ./ci/checks/CheckDuplicateTestConfiguration.java .
retVal=$?
if [ $retVal != 0 ]; then
    echo -e "Code analysis has found duplicate test configurations."
    exit $retVal
fi

echo -e "Checking for missing utility class annotation"
java ./ci/java/CheckMissingUtilityClassAnnotation.java .
retVal=$?
if [ $retVal != 0 ]; then
    echo -e "Code analysis has found issues with utility classes."
    exit $retVal
fi

echo -e "Checking tests suite classes"
java ./ci/java/CheckMissingTestsSuiteClassAnnotation.java .
retVal=$?
if [ $retVal != 0 ]; then
    echo -e "Code analysis has found issues with test suites."
    exit $retVal
fi

echo -e "Checking for duplicate test configuration in inheritance hierarchy"
java ./ci/checks/CheckRedundantTestConfigurationInheritance.java .
retVal=$?
if [ $retVal != 0 ]; then
    echo -e "Code analysis has found issues with test configuration classes."
    exit $retVal
fi

echo -e "Checking for duplicate configurations in Gradle build files"
java ./ci/java/CheckDuplicateGradleConfiguration.java .
retVal=$?
if [ $retVal != 0 ]; then
    echo -e "Code analysis has found issues with Gradle configurations."
    exit $retVal
fi

echo -e "Checking for missing class in tests suites"
java ./ci/checks/CheckMissingClassInTestsSuite.java .
retVal=$?
if [ $retVal != 0 ]; then
    echo -e "Code analysis has found issues with test suites."
    exit $retVal
fi

echo -e "Checking for bean proxying in Spring configuration files"
java ./ci/java/CheckSpringConfigurationBeanProxying.java .
retVal=$?
if [ $retVal != 0 ]; then
    echo -e "Code analysis has found issues with bean proxying in Spring configuration files."
    exit $retVal
fi

echo -e "Checking Spring configuration factories"
java ./ci/checks/CheckSpringConfigurationFactories.java .
retVal=$?
if [ $retVal != 0 ]; then
    echo -e "Code analysis has found issues with Spring configuration factories"
    exit $retVal
fi

echo -e "Code analysis has successfully examined the codebase."
echo -e "***************************************************************************************"
echo -e "Build finished at `date`"
echo -e "***************************************************************************************"
