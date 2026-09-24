#!/bin/bash

RED="\e[31m"
GREEN="\e[32m"
ENDCOLOR="\e[0m"

function printred() {
  printf "\n🔥 ${RED}$1${ENDCOLOR}\n"
}

function printgreen() {
  printf "\n🍀 ${GREEN}$1${ENDCOLOR}\n"
}

# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

# Using variables to turn off msys2 bash on windows behavior of messing with anything resembling a path
export DOCKER_IMAGE="mcr.microsoft.com/azure-sql-edge:latest"
#export MSYS2_ARG_CONV_EXCL="*"
#export MSYS_NO_PATHCONV=1

printgreen "Running SQL Server docker container..."
docker stop mssql-server || true
# docker run --quiet  --rm -e 'ACCEPT_EULA=Y' -e 'SA_PASSWORD=p@ssw0rd' \
#   --name "mssql-server" --rm -d \
#   -p 1433:1433 mcr.microsoft.com/mssql/server:2022-latest

docker run --quiet  --platform linux/amd64 -d -e "ACCEPT_EULA=1" -e "MSSQL_SA_PASSWORD=p@ssw0rd" \
    -p 1433:1433 --rm --name "mssql-server" ${DOCKER_IMAGE}
docker logs -f mssql-server &> mssql.log &
sleep 20
docker ps | grep "mssql-server"
retVal=$?
if [ $retVal == 0 ]; then
    printgreen "SQL Server docker container is running."

    printgreen "Creating audit database"
    docker exec mssql-server /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P 'p@ssw0rd' -Q 'CREATE DATABASE audit;'

    printgreen "Creating saml database"
    docker exec mssql-server /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P 'p@ssw0rd' -Q 'CREATE DATABASE saml;'

    printgreen "Creating samlidp database"  
    docker exec mssql-server /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P 'p@ssw0rd' -Q 'CREATE DATABASE samlidp;'

    printgreen "Creating services database"
    docker exec mssql-server /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P 'p@ssw0rd' -Q 'CREATE DATABASE services;'

    printgreen "Creating yubikey database"
    docker exec mssql-server /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P 'p@ssw0rd' -Q 'CREATE DATABASE yubikey;'

    printgreen "Creating tickets database"
    docker exec mssql-server /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P 'p@ssw0rd' -Q 'CREATE DATABASE tickets;'

    # SQL Server reads under READ COMMITTED take shared locks, where Postgres and InnoDB use row
    # versioning. Tests that run concurrently against one table therefore deadlock on this vendor and
    # on no other. Row versioning is turned on here rather than worked around in the tests. It needs
    # exclusive access to the database, so it runs before anything connects.
    for database in audit saml samlidp services yubikey tickets; do
        printgreen "Enabling read-committed snapshot isolation for ${database}"
        docker exec mssql-server /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P 'p@ssw0rd' \
            -Q "ALTER DATABASE ${database} SET READ_COMMITTED_SNAPSHOT ON WITH ROLLBACK IMMEDIATE;"
    done

    rm -Rf ./mssql.log
else
    printred "SQL Server docker container failed to start."
    cat mssql.log
    exit $retVal
fi

