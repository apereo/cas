# MongoDb Device Storage - Multifactor Authentication Trusted Device/Browser

Source: https://apereo.github.io/cas/development/mfa/Multifactor-TrustedDevice-Authentication-Storage-MongoDb.html

User decisions may also be kept inside a MongoDb instance.

Support is provided via the following module:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-trusted-mfa-mongo"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.trusted.mongo.client-uri` (Required; type `String`): The connection uri to the mongodb instance. This typically takes on the form of `mongodb://user:psw@ds135522.somewhere.com:35522/db`. If not specified, will fallback onto other individual settings. If specified, takes over all other settings where applicable.
- `cas.authn.mfa.trusted.mongo.collection` (Required; type `String`): MongoDb database collection name to fetch and/or create.
- `cas.authn.mfa.trusted.mongo.database-name` (Required; type `String`): MongoDb database instance name.
- `cas.authn.mfa.trusted.mongo.host` (Required; type `String`; default `localhost`): MongoDb database host for authentication. Multiple host addresses may be defined, separated by comma. If more than one host is defined, it is assumed that each host contains the port as well, if any. Otherwise the configuration may fallback onto the port defined.
- `cas.authn.mfa.trusted.mongo.password` (Required; type `String`): MongoDb database password for authentication.
- `cas.authn.mfa.trusted.mongo.port` (Required; type `Integer`; default `27017`): MongoDb database port.
- `cas.authn.mfa.trusted.mongo.user-id` (Required; type `String`): MongoDb database user for authentication.
- `cas.authn.mfa.trusted.mongo.authentication-database-name` (Optional; type `String`): Name of the database to use for authentication.
- `cas.authn.mfa.trusted.mongo.drop-collection` (Optional; type `Boolean`; default `false`): Whether collections should be dropped on startup and re-created.
- `cas.authn.mfa.trusted.mongo.pool.idle-time` (Optional)
- `cas.authn.mfa.trusted.mongo.pool.life-time` (Optional)
- `cas.authn.mfa.trusted.mongo.pool.max-size` (Optional)
- `cas.authn.mfa.trusted.mongo.pool.max-wait-time` (Optional)
- `cas.authn.mfa.trusted.mongo.pool.min-size` (Optional; type `Integer`; default `1`): Minimum number of connections to keep around.
- `cas.authn.mfa.trusted.mongo.read-concern` (Optional; type `String`; default `AVAILABLE`): Read concern. Accepted values are: - `LOCAL` - `MAJORITY` - `LINEARIZABLE` - `SNAPSHOT` - `AVAILABLE`
- `cas.authn.mfa.trusted.mongo.read-preference` (Optional; type `String`; default `PRIMARY`): Read preference. Accepted values are: - `PRIMARY` - `SECONDARY` - `SECONDARY_PREFERRED` - `PRIMARY_PREFERRED` - `NEAREST`
- `cas.authn.mfa.trusted.mongo.replica-set` (Optional; type `String`): A replica set in MongoDB is a group of `mongod` processes that maintain the same data set. Replica sets provide redundancy and high availability, and are the basis for all production deployments.
- `cas.authn.mfa.trusted.mongo.retry-writes` (Optional; type `Boolean`; default `false`): Sets whether writes should be retried if they fail due to a network error.
- `cas.authn.mfa.trusted.mongo.ssl-enabled` (Optional; type `Boolean`; default `false`): Whether connections require SSL.
- `cas.authn.mfa.trusted.mongo.timeout` (Optional; type `String`; default `PT5S`): MongoDb database connection timeout.
- `cas.authn.mfa.trusted.mongo.write-concern` (Optional; type `String`; default `ACKNOWLEDGED`): Write concern describes the level of acknowledgement requested from MongoDB for write operations to a standalone mongo db or to replica sets or to sharded clusters. In sharded clusters, mongo db instances will pass the write concern on to the shards.
