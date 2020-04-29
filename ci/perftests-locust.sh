#!/bin/bash
source ./ci/functions.sh

runBuild=false
echo "Reviewing changes that might affect the Gradle build..."
currentChangeSetAffectsBuild
retval=$?
if [ "$retval" == 0 ]
then
    echo "Found changes that require the build to run."
    runBuild=true
else
    echo "Changes do NOT affect the project build."
    runBuild=false
fi

if [ "$runBuild" = false ]; then
    exit 0
fi

echo -e "***********************************************"
echo -e "Build started at `date`"
echo -e "***********************************************"

echo -e "Python version is: `python3 --version`\n"

cd etc/loadtests/locust
echo -e "Current directory contains: \n\n`ls`"

echo -e "Installing virtual environment..."
pip3 install virtualenv

echo -e "Configuring virtual environment for mylocustenv..."
virtualenv mylocustenv

echo -e "Installing requirements..."
pip3 install -r requirements.txt

echo -e "Installing locust..."
pip3 install locustio

echo -e "\nRunning locust...\n"
locust -f cas/casLocust.py --no-web --host=https://casserver.herokuapp.com --hatch-rate 3 --clients 10 --run-time 5s --exit-code-on-error 1

retVal=$?

echo -e "***************************************************************************************"
echo -e "Gradle build finished at `date` with exit code $retVal"
echo -e "***************************************************************************************"

if [ $retVal == 0 ]; then
    echo "Gradle build finished successfully."
    exit 0
else
    echo "Gradle build did NOT finish successfully."
    exit $retVal
fi
