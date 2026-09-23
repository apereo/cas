#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &
export DOCKER_IMAGE="mariadb:13.0.2"
echo "Running MariaDb docker container..."
docker stop mariadb || true
# InnoDB's default REPEATABLE READ takes next-key locks, so a delete by a secondary key locks the gaps
# around the rows it matches and blocks inserts of neighbouring keys. Tests that delete by principal while
# others insert therefore deadlock on this engine and on no other. READ COMMITTED drops gap locking, which
# is the isolation most CAS deployments run anyway, and is set on the server rather than worked around in
# the tests.
docker run --quiet  --rm -p 3306:3306 --rm --name mariadb \
  -e MYSQL_ROOT_PASSWORD=mypass -d ${DOCKER_IMAGE} \
  --transaction-isolation=READ-COMMITTED

docker ps | grep "mariadb"
retVal=$?
if [ $retVal == 0 ]; then
    echo "MariaDb docker container is running."
else
    echo "MariaDb docker container failed to start."
    exit $retVal
fi
