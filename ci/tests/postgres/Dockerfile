FROM library/postgres:17

COPY init_cas.sql /docker-entrypoint-initdb.d/
COPY init_users.sh /docker-entrypoint-initdb.d/
