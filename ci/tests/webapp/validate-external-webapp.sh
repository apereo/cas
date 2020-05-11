#!/bin/bash

gradle="./gradlew "
gradleBuild=""
gradleBuildOptions="--build-cache --configure-on-demand --no-daemon "

tomcatVersion="9.0.35"
tomcatVersionTag="v${tomcatVersion}"
tomcatUrl="https://www-eu.apache.org/dist/tomcat/tomcat-9/${tomcatVersionTag}/bin/apache-tomcat-${tomcatVersion}.zip"

echo -e "***********************************************"
echo -e "Gradle build started at `date` for web application server"
echo -e "***********************************************"

gradleBuild="$gradleBuild :webapp:cas-server-webapp:build -x check -x test -x javadoc -DskipNestedConfigMetadataGen=true "

if [[ "${TRAVIS_COMMIT_MESSAGE}" == *"[show streams]"* ]]; then
    gradleBuild="$gradleBuild -DshowStandardStreams=true "
fi

if [[ "${TRAVIS_COMMIT_MESSAGE}" == *"[rerun tasks]"* ]]; then
    gradleBuild="$gradleBuild --rerun-tasks "
fi

if [[ "${TRAVIS_COMMIT_MESSAGE}" == *"[refresh dependencies]"* ]]; then
    gradleBuild="$gradleBuild --refresh-dependencies "
fi

if [ -z "$gradleBuild" ]; then
    echo "Gradle build will be ignored since no commands are specified to run."
else
    tasks="$gradle $gradleBuildOptions $gradleBuild"
    echo -e "***************************************************************************************"

    echo $tasks
    echo -e "***************************************************************************************"

    eval $tasks
    retVal=$?

    echo -e "***************************************************************************************"
    echo -e "Gradle build finished at `date` with exit code $retVal"
    echo -e "***************************************************************************************"

    if [ $retVal == 0 ]; then
        echo -e "Gradle build finished successfully.\nPreparing CAS web application WAR artifact..."
        export CATALINA_HOME=./apache-tomcat-${tomcatVersion}
        rm -Rf ${CATALINA_HOME}
        wget ${tomcatUrl}
        unzip apache-tomcat-${tomcatVersion}.zip

        mv webapp/cas-server-webapp/build/libs/cas-server-webapp-*.war ${CATALINA_HOME}/webapps/cas.war
        chmod +x ${CATALINA_HOME}/bin/*.sh
        touch ${CATALINA_HOME}/logs/catalina.out ; tail -F ${CATALINA_HOME}/logs/catalina.out &
        ${CATALINA_HOME}/bin/startup.sh & >/dev/null 2>&1
        sleep 30
        rc=`curl -k --connect-timeout 60 -s -o /dev/null -I -w "%{http_code}" http://localhost:8080/cas/login`
        ${CATALINA_HOME}/bin/shutdown.sh & >/dev/null 2>&1
        if [ "$rc" == 200 ]; then
            echo "Deployed the CAS web application successfully."
            exit 0
        else
            echo "Failed to deploy the CAS web application with status $rc."
            exit 1
        fi
    else
        echo "Gradle build did NOT finish successfully."
        exit $retVal
    fi
fi

