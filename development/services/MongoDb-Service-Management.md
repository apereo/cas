# Mongo Service Registry

Source: https://apereo.github.io/cas/development/services/MongoDb-Service-Management.html

This registry uses a [MongoDb](https://www.mongodb.org/) instance to load and persist service definitions. Support is enabled by adding the following module into the overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-mongo-service-registry"
}
```

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.service-registry.mongo.client-uri` (Required; type `String`): The connection uri to the mongodb instance. This typically takes on the form of `mongodb://user:psw@ds135522.somewhere.com:35522/db`. If not specified, will fallback onto other individual settings. If specified, takes over all other settings where applicable.
- `cas.service-registry.mongo.collection` (Required; type `String`): MongoDb database collection name to fetch and/or create.
- `cas.service-registry.mongo.database-name` (Required; type `String`): MongoDb database instance name.
- `cas.service-registry.mongo.host` (Required; type `String`; default `localhost`): MongoDb database host for authentication. Multiple host addresses may be defined, separated by comma. If more than one host is defined, it is assumed that each host contains the port as well, if any. Otherwise the configuration may fallback onto the port defined.
- `cas.service-registry.mongo.password` (Required; type `String`): MongoDb database password for authentication.
- `cas.service-registry.mongo.port` (Required; type `Integer`; default `27017`): MongoDb database port.
- `cas.service-registry.mongo.user-id` (Required; type `String`): MongoDb database user for authentication.
- `cas.service-registry.mongo.authentication-database-name` (Optional; type `String`): Name of the database to use for authentication.
- `cas.service-registry.mongo.change-stream.enabled` (Optional; type `Boolean`; default `true`)
- `cas.service-registry.mongo.change-stream.quiet-period` (Optional; type `String`; default `PT2S`)
- `cas.service-registry.mongo.drop-collection` (Optional; type `Boolean`; default `false`): Whether collections should be dropped on startup and re-created.
- `cas.service-registry.mongo.order` (Optional; type `Integer`)
- `cas.service-registry.mongo.pool.idle-time` (Optional)
- `cas.service-registry.mongo.pool.life-time` (Optional)
- `cas.service-registry.mongo.pool.max-size` (Optional)
- `cas.service-registry.mongo.pool.max-wait-time` (Optional)
- `cas.service-registry.mongo.pool.min-size` (Optional; type `Integer`; default `1`): Minimum number of connections to keep around.
- `cas.service-registry.mongo.read-concern` (Optional; type `String`; default `AVAILABLE`): Read concern. Accepted values are: - `LOCAL` - `MAJORITY` - `LINEARIZABLE` - `SNAPSHOT` - `AVAILABLE`
- `cas.service-registry.mongo.read-preference` (Optional; type `String`; default `PRIMARY`): Read preference. Accepted values are: - `PRIMARY` - `SECONDARY` - `SECONDARY_PREFERRED` - `PRIMARY_PREFERRED` - `NEAREST`
- `cas.service-registry.mongo.replica-set` (Optional; type `String`): A replica set in MongoDB is a group of `mongod` processes that maintain the same data set. Replica sets provide redundancy and high availability, and are the basis for all production deployments.
- `cas.service-registry.mongo.retry-writes` (Optional; type `Boolean`; default `false`): Sets whether writes should be retried if they fail due to a network error.
- `cas.service-registry.mongo.ssl-enabled` (Optional; type `Boolean`; default `false`): Whether connections require SSL.
- `cas.service-registry.mongo.timeout` (Optional; type `String`; default `PT5S`): MongoDb database connection timeout.
- `cas.service-registry.mongo.write-concern` (Optional; type `String`; default `ACKNOWLEDGED`): Write concern describes the level of acknowledgement requested from MongoDB for write operations to a standalone mongo db or to replica sets or to sharded clusters. In sharded clusters, mongo db instances will pass the write concern on to the shards.

## Change Streams

When MongoDb runs as a [replica set](https://www.mongodb.com/docs/manual/replication/), CAS watches the service registry collection through a [change stream](https://www.mongodb.com/docs/manual/changeStreams/) and reloads service definitions shortly after they change, on every CAS node. Changes made by another CAS node, or directly in MongoDb, are picked up in seconds instead of at the next [scheduled reload](https://apereo.github.io/cas/development/services/Service-Management-Caching-Reloading.html).

Change streams are used only when both of the following hold:

- A replica set is defined: the `replica-set` setting is set, or the `client-uri` names one with its `replicaSet` option or uses the `mongodb+srv` scheme.
- The server confirms that it is a member of a replica set. Otherwise, such as for a standalone server or a sharded cluster reached through `mongos`, CAS logs this once and relies on the scheduled reload alone.

Changes that arrive close together, such as a bulk import, result in a single reload once no change has been seen for the configured quiet period; under a steady stream of changes, CAS still reloads at least once every ten quiet periods. A configuration refresh restarts the change stream with the refreshed settings. If the stream is interrupted, CAS resumes it where it left off; when that is no longer possible, it reloads all service definitions and starts a new stream. The database user needs the `changeStream` and `find` privileges on the collection, both of which the built-in `read` role grants. Each CAS node keeps one long-running request open against the replica set, which holds one connection from the pool while it waits.

> **Scheduled Reloads**
>
> Keep the scheduled reload enabled as a safety net, since it also covers changes made while a node could not reach MongoDb. With change streams in place, its interval can be much longer than the default. Disabling the scheduler instead makes CAS consult the registry for every request that matches no cached definition.

## Auto Initialization

Upon startup and configuration permitting, the registry is able to auto initialize itself from default JSON service definitions available to CAS. See [this guide](https://apereo.github.io/cas/development/services/AutoInitialization-Service-Management.html) for more info.
