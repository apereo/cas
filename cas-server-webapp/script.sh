echo "This is a build script that copies the build to a tomcat instance as specified in the variable"
echo "This script will not work if path to the tomcat is not set."
echo "To set the path to the tomcat, do export TOMCAT_HOME=%path_to_tomcat%"

if [ -z != $TOMCAT_HOME ]
then

echo "stopping the tomcat"
$TOMCAT_HOME/bin/catalina.sh stop

echo "copying the file to the /tmp"
cp ./build/libs/cas-server-webapp-4.3.0-SNAPSHOT.war /tmp/auth.war


echo "removing as webapp from tomcat"
rm -rf $TOMCAT_HOME/webapps/auth*

echo "copying the new war to tomcat"
cp /tmp/auth.war $TOMCAT_HOME/webapps/.
echo "done."

echo "starting tomcat in the debug mode"
$TOMCAT_HOME/bin/catalina.sh jpda start

else
echo "TOMCAT_HOME is not set"
exit
fi
