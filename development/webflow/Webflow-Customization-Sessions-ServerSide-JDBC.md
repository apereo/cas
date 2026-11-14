# Server-side Sessions - JDBC

Source: https://apereo.github.io/cas/development/webflow/Webflow-Customization-Sessions-ServerSide-JDBC.html

If you don’t wish to use the native container’s strategy for session replication, you can use CAS’s support for JDBC session replication.

This feature is enabled via the following module:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-session-jdbc"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `spring.datasource.azure.credential.client-certificate-password` (Third party; type `String`): Password of the certificate file.
- `spring.datasource.azure.credential.client-certificate-path` (Third party; type `String`): Path of a PEM certificate file to use when performing service principal authentication with Azure.
- `spring.datasource.azure.credential.client-id` (Third party; type `String`): Client ID to use when performing service principal authentication with Azure.
- `spring.datasource.azure.credential.client-secret` (Third party; type `String`): Client secret to use when performing service principal authentication with Azure.
- `spring.datasource.azure.credential.managed-identity-enabled` (Third party; type `Boolean`; default `false`): Whether to enable managed identity to authenticate with Azure. If true and the client-id is set, will use the client ID as user assigned managed identity client ID.
- `spring.datasource.azure.credential.password` (Third party; type `String`): Password to use when performing username/password authentication with Azure.
- `spring.datasource.azure.credential.username` (Third party; type `String`): Username to use when performing username/password authentication with Azure.
- `spring.datasource.azure.passwordless-enabled` (Third party; type `Boolean`; default `false`): Whether to enable passwordless connections to Azure databases by using OAuth2 Microsoft Entra token credentials.
- `spring.datasource.azure.profile.cloud-type` (Third party; type `String`): Name of the Azure cloud to connect to. Supported types are: AZURE, AZURE_CHINA, AZURE_US_GOVERNMENT, OTHER. The default value is `AZURE`.
- `spring.datasource.azure.profile.environment.active-directory-endpoint` (Third party; type `String`): The Microsoft Entra endpoint to connect to.
- `spring.datasource.azure.profile.tenant-id` (Third party; type `String`): Tenant ID for Azure resources. The values allowed for 'tenant-id' are: 'common', 'organizations', 'consumers', or the tenant ID.
- `spring.datasource.connection-fetch` (Third party; type `String`; default `eager`): Whether the auto-configured DataSource should be wrapped in a proxy that fetches JDBC connections as late as possible.
- `spring.datasource.continue-on-error` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `spring.sql.init.continue-on-error`)
- `spring.datasource.data` (Third party; type `List<String>`; deprecation `ERROR`, replaced by `spring.sql.init.data-locations`)
- `spring.datasource.data-password` (Third party; type `String`; deprecation `ERROR`, replaced by `spring.sql.init.password`)
- `spring.datasource.data-username` (Third party; type `String`; deprecation `ERROR`, replaced by `spring.sql.init.username`)
- `spring.datasource.dbcp2.abandoned-usage-tracking` (Third party; type `Boolean`)
- `spring.datasource.dbcp2.access-to-underlying-connection-allowed` (Third party; type `Boolean`)
- `spring.datasource.dbcp2.auto-commit-on-return` (Third party; type `Boolean`)
- `spring.datasource.dbcp2.cache-state` (Third party; type `Boolean`)
- `spring.datasource.dbcp2.clear-statement-pool-on-return` (Third party; type `Boolean`)
- `spring.datasource.dbcp2.connection-factory-class-name` (Third party; type `String`)
- `spring.datasource.dbcp2.connection-init-sqls` (Third party; type `List<String>`)
- `spring.datasource.dbcp2.default-auto-commit` (Third party; type `Boolean`)
- `spring.datasource.dbcp2.default-catalog` (Third party; type `String`)
- `spring.datasource.dbcp2.default-query-timeout` (Third party; type `Integer`; deprecation `WARNING`, no replacement)
- `spring.datasource.dbcp2.default-read-only` (Third party; type `Boolean`)
- `spring.datasource.dbcp2.default-schema` (Third party; type `String`)
- `spring.datasource.dbcp2.default-transaction-isolation` (Third party; type `Integer`)
- `spring.datasource.dbcp2.disconnection-ignore-sql-codes` (Third party; type `Set<String>`)
- `spring.datasource.dbcp2.disconnection-sql-codes` (Third party; type `Set<String>`)
- `spring.datasource.dbcp2.driver-class-name` (Third party; type `String`)
- `spring.datasource.dbcp2.duration-between-eviction-runs` (Third party; type `Duration`)
- `spring.datasource.dbcp2.enable-auto-commit-on-return` (Third party; type `Boolean`; deprecation `WARNING`, no replacement)
- `spring.datasource.dbcp2.eviction-policy-class-name` (Third party; type `String`)
- `spring.datasource.dbcp2.fast-fail-validation` (Third party; type `Boolean`)
- `spring.datasource.dbcp2.initial-size` (Third party; type `Integer`)
- `spring.datasource.dbcp2.jmx-name` (Third party; type `String`)
- `spring.datasource.dbcp2.lifo` (Third party; type `Boolean`)
- `spring.datasource.dbcp2.log-abandoned` (Third party; type `Boolean`)
- `spring.datasource.dbcp2.log-expired-connections` (Third party; type `Boolean`)
- `spring.datasource.dbcp2.login-timeout` (Third party; type `Integer`)
- `spring.datasource.dbcp2.max-conn-lifetime-millis` (Third party; type `Long`; deprecation `WARNING`, no replacement)
- `spring.datasource.dbcp2.max-idle` (Third party; type `Integer`)
- `spring.datasource.dbcp2.max-open-prepared-statements` (Third party; type `Integer`)
- `spring.datasource.dbcp2.max-total` (Third party; type `Integer`)
- `spring.datasource.dbcp2.max-wait-millis` (Third party; type `Long`; deprecation `WARNING`, no replacement)
- `spring.datasource.dbcp2.min-evictable-idle-time-millis` (Third party; type `Long`; deprecation `WARNING`, no replacement)
- `spring.datasource.dbcp2.min-idle` (Third party; type `Integer`)
- `spring.datasource.dbcp2.num-tests-per-eviction-run` (Third party; type `Integer`)
- `spring.datasource.dbcp2.password` (Third party; type `String`)
- `spring.datasource.dbcp2.pool-prepared-statements` (Third party; type `Boolean`)
- `spring.datasource.dbcp2.remove-abandoned-on-borrow` (Third party; type `Boolean`)
- `spring.datasource.dbcp2.remove-abandoned-on-maintenance` (Third party; type `Boolean`)
- `spring.datasource.dbcp2.remove-abandoned-timeout` (Third party; type `Integer`; deprecation `WARNING`, no replacement)
- `spring.datasource.dbcp2.rollback-on-return` (Third party; type `Boolean`)
- `spring.datasource.dbcp2.soft-min-evictable-idle-time-millis` (Third party; type `Long`; deprecation `WARNING`, no replacement)
- `spring.datasource.dbcp2.test-on-borrow` (Third party; type `Boolean`)
- `spring.datasource.dbcp2.test-on-create` (Third party; type `Boolean`)
- `spring.datasource.dbcp2.test-on-return` (Third party; type `Boolean`)
- `spring.datasource.dbcp2.test-while-idle` (Third party; type `Boolean`)
- `spring.datasource.dbcp2.time-between-eviction-runs-millis` (Third party; type `Long`; deprecation `WARNING`, no replacement)
- `spring.datasource.dbcp2.url` (Third party; type `String`)
- `spring.datasource.dbcp2.username` (Third party; type `String`)
- `spring.datasource.dbcp2.validation-query` (Third party; type `String`)
- `spring.datasource.dbcp2.validation-query-timeout` (Third party; type `Integer`; deprecation `WARNING`, no replacement)
- `spring.datasource.driver-class-name` (Third party; type `String`): Fully qualified name of the JDBC driver. Auto-detected based on the URL by default.
- `spring.datasource.embedded-database-connection` (Third party; type `EmbeddedDatabaseConnection`): Connection details for an embedded database. Defaults to the most suitable embedded database that is available on the classpath.
- `spring.datasource.generate-unique-name` (Third party; type `Boolean`; default `true`): Whether to generate a random datasource name.
- `spring.datasource.hikari.allow-pool-suspension` (Third party; type `Boolean`)
- `spring.datasource.hikari.auto-commit` (Third party; type `Boolean`)
- `spring.datasource.hikari.catalog` (Third party; type `String`)
- `spring.datasource.hikari.connection-init-sql` (Third party; type `String`)
- `spring.datasource.hikari.connection-test-query` (Third party; type `String`)
- `spring.datasource.hikari.connection-timeout` (Third party; type `Long`)
- `spring.datasource.hikari.credentials-provider-class-name` (Third party; type `String`)
- `spring.datasource.hikari.data-source-class-name` (Third party; type `String`)
- `spring.datasource.hikari.data-source-j-n-d-i` (Third party; type `String`)
- `spring.datasource.hikari.data-source-properties` (Third party; type `Properties`)
- `spring.datasource.hikari.driver-class-name` (Third party; type `String`)
- `spring.datasource.hikari.exception-override-class-name` (Third party; type `String`)
- `spring.datasource.hikari.health-check-properties` (Third party; type `Properties`)
- `spring.datasource.hikari.idle-timeout` (Third party; type `Long`)
- `spring.datasource.hikari.initialization-fail-timeout` (Third party; type `Long`)
- `spring.datasource.hikari.isolate-internal-queries` (Third party; type `Boolean`)
- `spring.datasource.hikari.jdbc-url` (Third party; type `String`)
- `spring.datasource.hikari.keepalive-time` (Third party; type `Long`)
- `spring.datasource.hikari.leak-detection-threshold` (Third party; type `Long`)
- `spring.datasource.hikari.login-timeout` (Third party; type `Integer`)
- `spring.datasource.hikari.max-lifetime` (Third party; type `Long`)
- `spring.datasource.hikari.maximum-pool-size` (Third party; type `Integer`)
- `spring.datasource.hikari.minimum-idle` (Third party; type `Integer`)
- `spring.datasource.hikari.password` (Third party; type `String`)
- `spring.datasource.hikari.pool-name` (Third party; type `String`)
- `spring.datasource.hikari.read-only` (Third party; type `Boolean`)
- `spring.datasource.hikari.register-mbeans` (Third party; type `Boolean`)
- `spring.datasource.hikari.schema` (Third party; type `String`)
- `spring.datasource.hikari.transaction-isolation` (Third party; type `String`)
- `spring.datasource.hikari.username` (Third party; type `String`)
- `spring.datasource.hikari.validation-timeout` (Third party; type `Long`)
- `spring.datasource.initialization-mode` (Third party; type `DatabaseInitializationMode`; deprecation `ERROR`, replaced by `spring.sql.init.mode`)
- `spring.datasource.jmx-enabled` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `spring.datasource.tomcat.jmx-enabled`; default `false`): Whether to enable JMX support (if provided by the underlying pool).
- `spring.datasource.jndi-name` (Third party; type `String`): JNDI location of the datasource. Class, url, username and password are ignored when set.
- `spring.datasource.name` (Third party; type `String`): Datasource name to use if "generate-unique-name" is false. Defaults to "testdb" when using an embedded database, otherwise null.
- `spring.datasource.oracleucp.abandoned-connection-timeout` (Third party; type `Integer`)
- `spring.datasource.oracleucp.background-validation-interval` (Third party; type `Integer`)
- `spring.datasource.oracleucp.commit-on-connection-return` (Third party; type `Boolean`)
- `spring.datasource.oracleucp.connection-affinity-callback` (Third party; type `ConnectionAffinityCallback`)
- `spring.datasource.oracleucp.connection-creation-consumer` (Third party; type `Consumer<ConnectionCreationInformation>`)
- `spring.datasource.oracleucp.connection-factory-class-name` (Third party; type `String`)
- `spring.datasource.oracleucp.connection-factory-properties` (Third party; type `Properties`)
- `spring.datasource.oracleucp.connection-harvest-max-count` (Third party; type `Integer`)
- `spring.datasource.oracleucp.connection-harvest-trigger-count` (Third party; type `Integer`)
- `spring.datasource.oracleucp.connection-initialization-callback` (Third party; type `ConnectionInitializationCallback`)
- `spring.datasource.oracleucp.connection-labeling-callback` (Third party; type `ConnectionLabelingCallback`)
- `spring.datasource.oracleucp.connection-labeling-high-cost` (Third party; type `Integer`)
- `spring.datasource.oracleucp.connection-pool-name` (Third party; type `String`)
- `spring.datasource.oracleucp.connection-properties` (Third party; type `Properties`)
- `spring.datasource.oracleucp.connection-repurpose-threshold` (Third party; type `Integer`)
- `spring.datasource.oracleucp.connection-validation-timeout` (Third party; type `Integer`)
- `spring.datasource.oracleucp.connection-wait-duration` (Third party; type `Duration`)
- `spring.datasource.oracleucp.connection-wait-timeout` (Third party; type `Integer`; deprecation `WARNING`, no replacement)
- `spring.datasource.oracleucp.connection-wait-timeout-while-service-down` (Third party; type `Long`)
- `spring.datasource.oracleucp.create-connection-in-borrow-thread` (Third party; type `Boolean`)
- `spring.datasource.oracleucp.data-source-name` (Third party; type `String`)
- `spring.datasource.oracleucp.database-name` (Third party; type `String`)
- `spring.datasource.oracleucp.description` (Third party; type `String`)
- `spring.datasource.oracleucp.fast-connection-failover-enabled` (Third party; type `Boolean`)
- `spring.datasource.oracleucp.high-cost-connection-reuse-threshold` (Third party; type `Integer`)
- `spring.datasource.oracleucp.inactive-connection-timeout` (Third party; type `Integer`)
- `spring.datasource.oracleucp.initial-pool-size` (Third party; type `Integer`)
- `spring.datasource.oracleucp.login-timeout` (Third party; type `Integer`)
- `spring.datasource.oracleucp.max-connection-reuse-count` (Third party; type `Integer`)
- `spring.datasource.oracleucp.max-connection-reuse-time` (Third party; type `Long`)
- `spring.datasource.oracleucp.max-connections-per-shard` (Third party; type `Integer`)
- `spring.datasource.oracleucp.max-idle` (Third party; type `Integer`)
- `spring.datasource.oracleucp.max-idle-time` (Third party; type `Integer`)
- `spring.datasource.oracleucp.max-pool-size` (Third party; type `Integer`)
- `spring.datasource.oracleucp.max-statements` (Third party; type `Integer`)
- `spring.datasource.oracleucp.min-idle` (Third party; type `Integer`)
- `spring.datasource.oracleucp.min-pool-size` (Third party; type `Integer`)
- `spring.datasource.oracleucp.network-protocol` (Third party; type `String`)
- `spring.datasource.oracleucp.o-n-s-configuration` (Third party; type `String`)
- `spring.datasource.oracleucp.password` (Third party; type `String`)
- `spring.datasource.oracleucp.pdb-roles` (Third party; type `Properties`)
- `spring.datasource.oracleucp.port-number` (Third party; type `Integer`)
- `spring.datasource.oracleucp.property-cycle` (Third party; type `Integer`)
- `spring.datasource.oracleucp.query-timeout` (Third party; type `Integer`)
- `spring.datasource.oracleucp.read-only-instance-allowed` (Third party; type `Boolean`)
- `spring.datasource.oracleucp.s-q-l-for-validate-connection` (Third party; type `String`)
- `spring.datasource.oracleucp.seconds-to-trust-idle-connection` (Third party; type `Integer`)
- `spring.datasource.oracleucp.server-name` (Third party; type `String`)
- `spring.datasource.oracleucp.sharding-mode` (Third party; type `Boolean`)
- `spring.datasource.oracleucp.time-to-live-connection-timeout` (Third party; type `Integer`)
- `spring.datasource.oracleucp.timeout-check-interval` (Third party; type `Integer`)
- `spring.datasource.oracleucp.u-r-l` (Third party; type `String`)
- `spring.datasource.oracleucp.user` (Third party; type `String`)
- `spring.datasource.oracleucp.validate-connection-on-borrow` (Third party; type `Boolean`)
- `spring.datasource.password` (Third party; type `String`): Login password of the database.
- `spring.datasource.platform` (Third party; type `String`; deprecation `ERROR`, replaced by `spring.sql.init.platform`)
- `spring.datasource.schema` (Third party; type `List<String>`; deprecation `ERROR`, replaced by `spring.sql.init.schema-locations`)
- `spring.datasource.schema-password` (Third party; type `String`; deprecation `ERROR`, replaced by `spring.sql.init.password`)
- `spring.datasource.schema-username` (Third party; type `String`; deprecation `ERROR`, replaced by `spring.sql.init.username`)
- `spring.datasource.separator` (Third party; type `String`; deprecation `ERROR`, replaced by `spring.sql.init.separator`)
- `spring.datasource.sql-script-encoding` (Third party; type `Charset`; deprecation `ERROR`, replaced by `spring.sql.init.encoding`)
- `spring.datasource.tomcat.abandon-when-percentage-full` (Third party; type `Integer`)
- `spring.datasource.tomcat.access-to-underlying-connection-allowed` (Third party; type `Boolean`)
- `spring.datasource.tomcat.alternate-username-allowed` (Third party; type `Boolean`)
- `spring.datasource.tomcat.commit-on-return` (Third party; type `Boolean`)
- `spring.datasource.tomcat.connection-properties` (Third party; type `String`)
- `spring.datasource.tomcat.data-source-j-n-d-i` (Third party; type `String`)
- `spring.datasource.tomcat.db-properties` (Third party; type `Properties`)
- `spring.datasource.tomcat.default-auto-commit` (Third party; type `Boolean`)
- `spring.datasource.tomcat.default-catalog` (Third party; type `String`)
- `spring.datasource.tomcat.default-read-only` (Third party; type `Boolean`)
- `spring.datasource.tomcat.default-transaction-isolation` (Third party; type `Integer`)
- `spring.datasource.tomcat.driver-class-name` (Third party; type `String`)
- `spring.datasource.tomcat.fair-queue` (Third party; type `Boolean`)
- `spring.datasource.tomcat.ignore-exception-on-pre-load` (Third party; type `Boolean`)
- `spring.datasource.tomcat.init-s-q-l` (Third party; type `String`)
- `spring.datasource.tomcat.initial-size` (Third party; type `Integer`)
- `spring.datasource.tomcat.jdbc-interceptors` (Third party; type `String`)
- `spring.datasource.tomcat.jmx-enabled` (Third party; type `Boolean`)
- `spring.datasource.tomcat.log-abandoned` (Third party; type `Boolean`)
- `spring.datasource.tomcat.log-validation-errors` (Third party; type `Boolean`)
- `spring.datasource.tomcat.login-timeout` (Third party; type `Integer`)
- `spring.datasource.tomcat.max-active` (Third party; type `Integer`)
- `spring.datasource.tomcat.max-age` (Third party; type `Long`)
- `spring.datasource.tomcat.max-idle` (Third party; type `Integer`)
- `spring.datasource.tomcat.max-wait` (Third party; type `Integer`)
- `spring.datasource.tomcat.min-evictable-idle-time-millis` (Third party; type `Integer`)
- `spring.datasource.tomcat.min-idle` (Third party; type `Integer`)
- `spring.datasource.tomcat.name` (Third party; type `String`)
- `spring.datasource.tomcat.num-tests-per-eviction-run` (Third party; type `Integer`)
- `spring.datasource.tomcat.password` (Third party; type `String`)
- `spring.datasource.tomcat.propagate-interrupt-state` (Third party; type `Boolean`)
- `spring.datasource.tomcat.remove-abandoned` (Third party; type `Boolean`)
- `spring.datasource.tomcat.remove-abandoned-timeout` (Third party; type `Integer`)
- `spring.datasource.tomcat.rollback-on-return` (Third party; type `Boolean`)
- `spring.datasource.tomcat.suspect-timeout` (Third party; type `Integer`)
- `spring.datasource.tomcat.test-on-borrow` (Third party; type `Boolean`)
- `spring.datasource.tomcat.test-on-connect` (Third party; type `Boolean`)
- `spring.datasource.tomcat.test-on-return` (Third party; type `Boolean`)
- `spring.datasource.tomcat.test-while-idle` (Third party; type `Boolean`)
- `spring.datasource.tomcat.time-between-eviction-runs-millis` (Third party; type `Integer`)
- `spring.datasource.tomcat.url` (Third party; type `String`)
- `spring.datasource.tomcat.use-disposable-connection-facade` (Third party; type `Boolean`)
- `spring.datasource.tomcat.use-equals` (Third party; type `Boolean`)
- `spring.datasource.tomcat.use-lock` (Third party; type `Boolean`)
- `spring.datasource.tomcat.use-statement-facade` (Third party; type `Boolean`)
- `spring.datasource.tomcat.username` (Third party; type `String`)
- `spring.datasource.tomcat.validation-interval` (Third party; type `Long`)
- `spring.datasource.tomcat.validation-query` (Third party; type `String`)
- `spring.datasource.tomcat.validation-query-timeout` (Third party; type `Integer`)
- `spring.datasource.tomcat.validator-class-name` (Third party; type `String`)
- `spring.datasource.type` (Third party; type `Class<? extends DataSource>`): Fully qualified name of the DataSource implementation to use. By default, a connection pool implementation is auto-detected from the classpath.
- `spring.datasource.url` (Third party; type `String`): JDBC URL of the database.
- `spring.datasource.username` (Third party; type `String`): Login username of the database.
- `spring.datasource.xa.data-source-class-name` (Third party; type `String`): XA datasource fully qualified name.
- `spring.datasource.xa.properties` (Third party; type `Map<String,String>`): Properties to pass to the XA data source.
- `spring.session.jdbc.cleanup-cron` (Third party; type `String`; default `0 * * * * *`): Cron expression for expired session cleanup job.
- `spring.session.jdbc.continue-on-error` (Third party; type `Boolean`; default `true`): Whether initialization should continue when an error occurs when applying a schema script.
- `spring.session.jdbc.flush-mode` (Third party; type `FlushMode`; default `on-save`): Sessions flush mode. Determines when session changes are written to the session store.
- `spring.session.jdbc.initialize-schema` (Third party; type `DatabaseInitializationMode`; default `embedded`): Database schema initialization mode.
- `spring.session.jdbc.platform` (Third party; type `String`): Platform to use in initialization scripts if the @@platform@@ placeholder is used. Auto-detected by default.
- `spring.session.jdbc.save-mode` (Third party; type `SaveMode`; default `on-set-attribute`): Sessions save mode. Determines how session changes are tracked and saved to the session store.
- `spring.session.jdbc.schema` (Third party; type `String`): Path to the SQL file to use to initialize the database schema.
- `spring.session.jdbc.table-name` (Third party; type `String`; default `SPRING_SESSION`): Name of the database table used to store sessions.

**Hibernate & JDBC**

Control global properties that are relevant to Hibernate, when CAS attempts to employ and utilize database resources, connections and queries.

- `cas.jdbc.gen-ddl` (Optional; type `Boolean`; default `true`): Whether to generate DDL after the EntityManagerFactory has been initialized creating/updating all relevant tables.
- `cas.jdbc.physical-table-names` (Optional; type `Map<String,String>`): Indicate a physical table name to be used by the hibernate naming strategy in case table names need to be customized for the specific type of database. The key here indicates the CAS-provided table name and the value is the translate physical name for the database. If a match is not found for the CAS-provided table name, then that name will be used by default.
- `cas.jdbc.show-sql` (Optional; type `Boolean`; default `false`): Whether SQL queries should be displayed in the console/logs.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.
