#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Running SQL Server docker image..."
docker run -e 'ACCEPT_EULA=Y' -e 'SA_PASSWORD=p@ssw0rd' --name "mssql-server" -d -p 1433:1433 microsoft/mssql-server-linux:2017-CU7

docker ps | grep "mssql-server"
retVal=$?
if [ $retVal == 0 ]; then
    echo "SQL Server docker image is running."
else
    echo "SQL Server docker image failed to start."
    exit $retVal
fi
