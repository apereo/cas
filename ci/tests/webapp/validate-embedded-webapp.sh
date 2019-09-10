#!/bin/bash

prepCommand="echo 'Running command...'; "
gradle="./gradlew "
gradleBuild=""
gradleBuildOptions="--stacktrace --build-cache --configure-on-demand --no-daemon -PchecksumFailOn=build_finish -PchecksumPrint "

webAppServerType="$1"

echo -e "***********************************************"
echo -e "Gradle build started at `date` for web application server ${webAppServerType}"
echo -e "***********************************************"

gradleBuild="$gradleBuild :webapp:cas-server-webapp-${webAppServerType}:build -x check -x test -x javadoc -DskipNestedConfigMetadataGen=true -DskipGradleLint=true "

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
    echo $prepCommand
    echo $tasks
    echo -e "***************************************************************************************"
    
    eval $prepCommand
    eval $tasks
    retVal=$?

    echo -e "***************************************************************************************"
    echo -e "Gradle build finished at `date` with exit code $retVal"
    echo -e "***************************************************************************************"

    if [ $retVal == 0 ]; then
        echo "Gradle build finished successfully."

        echo "Preparing CAS web application WAR artifact..."
        mv webapp/cas-server-webapp-"${webAppServerType}"/build/libs/cas-server-webapp-"${webAppServerType}"-*.war webapp/cas-server-webapp-"${webAppServerType}"/build/libs/cas.war
        echo "Launching CAS web application ${webAppServerType} server..."
        java -jar webapp/cas-server-webapp-"${webAppServerType}"/build/libs/cas.war --server.ssl.enabled=false --server.port=8080 &> /dev/null &
        pid=$!
        echo "Launched CAS with pid ${pid}. Waiting for CAS server to come online..."
        sleep 60
        cmd=`curl --connect-timeout 60 -s -o /dev/null -I -w "%{http_code}" http://localhost:8080/cas/login`
        kill -9 "${pid}"
        echo "CAS server is responding with HTTP status code ${cmd}."
        if [ "$cmd" == 200 ]; then
          echo "CAS server with ${webAppServerType} is successfully up and running."
          exit 0
        else
          echo "CAS server with ${webAppServerType} failed to start successfully."
          exit 1
        fi
    else
        echo "Gradle build did NOT finish successfully."
        exit $retVal
    fi
fi
