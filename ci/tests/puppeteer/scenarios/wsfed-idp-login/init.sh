#!/bin/bash
#SCENARIO_FOLDER=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
#BRANCH=${WSFEDSP_BRANCH:-master}
#GROUP=${WSFEDSP_GROUP:-apereo}
#rm -Rf ${SCENARIO_FOLDER}/wsfed-sp
#echo -e "Installing WSFED SP..."
#curl -L https://github.com/${GROUP}/wsfed-sample-java-webapp/archive/refs/heads/${BRANCH}.zip \
#  -o "${SCENARIO_FOLDER}/${BRANCH}.zip" &> /dev/null \
#  && unzip "${SCENARIO_FOLDER}/${BRANCH}.zip" -d "${SCENARIO_FOLDER}" &> /dev/null \
#  && mv "${SCENARIO_FOLDER}/wsfed-sample-java-webapp-${BRANCH}" "${SCENARIO_FOLDER}/wsfed-sp" \
#  && chmod +x "${SCENARIO_FOLDER}/wsfed-sp/gradlew" \
#  && [ -f ./gradlew ] \
#  && echo "Cloned WSFED SP project." \
#  && rm "${SCENARIO_FOLDER}/${BRANCH}.zip"

echo "Running Fediz sample client web application using keystore ${CAS_KEYSTORE}"
docker run --rm -d -p9876:9876 -p8976:8076 \
  -e SP_SSL_KEYSTORE_PATH="${CAS_KEYSTORE}"\
  --name "fediz" apereo/fediz-client-webapp
docker logs -f fediz &
sleep 15
until curl -k -L --output /dev/null --silent --fail https://localhost:9876/fediz; do
    printf '.'
    sleep 1
done
