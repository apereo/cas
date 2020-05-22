#!/bin/bash


gradle="./gradlew "
gradleBuild=""
gradleBuildOptions="--build-cache --configure-on-demand --no-daemon --parallel "
webAppServerType="$1"

echo -e "***********************************************"
echo -e "Build started at `date`"
echo -e "***********************************************"
gradleBuild="$gradleBuild :webapp:cas-server-webapp-${webAppServerType}:build -x check -x test -x javadoc -DskipNestedConfigMetadataGen=true "
tasks="$gradle $gradleBuildOptions $gradleBuild"
echo $tasks
echo -e "***************************************************************************************"
eval $tasks
retVal=$?

if [ $retVal == 0 ]; then
    echo -e "Gradle build finished successfully.\nPreparing CAS web application WAR artifact..."
    mv webapp/cas-server-webapp-"${webAppServerType}"/build/libs/cas-server-webapp-"${webAppServerType}"-*.war \
      webapp/cas-server-webapp-"${webAppServerType}"/build/libs/cas.war

    dname="${dname:-CN=cas.example.org,OU=Example,OU=Org,C=US}"
    subjectAltName="${subjectAltName:-dns:example.org,dns:localhost,ip:127.0.0.1}"
    keystore="./thekeystore"
    echo "Generating keystore ${keystore} for CAS with DN=${dname}, SAN=${subjectAltName}"
    [ -f "${keystore}" ] && rm "${keystore}"
    keytool -genkey -noprompt -alias cas -keyalg RSA -keypass changeit -storepass changeit \
      -keystore "${keystore}" -dname "${dname}" -ext SAN="${subjectAltName}"
    echo "Launching CAS web application ${webAppServerType} server..."
    casOutput="/tmp/logs/cas.log"
    cmd="java -jar webapp/cas-server-webapp-${webAppServerType}/build/libs/cas.war \\
      --server.ssl.key-store=${keystore} --cas.service-registry.init-from-json=true --logging.level.org.apereo.cas=info"
#    exec $cmd > ${casOutput} 2>&1 &
    exec $cmd &
    pid=$!
    echo "Launched CAS with pid ${pid}. Waiting for CAS server to come online..."
    sleep 60
    
    cd etc/loadtests/locust
    echo -e "Current directory contains: \n\n`ls`"

    echo -e "Installing virtual environment..."
    pip install virtualenv

    echo -e "Configuring virtual environment for mylocustenv..."
    virtualenv mylocustenv

    echo -e "Installing requirements..."
    pip install -r requirements.txt

    echo -e "Installing locust..."
    pip install locustio

    echo -e "\nRunning locust...\n"
    locust -f cas/casLocust.py --no-web --host=https://localhost:8443 --hatch-rate 3 --clients 5 --run-time 5m --exit-code-on-error 1

    retVal=$?

    echo -e "***************************************************************************************"
    echo -e "Gradle build finished at `date` with exit code $retVal"
    echo -e "***************************************************************************************"

    if [ $retVal == 0 ]; then
        echo "Gradle build finished successfully."
    else
        echo "Gradle build did NOT finish successfully."
    fi

    kill -9 "${pid}"
    [ -f "${keystore}" ] && rm "${keystore}"
    [ -f "${casOutput}" ] && rm "${casOutput}"
    exit $retVal
else
    echo "Gradle build did NOT finish successfully."
    exit $retVal
fi
