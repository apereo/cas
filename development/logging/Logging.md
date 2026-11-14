# Logging

Source: https://apereo.github.io/cas/development/logging/Logging.html

CAS provides a logging facility that logs important informational events like authentication success and failure; it can be customized to produce additional information for troubleshooting. CAS uses the Slf4j Logging framework as a facade for the [Log4j engine](http://logging.apache.org/) by default.

The default log4j configuration file is located in `src/main/resources/log4j2.xml` of the `cas-server-webapp-resources` source module. In the `cas.war` it is found at the root of the `cas-server-webapp-resources*.jar`. The cas-overlay comes with an external `log4j2.xml` in `/etc/cas/config` and sets the location of the logging configuration to `file:/etc/cas/config/log4j2.xml` to reference it. By default logging is set to `INFO` for all functionality related to `org.apereo.cas` code. For debugging and diagnostic purposes you may want to set these levels to `DEBUG` or `TRACE`.

> **Production**
>
> You should always run everything under `WARN`. In production warnings and errors are things you care about. Everything else is just diagnostics. Only turn up `DEBUG` or `INFO` if you need to research a particular issue.

## Actuator Endpoints

The following endpoints are provided:

`loggingConfig`

CAS endpoint · 2 operations · Not exposed by default

**GET** `loggingConfig`: Get logging configuration report. (no params)

Response

- **Returns**: `Map`
- **Produces**: `application/json`
- **Java method**: `configuration()`
- **Defined in**: `LoggingConfigurationEndpoint`

Example request

```bash
curl -u "casuser:password" \
  -H "Accept: application/json" \
  "https://sso.example.org/cas/actuator/loggingConfig"
```

**GET** `loggingConfig/stream`: Fetch the last X number of log entries from all CAS appenders configured to capture logs. (2 params)

Parameters

| Name | In | Type | Description |
| --- | --- | --- | --- |
| `count` | query | – | The number of log entries to fetch |
| `level` | query | – | The log level to filter statements |

Response

- **Returns**: `List`
- **Produces**: `application/json`
- **Java method**: `getLogEntries(int,String,String)`
- **Defined in**: `LoggingConfigurationEndpoint`

Example request

```bash
curl -u "casuser:password" \
  -H "Accept: application/json" \
  "https://sso.example.org/cas/actuator/loggingConfig/stream?count=...&level=..."
```

`loggers`

Third-party endpoint · 3 operations · Not exposed by default

**GET** `loggers`: Logger configuration and levels. (no params)

Response

- **Returns**: `LoggersDescriptor`
- **Java method**: `loggers()`
- **Defined in**: `LoggersEndpoint`

Example request

```bash
curl -u "casuser:password" \
  -H "Accept: application/json" \
  "https://sso.example.org/cas/actuator/loggers"
```

**GET** `loggers/{name}`: Logger configuration and levels. (no params)

Response

- **Returns**: `LoggerLevelsDescriptor`
- **Java method**: `loggerLevels(String)`
- **Defined in**: `LoggersEndpoint`

Example request

```bash
curl -u "casuser:password" \
  -H "Accept: application/json" \
  "https://sso.example.org/cas/actuator/loggers/{name}"
```

**POST** `loggers/{name}`: Logger configuration and levels. (no params)

Response

- **Returns**: nothing
- **Java method**: `configureLogLevel(String,org.springframework.boot.logging.LogLevel)`
- **Defined in**: `LoggersEndpoint`

Example request

```bash
curl -u "casuser:password" \
  -X POST \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  "https://sso.example.org/cas/actuator/loggers/{name}"
```

`logfile`

Third-party endpoint · 1 operation · Not exposed by default

**GET** `logfile`: Returns the log file content if `logging.file` or `logging.path` are set with support for HTTP `Range` header. (no params)

Response

- **Returns**: `Resource`
- **Produces**: `text/plain; charset=UTF-8`
- **Java method**: `logFile()`
- **Defined in**: `LogFileWebEndpoint`

Example request

```bash
curl -u "casuser:password" \
  -H "Accept: text/plain; charset=UTF-8" \
  "https://sso.example.org/cas/actuator/logfile"
```

**Enable & expose**

Turn the endpoints on and expose them over the web. One entry covers every operation. By default only `info`, `health` and `status` are exposed.

```properties
management.endpoint.loggingConfig.access=UNRESTRICTED
management.endpoint.loggers.access=UNRESTRICTED
management.endpoint.logfile.access=UNRESTRICTED
management.endpoints.web.exposure.include=loggingConfig,loggers,logfile
```

Endpoints may be mapped to other paths. For example, to serve `health` at `healthcheck`:

```properties
management.endpoints.web.path-mapping.health=healthcheck
```

**Security**

Once exposed, every call goes through [Spring Security](https://spring.io/projects/spring-security), and CAS decides access per endpoint. Without a rule, the special `defaults` endpoint applies, which denies access.

Who may call them

*Authenticated*

```properties
cas.monitor.endpoints.endpoint.loggingConfig.access=AUTHENTICATED
cas.monitor.endpoints.endpoint.loggers.access=AUTHENTICATED
cas.monitor.endpoints.endpoint.logfile.access=AUTHENTICATED
```

*Role*

```properties
cas.monitor.endpoints.endpoint.loggingConfig.access=ROLE
cas.monitor.endpoints.endpoint.loggingConfig.required-roles=ADMIN
cas.monitor.endpoints.endpoint.loggers.access=ROLE
cas.monitor.endpoints.endpoint.loggers.required-roles=ADMIN
cas.monitor.endpoints.endpoint.logfile.access=ROLE
cas.monitor.endpoints.endpoint.logfile.required-roles=ADMIN
```

*IP address*

```properties
cas.monitor.endpoints.endpoint.loggingConfig.access=IP_ADDRESS
cas.monitor.endpoints.endpoint.loggingConfig.required-ip-addresses=127.0.0.1
cas.monitor.endpoints.endpoint.loggers.access=IP_ADDRESS
cas.monitor.endpoints.endpoint.loggers.required-ip-addresses=127.0.0.1
cas.monitor.endpoints.endpoint.logfile.access=IP_ADDRESS
cas.monitor.endpoints.endpoint.logfile.required-ip-addresses=127.0.0.1
```

*Anyone*

```properties
cas.monitor.endpoints.endpoint.loggingConfig.access=PERMIT
cas.monitor.endpoints.endpoint.loggers.access=PERMIT
cas.monitor.endpoints.endpoint.logfile.access=PERMIT
```

Where credentials are checked

*Basic*

A single user defined in CAS settings. If the name and password are left undefined, CAS generates them at startup.

- `spring.security.user.name` (Third party; type `String`; default `user`): Default user name.
- `spring.security.user.password` (Third party; type `String`): Password for the default user name.
- `spring.security.user.roles` (Third party; type `List<String>`): Granted roles for the default user name.

*JAAS*

- `cas.monitor.endpoints.jaas.login-config` (Optional; type `Resource`): JAAS login resource file.
- `cas.monitor.endpoints.jaas.login-context-name` (Optional; type `String`): The login context name should coincide with a given index in the login config specified. This name is used as the index to the configuration specified in the login config property. ``` JAASTest { org.springframework.security.authentication.jaas.TestLoginModule required; }; ``` In the above example, `JAASTest` should be set as the context name.
- `cas.monitor.endpoints.jaas.refresh-configuration-on-startup` (Optional; type `Boolean`; default `true`): If set, a call to `Configuration#refresh()` will be made by `#configureJaas(Resource)` method.

*LDAP*

- `cas.monitor.endpoints.ldap.base-dn` (Required; type `String`): Base DN to use. There may be scenarios where different parts of a single LDAP tree could be considered as base-dns. Rather than duplicating the LDAP configuration block for each individual base-dn, each entry can be specified and joined together using a special delimiter character. The user DN is retrieved using the combination of all base-dn and DN resolvers in the order defined. DN resolution should fail if multiple DNs are found. Otherwise the first DN found is returned. Usual syntax is: `subtreeA,dc=example,dc=net|subtreeC,dc=example,dc=net`.
- `cas.monitor.endpoints.ldap.bind-credential` (Required; type `String`): The bind credential to use when connecting to LDAP.
- `cas.monitor.endpoints.ldap.bind-dn` (Required; type `String`): The bind DN to use when connecting to LDAP. LDAP connection configuration injected into the LDAP connection pool can be initialized with the following parameters: - `bindDn/bindCredential` provided - Use the provided credentials to bind when initializing connections. - `bindDn/bindCredential` set to `*` - Use a fast-bind strategy to initialize the pool. - `bindDn/bindCredential` set to blank - Skip connection initializing; perform operations anonymously. - SASL mechanism provided - Use the given SASL mechanism to bind when initializing connections.
- `cas.monitor.endpoints.ldap.ldap-url` (Required; type `String`): The LDAP url to the server. More than one may be specified, separated by space and/or comma.
- `cas.monitor.endpoints.ldap.search-filter` (Required; type `String`): User filter to use for searching. Syntax is `cn={user}` or `cn={0}`. You may also provide an external groovy script in the syntax of `file:/path/to/GroovyScript.groovy` to fully build the final filter template dynamically.
- `cas.monitor.endpoints.ldap.type` (Required; type `AbstractLdapAuthenticationProperties.AuthenticationTypes`; default `AUTHENTICATED`): The authentication type. - `AD` - Users authenticate with `sAMAccountName`. - `AUTHENTICATED` - Manager bind/search type of authentication. If {@code} principalAttributePassword} is empty then a user simple bind is done to validate credentials. Otherwise the given attribute is compared with the given `principalAttributePassword` using the `SHA` encrypted value of it. - `ANONYMOUS`: Similar semantics as `AUTHENTICATED` except no `bindDn` and `bindCredential` may be specified to initialize the connection. If `principalAttributePassword` is empty then a user simple bind is done to validate credentials. Otherwise the given attribute is compared with the given `principalAttributePassword` using the `SHA` encrypted value of it. - DIRECT: Direct Bind - Compute user DN from format string and perform simple bind. This is relevant when no search is required to compute the DN needed for a bind operation. Use cases for this type are: 1) All users are under a single branch in the directory, `e.g. ou=Users,dc=example,dc=org.` 2) The username provided on the CAS login form is part of the DN, e.g. `uid=%s,ou=Users,dc=example,dc=org`. Available values are as follows: - `AD`: Active Directory. - `AUTHENTICATED`: Authenticated Search. - `DIRECT`: Direct Bind. - `ANONYMOUS`: Anonymous Search.
- `cas.monitor.endpoints.ldap.allow-multiple-dns` (Optional; type `Boolean`; default `false`): Whether search/query results are allowed to match on multiple DNs, or whether a single unique DN is expected for the result.
- `cas.monitor.endpoints.ldap.allow-multiple-entries` (Optional; type `Boolean`; default `false`): Set if multiple Entries are allowed.
- `cas.monitor.endpoints.ldap.binary-attributes` (Optional; type `List<String>`): Indicate the collection of attributes that are to be tagged and processed as binary attributes by the underlying search resolver.
- `cas.monitor.endpoints.ldap.block-wait-time` (Optional; type `String`; default `PT3S`): The length of time the pool will block. By default the pool will block indefinitely and there is no guarantee that waiting threads will be serviced in the order in which they made their request. This option should be used with a blocking connection pool when you need to control the exact number of connections that can be created
- `cas.monitor.endpoints.ldap.connect-timeout` (Optional; type `String`; default `PT5S`): Sets the maximum amount of time that connects will block.
- `cas.monitor.endpoints.ldap.connection-strategy` (Optional; type `String`): If multiple URLs are provided as the ldapURL this describes how each URL will be processed. - `ACTIVE_PASSIVE` First LDAP will be used for every request unless it fails and then the next shall be used. - `ROUND_ROBIN` For each new connection the next url in the list will be used. - `RANDOM` For each new connection a random LDAP url will be selected. - `DNS_SRV` LDAP urls based on DNS SRV records of the configured/given LDAP url will be used.
- `cas.monitor.endpoints.ldap.deref-aliases` (Optional; type `String`): Define how aliases are de-referenced. Accepted values are: - `NEVER` - `SEARCHING`: dereference when searching the entries beneath the starting point but not when searching for the starting entry. - `FINDING`: dereference when searching for the starting entry but not when searching the entries beneath the starting point. - `ALWAYS`: dereference when searching for the starting entry and when searching the entries beneath the starting point.
- `cas.monitor.endpoints.ldap.disable-pooling` (Optional; type `Boolean`; default `false`): Whether to use a pooled connection factory in components.
- `cas.monitor.endpoints.ldap.dn-format` (Optional; type `String`): Specify the dn format accepted by the AD authenticator, etc. Example format might be `uid=%s,ou=people,dc=example,dc=org`.
- `cas.monitor.endpoints.ldap.enhance-with-entry-resolver` (Optional; type `Boolean`; default `true`): Whether specific search entry resolvers need to be set on the authenticator, or the default should be used.
- `cas.monitor.endpoints.ldap.fail-fast` (Optional; type `Boolean`; default `true`): Attempt to populate the connection pool early on startup and fail quickly if something goes wrong.
- `cas.monitor.endpoints.ldap.follow-referrals` (Optional; type `Boolean`; default `true`): Set if search referrals should be followed.
- `cas.monitor.endpoints.ldap.hostname-verifier` (Optional; type `AbstractLdapProperties.LdapHostnameVerifierOptions`; default `DEFAULT`): Hostname verification options. Available values are as follows: - `DEFAULT`: Default option, forcing verification. - `ANY`: Skip hostname verification and allow all.
- `cas.monitor.endpoints.ldap.idle-time` (Optional; type `String`; default `PT10M`): Removes connections from the pool based on how long they have been idle in the available queue. Prunes connections that have been idle for more than the indicated amount.
- `cas.monitor.endpoints.ldap.keystore` (Optional; type `String`): Path to the keystore used for SSL connections. Typically contains SSL certificates for the LDAP server.
- `cas.monitor.endpoints.ldap.keystore-password` (Optional; type `String`): Keystore password.
- `cas.monitor.endpoints.ldap.keystore-type` (Optional; type `String`): The type of keystore. `PKCS12` or `JKS`. If left blank, defaults to the default keystore type indicated by the underlying Java platform.
- `cas.monitor.endpoints.ldap.ldap-authz.allow-multiple-results` (Optional; type `Boolean`; default `false`): Indicate whether the LDAP search query is allowed to return multiple entries.
- `cas.monitor.endpoints.ldap.ldap-authz.base-dn` (Optional; type `String`): Base DN to start the search.
- `cas.monitor.endpoints.ldap.ldap-authz.group-attribute` (Optional; type `String`): Attribute expected to be found on the entry resulting from the group search whose value is going to be used to construct roles. The final value is always prefixed with `#groupPrefix`. This is useful in scenarios where you wish to grant access to a resource to all users who a member of a given group.
- `cas.monitor.endpoints.ldap.ldap-authz.group-base-dn` (Optional; type `String`): Base DN to start the search looking for groups.
- `cas.monitor.endpoints.ldap.ldap-authz.group-filter` (Optional; type `String`): Search filter to begin looking for groups.
- `cas.monitor.endpoints.ldap.ldap-authz.group-prefix` (Optional; type `String`): A prefix that is prepended to the group attribute value to construct an authorized role.
- `cas.monitor.endpoints.ldap.ldap-authz.role-attribute` (Optional; type `String`; default `uugid`): Attribute expected to be found on the entry whose value is going to be used to construct roles. The final value is always prefixed with `#rolePrefix`. This is useful in scenarios where you wish to grant access to a resource to all users who carry a special attribute.
- `cas.monitor.endpoints.ldap.ldap-authz.role-prefix` (Optional; type `String`; default `ROLE_`): Prefix for the role.
- `cas.monitor.endpoints.ldap.ldap-authz.search-filter` (Optional; type `String`): LDAP search filter to locate accounts.
- `cas.monitor.endpoints.ldap.max-pool-size` (Optional; type `Integer`; default `10`): Maximum LDAP connection pool size which the pool can use to grow.
- `cas.monitor.endpoints.ldap.min-pool-size` (Optional; type `Integer`; default `3`): Minimum LDAP connection pool size. Size the pool should be initialized to and pruned to
- `cas.monitor.endpoints.ldap.name` (Optional; type `String`): Name of the LDAP handler.
- `cas.monitor.endpoints.ldap.page-size` (Optional; type `Integer`; default `0`): Request that the server return results in batches of a specific size. See [RFC 2696](http://www.ietf.org/rfc/rfc2696.txt). This control is often used to work around server result size limits. A negative/zero value disables paged requests.
- `cas.monitor.endpoints.ldap.pool-passivator` (Optional; type `String`; default `BIND`): You may receive unexpected LDAP failures, when CAS is configured to authenticate using `DIRECT` or `AUTHENTICATED` types and LDAP is locked down to not allow anonymous binds/searches. Every second attempt with a given LDAP connection from the pool would fail if it was on the same connection as a failed login attempt, and the regular connection validator would similarly fail. When a connection is returned back to a pool, it still may contain the principal and credentials from the previous attempt. Before the next bind attempt using that connection, the validator tries to validate the connection again but fails because it’s no longer trying with the configured bind credentials but with whatever user DN was used in the previous step. Given the validation failure, the connection is closed and CAS would deny access by default. Passivators attempt to reconnect to LDAP with the configured bind credentials, effectively resetting the connection to what it should be after each bind request. Furthermore if you are seeing errors in the logs that resemble a 'Operation exception encountered, reopening connection' type of message, this usually is an indication that the connection pool’s validation timeout established and created by CAS is greater than the timeout configured in the LDAP server, or more likely, in the load balancer in front of the LDAP servers. You can adjust the LDAP server session’s timeout for connections, or you can teach CAS to use a validity period that is equal or less than the LDAP server session’s timeout. Accepted values are: - `NONE`: No passivation takes place. - `BIND`: The default behavior which passivates a connection by performing a bind operation on it. This option requires the availability of bind credentials when establishing connections to LDAP.
- `cas.monitor.endpoints.ldap.principal-attribute-password` (Optional; type `String`): If principalAttributePassword is empty then a user simple bind is done to validate credentials otherwise the given attribute is compared with the given principalAttributePassword using the SHA encrypted value of it. For the anonymous authentication type, if principalAttributePassword is empty then a user simple bind is done to validate credentials otherwise the given attribute is compared with the given principalAttributePassword using the SHA encrypted value of it.
- `cas.monitor.endpoints.ldap.prune-period` (Optional; type `String`; default `PT2H`): Removes connections from the pool based on how long they have been idle in the available queue. Run the pruning process at the indicated interval.
- `cas.monitor.endpoints.ldap.resolve-from-attribute` (Optional; type `String`): If this attribute is set, the value found in the first attribute value will be used in place of the DN.
- `cas.monitor.endpoints.ldap.response-timeout` (Optional; type `String`; default `PT5S`): Duration of time to wait for responses.
- `cas.monitor.endpoints.ldap.sasl-authorization-id` (Optional; type `String`): SASL authorization id.
- `cas.monitor.endpoints.ldap.sasl-mechanism` (Optional; type `String`): The SASL mechanism.
- `cas.monitor.endpoints.ldap.sasl-mutual-auth` (Optional; type `Boolean`): Whether SASL mutual authentication is enabled.
- `cas.monitor.endpoints.ldap.sasl-quality-of-protection` (Optional; type `String`): SASL quality of protected.
- `cas.monitor.endpoints.ldap.sasl-realm` (Optional; type `String`): The SASL realm.
- `cas.monitor.endpoints.ldap.sasl-security-strength` (Optional; type `String`): SASL security strength.
- `cas.monitor.endpoints.ldap.search-entry-handlers` (Optional; type `List<LdapSearchEntryHandlersProperties>`): Search handlers.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].case-change.attribute-name-case-change` (Optional; type `String`): The Attribute name case change.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].case-change.attribute-names` (Optional; type `List<String>`): The Attribute names.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].case-change.attribute-value-case-change` (Optional; type `String`): The Attribute value case change.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].case-change.dn-case-change` (Optional; type `String`): The Dn case change.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].dn-attribute.add-if-exists` (Optional; type `boolean`): The Add if exists.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].dn-attribute.dn-attribute-name` (Optional; type `String`; default `entryDN`): The Dn attribute name.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].merge-attribute.attribute-names` (Optional; type `List<String>`): The Attribute names.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].merge-attribute.merge-attribute-name` (Optional; type `String`): The Merge attribute name.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].primary-group-id.base-dn` (Optional; type `String`): The Base dn.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].primary-group-id.group-filter` (Optional; type `String`; default `(&(objectClass=group)(objectSid={0}))`): The Group filter.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].recursive.merge-attributes` (Optional; type `List<String>`): The Merge attributes.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].recursive.search-attribute` (Optional; type `String`): The Search attribute.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].search-referral.limit` (Optional; type `int`; default `10`): The default referral limit.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].search-result.limit` (Optional; type `int`; default `10`): The default referral limit.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].type` (Optional; type `LdapSearchEntryHandlersProperties.SearchEntryHandlerTypes`): The type of search entry handler to choose. Available values are as follows: - `FOLLOW_SEARCH_REFERRAL`: Provides handling of an ldap referral for search operations. - `FOLLOW_SEARCH_RESULT_REFERENCE`: Provides handling of an ldap continuation reference for search operations. - `ACTIVE_DIRECTORY`: Process the entry results fetched from active directory and check for account status controls for disabled/expired accounts, etc. - `OBJECT_GUID`: Object guid search entry handler. Handles the `objectGUID` attribute fetching and conversion. - `OBJECT_SID`: Object sid search entry handler. Handles the `objectSid` attribute fetching and conversion. - `CASE_CHANGE`: Case change search entry handler. Provides the ability to modify the case of search entry DNs, attribute names, and attribute values. - `DN_ATTRIBUTE_ENTRY`: DN attribute entry handler. Adds the entry DN as an attribute to the result set. Provides a client side implementation of RFC 5020. - `MERGE`: Merge search entry handler. Merges the values of one or more attributes into a single attribute. - `PRIMARY_GROUP`: Primary group search handler. Constructs the primary group SID and then searches for that group and puts it's DN in the `memberOf` attribute of the original search entry. - `RANGE_ENTRY`: Range entry search handler. Rewrites attributes returned from Active Directory to include all values by performing additional searches. - `RECURSIVE_ENTRY`: Recursive entry search handler. This recursively searches based on a supplied attribute and merges those results into the original entry. - `MERGE_ENTRIES`: Merge entries handler. Merges the values of one or more attributes in all entries into a single attribute. The merged attribute may or may not already exist on the entry. If it does exist it's existing values will remain intact.
- `cas.monitor.endpoints.ldap.subtree-search` (Optional; type `Boolean`; default `true`): Whether subtree searching is allowed.
- `cas.monitor.endpoints.ldap.trust-certificates` (Optional; type `String`): Path of the trust certificates to use for the SSL connection. Ignores keystore-related settings when activated and used.
- `cas.monitor.endpoints.ldap.trust-manager` (Optional; type `String`): Trust Manager options. Trust managers are responsible for managing the trust material that is used when making LDAP trust decisions, and for deciding whether credentials presented by a peer should be accepted. Accepted values are: - `DEFAULT`: Enable and force the default JVM trust managers. - `ANY`: Trust any client or server.
- `cas.monitor.endpoints.ldap.trust-store` (Optional; type `String`): Path to the keystore used to determine which certificates or certificate authorities should be trusted. Used when connecting to an LDAP server via LDAPS or startTLS connection. If left blank, the default truststore for the Java runtime is used.
- `cas.monitor.endpoints.ldap.trust-store-password` (Optional; type `String`): Password needed to open the truststore.
- `cas.monitor.endpoints.ldap.trust-store-type` (Optional; type `String`): The type of trust keystore that determines which certificates or certificate authorities are trusted. Types depend on underlying java platform, typically `PKCS12` or `JKS`. If left blank, defaults to the default keystore type indicated by the underlying Java platform.
- `cas.monitor.endpoints.ldap.use-start-tls` (Optional; type `Boolean`; default `false`): Whether TLS should be used and enabled when establishing the connection.
- `cas.monitor.endpoints.ldap.validate-on-checkout` (Optional; type `Boolean`; default `true`): Whether connections should be validated when loaned out from the pool.
- `cas.monitor.endpoints.ldap.validate-period` (Optional; type `String`; default `PT5M`): Period at which pool should be validated.
- `cas.monitor.endpoints.ldap.validate-periodically` (Optional; type `Boolean`; default `true`): Whether connections should be validated periodically when the pool is idle.
- `cas.monitor.endpoints.ldap.validate-timeout` (Optional; type `String`; default `PT5S`): Period at which validation operations may time out.
- `cas.monitor.endpoints.ldap.validator.attribute-name` (Optional; type `String`; default `objectClass`): Attribute name to use for the compare validator.
- `cas.monitor.endpoints.ldap.validator.attribute-value` (Optional; type `String`; default `top`): Attribute values to use for the compare validator.
- `cas.monitor.endpoints.ldap.validator.base-dn` (Optional; type `String`): Base DN to use for the search request of the search validator.
- `cas.monitor.endpoints.ldap.validator.dn` (Optional; type `String`): DN to compare to use for the compare validator.
- `cas.monitor.endpoints.ldap.validator.scope` (Optional; type `String`; default `OBJECT`): Search scope to use for the search request of the search validator.
- `cas.monitor.endpoints.ldap.validator.search-filter` (Optional; type `String`; default `(objectClass=*)`): Search filter to use for the search request of the search validator.
- `cas.monitor.endpoints.ldap.validator.type` (Optional; type `String`; default `search`): Determine the LDAP validator type. The following LDAP validators can be used to test connection health status: - `search`: Validates a connection is healthy by performing a search operation. Validation is considered successful if the search result size is greater than zero. - `none`: No validation takes place. - `compare`: Validates a connection is healthy by performing a compare operation.

**LDAP & Active Directory**

**LDAP Scriptable Search Filter**

LDAP search filters can point to an external Groovy script to dynamically construct the final filter template.

The script itself may be designed as:

```groovy
import org.ldaptive.*
import org.springframework.context.*

