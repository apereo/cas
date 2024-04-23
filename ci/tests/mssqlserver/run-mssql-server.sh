#!/bin/bash

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

# Using variables to turn off msys2 bash on windows behavior of messing with anything resembling a path
export MSYS2_ARG_CONV_EXCL="*"
export MSYS_NO_PATHCONV=1

echo "Running SQL Server docker image..."
docker stop mssql-server || true
# docker run --rm -e 'ACCEPT_EULA=Y' -e 'SA_PASSWORD=p@ssw0rd' \
#   --name "mssql-server" --rm -d \
#   -p 1433:1433 mcr.microsoft.com/mssql/server:2022-latest

docker run -e "ACCEPT_EULA=1" -e "MSSQL_SA_PASSWORD=p@ssw0rd" \
    -p 1433:1433 --rm -d --name "mssql-server" mcr.microsoft.com/azure-sql-edge
sleep 45
docker ps | grep "mssql-server"
retVal=$?
if [ $retVal == 0 ]; then
    echo "SQL Server docker container is running."

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

        echo "Creating tickets database"
        docker exec mssql-server /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P 'p@ssw0rd' -Q 'CREATE DATABASE tickets;'
else
    echo "SQL Server docker container failed to start."
    exit $retVal
fi

