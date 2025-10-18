#!/bin/bash
export DOCKER_IMAGE="personify/personify-scim-server:2.1.2.RELEASE"
echo "Running SCIM docker container..."
docker stop scim-server || true && docker rm scim-server || true
docker run --rm --name="scim-server" -p 9666:8080 -d -e JAVA_TOOL_OPTIONS='-XX:-UseContainerSupport' ${DOCKER_IMAGE}
docker logs -f scim-server &
echo "Waiting for SCIM container to prepare..."
sleep 5
docker ps | grep "scim-server"
retVal=$?
if [ $retVal == 0 ]; then
  echo "SCIM docker container is running."
else
  echo "SCIM docker container failed to start."
  exit $retVal
fi
