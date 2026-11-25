# JPA Ticket Registry

Source: https://apereo.github.io/cas/development/ticketing/JPA-Ticket-Registry.html

The JPA Ticket Registry allows CAS to store tickets in a relational database back-end such as MySQL.

> **Usage Warning!**
>
> Using a relational database as the back-end persistence choice for ticket registry state management can be complicated. Unless you are already outfitted with clustered database technology and the resources to manage it, the complexity is usually not worth the trouble.

Support is enabled by adding the following module into the overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-jpa-ticket-registry"
}
```

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.ticket.registry.jpa.crypto.encryption.key` (Required; type `String`): The encryption key. The encryption key by default and unless specified otherwise must be randomly-generated string whose length is defined by the encryption key size setting.
- `cas.ticket.registry.jpa.crypto.signing.key` (Required; type `String`): The signing key is a string whose length is defined by the signing key size setting.
- `cas.ticket.registry.jpa.driver-class` (Required; type `String`; default `org.hsqldb.jdbcDriver`): The JDBC driver used to connect to the database.
- `cas.ticket.registry.jpa.enabled` (Required; type `Boolean`; default `true`): Whether managing tickets via JPA is enabled.
- `cas.ticket.registry.jpa.password` (Required; type `String`): The database connection password.
- `cas.ticket.registry.jpa.url` (Required; type `String`; default `jdbc:hsqldb:mem:cas-hsql-database`): The database connection URL.
- `cas.ticket.registry.jpa.user` (Required; type `String`; default `sa`): The database user. The database user must have sufficient permissions to be able to handle schema changes and updates, when needed.
- `cas.ticket.registry.jpa.autocommit` (Optional; type `Boolean`; default `false`): The default auto-commit behavior of connections in the pool. Determined whether queries such as update/insert should be immediately executed without waiting for an underlying transaction.
- `cas.ticket.registry.jpa.batch-size` (Optional; type `Integer`; default `100`): A non-zero value enables use of JDBC2 batch updates by Hibernate. e.g. recommended values between 5 and 30.
- `cas.ticket.registry.jpa.connection-timeout` (Optional; type `String`; default `PT30S`): Indicates the maximum number of milliseconds that the service can wait to obtain a connection.
- `cas.ticket.registry.jpa.crypto.alg` (Optional; type `String`; default `AES`): The signing/encryption algorithm to use.
- `cas.ticket.registry.jpa.crypto.enabled` (Optional; type `Boolean`; default `true`): Whether crypto operations are enabled.
- `cas.ticket.registry.jpa.crypto.encryption.key-size` (Optional; type `Integer`; default `16`): Encryption key size.
- `cas.ticket.registry.jpa.crypto.signing-enabled` (Optional; type `Boolean`; default `true`): Whether signing encryption operations are enabled.
- `cas.ticket.registry.jpa.crypto.signing.key-size` (Optional; type `Integer`; default `512`): The signing key size.
- `cas.ticket.registry.jpa.data-source-name` (Optional; type `String`): Attempts to do a JNDI data source look up for the data source name specified. Will attempt to locate the data source object as is.
- `cas.ticket.registry.jpa.ddl-auto` (Optional; type `String`; default `update`): Hibernate feature to automatically validate and exports DDL to the schema. By default, creates and drops the schema automatically when a session is starts and ends. Setting the value to `validate` or `none` may be more desirable for production, but any of the following options can be used: - `validate`: Validate the schema, but make no changes to the database. - `update`: Update the schema. - `create`: Create the schema, destroying previous data. - `create-drop`: Drop the schema at the end of the session. - `none`: Do nothing. Note that during a version migration where any schema has changed `create-drop` will result in the loss of all data as soon as CAS is started. For transient data like tickets this is probably not an issue, but in cases like the audit table important data could be lost. Using `update`, while safe for data, is confirmed to result in invalid database state. `validate` or `none` settings are likely the only safe options for production use. For more info, [see this](http://docs.spring.io/spring-framework/docs/current/javadoc-api).
- `cas.ticket.registry.jpa.default-catalog` (Optional; type `String`): Qualifies unqualified table names with the given catalog in generated SQL.
- `cas.ticket.registry.jpa.default-schema` (Optional; type `String`): Qualify unqualified table names with the given schema/tablespace in generated SQL.
- `cas.ticket.registry.jpa.dialect` (Optional; type `String`; default `org.hibernate.dialect.HSQLDialect`): The database dialect is a configuration setting for platform independent software (JPA, Hibernate, etc) which allows such software to translate its generic SQL statements into vendor specific DDL, DML.
- `cas.ticket.registry.jpa.fail-fast-timeout` (Optional; type `Long`; default `1`): Set the pool initialization failure timeout. - Any value greater than zero will be treated as a timeout for pool initialization. The calling thread will be blocked from continuing until a successful connection to the database, or until the timeout is reached. If the timeout is reached, then a `PoolInitializationException` will be thrown. - A value of zero will *not* prevent the pool from starting in the case that a connection cannot be obtained. However, upon start the pool will attempt to obtain a connection and validate that the `connectionTestQuery` and `connectionInitSql` are valid. If those validations fail, an exception will be thrown. If a connection cannot be obtained, the validation is skipped and the pool will start and continue to try to obtain connections in the background. This can mean that callers to `DataSource#getConnection()` may encounter exceptions. - A value less than zero will *not* bypass any connection attempt and validation during startup, and therefore the pool will start immediately. The pool will continue to try to obtain connections in the background. This can mean that callers to `DataSource#getConnection()` may encounter exceptions. Note that if this timeout value is greater than or equal to zero (0), and therefore an initial connection validation is performed, this timeout does not override the `connectionTimeout` or `validationTimeout`; they will be honored before this timeout is applied. The default value is one millisecond.
- `cas.ticket.registry.jpa.fetch-size` (Optional; type `Integer`; default `100`): Used to specify number of rows to be fetched in a select query.
- `cas.ticket.registry.jpa.generate-statistics` (Optional; type `Boolean`; default `false`): Allow hibernate to generate query statistics.
- `cas.ticket.registry.jpa.health-query` (Optional; type `String`): The SQL query to be executed to test the validity of connections. This is for "legacy" databases that do not support the JDBC4 `Connection.isValid()` API.
- `cas.ticket.registry.jpa.idle-timeout` (Optional; type `String`; default `PT10M`): Controls the maximum amount of time that a connection is allowed to sit idle in the pool.
- `cas.ticket.registry.jpa.isolate-internal-queries` (Optional; type `Boolean`; default `false`): This property determines whether data source isolates internal pool queries, such as the connection alive test, in their own transaction. Since these are typically read-only queries, it is rarely necessary to encapsulate them in their own transaction. This property only applies if `#autocommit` is disabled.
- `cas.ticket.registry.jpa.isolation-level-name` (Optional; type `String`; default `ISOLATION_READ_COMMITTED`): Defines the isolation level for transactions. @see org.springframework.transaction.TransactionDefinition
- `cas.ticket.registry.jpa.jpa-locking-timeout` (Optional; type `String`; default `PT1H`): Indicates the lock duration when one is about to be acquired by the cleaner.
- `cas.ticket.registry.jpa.leak-threshold` (Optional; type `String`; default `PT6S`): Controls the amount of time that a connection can be out of the pool before a message is logged indicating a possible connection leak.
- `cas.ticket.registry.jpa.physical-naming-strategy-class-name` (Optional; type `String`; default `org.apereo.cas.hibernate.CasHibernatePhysicalNamingStrategy`): Fully-qualified name of the class that can control the physical naming strategy of hibernate.
- `cas.ticket.registry.jpa.pool.keep-alive-time` (Optional; type `String`; default `0`): This property controls the keepalive interval for a connection in the pool. An in-use connection will never be tested by the keepalive thread, only when it is idle will it be tested. Default is zero, which disables this feature.
- `cas.ticket.registry.jpa.pool.max-size` (Optional; type `Integer`; default `18`): Controls the maximum number of connections to keep in the pool, including both idle and in-use connections.
- `cas.ticket.registry.jpa.pool.max-wait` (Optional; type `String`; default `PT2S`): Sets the maximum time in seconds that this data source will wait while attempting to connect to a database. A value of zero specifies that the timeout is the default system timeout if there is one; otherwise, it specifies that there is no timeout.
- `cas.ticket.registry.jpa.pool.maximum-lifetime` (Optional; type `String`; default `PT10M`): This property controls the maximum lifetime of a connection in the pool. When a connection reaches this timeout, even if recently used, it will be retired from the pool. An in-use connection will never be retired, only when it is idle will it be removed.
- `cas.ticket.registry.jpa.pool.min-size` (Optional; type `Integer`; default `6`): Controls the minimum size that the pool is allowed to reach, including both idle and in-use connections.
- `cas.ticket.registry.jpa.pool.name` (Optional; type `String`): Set the name of the connection pool. This is primarily used for the MBean to uniquely identify the pool configuration.
- `cas.ticket.registry.jpa.pool.suspension` (Optional; type `Boolean`; default `false`): Whether or not pool suspension is allowed. There is a performance impact when pool suspension is enabled. Unless you need it (for a redundancy system for example) do not enable it.
- `cas.ticket.registry.jpa.pool.timeout-millis` (Optional; type `Long`; default `1000`): The maximum number of milliseconds that the pool will wait for a connection to be validated as alive.
- `cas.ticket.registry.jpa.propagation-behavior-name` (Optional; type `String`; default `PROPAGATION_REQUIRED`): Defines the propagation behavior for transactions. @see org.springframework.transaction.TransactionDefinition
- `cas.ticket.registry.jpa.properties` (Optional; type `Map<String,String>`): Additional settings provided by Hibernate (or the connection provider) in form of key-value pairs.
- `cas.ticket.registry.jpa.read-only` (Optional; type `Boolean`; default `false`): Configures the Connections to be added to the pool as read-only Connections.
- `cas.ticket.registry.jpa.ticket-lock-type` (Optional; type `String`; default `NONE`): Ticket locking type. Acceptable values are `READ,WRITE,OPTIMISTIC,OPTIMISTIC_FORCE_INCREMENT,PESSIMISTIC_READ, PESSIMISTIC_WRITE,PESSIMISTIC_FORCE_INCREMENT,NONE`.

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

**Hibernate & JDBC**

Control global properties that are relevant to Hibernate, when CAS attempts to employ and utilize database resources, connections and queries.

- `cas.jdbc.gen-ddl` (Optional; type `Boolean`; default `true`): Whether to generate DDL after the EntityManagerFactory has been initialized creating/updating all relevant tables.
- `cas.jdbc.physical-table-names` (Optional; type `Map<String,String>`): Indicate a physical table name to be used by the hibernate naming strategy in case table names need to be customized for the specific type of database. The key here indicates the CAS-provided table name and the value is the translate physical name for the database. If a match is not found for the CAS-provided table name, then that name will be used by default.
- `cas.jdbc.show-sql` (Optional; type `Boolean`; default `false`): Whether SQL queries should be displayed in the console/logs.

> **Start With A Clean Table**
>
> New deployments of CAS, and upgrades to a new CAS version, should always start with a clean ticket registry table: drop the existing table and let CAS create it. Schema updates add missing tables, columns and indexes, but they do not change the type or length of an existing column, so a table created by an earlier version can keep a schema that the current version does not expect. Tickets are short-lived, and dropping the table only ends the single sign-on sessions it holds.

## Expiration

Each ticket row stores the time the ticket expires unless it is used again: the earlier of its maximum and its idle expiration time, or the time it was saved for a ticket that was already expired, such as one marked expired. The row is rewritten each time the ticket is updated, so the idle expiration follows its last use. The column is indexed, and the [ticket registry cleaner](https://apereo.github.io/cas/development/ticketing/Ticket-Registry-Cleaner.html) reads only the rows whose time has passed, apart from a full scan at startup and hourly. Rows written by earlier versions hold the maximum expiration time; they are caught by those full scans until their tickets are next updated. Schema updates add the index to an existing table; with schema generation turned off, create it by hand:

```sql
CREATE INDEX idx_ticket_expiration ON cas_tickets (expiration_time);
```

## MySQL

To scan large tables without reading every row into memory, CAS sets `useCursorFetch=true` for MySQL, together with `cachePrepStmts=true`, `prepStmtCacheSize=250` and `prepStmtCacheSqlLimit=2048`, unless the additional JPA properties of the ticket registry or the JDBC URL already set them. Connector/J uses a server-side cursor only for statements with a fetch size, so CAS applies the configured fetch size only to queries that stream tickets, such as the cleaner’s scan; single-ticket lookups read their row directly. With `useCursorFetch`, Connector/J prepares every statement on the server, and the prepared statement cache keeps that to one round trip per lookup.

MySQL limits an index key to 3072 bytes, which with the `utf8mb4` character set is 768 characters. On MySQL the ticket table therefore declares `type` as `VARCHAR(255)`, `principal_id` as `VARCHAR(512)` and `parent_id` as `VARCHAR(768)`, so that its indexes on the principal, the parent ticket and the ticket type with the principal can be created; the `service` column keeps its length and is not indexed on MySQL. Tables created by earlier versions declared longer columns, and MySQL refused every one of those indexes, so lookups by principal or parent ticket scanned the whole table. Schema updates do not shorten existing columns: start with a clean table, as recommended above, or apply the change to the existing table by hand:

```sql
ALTER TABLE cas_tickets
    MODIFY type VARCHAR(255) NOT NULL,
    MODIFY principal_id VARCHAR(512),
    MODIFY parent_id VARCHAR(768);
CREATE INDEX idx_ticket_principal ON cas_tickets (principal_id);
CREATE INDEX idx_ticket_parent ON cas_tickets (parent_id);
CREATE INDEX idx_type_principal ON cas_tickets (type, principal_id);
```

The ticket table no longer declares an index on `type` alone, since the index on the ticket type and the principal serves the same queries. Schema updates do not drop indexes either; an existing `idx_ticket_type` can be removed with `DROP INDEX idx_ticket_type ON cas_tickets` (or `DROP INDEX idx_ticket_type` where indexes are not scoped to a table).

## PostgreSQL

On PostgreSQL, ticket attributes are stored as `jsonb`, so that finding single sign-on sessions by attribute, as delegated OpenID Connect and SAML2 logout do, can be answered by a GIN index rather than by reading every session. JPA cannot declare a GIN index, and CAS does not create it. The index is required and must be created manually: the administrator runs the statement below once, after the ticket table has been created. Without it, those lookups still work but read the whole table.

```sql
CREATE INDEX IF NOT EXISTS idx_ticket_attributes ON cas_tickets USING GIN (attributes jsonb_path_ops);
```

The index is part of the table, so it is needed again whenever the table is dropped and recreated, such as when starting with a clean table as recommended above.

Tables created by earlier versions declare the column as `json`, which schema updates do not change, and session lookups by attribute then fail. Start with a clean table, as recommended above, or convert the column first, which rewrites the table and locks it while it runs:

```sql
ALTER TABLE cas_tickets ALTER COLUMN attributes TYPE jsonb USING attributes::jsonb;
```

## Ticket Registry Locking

This ticket registry implementation automatically supports [distributed locking](https://apereo.github.io/cas/development/ticketing/Ticket-Registry-Locking.html). The tables that track locking operations come from [Spring Integration](https://spring.io/projects/spring-integration) JDBC support. Spring Integration only creates them on its own for embedded databases; for any other database, either create them up front from the schema scripts that ship with Spring Integration, or let CAS create them on startup by setting the Spring Integration JDBC schema initialization mode to `always`.

Without these tables, every attempt to lock a ticket fails, and CAS cannot issue service tickets.

The following settings and properties are available from the CAS configuration catalog:

- `spring.integration.jdbc.continue-on-error` (Third party; type `Boolean`; default `true`): Whether initialization should continue when an error occurs when applying a schema script.
- `spring.integration.jdbc.initialize-schema` (Third party; type `DatabaseInitializationMode`; default `embedded`): Database schema initialization mode.
- `spring.integration.jdbc.platform` (Third party; type `String`): Platform to use in initialization scripts if the @@platform@@ placeholder is used. Auto-detected by default.
- `spring.integration.jdbc.schema` (Third party; type `String`): Path to the SQL file to use to initialize the database schema.

**Hibernate & JDBC**

Control global properties that are relevant to Hibernate, when CAS attempts to employ and utilize database resources, connections and queries.

- `cas.jdbc.gen-ddl` (Optional; type `Boolean`; default `true`): Whether to generate DDL after the EntityManagerFactory has been initialized creating/updating all relevant tables.
- `cas.jdbc.physical-table-names` (Optional; type `Map<String,String>`): Indicate a physical table name to be used by the hibernate naming strategy in case table names need to be customized for the specific type of database. The key here indicates the CAS-provided table name and the value is the translate physical name for the database. If a match is not found for the CAS-provided table name, then that name will be used by default.
- `cas.jdbc.show-sql` (Optional; type `Boolean`; default `false`): Whether SQL queries should be displayed in the console/logs.