def run(Object[] args) {
    def (filter,parameters,applicationContext,logger) = args

    logger.info("Configuring LDAP filter")
    filter.setFilter("uid=something")
}
```

The following parameters are passed to the script:

| Parameter | Description |
| --- | --- |
| `filter` | `FilterTemplate` to be updated by the script and used for the LDAP query. |
| `parameters` | Map of query parameters which may be used to construct the final filter. |
| `applicationContext` | Reference to the Spring `ApplicationContext` reference. |
| `logger` | The object responsible for issuing log messages such as `logger.info(...)`. |

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development//integration/Apache-Groovy-Scripting.html).

*JDBC*

- `cas.monitor.endpoints.jdbc.driver-class` (Required; type `String`; default `org.hsqldb.jdbcDriver`): The JDBC driver used to connect to the database.
- `cas.monitor.endpoints.jdbc.password` (Required; type `String`): The database connection password.
- `cas.monitor.endpoints.jdbc.password-encoder.encoding-algorithm` (Required; type `String`): The encoding algorithm to use such as `MD5`. Relevant when the type used is `DEFAULT` or `GLIBC_CRYPT`. When used with `PasswordEncoderTypes#PBKDF2`, it should be one of `PBKDF2WithHmacSHA1`, `PBKDF2WithHmacSHA256` or `PBKDF2WithHmacSHA512`.
- `cas.monitor.endpoints.jdbc.password-encoder.type` (Required; type `String`; default `NONE`): Define the password encoder type to use. Type may be specified as blank or `NONE` to disable password encoding. It may also refer to a fully-qualified class name that implements the Spring Security's `PasswordEncoder` interface if you wish you define your own encoder. The following types may be used: - `NONE`: No password encoding (i.e. plain-text) takes place. - `DEFAULT`: Use the `DefaultPasswordEncoder` of CAS. For message-digest algorithms via `character-encoding` and `encoding-algorithm`. - `BCRYPT`: Use the `BCryptPasswordEncoder` based on the strength provided and an optional secret. - `SCRYPT`: Use the `SCryptPasswordEncoder`. - `PBKDF2`: Use the `Pbkdf2PasswordEncoder` based on the strength provided and an optional secret. - `STANDARD`: Use the `StandardPasswordEncoder` based on the secret provided. - `SSHA`: Use the `LdapShaPasswordEncoder` supports Ldap SHA and SSHA (salted-SHA). The values are base-64 encoded and have the label {SHA} or {SSHA} prepended to the encoded hash. - `GLIBC_CRYPT`: Use the `GlibcCryptPasswordEncoder` based on the `encoding-algorithm`, strength provided and an optional secret. - `org.example.MyEncoder`: An implementation of `PasswordEncoder` of your own choosing. - `file:///path/to/script.groovy`: Path to a Groovy script charged with handling password encoding operations.
- `cas.monitor.endpoints.jdbc.url` (Required; type `String`; default `jdbc:hsqldb:mem:cas-hsql-database`): The database connection URL.
- `cas.monitor.endpoints.jdbc.user` (Required; type `String`; default `sa`): The database user. The database user must have sufficient permissions to be able to handle schema changes and updates, when needed.
- `cas.monitor.endpoints.jdbc.autocommit` (Optional; type `Boolean`; default `false`): The default auto-commit behavior of connections in the pool. Determined whether queries such as update/insert should be immediately executed without waiting for an underlying transaction.
- `cas.monitor.endpoints.jdbc.batch-size` (Optional; type `Integer`; default `100`): A non-zero value enables use of JDBC2 batch updates by Hibernate. e.g. recommended values between 5 and 30.
- `cas.monitor.endpoints.jdbc.connection-timeout` (Optional; type `String`; default `PT30S`): Indicates the maximum number of milliseconds that the service can wait to obtain a connection.
- `cas.monitor.endpoints.jdbc.data-source-name` (Optional; type `String`): Attempts to do a JNDI data source look up for the data source name specified. Will attempt to locate the data source object as is.
- `cas.monitor.endpoints.jdbc.ddl-auto` (Optional; type `String`; default `update`): Hibernate feature to automatically validate and exports DDL to the schema. By default, creates and drops the schema automatically when a session is starts and ends. Setting the value to `validate` or `none` may be more desirable for production, but any of the following options can be used: - `validate`: Validate the schema, but make no changes to the database. - `update`: Update the schema. - `create`: Create the schema, destroying previous data. - `create-drop`: Drop the schema at the end of the session. - `none`: Do nothing. Note that during a version migration where any schema has changed `create-drop` will result in the loss of all data as soon as CAS is started. For transient data like tickets this is probably not an issue, but in cases like the audit table important data could be lost. Using `update`, while safe for data, is confirmed to result in invalid database state. `validate` or `none` settings are likely the only safe options for production use. For more info, [see this](http://docs.spring.io/spring-framework/docs/current/javadoc-api).
- `cas.monitor.endpoints.jdbc.default-catalog` (Optional; type `String`): Qualifies unqualified table names with the given catalog in generated SQL.
- `cas.monitor.endpoints.jdbc.default-schema` (Optional; type `String`): Qualify unqualified table names with the given schema/tablespace in generated SQL.
- `cas.monitor.endpoints.jdbc.dialect` (Optional; type `String`; default `org.hibernate.dialect.HSQLDialect`): The database dialect is a configuration setting for platform independent software (JPA, Hibernate, etc) which allows such software to translate its generic SQL statements into vendor specific DDL, DML.
- `cas.monitor.endpoints.jdbc.fail-fast-timeout` (Optional; type `Long`; default `1`): Set the pool initialization failure timeout. - Any value greater than zero will be treated as a timeout for pool initialization. The calling thread will be blocked from continuing until a successful connection to the database, or until the timeout is reached. If the timeout is reached, then a `PoolInitializationException` will be thrown. - A value of zero will *not* prevent the pool from starting in the case that a connection cannot be obtained. However, upon start the pool will attempt to obtain a connection and validate that the `connectionTestQuery` and `connectionInitSql` are valid. If those validations fail, an exception will be thrown. If a connection cannot be obtained, the validation is skipped and the pool will start and continue to try to obtain connections in the background. This can mean that callers to `DataSource#getConnection()` may encounter exceptions. - A value less than zero will *not* bypass any connection attempt and validation during startup, and therefore the pool will start immediately. The pool will continue to try to obtain connections in the background. This can mean that callers to `DataSource#getConnection()` may encounter exceptions. Note that if this timeout value is greater than or equal to zero (0), and therefore an initial connection validation is performed, this timeout does not override the `connectionTimeout` or `validationTimeout`; they will be honored before this timeout is applied. The default value is one millisecond.
- `cas.monitor.endpoints.jdbc.fetch-size` (Optional; type `Integer`; default `100`): Used to specify number of rows to be fetched in a select query.
- `cas.monitor.endpoints.jdbc.generate-statistics` (Optional; type `Boolean`; default `false`): Allow hibernate to generate query statistics.
- `cas.monitor.endpoints.jdbc.health-query` (Optional; type `String`): The SQL query to be executed to test the validity of connections. This is for "legacy" databases that do not support the JDBC4 `Connection.isValid()` API.
- `cas.monitor.endpoints.jdbc.idle-timeout` (Optional; type `String`; default `PT10M`): Controls the maximum amount of time that a connection is allowed to sit idle in the pool.
- `cas.monitor.endpoints.jdbc.isolate-internal-queries` (Optional; type `Boolean`; default `false`): This property determines whether data source isolates internal pool queries, such as the connection alive test, in their own transaction. Since these are typically read-only queries, it is rarely necessary to encapsulate them in their own transaction. This property only applies if `#autocommit` is disabled.
- `cas.monitor.endpoints.jdbc.isolation-level-name` (Optional; type `String`; default `ISOLATION_READ_COMMITTED`): Defines the isolation level for transactions. @see org.springframework.transaction.TransactionDefinition
- `cas.monitor.endpoints.jdbc.leak-threshold` (Optional; type `String`; default `PT6S`): Controls the amount of time that a connection can be out of the pool before a message is logged indicating a possible connection leak.
- `cas.monitor.endpoints.jdbc.password-encoder.character-encoding` (Optional; type `String`; default `UTF-8`): The encoding algorithm to use such as 'UTF-8'. Relevant when the type used is `DEFAULT`.
- `cas.monitor.endpoints.jdbc.password-encoder.hash-length` (Optional; type `Integer`; default `16`): When used by `PasswordEncoderTypes#ARGON2`, it indicates the hash strength/length.
- `cas.monitor.endpoints.jdbc.password-encoder.iterations` (Optional; type `Integer`; default `310000`): When used by `PasswordEncoderTypes#PBKDF2`, it indicates the required number of iterations.
- `cas.monitor.endpoints.jdbc.password-encoder.secret` (Optional; type `String`): Secret to use with `PasswordEncoderTypes#STANDARD`, `PasswordEncoderTypes#PBKDF2`, `PasswordEncoderTypes#BCRYPT`, `PasswordEncoderTypes#GLIBC_CRYPT` password encoders. Secret usually is an optional setting.
- `cas.monitor.endpoints.jdbc.password-encoder.strength` (Optional; type `Integer`; default `16`): Strength or number of iterations to use for password hashing. Usually relevant when dealing with `PasswordEncoderTypes#BCRYPT`, `PasswordEncoderTypes#PBKDF2` or `PasswordEncoderTypes#GLIBC_CRYPT`. When used by `PasswordEncoderTypes#ARGON2` or `PasswordEncoderTypes#PBKDF2`, it indicates the salt strength.
- `cas.monitor.endpoints.jdbc.physical-naming-strategy-class-name` (Optional; type `String`; default `org.apereo.cas.hibernate.CasHibernatePhysicalNamingStrategy`): Fully-qualified name of the class that can control the physical naming strategy of hibernate.
- `cas.monitor.endpoints.jdbc.pool.keep-alive-time` (Optional; type `String`; default `0`): This property controls the keepalive interval for a connection in the pool. An in-use connection will never be tested by the keepalive thread, only when it is idle will it be tested. Default is zero, which disables this feature.
- `cas.monitor.endpoints.jdbc.pool.max-size` (Optional; type `Integer`; default `18`): Controls the maximum number of connections to keep in the pool, including both idle and in-use connections.
- `cas.monitor.endpoints.jdbc.pool.max-wait` (Optional; type `String`; default `PT2S`): Sets the maximum time in seconds that this data source will wait while attempting to connect to a database. A value of zero specifies that the timeout is the default system timeout if there is one; otherwise, it specifies that there is no timeout.
- `cas.monitor.endpoints.jdbc.pool.maximum-lifetime` (Optional; type `String`; default `PT10M`): This property controls the maximum lifetime of a connection in the pool. When a connection reaches this timeout, even if recently used, it will be retired from the pool. An in-use connection will never be retired, only when it is idle will it be removed.
- `cas.monitor.endpoints.jdbc.pool.min-size` (Optional; type `Integer`; default `6`): Controls the minimum size that the pool is allowed to reach, including both idle and in-use connections.
- `cas.monitor.endpoints.jdbc.pool.name` (Optional; type `String`): Set the name of the connection pool. This is primarily used for the MBean to uniquely identify the pool configuration.
- `cas.monitor.endpoints.jdbc.pool.suspension` (Optional; type `Boolean`; default `false`): Whether or not pool suspension is allowed. There is a performance impact when pool suspension is enabled. Unless you need it (for a redundancy system for example) do not enable it.
- `cas.monitor.endpoints.jdbc.pool.timeout-millis` (Optional; type `Long`; default `1000`): The maximum number of milliseconds that the pool will wait for a connection to be validated as alive.
- `cas.monitor.endpoints.jdbc.propagation-behavior-name` (Optional; type `String`; default `PROPAGATION_REQUIRED`): Defines the propagation behavior for transactions. @see org.springframework.transaction.TransactionDefinition
- `cas.monitor.endpoints.jdbc.properties` (Optional; type `Map<String,String>`): Additional settings provided by Hibernate (or the connection provider) in form of key-value pairs.
- `cas.monitor.endpoints.jdbc.query` (Optional; type `String`): Query to execute in order to authenticate users via JDBC. Example: `SELECT username,password,enabled FROM users WHERE username=?`
- `cas.monitor.endpoints.jdbc.read-only` (Optional; type `Boolean`; default `false`): Configures the Connections to be added to the pool as read-only Connections.
- `cas.monitor.endpoints.jdbc.role-prefix` (Optional; type `String`): Prefix to add to the role.

**Hibernate & JDBC**

Control global properties that are relevant to Hibernate, when CAS attempts to employ and utilize database resources, connections and queries.

- `cas.jdbc.gen-ddl` (Optional; type `Boolean`; default `true`): Whether to generate DDL after the EntityManagerFactory has been initialized creating/updating all relevant tables.
- `cas.jdbc.physical-table-names` (Optional; type `Map<String,String>`): Indicate a physical table name to be used by the hibernate naming strategy in case table names need to be customized for the specific type of database. The key here indicates the CAS-provided table name and the value is the translate physical name for the database. If a match is not found for the CAS-provided table name, then that name will be used by default.
- `cas.jdbc.show-sql` (Optional; type `Boolean`; default `false`): Whether SQL queries should be displayed in the console/logs.

**Password encoding**

If you need to design your own password encoding scheme where the type is specified as a fully qualified Java class name, the structure of the class would be similar to the following:

```java
package org.example.cas;

import org.springframework.security.crypto.codec.*;
import org.springframework.security.crypto.password.*;

public class MyEncoder extends AbstractPasswordEncoder {
    @Override
    protected byte[] encode(CharSequence rawPassword, byte[] salt) {
        return ...
    }
}
```

If you need to design your own password encoding scheme where the type is specified as a path to a Groovy script, the structure of the script would be similar to the following:

```groovy
import java.util.*

byte[] run(final Object... args) {
    def (rawPassword,generatedSalt,logger,applicationContext) = args
    logger.debug("Encoding password...")
    return ...
}

Boolean matches(final Object... args) {
    def (rawPassword,encodedPassword,logger,applicationContext) = args
    logger.debug("Does match or not ?");
    return ...
```

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development//integration/Apache-Groovy-Scripting.html).

*JSON*

A static list of users, passwords and roles in a JSON file. Supported password encodings are `{sha512}`, `{sha256}`, `{bcrypt}`, `{noop}`, `{pbkdf2}`, `{scrypt}` and `{argon2}`.

```json
[
    {
        "username": "casuser",
        "password": "{sha512}<hashed-password>",
        "authorities": [ "ROLE_ADMIN" ]
    }
]
```

- `cas.monitor.endpoints.json.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.

**Settings**

- `cas.monitor.endpoints.endpoint.[key].access` (Optional; type `ActuatorEndpointProperties.EndpointAccessLevel`; default `DENY`): Define the security access level of the endpoint. Available values are as follows: - `PERMIT`: Allow open access to the endpoint. - `ANONYMOUS`: Allow anonymous access to the endpoint. - `DENY`: Block access to the endpoint. - `AUTHENTICATED`: Require authenticated access to the endpoint. - `ROLE`: Require authenticated access to the endpoint along with a role requirement. - `AUTHORITY`: Require authenticated access to the endpoint along with an authority requirement. - `IP_ADDRESS`: Require authenticated access to the endpoint using a collection of IP addresses.
- `cas.monitor.endpoints.endpoint.[key].required-authorities` (Optional; type `List<String>`): Required user authorities.
- `cas.monitor.endpoints.endpoint.[key].required-ip-addresses` (Optional; type `List<String>`): Required IP addresses. CIDR ranges are accepted.
- `cas.monitor.endpoints.endpoint.[key].required-roles` (Optional; type `List<String>`): Required user roles.
- `cas.monitor.endpoints.form-login-enabled` (Optional; type `Boolean`; default `false`): Control whether access to endpoints can be controlled via form-based login over the web via a special admin login endpoint.

**Spring Boot settings**

- `management.endpoint.health.access` (Third party; type `Access`; default `unrestricted`): Permitted level of access for the health endpoint.
- `management.endpoint.health.cache.time-to-live` (Third party; type `Duration`; default `0ms`): Maximum time that a response can be cached.
- `management.endpoint.health.group` (Third party; type `Map<String,HealthEndpointProperties.Group>`): Health endpoint groups.
- `management.endpoint.health.logging.slow-indicator-threshold` (Third party; type `Duration`; default `10s`): Threshold after which a warning will be logged for slow health indicators.
- `management.endpoint.health.probes.add-additional-paths` (Third party; type `Boolean`; default `false`): Whether to make the liveness and readiness health groups available on the main server port.
- `management.endpoint.health.probes.enabled` (Third party; type `Boolean`; default `true`): Whether to enable liveness and readiness probes.
- `management.endpoint.health.roles` (Third party; type `Set<String>`): Roles used to determine whether a user is authorized to be shown details. When empty, all authenticated users are authorized.
- `management.endpoint.health.show-components` (Third party; type `Show`): When to show components. If not specified the 'show-details' setting will be used.
- `management.endpoint.health.show-details` (Third party; type `Show`; default `never`): When to show full health details.
- `management.endpoint.health.status.http-mapping` (Third party; type `Map<String,Integer>`): Mapping of health statuses to HTTP status codes. By default, registered health statuses map to sensible defaults (for example, UP maps to 200).
- `management.endpoint.health.status.order` (Third party; type `List<String>`; default `["DOWN", "OUT_OF_SERVICE", "UP", "UNKNOWN"]`): List of health statuses in order of severity.
- `management.endpoint.health.validate-group-membership` (Third party; type `Boolean`; default `true`): Whether to validate health group membership on startup. Validation fails if a group includes or excludes a health contributor that does not exist.
- `management.endpoints.access.default` (Third party; type `Access`): Default access level for all endpoints.
- `management.endpoints.access.max-permitted` (Third party; default `unrestricted`): Maximum level of endpoint access that is permitted. Caps an endpoint's individual access level (management.endpoint.\<id>.access) and the default access (management.endpoints.access.default).'
- `management.endpoints.enabled-by-default` (Third party; type `Boolean`; deprecation `WARNING`, replaced by `management.endpoints.access.default`): Whether to enable or disable all endpoints by default.
- `management.endpoints.jackson.isolated-json-mapper` (Third party; type `Boolean`; default `true`): Whether to use an isolated JsonMapper to serialize endpoint JSON.
- `management.endpoints.jackson2.isolated-object-mapper` (Third party; type `Boolean`; deprecation `WARNING`, no replacement; default `true`): Whether to use an isolated object mapper to serialize endpoint JSON.
- `management.endpoints.jmx.domain` (Third party; type `String`; default `org.springframework.boot`): Endpoints JMX domain name. Fallback to 'spring.jmx.default-domain' if set.
- `management.endpoints.jmx.exposure.exclude` (Third party; type `Set<String>`): Endpoint IDs that should be excluded or '*' for all.
- `management.endpoints.jmx.exposure.include` (Third party; type `Set<String>`; default `health`): Endpoint IDs that should be included or '*' for all.
- `management.endpoints.jmx.static-names` (Third party; type `Properties`): Additional static properties to append to all ObjectNames of MBeans representing Endpoints.
- `management.endpoints.jmx.unique-names` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `spring.jmx.unique-names`): Whether unique runtime object names should be ensured.
- `management.endpoints.migrate-legacy-ids` (Third party; type `Boolean`; default `false`): Whether to transparently migrate legacy endpoint IDs.
- `management.endpoints.web.base-path` (Third party; type `String`; default `/actuator`): Base path for Web endpoints. Relative to the servlet context path (server.servlet.context-path) or WebFlux base path (spring.webflux.base-path) when the management server is sharing the main server port. Relative to the management server base path (management.server.base-path) when a separate management server port (management.server.port) is configured.
- `management.endpoints.web.cors.allow-credentials` (Third party; type `Boolean`): Whether credentials are supported. When not set, credentials are not supported.
- `management.endpoints.web.cors.allowed-headers` (Third party; type `List<String>`): List of headers to allow in a request. '*' allows all headers.
- `management.endpoints.web.cors.allowed-methods` (Third party; type `List<String>`): List of methods to allow. '*' allows all methods. When not set, defaults to GET.
- `management.endpoints.web.cors.allowed-origin-patterns` (Third party; type `List<String>`): List of origin patterns to allow. Unlike allowed origins which only supports '*', origin patterns are more flexible (for example 'https://*.example.com') and can be used when credentials are allowed. When no allowed origin patterns or allowed origins are set, CORS support is disabled.
- `management.endpoints.web.cors.allowed-origins` (Third party; type `List<String>`): List of origins to allow. '*' allows all origins. When credentials are allowed, '*' cannot be used and origin patterns should be configured instead. When no allowed origins or allowed origin patterns are set, CORS support is disabled.
- `management.endpoints.web.cors.exposed-headers` (Third party; type `List<String>`): List of headers to include in a response.
- `management.endpoints.web.cors.max-age` (Third party; type `Duration`; default `1800s`): How long the response from a pre-flight request can be cached by clients. If a duration suffix is not specified, seconds will be used.
- `management.endpoints.web.discovery.enabled` (Third party; type `Boolean`; default `true`): Whether the discovery page is enabled.
- `management.endpoints.web.exposure.exclude` (Third party; type `Set<String>`): Endpoint IDs that should be excluded or '*' for all.
- `management.endpoints.web.exposure.include` (Third party; type `Set<String>`; default `["health"]`): Endpoint IDs that should be included or '*' for all.
- `management.endpoints.web.path-mapping` (Third party; type `Map<String,String>`): Mapping between endpoint IDs and the path that should expose them.
- `management.health.binders.enabled` (Third party; type `Boolean`; default `true`): Allows to enable/disable binder's' health indicators. If you want to disable health indicator completely, then set it to `false`.
- `management.health.db.enabled` (Third party; type `Boolean`; default `true`): Whether to enable database health check.
- `management.health.db.ignore-routing-data-sources` (Third party; type `Boolean`; default `false`): Whether to ignore AbstractRoutingDataSources when creating database health indicators.
- `management.health.defaults.enabled` (Third party; type `Boolean`; default `true`): Whether to enable default health indicators.
- `management.health.diskspace.enabled` (Third party; type `Boolean`; default `true`): Whether to enable disk space health check.
- `management.health.diskspace.path` (Third party; type `File`): Path used to compute the available disk space.
- `management.health.diskspace.threshold` (Third party; type `DataSize`; default `10MB`): Minimum disk space that should be available.
- `management.health.influxdb.enabled` (Third party; type `Boolean`; deprecation `ERROR`, no replacement)
- `management.health.livenessstate.enabled` (Third party; type `Boolean`; default `false`): Whether to enable liveness state health check.
- `management.health.mail.enabled` (Third party; type `Boolean`; default `true`): Whether to enable Mail health check.
- `management.health.mongo.enabled` (Third party; deprecation `ERROR`, replaced by `management.health.mongodb.enabled`)
- `management.health.mongodb.enabled` (Third party; type `Boolean`; default `true`): Whether to enable MongoDB health check.
- `management.health.ping.enabled` (Third party; type `Boolean`; default `true`): Whether to enable ping health check.
- `management.health.probes.enabled` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `management.endpoint.health.probes.enabled`; default `false`): Whether to enable liveness and readiness probes.
- `management.health.pubsub.enabled` (Third party; type `Boolean`; default `true`): Whether to enable the Pub/Sub health indicator when used with Spring Boot Actuator.
- `management.health.rabbit.enabled` (Third party; type `Boolean`; default `true`): Whether to enable RabbitMQ health check.
- `management.health.readinessstate.enabled` (Third party; type `Boolean`; default `false`): Whether to enable readiness state health check.
- `management.health.redis.enabled` (Third party; type `Boolean`; default `true`): Whether to enable Redis health check.
- `management.health.refresh.enabled` (Third party; type `Boolean`; default `true`): Enable the health endpoint for the refresh scope.
- `management.health.ssl.certificate-validity-warning-threshold` (Third party; type `Duration`; default `14d`): If an SSL Certificate will be invalid within the time span defined by this threshold, it should trigger a warning.
- `management.health.ssl.enabled` (Third party; type `Boolean`; default `true`): Whether to enable SSL certificate health check.
- `management.health.zookeeper.enabled` (Third party; type `Boolean`; default `true`): Enable the health endpoint for zookeeper.

**Troubleshooting**

- `404`: the endpoint is not enabled or not exposed; check the *Enable & expose* tab.
- `401` or `403`: the access rule or the credentials rejected the call; check the *Security* tab.

For more detail, raise these log levels in the log4j configuration:

```xml
<Logger name="org.apereo.cas.logging.web" level="debug" additivity="false">
    <AppenderRef ref="console" />
    <AppenderRef ref="file" />
</Logger>
<Logger name="org.springframework.boot.actuate.logging" level="debug" additivity="false">
    <AppenderRef ref="console" />
    <AppenderRef ref="file" />
</Logger>
<Logger name="org.springframework.security" level="debug" additivity="false">
    <AppenderRef ref="console" />
    <AppenderRef ref="file" />
</Logger>
```

## CAS Plugins

The `log4j2.xml` file use by CAS includes custom Log4j2 plugins:

- `CasAppender`: The CasAppender wraps another regular appender and removes sensitive values from the log entries such as Ticket Granting Tickets or Proxy Granting Tickets. It may be used via the following Log4j2 configuration template:

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<Configuration monitorInterval="2">
  <!-- Wrap the console appender inside -->
  <CasAppender name="casConsole" maxEntries="50">
      <AppenderRef ref="console" />
  </CasAppender>
</Configuration>
```

The `maxEntries` attribute indicates the maximum number of latest log entries to keep in an in-memory cache. When the cache is full, the oldest entries are removed. By default, nothing is kept in the cache. This ability is useful when you want to stream the latest log entries via actuator endpoints, etc.

- `ExceptionOnlyFilter`: In order to allow CAS to freely log unexpected errors at WARN and ERROR without obscuring everything with stacktraces, exceptions in the logs are disabled by default but there are log4j2.xml properties that can turn them back on. By default, all exceptions are written to a dedicated stacktrace rolling log file and this is done using a custom ExceptionOnlyFilter nested in the CasAppender.

## Log4j2 Properties

The `log4j2.xml` file includes properties for various settings and those can be set in the properties section of the `log4j2.xml` file, in a property file called `log4j2.component.properties` on the classpath, or as system properties. If setting properties in a `log4j2.component.properties`, be sure to include:

```properties
log4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector
```

in order to keep using asynchronous logging which CAS sets by default. To turn off asynchronous logging, include the following in `log4j2.component.properties` or as a system property:

```properties
log4j2.contextSelector=org.apache.logging.log4j.core.selector.BasicContextSelector
```

## Configuration

It is often helpful to externalize the `log4j2.xml` file to a system path to preserve settings between upgrades. The location of `log4j2.xml` file by default is on the runtime classpath and can be controlled via the CAS properties.

The following settings and properties are available from the CAS configuration catalog:

- `logging.charset.console` (Third party; type `Charset`): Charset to use for console output.
- `logging.charset.file` (Third party; type `Charset`): Charset to use for file output.
- `logging.config` (Third party; type `String`): Location of the logging configuration file. For instance, `classpath:logback.xml` for Logback.
- `logging.console.enabled` (Third party; type `Boolean`): Whether to enable console-based logging.
- `logging.exception-conversion-word` (Third party; type `String`; default `%wEx`): Conversion word used when logging exceptions.
- `logging.file` (Third party; type `String`; deprecation `ERROR`, replaced by `logging.file.name`): Log file name (for instance, `myapp.log`). Names can be an exact location or relative to the current directory.
- `logging.file.clean-history-on-start` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `logging.logback.rollingpolicy.clean-history-on-start`)
- `logging.file.max-history` (Third party; type `Integer`; deprecation `ERROR`, replaced by `logging.logback.rollingpolicy.max-history`)
- `logging.file.max-size` (Third party; type `DataSize`; deprecation `ERROR`, replaced by `logging.logback.rollingpolicy.max-file-size`)
- `logging.file.name` (Third party; type `String`): Log file name (for instance, `myapp.log`). Names can be an exact location or relative to the current directory.
- `logging.file.path` (Third party; type `String`): Location of the log file. For instance, `/var/log`.
- `logging.file.total-size-cap` (Third party; type `DataSize`; deprecation `ERROR`, replaced by `logging.logback.rollingpolicy.total-size-cap`)
- `logging.group` (Third party; type `Map<String,List<String>>`): Log groups to quickly change multiple loggers at the same time. For instance, `logging.group.db=org.hibernate,org.springframework.jdbc`.
- `logging.include-application-group` (Third party; type `Boolean`; default `true`): Whether to include the application group in the logs.
- `logging.include-application-name` (Third party; type `Boolean`; default `true`): Whether to include the application name in the logs.
- `logging.level` (Third party; type `Map<String,String>`): Log levels severity mapping. For instance, `logging.level.org.springframework=DEBUG`.
- `logging.log4j2.config.override` (Third party; type `List<String>`): Overriding configuration files used to create a composite configuration. Can be prefixed with 'optional:' to only load the override if it exists.
- `logging.log4j2.rollingpolicy.cron` (Third party; type `String`; default `0 0 0 * * ?`): Cron expression used when the strategy is 'cron'.
- `logging.log4j2.rollingpolicy.file-name-pattern` (Third party; type `String`; default `${LOG_FILE}.%d{yyyy-MM-dd}.%i.gz`): Pattern for rolled-over log file names.
- `logging.log4j2.rollingpolicy.max-file-size` (Third party; type `DataSize`; default `10MB`): Maximum log file size.
- `logging.log4j2.rollingpolicy.max-history` (Third party; type `Integer`; default `7`): Maximum number of archive log files to keep.
- `logging.log4j2.rollingpolicy.strategy` (Third party; type `RollingPolicyStrategy`; default `size`): Rolling policy strategy.
- `logging.log4j2.rollingpolicy.time-interval` (Third party; type `Integer`; default `1`): Time based triggering interval when the strategy is 'time' or 'size-and-time'.
- `logging.log4j2.rollingpolicy.time-modulate` (Third party; type `Boolean`; default `false`): Whether to align the next rollover time to occur at the top of the interval when the strategy is time based.
- `logging.logback.rollingpolicy.clean-history-on-start` (Third party; type `Boolean`; default `false`): Whether to clean the archive log files on startup.
- `logging.logback.rollingpolicy.file-name-pattern` (Third party; type `String`; default `${LOG_FILE}.%d{yyyy-MM-dd}.%i.gz`): Pattern for rolled-over log file names.
- `logging.logback.rollingpolicy.max-file-size` (Third party; type `DataSize`; default `10MB`): Maximum log file size.
- `logging.logback.rollingpolicy.max-history` (Third party; type `Integer`; default `7`): Maximum number of archive log files to keep.
- `logging.logback.rollingpolicy.total-size-cap` (Third party; type `DataSize`; default `0B`): Total size of log backups to be kept.
- `logging.path` (Third party; type `String`; deprecation `ERROR`, replaced by `logging.file.path`): Location of the log file. For instance, `/var/log`.
- `logging.pattern.console` (Third party; type `String`): Appender pattern for output to the console. Its default value varies according to the logging system.
- `logging.pattern.correlation` (Third party; type `String`): Appender pattern for log correlation. Its default value varies according to the logging system.
- `logging.pattern.dateformat` (Third party; type `String`; default `yyyy-MM-dd'T'HH:mm:ss.SSSXXX`): Appender pattern for log date format.
- `logging.pattern.file` (Third party; type `String`): Appender pattern for output to a file. Its default value varies according to the logging system.
- `logging.pattern.level` (Third party; type `String`; default `%5p`): Appender pattern for log level.
- `logging.pattern.rolling-file-name` (Third party; type `String`; deprecation `ERROR`, replaced by `logging.logback.rollingpolicy.file-name-pattern`)
- `logging.register-shutdown-hook` (Third party; type `Boolean`; default `true`): Register a shutdown hook for the logging system when it is initialized. Disabled automatically when deployed as a war file.
- `logging.structured.ecs.service.environment` (Third party; type `String`): Structured ECS service environment.
- `logging.structured.ecs.service.name` (Third party; type `String`): Structured ECS service name (defaults to 'spring.application.name').
- `logging.structured.ecs.service.node-name` (Third party; type `String`): Structured ECS service node name.
- `logging.structured.ecs.service.version` (Third party; type `String`): Structured ECS service version (defaults to 'spring.application.version').
- `logging.structured.format.console` (Third party; type `String`): Structured logging format for output to the console. Must be either a format id or a fully qualified class name.
- `logging.structured.format.file` (Third party; type `String`): Structured logging format for output to a file. Must be either a format id or a fully qualified class name.
- `logging.structured.gelf.host` (Third party; type `String`): Structured GELF host (defaults to 'spring.application.name').
- `logging.structured.gelf.service.version` (Third party; type `String`): Structured GELF service version (defaults to 'spring.application.version').
- `logging.structured.json.add` (Third party; type `Map<String,String>`): Additional members that should be added to structured logging JSON
- `logging.structured.json.context.include` (Third party; type `Boolean`): Whether context data should be included in the JSON.
- `logging.structured.json.context.prefix` (Third party; type `String`): The prefix to use when inserting context data.
- `logging.structured.json.customizer` (Third party; type `Set<Class<? extends StructuredLoggingJsonMembersCustomizer<?>>>`): Fully qualified class names of StructuredLoggingJsonMembersCustomizer implementations
- `logging.structured.json.exclude` (Third party; type `Set<String>`): Member paths that should be excluded from structured logging JSON
- `logging.structured.json.include` (Third party; type `Set<String>`): Member paths that should be included in structured logging JSON
- `logging.structured.json.rename` (Third party; type `Map<String,String>`): Mapping between member paths and an alternative name that should be used in structured logging JSON
- `logging.structured.json.stacktrace.include-common-frames` (Third party; type `Boolean`): Whether common frames should be included.
- `logging.structured.json.stacktrace.include-hashes` (Third party; type `Boolean`): Whether stack trace hashes should be included.
- `logging.structured.json.stacktrace.max-length` (Third party; type `Integer`): Maximum length to print.
- `logging.structured.json.stacktrace.max-throwable-depth` (Third party; type `Integer`): Maximum throwable depth to print.
- `logging.structured.json.stacktrace.printer` (Third party; type `String`): Name of the printer to use. Can be 'standard', 'logging-system', or the fully-qualified class name of a StackTracePrinter. When not specified 'logging-system' or 'standard' will be used depending if other properties are set.
- `logging.structured.json.stacktrace.root` (Third party; type `StructuredLoggingJsonProperties.StackTrace.Root`): Root ordering (root first or root last).
- `logging.threshold.console` (Third party; type `String`; default `TRACE`): Log level threshold for console output.
- `logging.threshold.file` (Third party; type `String`; default `TRACE`): Log level threshold for file output.

