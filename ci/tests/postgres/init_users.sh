#!/bin/bash
set -e
export PGPASSWORD=password

GREEN="\e[32m"
ENDCOLOR="\e[0m"

function printgreen() {
  printf "🍀 ${GREEN}$1${ENDCOLOR}\n"
}

printgreen "Creating users table and data..."
psql -v ON_ERROR_STOP=1 --username "postgres" --dbname "users" <<-EOSQL
  CREATE TABLE simple_table1 (
     id              SERIAL PRIMARY KEY,
     username        VARCHAR(256) NOT NULL,
     email           VARCHAR(256) NOT NULL,
     role            VARCHAR(256) NOT NULL,
     department      VARCHAR(256) NOT NULL
  );
  INSERT INTO simple_table1 (username, email, role, department)
    VALUES ('caskeycloak@example.org', 'caskeycloak@example.org', 'admin', 'IAM');

EOSQL

printgreen "Creating stored procedures..."
psql -v ON_ERROR_STOP=1 --username "postgres" --dbname "users" <<-EOSQL
  CREATE OR REPLACE FUNCTION sp_authenticate(
      username IN VARCHAR,
      password IN VARCHAR,
      OUT status BOOLEAN,
      OUT color VARCHAR
  )
  AS \$\$
  BEGIN
      IF username = 'casuser' AND password = 'Mellon' THEN
          status := TRUE;
          color := 'blue';
      ELSE
          status := FALSE;
          color := NULL;
      END IF;
  END;
  \$\$ LANGUAGE plpgsql;
EOSQL

printgreen "Creating impersonation table and data..."
psql -v ON_ERROR_STOP=1 --username "postgres" --dbname "impersonation" <<-EOSQL
  DROP TABLE IF EXISTS surrogate_accounts;
  CREATE TABLE surrogate_accounts (
     id              SERIAL PRIMARY KEY,
     username        VARCHAR(256) NOT NULL,
     surrogateAccount       VARCHAR(256) NOT NULL
  );

  INSERT INTO surrogate_accounts (username, surrogateAccount) VALUES ('casuser', 'casimpersonated1');
  INSERT INTO surrogate_accounts (username, surrogateAccount) VALUES ('casuser', 'casimpersonated2');
  INSERT INTO surrogate_accounts (username, surrogateAccount) VALUES ('casuser', 'casimpersonated3');
  
EOSQL
