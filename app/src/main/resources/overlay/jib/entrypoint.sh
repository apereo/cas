#!/bin/sh

#echo -e "\nChecking java..."
#java -version

JVM_MEM_OPTS=${JVM_MEM_OPTS:-Xms512m -Xmx2048M}
JVM_EXTRA_OPTS==${JVM_EXTRA_OPTS:-server -noverify -XX:+TieredCompilation -XX:TieredStopAtLevel=1}

#echo -e "\nListing CAS configuration under /etc/cas..."
#ls -R /etc/cas

echo -e "\nRunning CAS..."
exec java $JVM_EXTRA_OPTS $JVM_MEM_OPTS -jar cas.war "$@"