To disable log sanitization, start the container with the system property `CAS_TICKET_ID_SANITIZE_SKIP=true`.

### Log Levels

While log levels can directly be massaged via the native `log4j2.xml` syntax, they may also be modified using the usual CAS properties.

The following settings and properties are available from the CAS configuration catalog:

- `logging.level` (Third party; type `Map<String,String>`): Log levels severity mapping. For instance, `logging.level.org.springframework=DEBUG`.

### Refresh Interval

The `log4j2.xml` itself controls the refresh interval of the logging configuration. Log4j has the ability to automatically detect changes to the configuration file and reconfigure itself. If the `monitorInterval` attribute is specified on the configuration element and is set to a non-zero value then the file will be checked the next time a log event is evaluated and/or logged and the `monitorInterval` has elapsed since the last check. This will allow you to adjust the log levels and configuration without restarting the server environment.

```xml
<!-- Specify the refresh internal in seconds. -->
<Configuration monitorInterval="15" ...>
    ...
</Configuration>
```

### Appenders

Appenders are responsible for delivering log events to their destination. Appenders usually are only responsible for writing the event data to the target destination. In most cases they delegate responsibility for formatting the event to a layout. Some appenders wrap other appenders so that they can modify the log event, handle a failure in an `Appender`, route the event to a subordinate `Appender` based on advanced filtering criteria or provide similar functionality that does not directly format the event for viewing. `Appender`s always have a name so that they can be referenced from `Logger`s.

