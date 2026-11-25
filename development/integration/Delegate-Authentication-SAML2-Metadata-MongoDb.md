# SAML2 Delegated Authentication - MongoDb Service Provider Metadata

Source: https://apereo.github.io/cas/development/integration/Delegate-Authentication-SAML2-Metadata-MongoDb.html

Keep the SAML2 service provider metadata that CAS generates for delegated authentication in MongoDB, so every node shares it.

SAML2 metadata for CAS as the SAML2 service provider may also be managed inside a MongoDb instance. To active this feature, you need to start by including the following module in the overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-mongo-core"
}
```

**Feature Activation**

To activate this feature, the following feature toggle(s) must be turned on:

```properties
CasFeatureModule.DelegatedAuthentication.saml-mongodb.enabled=true
```

To learn more about configuration feature toggles, please [see this page](https://apereo.github.io/cas/development/configuration/Configuration-Feature-Toggles.html).

Finally, please make sure you have specified a collection name in your CAS settings that would ultimately house the generated SAML2 metadata.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.pac4j.saml[0].metadata.service-provider.mongo.client-uri` (Required; type `String`): The connection uri to the mongodb instance. This typically takes on the form of `mongodb://user:psw@ds135522.somewhere.com:35522/db`. If not specified, will fallback onto other individual settings. If specified, takes over all other settings where applicable.
- `cas.authn.pac4j.saml[0].metadata.service-provider.mongo.collection` (Required; type `String`): MongoDb database collection name to fetch and/or create.
- `cas.authn.pac4j.saml[0].metadata.service-provider.mongo.database-name` (Required; type `String`): MongoDb database instance name.
- `cas.authn.pac4j.saml[0].metadata.service-provider.mongo.host` (Required; type `String`): MongoDb database host for authentication. Multiple host addresses may be defined, separated by comma. If more than one host is defined, it is assumed that each host contains the port as well, if any. Otherwise the configuration may fallback onto the port defined.
- `cas.authn.pac4j.saml[0].metadata.service-provider.mongo.password` (Required; type `String`): MongoDb database password for authentication.
- `cas.authn.pac4j.saml[0].metadata.service-provider.mongo.port` (Required; type `int`): MongoDb database port.
- `cas.authn.pac4j.saml[0].metadata.service-provider.mongo.user-id` (Required; type `String`): MongoDb database user for authentication.
- `cas.authn.pac4j.saml[0].metadata.service-provider.mongo.authentication-database-name` (Optional; type `String`): Name of the database to use for authentication.
- `cas.authn.pac4j.saml[0].metadata.service-provider.mongo.drop-collection` (Optional; type `boolean`): Whether collections should be dropped on startup and re-created.
- `cas.authn.pac4j.saml[0].metadata.service-provider.mongo.pool.idle-time` (Optional)
- `cas.authn.pac4j.saml[0].metadata.service-provider.mongo.pool.life-time` (Optional)
- `cas.authn.pac4j.saml[0].metadata.service-provider.mongo.pool.max-size` (Optional)
- `cas.authn.pac4j.saml[0].metadata.service-provider.mongo.pool.max-wait-time` (Optional)
- `cas.authn.pac4j.saml[0].metadata.service-provider.mongo.pool.min-size` (Optional; type `int`; default `1`): Minimum number of connections to keep around.
- `cas.authn.pac4j.saml[0].metadata.service-provider.mongo.read-concern` (Optional; type `String`): Read concern. Accepted values are: - `LOCAL` - `MAJORITY` - `LINEARIZABLE` - `SNAPSHOT` - `AVAILABLE`
- `cas.authn.pac4j.saml[0].metadata.service-provider.mongo.read-preference` (Optional; type `String`): Read preference. Accepted values are: - `PRIMARY` - `SECONDARY` - `SECONDARY_PREFERRED` - `PRIMARY_PREFERRED` - `NEAREST`
- `cas.authn.pac4j.saml[0].metadata.service-provider.mongo.replica-set` (Optional; type `String`): A replica set in MongoDB is a group of `mongod` processes that maintain the same data set. Replica sets provide redundancy and high availability, and are the basis for all production deployments.
- `cas.authn.pac4j.saml[0].metadata.service-provider.mongo.retry-writes` (Optional; type `boolean`): Sets whether writes should be retried if they fail due to a network error.
- `cas.authn.pac4j.saml[0].metadata.service-provider.mongo.ssl-enabled` (Optional; type `boolean`): Whether connections require SSL.
- `cas.authn.pac4j.saml[0].metadata.service-provider.mongo.timeout` (Optional; type `String`): MongoDb database connection timeout.
- `cas.authn.pac4j.saml[0].metadata.service-provider.mongo.write-concern` (Optional; type `String`): Write concern describes the level of acknowledgement requested from MongoDB for write operations to a standalone mongo db or to replica sets or to sharded clusters. In sharded clusters, mongo db instances will pass the write concern on to the shards.
