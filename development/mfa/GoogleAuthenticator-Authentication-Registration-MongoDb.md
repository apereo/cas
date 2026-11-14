# MongoDb Google Authenticator Registration

Source: https://apereo.github.io/cas/development/mfa/GoogleAuthenticator-Authentication-Registration-MongoDb.html

Registration records and tokens may be kept inside a MongoDb instance, via the following module:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-gauth-mongo"
}
```

Usernames are matched ignoring case but not accents. CAS creates an index named `username_collated` on the `username` field with the same collation at startup, so lookups by user do not scan the collection. If CAS cannot create it, for example for lack of privileges, it logs a warning and lookups still work, without the index.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.gauth.mongo.client-uri` (Required; type `String`): The connection uri to the mongodb instance. This typically takes on the form of `mongodb://user:psw@ds135522.somewhere.com:35522/db`. If not specified, will fallback onto other individual settings. If specified, takes over all other settings where applicable.
- `cas.authn.mfa.gauth.mongo.collection` (Required; type `String`): MongoDb database collection name to fetch and/or create.
- `cas.authn.mfa.gauth.mongo.database-name` (Required; type `String`): MongoDb database instance name.
- `cas.authn.mfa.gauth.mongo.host` (Required; type `String`; default `localhost`): MongoDb database host for authentication. Multiple host addresses may be defined, separated by comma. If more than one host is defined, it is assumed that each host contains the port as well, if any. Otherwise the configuration may fallback onto the port defined.
- `cas.authn.mfa.gauth.mongo.password` (Required; type `String`): MongoDb database password for authentication.
- `cas.authn.mfa.gauth.mongo.port` (Required; type `Integer`; default `27017`): MongoDb database port.
- `cas.authn.mfa.gauth.mongo.token-collection` (Required; type `String`): Collection name where tokens are kept to prevent replay attacks.
- `cas.authn.mfa.gauth.mongo.user-id` (Required; type `String`): MongoDb database user for authentication.
- `cas.authn.mfa.gauth.mongo.authentication-database-name` (Optional; type `String`): Name of the database to use for authentication.
- `cas.authn.mfa.gauth.mongo.drop-collection` (Optional; type `Boolean`; default `false`): Whether collections should be dropped on startup and re-created.
- `cas.authn.mfa.gauth.mongo.pool.idle-time` (Optional)
- `cas.authn.mfa.gauth.mongo.pool.life-time` (Optional)
- `cas.authn.mfa.gauth.mongo.pool.max-size` (Optional)
- `cas.authn.mfa.gauth.mongo.pool.max-wait-time` (Optional)
- `cas.authn.mfa.gauth.mongo.pool.min-size` (Optional; type `Integer`; default `1`): Minimum number of connections to keep around.
- `cas.authn.mfa.gauth.mongo.read-concern` (Optional; type `String`; default `AVAILABLE`): Read concern. Accepted values are: - `LOCAL` - `MAJORITY` - `LINEARIZABLE` - `SNAPSHOT` - `AVAILABLE`
- `cas.authn.mfa.gauth.mongo.read-preference` (Optional; type `String`; default `PRIMARY`): Read preference. Accepted values are: - `PRIMARY` - `SECONDARY` - `SECONDARY_PREFERRED` - `PRIMARY_PREFERRED` - `NEAREST`
- `cas.authn.mfa.gauth.mongo.replica-set` (Optional; type `String`): A replica set in MongoDB is a group of `mongod` processes that maintain the same data set. Replica sets provide redundancy and high availability, and are the basis for all production deployments.
- `cas.authn.mfa.gauth.mongo.retry-writes` (Optional; type `Boolean`; default `false`): Sets whether writes should be retried if they fail due to a network error.
- `cas.authn.mfa.gauth.mongo.ssl-enabled` (Optional; type `Boolean`; default `false`): Whether connections require SSL.
- `cas.authn.mfa.gauth.mongo.timeout` (Optional; type `String`; default `PT5S`): MongoDb database connection timeout.
- `cas.authn.mfa.gauth.mongo.write-concern` (Optional; type `String`; default `ACKNOWLEDGED`): Write concern describes the level of acknowledgement requested from MongoDB for write operations to a standalone mongo db or to replica sets or to sharded clusters. In sharded clusters, mongo db instances will pass the write concern on to the shards.
