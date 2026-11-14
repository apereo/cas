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

- `spring.data.mongodb.additional-hosts` (Third party; deprecation `ERROR`, replaced by `spring.mongodb.additional-hosts`)
- `spring.data.mongodb.authentication-database` (Third party; deprecation `ERROR`, replaced by `spring.mongodb.authentication-database`)
- `spring.data.mongodb.auto-index-creation` (Third party; type `Boolean`): Whether to enable auto-index creation.
- `spring.data.mongodb.database` (Third party; deprecation `ERROR`, replaced by `spring.mongodb.database`)
- `spring.data.mongodb.field-naming-strategy` (Third party; type `Class<?>`): Fully qualified name of the FieldNamingStrategy to use.
- `spring.data.mongodb.grid-fs-database` (Third party; type `String`; deprecation `ERROR`, replaced by `spring.data.mongodb.gridfs.database`)
- `spring.data.mongodb.gridfs.bucket` (Third party; type `String`): GridFS bucket name.
- `spring.data.mongodb.gridfs.database` (Third party; type `String`): GridFS database name.
- `spring.data.mongodb.host` (Third party; deprecation `ERROR`, replaced by `spring.mongodb.host`)
- `spring.data.mongodb.password` (Third party; deprecation `ERROR`, replaced by `spring.mongodb.password`)
- `spring.data.mongodb.port` (Third party; deprecation `ERROR`, replaced by `spring.mongodb.port`)
- `spring.data.mongodb.protocol` (Third party; deprecation `ERROR`, replaced by `spring.mongodb.protocol`)
- `spring.data.mongodb.replica-set-name` (Third party; deprecation `ERROR`, replaced by `spring.mongodb.replica-set-name`)
- `spring.data.mongodb.repositories.type` (Third party; type `RepositoryType`; default `auto`): Type of Mongo repositories to enable.
- `spring.data.mongodb.representation.big-decimal` (Third party; type `MongoCustomConversions.BigDecimalRepresentation`; default `unspecified`): Representation to use when converting a BigDecimal.
- `spring.data.mongodb.ssl.bundle` (Third party; deprecation `ERROR`, replaced by `spring.mongodb.ssl.bundle`)
- `spring.data.mongodb.ssl.enabled` (Third party; deprecation `ERROR`, replaced by `spring.mongodb.ssl.enabled`)
- `spring.data.mongodb.uri` (Third party; deprecation `ERROR`, replaced by `spring.mongodb.uri`)
- `spring.data.mongodb.username` (Third party; deprecation `ERROR`, replaced by `spring.mongodb.username`)
- `spring.data.mongodb.uuid-representation` (Third party; deprecation `ERROR`, replaced by `spring.mongodb.representation.uuid`)
