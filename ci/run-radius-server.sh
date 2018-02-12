#!/bin/bash

while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Pulling FreeRadius docker image..."
docker pull tpdock/freeradius:2.2.9

echo "Running FreeRadius docker image..."
docker exec -it --name-="radius-server" tpdock/freeradius:2.2.9 mysql -ufreeradius -pfreeradius -hmysql -e "source /etc/freeradius/sql/mysql/schema.sql" tpdock/freeradius:2.2.9
docker exec -it radius-server mysql -ufreeradius -pfreeradius -hmysql -e "insert into radcheck (username, attribute, op, value) values ('casuser', 'Mellon', ':=', 'casuser');" freeradius

docker ps | grep "radius-server"
retVal=$?
if [ $retVal == 0 ]; then
    echo "FreeRadius docker image is running."
else
    echo "FreeRadius docker image failed to start."
    exit $retVal
fi

