#!/bin/bash
set -e
export PGPASSWORD=password

echo "Creating users table and data..."
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
