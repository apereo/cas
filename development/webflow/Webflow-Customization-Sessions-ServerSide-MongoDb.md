# Server-side Sessions - MongoDb

Source: https://apereo.github.io/cas/development/webflow/Webflow-Customization-Sessions-ServerSide-MongoDb.html

If you don’t wish to use the native container’s strategy for session replication, you can use CAS’s support for Mongo session replication.

This feature is enabled via the following module:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-session-mongo"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `spring.mongodb.additional-hosts` (Third party; type `List<String>`): Additional server hosts. Ignored if 'uri' is set or if 'host' is omitted. Additional hosts will use the default mongo port of 27017. If you want to use a different port you can use the "host:port" syntax.
- `spring.mongodb.authentication-database` (Third party; type `String`): Authentication database name.
- `spring.mongodb.database` (Third party; type `String`): Database name. Overrides database in URI.
- `spring.mongodb.host` (Third party; type `String`): Mongo server host. Ignored if 'uri' is set.
- `spring.mongodb.password` (Third party; type `Character[]`): Login password of the mongo server. Ignored if 'uri' is set.
- `spring.mongodb.port` (Third party; type `Integer`): Mongo server port. Ignored if 'uri' is set.
- `spring.mongodb.protocol` (Third party; type `String`; default `mongodb`): Protocol to be used for the MongoDB connection. Ignored if 'uri' is set.
- `spring.mongodb.replica-set-name` (Third party; type `String`): Required replica set name for the cluster. Ignored if 'uri' is set.
- `spring.mongodb.representation.uuid` (Third party; type `UuidRepresentation`; default `unspecified`): Representation to use when converting a UUID to a BSON binary value.
- `spring.mongodb.ssl.bundle` (Third party; type `String`): SSL bundle name.
- `spring.mongodb.ssl.enabled` (Third party; type `Boolean`): Whether to enable SSL support. Enabled automatically if "bundle" is provided unless specified otherwise.
- `spring.mongodb.uri` (Third party; type `String`; default `mongodb://localhost/test`): Mongo database URI. Overrides host, port, username, and password.
- `spring.mongodb.username` (Third party; type `String`): Login user of the mongo server. Ignored if 'uri' is set.
