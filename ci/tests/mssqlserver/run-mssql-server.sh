#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Running SQL Server docker image..."
docker run -e 'ACCEPT_EULA=Y' -e 'SA_PASSWORD=p@ssw0rd' --name "mssql-server" -d -p 1433:1433 mcr.microsoft.com/mssql/server:2019-CU4-ubuntu-16.04
sleep 30
docker ps | grep "mssql-server"
retVal=$?
if [ $retVal == 0 ]; then
    echo "SQL Server docker image is running."

    echo "Creating audit database"
    docker exec mssql-server /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P 'p@ssw0rd' -Q 'CREATE DATABASE audit;'

    echo "Creating saml database"
    docker exec mssql-server /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P 'p@ssw0rd' -Q 'CREATE DATABASE saml;'

    echo "Creating samlidp database"
    docker exec mssql-server /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P 'p@ssw0rd' -Q 'CREATE DATABASE samlidp;'

    echo "Creating services database"
    docker exec mssql-server /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P 'p@ssw0rd' -Q 'CREATE DATABASE services;'

    echo "Creating yubikey database"
    docker exec mssql-server /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P 'p@ssw0rd' -Q 'CREATE DATABASE yubikey;'
else
    echo "SQL Server docker image failed to start."
    exit $retVal
fi

