FROM library/postgres:16.2

COPY init_cas.sql /docker-entrypoint-initdb.d/
COPY init_users.sh /docker-entrypoint-initdb.d/
