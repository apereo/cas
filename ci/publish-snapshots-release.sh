#!/bin/bash
source ./ci/functions.sh

runBuild=false
echo "Reviewing changes that might affect the Gradle build..."

casVersion=$(./gradlew casVersion -q)
echo "Current CAS version is $casVersion"

prepCommand="echo 'Running command...'; "
gradle="./gradlew $@"
gradleBuild=""
gradleBuildOptions="--stacktrace --build-cache --configure-on-demand --no-daemon "

echo -e "***********************************************"
echo -e "Gradle build started at `date`"
echo -e "***********************************************"

publishSnapshot=true
if [[ "$casVersion" == *"-SNAPSHOT" ]]; then
    currentChangeSetAffectsSnapshots
    retval=$?
    if [ "$retval" == 0 ]
    then
        echo "Found changes that require snapshots to be published."
        runBuild=true
        publishSnapshot=true
    else
        echo "Changes do NOT affect project snapshots."
        runBuild=false
        publishSnapshot=false
    fi
else
    echo "Publishing CAS release for version $casVersion"
    publishSnapshot=false
    runBuild=true

    echo "Fetching keys..."
    openssl aes-256-cbc -k "$GPG_PSW" -in ./ci/gpg-keys.enc -out ./ci/gpg-keys.txt -d
    openssl aes-256-cbc -k "$GPG_PSW" -in ./ci/gpg-ownertrust.enc -out ./ci/gpg-ownertrust.txt -d
    echo "Loading keys..."
    cat ./ci/gpg-keys.txt | base64 --decode | gpg --import
    cat ./ci/gpg-ownertrust.txt | base64 --decode | gpg --import-ownertrust
    rm -Rf ./ci/gpg-keys.txt ./ci/gpg-ownertrust.txt
fi

if [ "$runBuild" = false ]; then
    echo -e "Gradle build will not run under Travis job ${TRAVIS_JOB_NUMBER}"
    exit 0
fi

if [ "$publishSnapshot" = true ]; then
    echo -e "The build will deploy SNAPSHOT artifacts to Sonatype under Travis job ${TRAVIS_JOB_NUMBER}"
    gradleBuild="$gradleBuild assemble uploadArchives -x test -x javadoc -x check \
            -DskipNpmLint=true \
            -DpublishSnapshots=true -DsonatypeUsername=${SONATYPE_USER} \
            -DsonatypePassword=${SONATYPE_PWD} --parallel "
else
    echo -e "The build will deploy RELEASE artifacts to Sonatype under Travis job ${TRAVIS_JOB_NUMBER}"
    gradleBuild="$gradleBuild assemble uploadArchives -x test -x javadoc -x check \
                -DskipNpmLint=true -Dorg.gradle.project.signing.password=${GPG_PASSPHRASE}\
                -Dorg.gradle.project.signing.secretKeyRingFile=/home/travis/.gnupg/secring.gpg \
                -Dorg.gradle.project.signing.keyId=6A2EF9AA \
                -DpublishReleases=true -DsonatypeUsername=${SONATYPE_USER} \
                -DsonatypePassword=${SONATYPE_PWD} "
fi

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

    echo -e "Installing NPM...\n"
    ./gradlew npmInstall --stacktrace -q

    tasks="$gradle $gradleBuildOptions $gradleBuild"
    echo -e "***************************************************************************************"
    echo $prepCommand
    echo $tasks
    echo -e "***************************************************************************************"

    waitloop="while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &"
    eval $waitloop
    waitRetVal=$?

    eval $prepCommand
    eval $tasks
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
fi
