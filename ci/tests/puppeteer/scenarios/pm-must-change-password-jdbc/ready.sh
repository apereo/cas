#!/bin/bash

createAcctsTable="create table cas.pm_table_accounts (id int, userid varchar(255), password varchar(255), email varchar(255), phone varchar(255), status boolean)"
createQsTable="create table cas.pm_table_questions (id int, userid varchar(255), question varchar(255), answer varchar(255));"

insertAcct="insert into cas.pm_table_accounts values(0, 'mustchangepswd', 'password', 'casuser@example.org', '3477464534', true)"
insertQ="insert into cas.pm_table_questions values(0, 'mustchangepswd', 'Question?', 'Answer')"

createHistory="create table cas.PasswordHistoryTable (id int, username varchar(255), password varchar(255), recordDate datetime)"

echo "Creating accounts table..."
docker exec mysql-server mysql -u root -ppassword mysql -e "$createAcctsTable"

echo "Creating questions table..."
docker exec mysql-server mysql -u root -ppassword mysql -e "$createQsTable"

echo "Creating history table..."
docker exec mysql-server mysql -u root -ppassword mysql -e "$createHistory"

echo "Inserting account..."
docker exec mysql-server mysql -u root -ppassword mysql -e "$insertAcct"

echo "Inserting security question..."
docker exec mysql-server mysql -u root -ppassword mysql -e "$insertQ"






