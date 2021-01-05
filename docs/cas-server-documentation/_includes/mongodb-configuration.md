### MongoDb Configuration

The following options related to MongoDb support in CAS apply equally to a number of CAS components (ticket registries, etc):

```properties
# {{ include.configKey }}.mongo.host=localhost
# {{ include.configKey }}.mongo.client-uri=localhost
# {{ include.configKey }}.mongo.port=27017
# {{ include.configKey }}.mongo.drop-collection=false
# {{ include.configKey }}.mongo.socket-keep-alive=false
# {{ include.configKey }}.mongo.password=

# {{ include.configKey }}.mongo.collection=cas-service-registry

# {{ include.configKey }}.mongo.database-name=cas-mongo-database
# {{ include.configKey }}.mongo.timeout=5000
# {{ include.configKey }}.mongo.user-id=
# {{ include.configKey }}.mongo.write-concern=NORMAL
# {{ include.configKey }}.mongo.read-concern=AVAILABLE
# {{ include.configKey }}.mongo.read-preference=PRIMARY
# {{ include.configKey }}.mongo.authentication-database-name=
# {{ include.configKey }}.mongo.replica-set=
# {{ include.configKey }}.mongo.ssl-enabled=false
# {{ include.configKey }}.mongo.retry-writes=false

# {{ include.configKey }}.mongo.pool.life-time=60000
# {{ include.configKey }}.mongo.pool.idle-time=30000
# {{ include.configKey }}.mongo.pool.max-wait-time=60000
# {{ include.configKey }}.mongo.pool.max-size=10
# {{ include.configKey }}.mongo.pool.min-size=1
# {{ include.configKey }}.mongo.pool.per-host=10
```
