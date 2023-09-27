#!/usr/bin/env bash

# register strapi administrator user
DATA='{"email":"admin@example.org","password":"P@ssw0rd","firstname":"Strapi","lastname":"Admin"}'
echo $DATA > .admin.txt
cat .admin.txt
# allow error in case already registered
set +e
curl -X POST -H "Content-Type: application/json" http://localhost:1337/admin/register-admin --data @./.admin.txt
rm .admin.txt
echo "\nRegistration Complete: $?"