The following `Appender` elements are only a partial collection of available options.

| Layout | Description |
| --- | --- |
| `AsyncAppender` | Accepts references to other Appenders and causes LogEvents to be written to them on a separate Thread. |
| `CassandraAppender` | Writes its output to an Apache Cassandra database. A keyspace and table must be configured ahead of time, and the columns should be mapped in a configuration file. |
| `ConsoleAppender` | Writes its output to either `System.out` or `System.err` with `System.out` being the default target. |
| `FailoverAppender` | Wraps a set of appenders. If the primary Appender fails the secondary appenders will be tried in order until one succeeds or there are no more secondaries to try. |
| `FileAppender` | Writes to the File named in the `fileName` parameter. |
| `CsvParameterLayout` | Converts an event’s parameters into a CSV record, ignoring the message. |
| `JDBCAppender` | Writes log events to a relational database table using standard JDBC. |
| `JPAAppender` | Writes log events to a relational database table using the Java Persistence API `2.1`. |
| `HttpAppender` | Sends log events over HTTP. A Layout must be provided to format the log event. |
| `KafkaAppender` | Logs events to an Apache Kafka topic. Each log event is sent as a Kafka record. |
| `NoSQLAppender` | Writes log events to a NoSQL database; Provider implementations exist for MongoDB and Apache CouchDB. |
| `RoutingAppender` | Evaluates log events and then routes them to a subordinate `Appender`. |
| `SMTPAppender` | Sends an e-mail when a specific logging event occurs, typically on errors or fatal errors. |
| `JeroMQ` | The ZeroMQ appender uses the JeroMQ library to send log events to one or more ZeroMQ endpoints. |
| `RollingFileAppender` | Writes to the File named in the fileName parameter and rolls the file over according the `TriggeringPolicy` and the `RolloverPolicy`. |
| `RewriteAppender` | Allows the log event to be manipulated before it is processed by another `Appender`. This can be used to mask sensitive information such as passwords or to inject information into each event. |

