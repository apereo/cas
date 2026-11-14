# MongoDb Ticket Registry

Source: https://apereo.github.io/cas/development/ticketing/MongoDb-Ticket-Registry.html

MongoDb ticket registry integration is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-mongo-ticket-registry"
}
```

This registry stores tickets in one or more [MongoDb](https://www.mongodb.com/) instances. Tickets are auto-converted and wrapped into document objects as JSON. Special indices are created to let MongoDb handle the expiration of each document and cleanup tasks. Note that CAS generally tries to create the relevant collections automatically to manage different ticket types.

Each ticket is stored with its identifier as the document `_id`, so tickets are found through the index MongoDb maintains for every collection. Earlier versions stored the identifier in a separate `ticketId` field with its own `IDX_ID` index. At startup, CAS converts such documents in place and, when index updates are enabled, removes the `IDX_ID` index. The conversion only touches documents in the earlier shape, can safely run again if interrupted, and needs no downtime beyond the upgrade itself; all CAS nodes sharing the registry should be upgraded together, since a node running an earlier version cannot read converted tickets. If an index CAS replaces cannot be rebuilt, CAS logs a warning and puts the previous index back. Principal attributes are stored and indexed for ticket-granting tickets only, and the service index exists only on collections of tickets linked to a service; CAS removes those two indexes from other collections when it updates indexes.

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.ticket.registry.mongo.client-uri` (Required; type `String`): The connection uri to the mongodb instance. This typically takes on the form of `mongodb://user:psw@ds135522.somewhere.com:35522/db`. If not specified, will fallback onto other individual settings. If specified, takes over all other settings where applicable.
- `cas.ticket.registry.mongo.crypto.encryption.key` (Required; type `String`): The encryption key. The encryption key by default and unless specified otherwise must be randomly-generated string whose length is defined by the encryption key size setting.
- `cas.ticket.registry.mongo.crypto.signing.key` (Required; type `String`): The signing key is a string whose length is defined by the signing key size setting.
- `cas.ticket.registry.mongo.database-name` (Required; type `String`): MongoDb database instance name.
- `cas.ticket.registry.mongo.host` (Required; type `String`; default `localhost`): MongoDb database host for authentication. Multiple host addresses may be defined, separated by comma. If more than one host is defined, it is assumed that each host contains the port as well, if any. Otherwise the configuration may fallback onto the port defined.
- `cas.ticket.registry.mongo.password` (Required; type `String`): MongoDb database password for authentication.
- `cas.ticket.registry.mongo.port` (Required; type `Integer`; default `27017`): MongoDb database port.
- `cas.ticket.registry.mongo.user-id` (Required; type `String`): MongoDb database user for authentication.
- `cas.ticket.registry.mongo.authentication-database-name` (Optional; type `String`): Name of the database to use for authentication.
- `cas.ticket.registry.mongo.crypto.alg` (Optional; type `String`; default `AES`): The signing/encryption algorithm to use.
- `cas.ticket.registry.mongo.crypto.enabled` (Optional; type `Boolean`; default `true`): Whether crypto operations are enabled.
- `cas.ticket.registry.mongo.crypto.encryption.key-size` (Optional; type `Integer`; default `16`): Encryption key size.
- `cas.ticket.registry.mongo.crypto.signing-enabled` (Optional; type `Boolean`; default `true`): Whether signing encryption operations are enabled.
- `cas.ticket.registry.mongo.crypto.signing.key-size` (Optional; type `Integer`; default `512`): The signing key size.
- `cas.ticket.registry.mongo.drop-collection` (Optional; type `Boolean`; default `false`): Whether collections should be dropped on startup and re-created.
- `cas.ticket.registry.mongo.drop-indexes` (Optional; type `Boolean`; default `false`): When updating/creating indexes, decide if existing indexes should all be dropped once prior to creating/updating indexes. This may be useful to avoid conflicts between old and new indexes, in scenarios where CAS may be unable to locate the proper difference in index options or names during upgrades..
- `cas.ticket.registry.mongo.indexes` (Optional; type `List<String>`): Index names to create. By default, all indexes are created. Supported indexes are: - `IDX_ID`: no longer created; ticket identifiers are stored as the document id, and an existing index by this name is removed when indexes are updated. - `IDX_PRINCIPAL`: index created for principal attached to the ticket. - `IDX_EXPIRATION`: index created for ticket expiration date. - `IDX_SERVICE`: index created for service attached to the ticket, only on collections that hold tickets linked to a service. - `IDX_ATTRIBUTES`: wildcard index created for ticket attributes, only on the ticket-granting ticket collection, the only one that stores attributes. When indexes are updated, `IDX_SERVICE` and `IDX_ATTRIBUTES` are removed from collections where they do not apply.
- `cas.ticket.registry.mongo.pool.idle-time` (Optional)
- `cas.ticket.registry.mongo.pool.life-time` (Optional)
- `cas.ticket.registry.mongo.pool.max-size` (Optional)
- `cas.ticket.registry.mongo.pool.max-wait-time` (Optional)
- `cas.ticket.registry.mongo.pool.min-size` (Optional; type `Integer`; default `1`): Minimum number of connections to keep around.
- `cas.ticket.registry.mongo.read-concern` (Optional; type `String`; default `AVAILABLE`): Read concern. Accepted values are: - `LOCAL` - `MAJORITY` - `LINEARIZABLE` - `SNAPSHOT` - `AVAILABLE`
- `cas.ticket.registry.mongo.read-preference` (Optional; type `String`; default `PRIMARY`): Read preference. Accepted values are: - `PRIMARY` - `SECONDARY` - `SECONDARY_PREFERRED` - `PRIMARY_PREFERRED` - `NEAREST`
- `cas.ticket.registry.mongo.replica-set` (Optional; type `String`): A replica set in MongoDB is a group of `mongod` processes that maintain the same data set. Replica sets provide redundancy and high availability, and are the basis for all production deployments.
- `cas.ticket.registry.mongo.retry-writes` (Optional; type `Boolean`; default `false`): Sets whether writes should be retried if they fail due to a network error.
- `cas.ticket.registry.mongo.ssl-enabled` (Optional; type `Boolean`; default `false`): Whether connections require SSL.
- `cas.ticket.registry.mongo.timeout` (Optional; type `String`; default `PT5S`): MongoDb database connection timeout.
- `cas.ticket.registry.mongo.update-indexes` (Optional; type `Boolean`; default `true`): Whether CAS should attempt to create/update indexes automatically and figure out the differences between existing keys and new keys.
- `cas.ticket.registry.mongo.write-concern` (Optional; type `String`; default `ACKNOWLEDGED`): Write concern describes the level of acknowledgement requested from MongoDB for write operations to a standalone mongo db or to replica sets or to sharded clusters. In sharded clusters, mongo db instances will pass the write concern on to the shards.

**Signing & encryption**

This CAS feature is able to accept signing and encryption crypto keys. In most scenarios if keys are not provided, CAS will auto-generate them. The following instructions apply if you wish to manually and beforehand create the signing and encryption keys.

Note that if you are asked to create a [JWK](https://tools.ietf.org/html/rfc7517) of a certain size for the key, you are to use the following set of commands to generate the token:

```bash
wget https://raw.githubusercontent.com/apereo/cas/master/etc/jwk-gen.jar
java -jar jwk-gen.jar -t oct -s [size]
```

The outcome would be similar to:

```json
{
  "kty": "oct",
  "kid": "...",
  "k": "..."
}
```

The generated value for `k` needs to be assigned to the relevant CAS settings. Note that keys generated via the above algorithm are processed by CAS using the Advanced Encryption Standard (`AES`) algorithm which is a specification for the encryption of electronic data established by the U.S. National Institute of Standards and Technology.

---

## Troubleshooting

To enable additional logging, configure the log4j configuration file to add the following levels:

```xml
...
<Logger name="com.mongo" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
...
```
