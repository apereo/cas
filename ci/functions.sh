#!/bin/bash

installJdk() {
    echo -e "Installing Java...\n"
    jdkVersion="11.0.7"
    jdkRevision="10"

    jdkRepository="openjdk11-upstream-binaries"
    case "${jdkVersion}" in
    ^15)
        jdkRepository="openjdk15-upstream-binaries"
        shift
        ;;
    ^16)
        jdkRepository="openjdk16-upstream-binaries"
        shift
        ;;
    ^17)
        jdkRepository="openjdk17-upstream-binaries"
        shift
        ;;
    esac

    jdkDownloadUrl="https://github.com/AdoptOpenJDK/${jdkRepository}/releases/download"
    url="${jdkDownloadUrl}/jdk-${jdkVersion}%2B${jdkRevision}/OpenJDK11U-jdk_x64_linux_${jdkVersion}_${jdkRevision}.tar.gz"
    echo "Downloading JDK from ${url}\n"

    wget https://github.com/sormuras/bach/raw/master/install-jdk.sh -O ~/install-jdk.sh && chmod +x ~/install-jdk.sh
    for i in {1..5}; do
        export JAVA_HOME=$(~/install-jdk.sh --emit-java-home --url ${url} -c | tail --lines 1)
        if [[ -d ${JAVA_HOME} ]] ; then
            break;
        fi
        echo -e "Trying download again... [${i}]\n"
        sleep 5
    done
    echo JAVA_HOME=${JAVA_HOME}
    rm ~/install-jdk.sh
}
