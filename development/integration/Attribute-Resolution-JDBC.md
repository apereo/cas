# JDBC Attribute Resolution

Source: https://apereo.github.io/cas/development/integration/Attribute-Resolution-JDBC.html

CAS does allow for attributes to be retrieved from a variety of SQL databases.

Support is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-jdbc"
}
```

To learn how to configure database drivers, [please see this guide](https://apereo.github.io/cas/development/installation/JDBC-Drivers.html).

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.attribute-repository.jdbc[0].driver-class` (Required; type `String`): The JDBC driver used to connect to the database.
- `cas.authn.attribute-repository.jdbc[0].password` (Required; type `String`): The database connection password.
- `cas.authn.attribute-repository.jdbc[0].url` (Required; type `String`): The database connection URL.
- `cas.authn.attribute-repository.jdbc[0].user` (Required; type `String`): The database user. The database user must have sufficient permissions to be able to handle schema changes and updates, when needed.
- `cas.authn.attribute-repository.jdbc` (Optional; type `List<JdbcPrincipalAttributesProperties>`): Retrieve attributes from multiple JDBC repositories.
- `cas.authn.attribute-repository.jdbc[0].attributes` (Optional; type `Map<String, String>`): Map of attributes to fetch from the database. Attributes are defined using a key-value structure where CAS allows the attribute name/key to be renamed virtually to a different attribute. The key is the attribute fetched from the data source and the value is the attribute name CAS should use for virtual renames. Attributes may be allowed to be virtually renamed and remapped. The key in the attribute map is the original attribute, and the value should be the virtually-renamed attribute.
- `cas.authn.attribute-repository.jdbc[0].autocommit` (Optional; type `boolean`): The default auto-commit behavior of connections in the pool. Determined whether queries such as update/insert should be immediately executed without waiting for an underlying transaction.
- `cas.authn.attribute-repository.jdbc[0].batch-size` (Optional; type `int`): A non-zero value enables use of JDBC2 batch updates by Hibernate. e.g. recommended values between 5 and 30.
- `cas.authn.attribute-repository.jdbc[0].case-canonicalization` (Optional; type `String`; default `NONE`): When constructing the final person object from the attribute repository, indicate how the username should be canonicalized. Accepted values are: - `UPPER`: Transform the final person id into uppercase characters. - `LOWER`: Transform the final person id into lowercase characters. - `NONE`: Do nothing.
- `cas.authn.attribute-repository.jdbc[0].case-insensitive-query-attributes` (Optional; type `List<String>`): Collection of attributes, used to build the SQL query, that should go through a case canonicalization process defined as `key->value`. Note that the key is not the name of the attribute, but the query attribute that is used in generating the final query clause (i.e. `username`). The value can be `NONE, LOWER, UPPER`. It's also possible to define a list of attributes without a case canonicalization override such as `username, attribute2` in which case `#caseCanonicalization` will dictate the final outcome.
- `cas.authn.attribute-repository.jdbc[0].column-mappings` (Optional; type `Map<String, String>`): Used only when there is a mapping of many rows to one user. This is done using a key-value structure where the key is the name of the "attribute name" column the value is the name of the "attribute value" column. If the table structure is as such: ``` ----------------------------- uid | attr_name  | attr_value ----------------------------- tom | first_name | Thomas ``` Then a column mapping must be specified to teach CAS to use `attr_name` and `attr_value` for attribute names and values.
- `cas.authn.attribute-repository.jdbc[0].connection-timeout` (Optional; type `String`): Indicates the maximum number of milliseconds that the service can wait to obtain a connection.
- `cas.authn.attribute-repository.jdbc[0].data-source-name` (Optional; type `String`): Attempts to do a JNDI data source look up for the data source name specified. Will attempt to locate the data source object as is.
- `cas.authn.attribute-repository.jdbc[0].ddl-auto` (Optional; type `String`): Hibernate feature to automatically validate and exports DDL to the schema. By default, creates and drops the schema automatically when a session is starts and ends. Setting the value to `validate` or `none` may be more desirable for production, but any of the following options can be used: - `validate`: Validate the schema, but make no changes to the database. - `update`: Update the schema. - `create`: Create the schema, destroying previous data. - `create-drop`: Drop the schema at the end of the session. - `none`: Do nothing. Note that during a version migration where any schema has changed `create-drop` will result in the loss of all data as soon as CAS is started. For transient data like tickets this is probably not an issue, but in cases like the audit table important data could be lost. Using `update`, while safe for data, is confirmed to result in invalid database state. `validate` or `none` settings are likely the only safe options for production use. For more info, [see this](http://docs.spring.io/spring-framework/docs/current/javadoc-api).
- `cas.authn.attribute-repository.jdbc[0].default-catalog` (Optional; type `String`): Qualifies unqualified table names with the given catalog in generated SQL.
- `cas.authn.attribute-repository.jdbc[0].default-schema` (Optional; type `String`): Qualify unqualified table names with the given schema/tablespace in generated SQL.
- `cas.authn.attribute-repository.jdbc[0].dialect` (Optional; type `String`): The database dialect is a configuration setting for platform independent software (JPA, Hibernate, etc) which allows such software to translate its generic SQL statements into vendor specific DDL, DML.
- `cas.authn.attribute-repository.jdbc[0].fail-fast-timeout` (Optional; type `long`): Set the pool initialization failure timeout. - Any value greater than zero will be treated as a timeout for pool initialization. The calling thread will be blocked from continuing until a successful connection to the database, or until the timeout is reached. If the timeout is reached, then a `PoolInitializationException` will be thrown. - A value of zero will *not* prevent the pool from starting in the case that a connection cannot be obtained. However, upon start the pool will attempt to obtain a connection and validate that the `connectionTestQuery` and `connectionInitSql` are valid. If those validations fail, an exception will be thrown. If a connection cannot be obtained, the validation is skipped and the pool will start and continue to try to obtain connections in the background. This can mean that callers to `DataSource#getConnection()` may encounter exceptions. - A value less than zero will *not* bypass any connection attempt and validation during startup, and therefore the pool will start immediately. The pool will continue to try to obtain connections in the background. This can mean that callers to `DataSource#getConnection()` may encounter exceptions. Note that if this timeout value is greater than or equal to zero (0), and therefore an initial connection validation is performed, this timeout does not override the `connectionTimeout` or `validationTimeout`; they will be honored before this timeout is applied. The default value is one millisecond.
- `cas.authn.attribute-repository.jdbc[0].fetch-size` (Optional; type `int`): Used to specify number of rows to be fetched in a select query.
- `cas.authn.attribute-repository.jdbc[0].generate-statistics` (Optional; type `boolean`): Allow hibernate to generate query statistics.
- `cas.authn.attribute-repository.jdbc[0].health-query` (Optional; type `String`): The SQL query to be executed to test the validity of connections. This is for "legacy" databases that do not support the JDBC4 `Connection.isValid()` API.
- `cas.authn.attribute-repository.jdbc[0].id` (Optional; type `String`): A value can be assigned to this field to uniquely identify this resolver.
- `cas.authn.attribute-repository.jdbc[0].idle-timeout` (Optional; type `String`): Controls the maximum amount of time that a connection is allowed to sit idle in the pool.
- `cas.authn.attribute-repository.jdbc[0].isolate-internal-queries` (Optional; type `boolean`): This property determines whether data source isolates internal pool queries, such as the connection alive test, in their own transaction. Since these are typically read-only queries, it is rarely necessary to encapsulate them in their own transaction. This property only applies if `#autocommit` is disabled.
- `cas.authn.attribute-repository.jdbc[0].isolation-level-name` (Optional; type `String`): Defines the isolation level for transactions.
- `cas.authn.attribute-repository.jdbc[0].leak-threshold` (Optional; type `String`): Controls the amount of time that a connection can be out of the pool before a message is logged indicating a possible connection leak.
- `cas.authn.attribute-repository.jdbc[0].order` (Optional; type `int`): The order of this attribute repository in the chain of repositories. Can be used to explicitly position this source in chain and affects merging strategies.
- `cas.authn.attribute-repository.jdbc[0].physical-naming-strategy-class-name` (Optional; type `String`): Fully-qualified name of the class that can control the physical naming strategy of hibernate.
- `cas.authn.attribute-repository.jdbc[0].pool.keep-alive-time` (Optional; type `String`; default `0`): This property controls the keepalive interval for a connection in the pool. An in-use connection will never be tested by the keepalive thread, only when it is idle will it be tested. Default is zero, which disables this feature.
- `cas.authn.attribute-repository.jdbc[0].pool.max-size` (Optional; type `int`; default `18`): Controls the maximum number of connections to keep in the pool, including both idle and in-use connections.
- `cas.authn.attribute-repository.jdbc[0].pool.max-wait` (Optional; type `String`; default `PT2S`): Sets the maximum time in seconds that this data source will wait while attempting to connect to a database. A value of zero specifies that the timeout is the default system timeout if there is one; otherwise, it specifies that there is no timeout.
- `cas.authn.attribute-repository.jdbc[0].pool.maximum-lifetime` (Optional; type `String`; default `PT10M`): This property controls the maximum lifetime of a connection in the pool. When a connection reaches this timeout, even if recently used, it will be retired from the pool. An in-use connection will never be retired, only when it is idle will it be removed.
- `cas.authn.attribute-repository.jdbc[0].pool.min-size` (Optional; type `int`; default `6`): Controls the minimum size that the pool is allowed to reach, including both idle and in-use connections.
- `cas.authn.attribute-repository.jdbc[0].pool.name` (Optional; type `String`): Set the name of the connection pool. This is primarily used for the MBean to uniquely identify the pool configuration.
- `cas.authn.attribute-repository.jdbc[0].pool.suspension` (Optional; type `boolean`): Whether or not pool suspension is allowed. There is a performance impact when pool suspension is enabled. Unless you need it (for a redundancy system for example) do not enable it.
- `cas.authn.attribute-repository.jdbc[0].pool.timeout-millis` (Optional; type `long`; default `1000`): The maximum number of milliseconds that the pool will wait for a connection to be validated as alive.
- `cas.authn.attribute-repository.jdbc[0].propagation-behavior-name` (Optional; type `String`): Defines the propagation behavior for transactions.
- `cas.authn.attribute-repository.jdbc[0].properties` (Optional; type `Map<String,String>`): Additional settings provided by Hibernate (or the connection provider) in form of key-value pairs.
- `cas.authn.attribute-repository.jdbc[0].query-attributes` (Optional; type `Map<String, String>`): Define a `Map` of query attribute names to data-layer attribute names to use when building the query. The key is always the name of the query attribute that is defined by CAS and passed internally, and the value is the database column that should map.
- `cas.authn.attribute-repository.jdbc[0].query-type` (Optional; type `String`; default `AND`): Indicates how multiple attributes in a query should be concatenated together. Accepted values are: * - `AND`: Concatenate attributes in the query using an AND-clause. - `OR`: Concatenate attributes in the query using an OR-clause.
- `cas.authn.attribute-repository.jdbc[0].read-only` (Optional; type `boolean`): Configures the Connections to be added to the pool as read-only Connections.
- `cas.authn.attribute-repository.jdbc[0].require-all-attributes` (Optional; type `boolean`; default `true`): If the SQL should only be run if all attributes listed in the mappings exist in the query.
- `cas.authn.attribute-repository.jdbc[0].single-row` (Optional; type `boolean`; default `true`): Designed to work against a table where there is a mapping of one row to one user. The fields in the table structure is assumed to match `username|name|lastname|address` where there is only a single row per user. Setting this setting to `false` will force CAS to work against a table where there is a mapping of one row to one user. The fields in the table structure is assumed to match `username|attr_name|attr_value` where there is more than one row per username.
- `cas.authn.attribute-repository.jdbc[0].sql` (Optional; type `String`): The SQL statement to execute and fetch attributes. The syntax of the query must be `SELECT * FROM table WHERE {0}`. The `WHERE` clause is dynamically generated by CAS.
- `cas.authn.attribute-repository.jdbc[0].state` (Optional; type `AttributeRepositoryStates`; default `ACTIVE`): Whether attribute resolution based on this source is enabled. Available values are as follows: - `ACTIVE`: Active and enabled repository, and is invoked by default automatically. - `DISABLED`: Attribute repository is disabled and will not be used to resolve people and attributes. - `STANDBY`: Repository is in a semi-enabled state, waiting to be called only on-demand when explicitly asked for and will not be registered into the resolution plan.
- `cas.authn.attribute-repository.jdbc[0].username` (Optional; type `List<String>`): Username attribute(s) to use when running the SQL query.

**Hibernate & JDBC**

Control global properties that are relevant to Hibernate, when CAS attempts to employ and utilize database resources, connections and queries.

- `cas.jdbc.gen-ddl` (Optional; type `Boolean`; default `true`): Whether to generate DDL after the EntityManagerFactory has been initialized creating/updating all relevant tables.
- `cas.jdbc.physical-table-names` (Optional; type `Map<String,String>`): Indicate a physical table name to be used by the hibernate naming strategy in case table names need to be customized for the specific type of database. The key here indicates the CAS-provided table name and the value is the translate physical name for the database. If a match is not found for the CAS-provided table name, then that name will be used by default.
- `cas.jdbc.show-sql` (Optional; type `Boolean`; default `false`): Whether SQL queries should be displayed in the console/logs.

JDBC attribute sources can be defined based on the following mechanics.

## Single Row

Designed to work against a table where there is a mapping of one row to one user. An example of this table format would be:

| uid | first_name | last_name | email |
| --- | --- | --- | --- |
| `jsmith` | `John` | `Smith` | `jsmith@example.org` |

## Multi Row

Designed to work against a table where there is a mapping of one row to one user. An example of this table format would be:

| uid | attr_name | attr_value |
| --- | --- | --- |
| `jsmith` | `first_name` | `John` |
| `jsmith` | `last_name` | `Smith` |
| `jsmith` | `email` | `jsmith@example.org` |

You will need to define column mappings in your configuration to map the `attr_name` column to the `attr_value` column

# Examples

Suppose CAS is configured to authenticate against Active Directory. The account whose details are defined below authenticates via `sAMAccountName`.

| Attribute | Value |
| --- | --- |
| `sAMAccountName` | `johnsmith` |
| `cn` | `John Smith` |

## Example #1

If the resolver is configured to use `sAMAccountName` as the attribute for the principal id, then when authentication is complete the resolver attempts to construct attributes from attribute repository sources, it sees `sAMAccountName` as the attribute and sees the principal id is to be created by `sAMAccountName`. So it would remove the `sAMAccountName` from the attributes. The final result is a principal whose id is `johnsmith` who has a `cn` attribute of `John Smith`.

## Example #2

If the resolver is configured to use `cn` as the attribute for the principal id, then when authentication is complete the resolver attempts to construct attributes from attribute repository sources. It then sees `sAMAccountName` as the attribute and sees the principal id is to be created by `cn`. So it would remove the `cn` from the attributes. The final result is a principal whose id is `John Smith` who has a `sAMAccountName` attribute of `johnsmith`.

# Multitenancy

Configuration settings for SQL attribute resolution can be specified in a multitenant environment. Please [review this guide](https://apereo.github.io/cas/development/multitenancy/Multitenancy-Tenant-Properties.html) for more information.
