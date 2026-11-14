# User-Managed Access Protocol

Source: https://apereo.github.io/cas/development/protocol/OAuth-UMA-Protocol.html

User-Managed Access (UMA) is a lightweight access control protocol that defines a centralized workflow to allow an entity (user or corporation) to manage access to their resources.

> **Scopes**
>
> Remember that UMA-related operations that typically may carry special scopes such as `uma_protection` or `uma_authorization` must not only be requested in the initial authorization request, but should also be explicitly authorized in the registered service definition.

The protection and authorization API endpoints below expect the access token in the `Authorization` header, and answer a request without a token, with an unknown or expired token or without the `uma_protection` or `uma_authorization` scope the way [RFC 6750](https://www.rfc-editor.org/rfc/rfc6750#section-3.1) describes, with `401` or `403` and a `WWW-Authenticate` challenge. A [DPoP-bound](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-DPoP.html) access token is accepted only with the `DPoP` scheme and its proof.

## Configuration

Support is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-oauth-uma"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.oauth.uma.core.issuer` (Required; type `String`; default `http://localhost:8080/cas`): UMA issuer.

## Permission Tickets

A permission represents a resource identifier and its corresponding scopes. At a single instance, the resource server can only request permissions for access to the resources belonging to a single resource owner, protected by a single authorization server. As the response, the resource server receives a permission ticket which represents the same permissions that the resource server requested.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.oauth.uma.permission-ticket.max-time-to-live-in-seconds` (Optional; type `String`; default `PT3M`): Hard timeout to kill the UMA permission token and expire it.

## Requesting Party Tokens

The client can then obtain a Requesting Party Token (`RPT`) from the authorization server by presenting the received permission ticket and any other required claims. `RPT` will be issued with successful evaluation of policy conditions, scopes, claims, and any other relevant information.

A `RPT` is an OAuth access token associated with the UMA grant. An `RPT` is unique to a requesting party, client, authorization server, resource server, and resource owner. This token represents some number of granted permissions. The claims of the `RPT` are always signed with the keys of the configured keystore, whose public keys are published by the [requesting party token endpoint](https://apereo.github.io/cas/development/protocol/OAuth-UMA-Protocol.html#requesting-party-token); without that keystore, no `RPT` is issued.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.oauth.uma.requesting-party-token.jwks-file.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.oauth.uma.requesting-party-token.max-time-to-live-in-seconds` (Optional; type `String`; default `PT3M`): Hard timeout to kill the RP token and expire it.

## Resources Storage

Resource definitions are by default kept inside an in-memory repository.

CAS also provides an alternative implementation backed by the relational database of choice to track and manage such definitions. The repository choice is activated in CAS properties.

Support is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-oauth-uma-jpa"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.oauth.uma.resource-set.jpa.driver-class` (Required; type `String`; default `org.hsqldb.jdbcDriver`): The JDBC driver used to connect to the database.
- `cas.authn.oauth.uma.resource-set.jpa.password` (Required; type `String`): The database connection password.
- `cas.authn.oauth.uma.resource-set.jpa.url` (Required; type `String`; default `jdbc:hsqldb:mem:cas-hsql-database`): The database connection URL.
- `cas.authn.oauth.uma.resource-set.jpa.user` (Required; type `String`; default `sa`): The database user. The database user must have sufficient permissions to be able to handle schema changes and updates, when needed.
- `cas.authn.oauth.uma.resource-set.jpa.autocommit` (Optional; type `Boolean`; default `false`): The default auto-commit behavior of connections in the pool. Determined whether queries such as update/insert should be immediately executed without waiting for an underlying transaction.
- `cas.authn.oauth.uma.resource-set.jpa.batch-size` (Optional; type `Integer`; default `100`): A non-zero value enables use of JDBC2 batch updates by Hibernate. e.g. recommended values between 5 and 30.
- `cas.authn.oauth.uma.resource-set.jpa.connection-timeout` (Optional; type `String`; default `PT30S`): Indicates the maximum number of milliseconds that the service can wait to obtain a connection.
- `cas.authn.oauth.uma.resource-set.jpa.data-source-name` (Optional; type `String`): Attempts to do a JNDI data source look up for the data source name specified. Will attempt to locate the data source object as is.
- `cas.authn.oauth.uma.resource-set.jpa.ddl-auto` (Optional; type `String`; default `update`): Hibernate feature to automatically validate and exports DDL to the schema. By default, creates and drops the schema automatically when a session is starts and ends. Setting the value to `validate` or `none` may be more desirable for production, but any of the following options can be used: - `validate`: Validate the schema, but make no changes to the database. - `update`: Update the schema. - `create`: Create the schema, destroying previous data. - `create-drop`: Drop the schema at the end of the session. - `none`: Do nothing. Note that during a version migration where any schema has changed `create-drop` will result in the loss of all data as soon as CAS is started. For transient data like tickets this is probably not an issue, but in cases like the audit table important data could be lost. Using `update`, while safe for data, is confirmed to result in invalid database state. `validate` or `none` settings are likely the only safe options for production use. For more info, [see this](http://docs.spring.io/spring-framework/docs/current/javadoc-api).
- `cas.authn.oauth.uma.resource-set.jpa.default-catalog` (Optional; type `String`): Qualifies unqualified table names with the given catalog in generated SQL.
- `cas.authn.oauth.uma.resource-set.jpa.default-schema` (Optional; type `String`): Qualify unqualified table names with the given schema/tablespace in generated SQL.
- `cas.authn.oauth.uma.resource-set.jpa.dialect` (Optional; type `String`; default `org.hibernate.dialect.HSQLDialect`): The database dialect is a configuration setting for platform independent software (JPA, Hibernate, etc) which allows such software to translate its generic SQL statements into vendor specific DDL, DML.
- `cas.authn.oauth.uma.resource-set.jpa.fail-fast-timeout` (Optional; type `Long`; default `1`): Set the pool initialization failure timeout. - Any value greater than zero will be treated as a timeout for pool initialization. The calling thread will be blocked from continuing until a successful connection to the database, or until the timeout is reached. If the timeout is reached, then a `PoolInitializationException` will be thrown. - A value of zero will *not* prevent the pool from starting in the case that a connection cannot be obtained. However, upon start the pool will attempt to obtain a connection and validate that the `connectionTestQuery` and `connectionInitSql` are valid. If those validations fail, an exception will be thrown. If a connection cannot be obtained, the validation is skipped and the pool will start and continue to try to obtain connections in the background. This can mean that callers to `DataSource#getConnection()` may encounter exceptions. - A value less than zero will *not* bypass any connection attempt and validation during startup, and therefore the pool will start immediately. The pool will continue to try to obtain connections in the background. This can mean that callers to `DataSource#getConnection()` may encounter exceptions. Note that if this timeout value is greater than or equal to zero (0), and therefore an initial connection validation is performed, this timeout does not override the `connectionTimeout` or `validationTimeout`; they will be honored before this timeout is applied. The default value is one millisecond.
- `cas.authn.oauth.uma.resource-set.jpa.fetch-size` (Optional; type `Integer`; default `100`): Used to specify number of rows to be fetched in a select query.
- `cas.authn.oauth.uma.resource-set.jpa.generate-statistics` (Optional; type `Boolean`; default `false`): Allow hibernate to generate query statistics.
- `cas.authn.oauth.uma.resource-set.jpa.health-query` (Optional; type `String`): The SQL query to be executed to test the validity of connections. This is for "legacy" databases that do not support the JDBC4 `Connection.isValid()` API.
- `cas.authn.oauth.uma.resource-set.jpa.idle-timeout` (Optional; type `String`; default `PT10M`): Controls the maximum amount of time that a connection is allowed to sit idle in the pool.
- `cas.authn.oauth.uma.resource-set.jpa.isolate-internal-queries` (Optional; type `Boolean`; default `false`): This property determines whether data source isolates internal pool queries, such as the connection alive test, in their own transaction. Since these are typically read-only queries, it is rarely necessary to encapsulate them in their own transaction. This property only applies if `#autocommit` is disabled.
- `cas.authn.oauth.uma.resource-set.jpa.isolation-level-name` (Optional; type `String`; default `ISOLATION_READ_COMMITTED`): Defines the isolation level for transactions. @see org.springframework.transaction.TransactionDefinition
- `cas.authn.oauth.uma.resource-set.jpa.leak-threshold` (Optional; type `String`; default `PT6S`): Controls the amount of time that a connection can be out of the pool before a message is logged indicating a possible connection leak.
- `cas.authn.oauth.uma.resource-set.jpa.physical-naming-strategy-class-name` (Optional; type `String`; default `org.apereo.cas.hibernate.CasHibernatePhysicalNamingStrategy`): Fully-qualified name of the class that can control the physical naming strategy of hibernate.
- `cas.authn.oauth.uma.resource-set.jpa.pool.keep-alive-time` (Optional; type `String`; default `0`): This property controls the keepalive interval for a connection in the pool. An in-use connection will never be tested by the keepalive thread, only when it is idle will it be tested. Default is zero, which disables this feature.
- `cas.authn.oauth.uma.resource-set.jpa.pool.max-size` (Optional; type `Integer`; default `18`): Controls the maximum number of connections to keep in the pool, including both idle and in-use connections.
- `cas.authn.oauth.uma.resource-set.jpa.pool.max-wait` (Optional; type `String`; default `PT2S`): Sets the maximum time in seconds that this data source will wait while attempting to connect to a database. A value of zero specifies that the timeout is the default system timeout if there is one; otherwise, it specifies that there is no timeout.
- `cas.authn.oauth.uma.resource-set.jpa.pool.maximum-lifetime` (Optional; type `String`; default `PT10M`): This property controls the maximum lifetime of a connection in the pool. When a connection reaches this timeout, even if recently used, it will be retired from the pool. An in-use connection will never be retired, only when it is idle will it be removed.
- `cas.authn.oauth.uma.resource-set.jpa.pool.min-size` (Optional; type `Integer`; default `6`): Controls the minimum size that the pool is allowed to reach, including both idle and in-use connections.
- `cas.authn.oauth.uma.resource-set.jpa.pool.name` (Optional; type `String`): Set the name of the connection pool. This is primarily used for the MBean to uniquely identify the pool configuration.
- `cas.authn.oauth.uma.resource-set.jpa.pool.suspension` (Optional; type `Boolean`; default `false`): Whether or not pool suspension is allowed. There is a performance impact when pool suspension is enabled. Unless you need it (for a redundancy system for example) do not enable it.
- `cas.authn.oauth.uma.resource-set.jpa.pool.timeout-millis` (Optional; type `Long`; default `1000`): The maximum number of milliseconds that the pool will wait for a connection to be validated as alive.
- `cas.authn.oauth.uma.resource-set.jpa.propagation-behavior-name` (Optional; type `String`; default `PROPAGATION_REQUIRED`): Defines the propagation behavior for transactions. @see org.springframework.transaction.TransactionDefinition
- `cas.authn.oauth.uma.resource-set.jpa.properties` (Optional; type `Map<String,String>`): Additional settings provided by Hibernate (or the connection provider) in form of key-value pairs.
- `cas.authn.oauth.uma.resource-set.jpa.read-only` (Optional; type `Boolean`; default `false`): Configures the Connections to be added to the pool as read-only Connections.

**Hibernate & JDBC**

Control global properties that are relevant to Hibernate, when CAS attempts to employ and utilize database resources, connections and queries.

- `cas.jdbc.gen-ddl` (Optional; type `Boolean`; default `true`): Whether to generate DDL after the EntityManagerFactory has been initialized creating/updating all relevant tables.
- `cas.jdbc.physical-table-names` (Optional; type `Map<String,String>`): Indicate a physical table name to be used by the hibernate naming strategy in case table names need to be customized for the specific type of database. The key here indicates the CAS-provided table name and the value is the translate physical name for the database. If a match is not found for the CAS-provided table name, then that name will be used by default.
- `cas.jdbc.show-sql` (Optional; type `Boolean`; default `false`): Whether SQL queries should be displayed in the console/logs.

## Endpoints

### Requesting Party Token

Issue a `GET` request to `/oauth2.0/umaJwks` to retrieve signing public keys.

### Policies

#### Create

Issue a `POST` request to `/oauth2.0/${resourceId}/policy` with the payload body as:

```json
{
  "permissions": [{
    "subject": "casuser",
    "scopes": ["read","write"],
    "claims": {
        "givenName": "CAS"
      }
    }]
}
```

#### Delete

Issue a `DELETE` request as `/oauth2.0/${resourceId}/policy/${policyId}`

#### Update

Issue a `PUT` request as `/oauth2.0/${resourceId}/policy/${policyId}` with the payload body as one matching the `POST` method.

#### Find

- Issue a `GET` request as `/oauth2.0/${resourceId}/policy/` to fetch all policy definitions for a resource.
- Issue a `GET` request as `/oauth2.0/${resourceId}/policy/${policyId}` to fetch a specific policy definition for a resource.

### Resources

Resource-related operations are handled at endpoint `/oauth2.0/resourceSet`.

#### Create

The expected `POST` payload body is:

```json
{
  "uri": "...",
  "type": "...",
  "name": "...",
  "icon_uri": "...",
  "resource_scopes": ["read","write"]
}
```

#### Delete

Issue a `DELETE` request as `${resourceSetEndpoint}/${resourceId}`

#### Update

Issue a `PUT` request as `${resourceSetEndpoint}/${resourceId}` with the payload body as one matching the `POST` method.

#### Find

- Issue a `GET` request as `${resourceSetEndpoint}/${resourceId}` to fetch a specific resource definition.
- Issue a `GET` request as `${resourceSetEndpoint}` to fetch all resource definitions.

### Permission Tickets

Issue a `POST` request to `/oauth2.0/permission` with the payload body as:

```json
{
    "claims": {"givenName":"CAS"},
    "resource_id": "100",
    "resource_scopes": ["read"]
}
```

### Claims Collection

Issue a `GET` request to `/oauth2.0/rqpClaims` with the following query parameters:

- `client_id`
- `redirect_uri`
- `ticket`
- `state` (Optional)

### Discovery

UMA discovery is available via `GET` at `/oauth2.0/.well-known/uma-configuration`.

### Authorization

Issue a `POST` request to `/oauth2.0/rptAuthzRequest` with the payload body as:

```json
{
    "ticket": "...",
    "rpt": "...",
    "grant_type":"urn:ietf:params:oauth:grant-type:uma-ticket",
    "claim_token": "...",
    "claim_token_format": "..."
}
```
