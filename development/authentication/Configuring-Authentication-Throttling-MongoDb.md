# MongoDb Throttling Authentication Attempts

Source: https://apereo.github.io/cas/development/authentication/Configuring-Authentication-Throttling-MongoDb.html

Queries a MongoDb data source used by the CAS audit facility to prevent successive failed login attempts for a particular username from the same IP address. This component requires and depends on the [CAS auditing functionality](https://apereo.github.io/cas/development/audits/Audits-MongoDb.html) via MongoDb.

Enable the following module in your configuration overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-throttle-mongo"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.audit.mongo.client-uri` (Required; type `String`): The connection uri to the mongodb instance. This typically takes on the form of `mongodb://user:psw@ds135522.somewhere.com:35522/db`. If not specified, will fallback onto other individual settings. If specified, takes over all other settings where applicable.
- `cas.audit.mongo.collection` (Required; type `String`): MongoDb database collection name to fetch and/or create.
- `cas.audit.mongo.database-name` (Required; type `String`): MongoDb database instance name.
- `cas.audit.mongo.host` (Required; type `String`; default `localhost`): MongoDb database host for authentication. Multiple host addresses may be defined, separated by comma. If more than one host is defined, it is assumed that each host contains the port as well, if any. Otherwise the configuration may fallback onto the port defined.
- `cas.audit.mongo.password` (Required; type `String`): MongoDb database password for authentication.
- `cas.audit.mongo.port` (Required; type `Integer`; default `27017`): MongoDb database port.
- `cas.audit.mongo.user-id` (Required; type `String`): MongoDb database user for authentication.
- `cas.audit.mongo.asynchronous` (Optional; type `Boolean`; default `true`): Execute the recording of audit records in async manner. This setting must almost always be set to true.
- `cas.audit.mongo.authentication-database-name` (Optional; type `String`): Name of the database to use for authentication.
- `cas.audit.mongo.drop-collection` (Optional; type `Boolean`; default `false`): Whether collections should be dropped on startup and re-created.
- `cas.audit.mongo.pool.idle-time` (Optional)
- `cas.audit.mongo.pool.life-time` (Optional)
- `cas.audit.mongo.pool.max-size` (Optional)
- `cas.audit.mongo.pool.max-wait-time` (Optional)
- `cas.audit.mongo.pool.min-size` (Optional; type `Integer`; default `1`): Minimum number of connections to keep around.
- `cas.audit.mongo.read-concern` (Optional; type `String`; default `AVAILABLE`): Read concern. Accepted values are: - `LOCAL` - `MAJORITY` - `LINEARIZABLE` - `SNAPSHOT` - `AVAILABLE`
- `cas.audit.mongo.read-preference` (Optional; type `String`; default `PRIMARY`): Read preference. Accepted values are: - `PRIMARY` - `SECONDARY` - `SECONDARY_PREFERRED` - `PRIMARY_PREFERRED` - `NEAREST`
- `cas.audit.mongo.replica-set` (Optional; type `String`): A replica set in MongoDB is a group of `mongod` processes that maintain the same data set. Replica sets provide redundancy and high availability, and are the basis for all production deployments.
- `cas.audit.mongo.retry-writes` (Optional; type `Boolean`; default `false`): Sets whether writes should be retried if they fail due to a network error.
- `cas.audit.mongo.ssl-enabled` (Optional; type `Boolean`; default `false`): Whether connections require SSL.
- `cas.audit.mongo.timeout` (Optional; type `String`; default `PT5S`): MongoDb database connection timeout.
- `cas.audit.mongo.write-concern` (Optional; type `String`; default `ACKNOWLEDGED`): Write concern describes the level of acknowledgement requested from MongoDB for write operations to a standalone mongo db or to replica sets or to sharded clusters. In sharded clusters, mongo db instances will pass the write concern on to the shards.