For full details, please review the official [Log4j documentation](http://logging.apache.org/)

### Log Patterns

By default most appenders that are provided via the `log4j2.xml` file use pattern-based layouts to format log messages. The following alternative layouts may also be used:

| Layout | Description |
| --- | --- |
| `CsvParameterLayout` | Converts an event’s parameters into a CSV record, ignoring the message. |
| `GelfLayout` | Lays out events in the Graylog Extended Log Format (`GELF`). |
| `HTMLLayout` | Generates an HTML page and adds each LogEvent to a row in a table |
| `JSONLayout` | Creates log events in well-formed or fragmented JSON. |
| `PatternLayout` | Formats the log even based on a conversion pattern. |
| `RFC5424Layout` | Formats log events in accordance with [RFC 5424](https://tools.ietf.org/html/rfc5424), the enhanced Syslog specification. |
| `SerializedLayout` | Log events are transformed into byte arrays useful in JMS or socket connections. |
| `SyslogLayout` | Formats log events as BSD Syslog records. |
| `XMLLayout` | Creates log events in well-formed or fragmented XML. |
| `YamlLayout` | Creates log events in YAML. |

To learn more about nuances and configuration settings for each, please refer to the [official Log4J guides](http://logging.apache.org/).

## Log File Rotation

The default configuration specifies triggering policies for rolling over logs, at startup, size or at specific times. These policies apply to `RollingFile` appenders.

For example, the following XML fragment defines policies that rollover the log when the JVM starts, when the log size reaches `10` megabytes, and when the current date no longer matches the log’s start date.

```xml
<RollingFile name="file" fileName="${baseDir}/cas.log" append="true"
             filePattern="${baseDir}/cas-%d{yyyy-MM-dd-HH}-%i.log.gz">
    ...
    <Policies>
        <OnStartupTriggeringPolicy />
        <SizeBasedTriggeringPolicy size="10 MB"/>
        <TimeBasedTriggeringPolicy interval="24" />
    </Policies>
    ...
</RollingFile>
```

The triggering policies determines **if** a rollover should be performed and rollover strategy can also be design to indicate **how** that should be done. If no strategy is configured, the default will be used.

To find more a comprehensive documentation, please [review the guides here](http://logging.apache.org/).

### Rollover Strategy

Customized rollover strategies provide a delete action that gives users more control over what files are deleted at rollover time than what was possible with the DefaultRolloverStrategy max attribute. The delete action lets users configure one or more conditions that select the files to delete relative to a base directory.

For example, the following appender at rollover time deletes all files under the base directory that match the `*/*.log` glob and are `7` days old or older.

```xml
<RollingFile name="file" fileName="${baseDir}/cas.log" append="true"
             filePattern="${baseDir}/cas-%d{yyyy-MM-dd-HH}-%i.log.gz">
    ...
    <DefaultRolloverStrategy max="5" compressionLevel="9">
        <Delete basePath="${baseDir}" maxDepth="2">
            <IfFileName glob="*/*.log.gz" />
            <IfLastModified age="7d" />
        </Delete>
    </DefaultRolloverStrategy>
    ...
</RollingFile>
```

To find more a comprehensive documentation, please [review the guides here](http://logging.apache.org/).

## Log Data Sanitation

For security purposes, CAS by default will attempt to remove ticket-granting ticket and proxy-granting ticket ids from all log data. This will of course include messages that are routed to a log destination by the logging framework as well as all audit messages.

A sample follows below:

```bash
WHO: audit:unknown
WHAT: TGT-******************123456-cas01.example.org
ACTION: TICKET_GRANTING_TICKET_DESTROYED
APPLICATION: CAS
WHEN: Sat Jul 12 04:10:35 PDT 2014
CLIENT IP ADDRESS: ...
SERVER IP ADDRESS: ...
```

Certain number of characters are left at the trailing end of the ticket id to assist with troubleshooting and diagnostics.

## Log Message Summaries

By default, logged stacktraces are summarized and only the first few lines of the stacktrace are displayed in the output to reduce noise. If you wish to see the full stacktrace and disable the summary mode, you can consider changing the log level for the appropriate package to `DEBUG`. This behavior can be replaced and customized via your implementation of the `LogMessageSummarizer`, that should be registered with CAS using the Java ServiceLoader API. In doing so and at a minimum, you need to create a `src/main/resources/META-INF/services/org.apereo.cas.util.LogMessageSummarizer` file with the following contents:

```
org.sso.example.MyLogMessageSummarizer
```

If you need to disable summary mode altogether, the following contents should be used instead in the above file:

```bash
org.apereo.cas.util.logging.DisabledLogMessageSummarizer
```

You may also control the log summarization behavior via the system property `LOG_SUMMARY_ENABLED` set to `false`.
