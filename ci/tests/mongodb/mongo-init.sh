mongosh <<EOF

use sessions
db.getName()
db.createUser({ user: 'root', pwd: 'secret', roles: [ { role: 'clusterAdmin', db: 'admin' }, { role: 'readAnyDatabase', db: 'admin' }, 'readWrite']})

EOF
