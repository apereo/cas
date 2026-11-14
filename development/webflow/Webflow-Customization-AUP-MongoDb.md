# MongoDb Acceptable Usage Policy

Source: https://apereo.github.io/cas/development/webflow/Webflow-Customization-AUP-MongoDb.html

CAS can be configured to use a MongoDb instance as the storage mechanism. Upon accepting the policy, the decision is kept in the configured collection, in a document with a `username` field and a field that matches the AUP attribute name defined; the document is created if the user does not have one yet. The decision is read back at later logins when the principal does not carry the AUP attribute itself.

Support is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-aup-mongo"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.acceptable-usage-policy.mongo.client-uri` (Required; type `String`): The connection uri to the mongodb instance. This typically takes on the form of `mongodb://user:psw@ds135522.somewhere.com:35522/db`. If not specified, will fallback onto other individual settings. If specified, takes over all other settings where applicable.
- `cas.acceptable-usage-policy.mongo.collection` (Required; type `String`): MongoDb database collection name to fetch and/or create.
- `cas.acceptable-usage-policy.mongo.database-name` (Required; type `String`): MongoDb database instance name.
- `cas.acceptable-usage-policy.mongo.host` (Required; type `String`; default `localhost`): MongoDb database host for authentication. Multiple host addresses may be defined, separated by comma. If more than one host is defined, it is assumed that each host contains the port as well, if any. Otherwise the configuration may fallback onto the port defined.
- `cas.acceptable-usage-policy.mongo.password` (Required; type `String`): MongoDb database password for authentication.
- `cas.acceptable-usage-policy.mongo.port` (Required; type `Integer`; default `27017`): MongoDb database port.
- `cas.acceptable-usage-policy.mongo.user-id` (Required; type `String`): MongoDb database user for authentication.
- `cas.acceptable-usage-policy.mongo.authentication-database-name` (Optional; type `String`): Name of the database to use for authentication.
- `cas.acceptable-usage-policy.mongo.drop-collection` (Optional; type `Boolean`; default `false`): Whether collections should be dropped on startup and re-created.
- `cas.acceptable-usage-policy.mongo.pool.idle-time` (Optional)
- `cas.acceptable-usage-policy.mongo.pool.life-time` (Optional)
- `cas.acceptable-usage-policy.mongo.pool.max-size` (Optional)
- `cas.acceptable-usage-policy.mongo.pool.max-wait-time` (Optional)
- `cas.acceptable-usage-policy.mongo.pool.min-size` (Optional; type `Integer`; default `1`): Minimum number of connections to keep around.
- `cas.acceptable-usage-policy.mongo.read-concern` (Optional; type `String`; default `AVAILABLE`): Read concern. Accepted values are: - `LOCAL` - `MAJORITY` - `LINEARIZABLE` - `SNAPSHOT` - `AVAILABLE`
- `cas.acceptable-usage-policy.mongo.read-preference` (Optional; type `String`; default `PRIMARY`): Read preference. Accepted values are: - `PRIMARY` - `SECONDARY` - `SECONDARY_PREFERRED` - `PRIMARY_PREFERRED` - `NEAREST`
- `cas.acceptable-usage-policy.mongo.replica-set` (Optional; type `String`): A replica set in MongoDB is a group of `mongod` processes that maintain the same data set. Replica sets provide redundancy and high availability, and are the basis for all production deployments.
- `cas.acceptable-usage-policy.mongo.retry-writes` (Optional; type `Boolean`; default `false`): Sets whether writes should be retried if they fail due to a network error.
- `cas.acceptable-usage-policy.mongo.ssl-enabled` (Optional; type `Boolean`; default `false`): Whether connections require SSL.
- `cas.acceptable-usage-policy.mongo.timeout` (Optional; type `String`; default `PT5S`): MongoDb database connection timeout.
- `cas.acceptable-usage-policy.mongo.write-concern` (Optional; type `String`; default `ACKNOWLEDGED`): Write concern describes the level of acknowledgement requested from MongoDB for write operations to a standalone mongo db or to replica sets or to sharded clusters. In sharded clusters, mongo db instances will pass the write concern on to the shards.

# Multitenancy

Configuration settings for MongoDb can be specified in a multitenant environment. Please [review this guide](https://apereo.github.io/cas/development/multitenancy/Multitenancy-Tenant-Properties.html) for more information.
