#!/bin/sh

ENTRYPOINT_DEBUG=${ENTRYPOINT_DEBUG:-false}
JVM_MEM_OPTS=${JVM_MEM_OPTS:--Xms512m -Xmx2048M}
JVM_EXTRA_OPTS=${JVM_EXTRA_OPTS:--server -noverify -XX:+TieredCompilation -XX:TieredStopAtLevel=1}

if [ $ENTRYPOINT_DEBUG == "true" ]; then
  echo -e "\nChecking java..."
  java -version

  if [ -d /etc/cas ] ; then
    echo -e "\nListing CAS configuration under /etc/cas..."
    ls -R /etc/cas
  fi

  echo -e "\nJava args: ${JVM_MEM_OPTS} ${JVM_EXTRA_OPTS}"
fi

echo -e "\nRunning CAS..."
# shellcheck disable=SC2086
exec java $JVM_EXTRA_OPTS $JVM_MEM_OPTS -jar cas.war "$@"
