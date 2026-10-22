#!/bin/bash
# Runs once for both CAS nodes. An initScript would run again before the second node starts,
# restarting the shared backend under the first node.
"${PWD}/ci/tests/cassandra/run-cassandra-server.sh" || exit 1
"${PWD}/ci/tests/httpbin/run-httpbin-server.sh" || exit 1

echo "Storing the service definition for the cluster check in Cassandra"
definition='{"@class":"org.apereo.cas.services.CasRegisteredService","serviceId":"^https://localhost:9859/anything/cluster.*","name":"Cluster","id":1,"evaluationOrder":1}'
docker exec cassandra cqlsh --ssl -e "INSERT INTO cas.casservices (id, data) VALUES (1, '${definition}');" || exit 1
docker exec cassandra cqlsh --ssl -e "INSERT INTO cas.users_table (id, user_attr, pwd_attr) VALUES (4d6c5c5e-2f0b-4b33-9d5e-3c8b8e0b9a11, 'cassandrauser', 'Cassandr4');" || exit 1
