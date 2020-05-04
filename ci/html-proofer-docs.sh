#!/bin/bash
source ./ci/functions.sh

runBuild=false
echo "Reviewing changes that might affect the Gradle build..."
currentChangeSetAffectsDocumentation
retval=$?
if [ "$retval" == 0 ]
then
    echo "Found changes that affect project documentation."
    runBuild=true
else
    echo "Changes do NOT affect project documentation."
    runBuild=false
fi

if [ "$runBuild" = false ]; then
    exit 0
fi

HTML_PROOFER_IMAGE=hdeadman/html-proofer:latest
DOCS_FOLDER=$(pwd)/docs/cas-server-documentation
DOCS_OUTPUT=$(pwd)/docs/cas-server-documentation/build/out
HTML_PROOFER_SCRIPT=$(pwd)/ci/html-proofer-docs.rb

echo "Running html-proof image: ${HTML_PROOFER_IMAGE}"
docker run --name="html-proofer" --rm \
    --workdir /root \
    -v ${DOCS_FOLDER}:/root/docs \
    -v ${DOCS_OUTPUT}:/root/out \
    -v ${HTML_PROOFER_SCRIPT}:/root/html-proofer-docs.rb \
    --entrypoint /usr/local/bin/ruby \
     ${HTML_PROOFER_IMAGE} \
     /root/html-proofer-docs.rb
retVal=$?
if [[ ${retVal} -eq 0 ]]; then
    echo "HTML Proofer found no bad links."
    exit 0
else
    echo "HTML Proofer found bad links."
    exit ${retVal}
fi
