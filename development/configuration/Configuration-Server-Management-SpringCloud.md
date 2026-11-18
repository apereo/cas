# Configuration Server - Spring Cloud

Source: https://apereo.github.io/cas/development/configuration/Configuration-Server-Management-SpringCloud.html

CAS is able to use an external and central configuration server to obtain state and settings. The configuration server provides a very abstract way for CAS (and all of its other clients) to obtain settings from a variety of sources, such as file system, `git` or `svn` repositories, MongoDb databases, Vault, etc. The beauty of this solution is that to the CAS web application server, it matters not where settings come from and it has no knowledge of the underlying property sources. It talks to the configuration server to locate settings and move on.

> **Configuration Security**
>
> This is a very good strategy to ensure configuration settings are not scattered around various deployment environments leading to a more secure deployment. The configuration server need not be exposed to the outside world, and it can safely and secure be hidden behind firewalls, etc allowing access to only authorized clients such as the CAS server web application.

A full comprehensive guide is provided by the [Spring Cloud project](https://cloud.spring.io/spring-cloud-config/spring-cloud-config.html).

## Spring Cloud Configuration Server Overlay

The configuration server itself, similar to CAS, can be deployed using the [CAS Initializr](https://apereo.github.io/cas/development/installation/WAR-Overlay-Initializr.html).

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-webapp-config-server"
}
```

In addition to the [strategies outlined here](https://apereo.github.io/cas/development/configuration/Configuration-Management.html), the configuration server may load CAS settings and properties via the following order and mechanics:

1. Profile-specific application properties outside of your packaged web application (`application-{profile}.properties|yml`)
2. Profile-specific application properties packaged inside your jar (`application-{profile}.properties|yml`)
3. Application properties outside of your packaged jar (`application.properties|yml`).
4. Application properties packaged inside your jar (`application.properties|yml`).

The configuration and behavior of the configuration server is also controlled by its own `src/main/resources/bootstrap.properties` file. By default, it runs under port `8888` at `/casconfigserver` inside an embedded Apache Tomcat server whose endpoints are protected with basic authentication where the default credentials are `casuser` and an auto-generated password defined in `src/main/resources/application.properties`.

Furthermore, by default it runs under a `native` profile described below.

The following endpoints are secured and exposed by the configuration server:

| Parameter | Description |
| --- | --- |
| `/encrypt` | Accepts a `POST` to encrypt CAS configuration settings. |
| `/decrypt` | Accepts a `POST` to decrypt CAS configuration settings. |
| `/actuator/refresh` | Accepts a `POST` and attempts to refresh the internal state of configuration server. |
| `/actuator/env` | Accepts a `GET` and describes all configuration sources of the configuration server. |
| `/actuator/cas/default` | Describes what the configuration server knows about the `default` settings profile. |
| `/actuator/cas/native` | Describes what the configuration server knows about the `native` settings profile. |

Once you have the configuration server deployed and assuming the credentials used to secure the configuration server match the example below, you can observe the collection of settings via:

```bash
curl -u casuser:Mellon https://config.server.url:8888/casconfigserver/cas/native
```

Assuming actuator endpoints are enabled in the configuration, you can also observe the collection of property sources that provide settings to the configuration server:

```bash
curl -u casuser:Mellon https://config.server.url:8888/casconfigserver/actuator/env
```

> **Actuator Endpoints**
>
> Remember that actuator endpoints typically are prefixed with `/actuator`.

## Actuator Endpoints

The following endpoints are provided by CAS:

`health`

Third-party endpoint · 2 operations · Exposed by default

**GET** `health`: Reports back general health status of the system, produced by various monitors. (no params)

Response

- **Returns**: `HealthDescriptor`
- **Java method**: `health()`
- **Defined in**: `HealthEndpoint`

Example request

```bash
curl -u "casuser:password" \
  -H "Accept: application/json" \
  "https://sso.example.org/cas/actuator/health"
```

**GET** `health/{path}`: Reports back general health status of the system, produced by various monitors. (no params)

Response

- **Returns**: `HealthDescriptor`
- **Java method**: `healthForPath(String...)`
- **Defined in**: `HealthEndpoint`

Example request

```bash
curl -u "casuser:password" \
  -H "Accept: application/json" \
  "https://sso.example.org/cas/actuator/health/{path}"
```

`casConfig`

CAS endpoint · 5 operations · Not exposed by default

base path `/cas/actuator/`

**DELETE** `casConfig`: Remove configuration property. (2 params)

Parameters

| Name | In | Type | Description |
| --- | --- | --- | --- |
| `name` | query | – | The name of the property to remove. Leave blank to remove all properties |
| `propertySource` | query | – | The name of the property source that should be updated |

Response

- **Returns**: `List`
- **Java method**: `CasConfigurationEndpoint$ConfigurationPropertyDeleteRequest)`
- **Defined in**: `CasConfigurationEndpoint`

Example request

```bash
curl -u "casuser:password" \
  -X DELETE \
  -H "Accept: application/json" \
  "https://sso.example.org/cas/actuator/casConfig?name=...&propertySource=..."
```

**POST** `casConfig/decrypt`: Decrypt configuration value. (1 param)

Parameters

| Name | In | Type | Description |
| --- | --- | --- | --- |
| `value`required | query | – | The value to decrypt |

Response

- **Returns**: `ResponseEntity`
- **Produces**: `text/plain`
- **Consumes**: `text/plain`
- **Java method**: `decrypt(String)`
- **Defined in**: `CasConfigurationEndpoint`

Example request

```bash
curl -u "casuser:password" \
  -X POST \
  -H "Content-Type: text/plain" \
  -H "Accept: text/plain" \
  "https://sso.example.org/cas/actuator/casConfig/decrypt?value=..."
```

**POST** `casConfig/encrypt`: Encrypt configuration value. (1 param)

Parameters

| Name | In | Type | Description |
| --- | --- | --- | --- |
| `value`required | query | – | The value to encrypt |

Response

- **Returns**: `ResponseEntity`
- **Produces**: `text/plain`
- **Consumes**: `text/plain`
- **Java method**: `encrypt(String)`
- **Defined in**: `CasConfigurationEndpoint`

Example request

```bash
curl -u "casuser:password" \
  -X POST \
  -H "Content-Type: text/plain" \
  -H "Accept: text/plain" \
  "https://sso.example.org/cas/actuator/casConfig/encrypt?value=..."
```

**POST** `casConfig/retrieve`: Fetch configuration property. (3 params)

Parameters

| Name | In | Type | Description |
| --- | --- | --- | --- |
| `value`required | query | – | The value to match |
| `name`required | query | – | The name (pattern) of the property to fetch |
| `propertySource` | query | – | The name of the property source that should be examined |

Response

- **Returns**: `List`
- **Produces**: `application/json`
- **Consumes**: `application/json, application/x-www-form-urlencoded`
- **Java method**: `CasConfigurationEndpoint$ConfigurationPropertyRetrievalRequest)`
- **Defined in**: `CasConfigurationEndpoint`

Example request

```bash
curl -u "casuser:password" \
  -X POST \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  "https://sso.example.org/cas/actuator/casConfig/retrieve?value=...&name=...&propertySource=..."
```

**POST** `casConfig/update`: Update configuration value. (3 params)

Parameters

| Name | In | Type | Description |
| --- | --- | --- | --- |
| `value`required | query | – | The value to set |
| `name`required | query | – | The name of the property to update |
| `propertySource` | query | – | The name of the property source that should be updated |

Response

- **Returns**: `List`
- **Produces**: `application/json`
- **Consumes**: `application/json`
- **Java method**: `CasConfigurationEndpoint$ConfigurationPropertyUpdateRequest>)`
- **Defined in**: `CasConfigurationEndpoint`

Example request

```bash
curl -u "casuser:password" \
  -X POST \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  "https://sso.example.org/cas/actuator/casConfig/update?value=...&name=...&propertySource=..."
```

**Enable & expose**

Turn the endpoints on and expose them over the web. One entry covers every operation. By default only `info`, `health` and `status` are exposed.

```properties
management.endpoint.health.access=UNRESTRICTED
management.endpoint.casConfig.access=UNRESTRICTED
management.endpoints.web.exposure.include=health,casConfig
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
cas.monitor.endpoints.endpoint.health.access=AUTHENTICATED
cas.monitor.endpoints.endpoint.casConfig.access=AUTHENTICATED
```

*Role*

```properties
cas.monitor.endpoints.endpoint.health.access=ROLE
cas.monitor.endpoints.endpoint.health.required-roles=ADMIN
cas.monitor.endpoints.endpoint.casConfig.access=ROLE
cas.monitor.endpoints.endpoint.casConfig.required-roles=ADMIN
```

*IP address*

```properties
cas.monitor.endpoints.endpoint.health.access=IP_ADDRESS
cas.monitor.endpoints.endpoint.health.required-ip-addresses=127.0.0.1
cas.monitor.endpoints.endpoint.casConfig.access=IP_ADDRESS
cas.monitor.endpoints.endpoint.casConfig.required-ip-addresses=127.0.0.1
```

*Anyone*

```properties
cas.monitor.endpoints.endpoint.health.access=PERMIT
cas.monitor.endpoints.endpoint.casConfig.access=PERMIT
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

**Health**

The `health` endpoint may also be configured to show details via the following conditions:

| URL | Description |
| --- | --- |
| `never` | Never display details of health monitors. |
| `always` | Always display details of health monitors. |
| `when-authorized` | Details are only shown to authorized users. The authorized roles are set through the endpoint settings. |

The results and details of the `health` endpoints are produced by a number of health indicator components that may monitor different systems, such as LDAP connection pools, database connections, etc.

```properties
management.health.refresh.enabled=true
management.health.configServerHealthIndicator.enabled=true
```

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
<Logger name="org.apereo.cas.web.report" level="debug" additivity="false">
    <AppenderRef ref="console" />
    <AppenderRef ref="file" />
</Logger>
<Logger name="org.springframework.boot.health.actuate.endpoint" level="debug" additivity="false">
    <AppenderRef ref="console" />
    <AppenderRef ref="file" />
</Logger>
<Logger name="org.springframework.security" level="debug" additivity="false">
    <AppenderRef ref="console" />
    <AppenderRef ref="file" />
</Logger>
```

The following settings and properties are available from the CAS configuration catalog:

- `spring.cloud.config.server.health.accept-empty` (Third party; type `Boolean`; default `false`)
- `spring.cloud.config.server.health.down-health-status` (Third party; type `String`)
- `spring.cloud.config.server.health.repositories` (Third party; type `Map<String,ConfigServerHealthIndicator.Repository>`)

## Spring Cloud Configuration Server Clients

To let the CAS server web application (or any other client for that matter) talk to the configuration server, the following settings need to be applied to CAS’ own `src/main/resources/bootstrap.properties` file. The properties to configure the CAS server web application as the client of the configuration server must necessarily be read in before the rest of the application’s configuration is read from the configuration server, during the *bootstrap* phase.

The following settings and properties are available from the CAS configuration catalog:

- `spring.cloud.config.allow-override` (Third party; type `Boolean`; default `true`): Flag to indicate that `#isOverrideSystemProperties() systemPropertiesOverride` can be used. Set to false to prevent users from changing the default accidentally. Default true.
- `spring.cloud.config.charset` (Third party; type `Charset`; default `UTF-8`): The charset to read the resource from the config server.
- `spring.cloud.config.discovery.enabled` (Third party; type `Boolean`; default `false`): Flag to indicate that config server discovery is enabled (config server URL will be looked up via discovery).
- `spring.cloud.config.discovery.service-id` (Third party; type `String`; default `configserver`): Service id to locate config server.
- `spring.cloud.config.enabled` (Third party; type `Boolean`; default `true`): Flag to say that remote configuration is enabled. Default true;
- `spring.cloud.config.fail-fast` (Third party; type `Boolean`; default `false`): Flag to indicate that failure to connect to the server is fatal (default false).
- `spring.cloud.config.headers` (Third party; type `Map<String,String>`): Additional headers used to create the client request.
- `spring.cloud.config.initialize-on-context-refresh` (Third party; type `Boolean`; default `false`): Flag to initialize bootstrap configuration on context refresh event. Default false.
- `spring.cloud.config.label` (Third party; type `String`): The label name to use to pull remote configuration properties. The default is set on the server (generally "main" for a git based server).
- `spring.cloud.config.media-type` (Third party; type `String`): The Accept header media type to send to config server.
- `spring.cloud.config.monitor.endpoint.path` (Third party; type `String`; deprecation `WARNING`, replaced by `spring.cloud.config.server.monitor.endpoint.path`)
- `spring.cloud.config.multiple-uri-strategy` (Third party; type `ConfigClientProperties.MultipleUriStrategy`; default `always`): The strategy to use when call to server fails and there are multiple URLs configured on the uri property (default `MultipleUriStrategy#ALWAYS`).
- `spring.cloud.config.name` (Third party; type `String`): Name of application used to fetch remote properties.
- `spring.cloud.config.oauth2.client-registration-id` (Third party; type `String`): Spring Security client registration id (as configured under `spring.security.oauth2.client.registration.<id>`) to use when fetching the access token.
- `spring.cloud.config.oauth2.enabled` (Third party; type `Boolean`; default `false`): Whether to attach an OAuth2 bearer token to outbound config-client requests.
- `spring.cloud.config.override-none` (Third party; type `Boolean`; default `false`): Flag to indicate that when `#setAllowOverride(boolean) allowOverride` is true, external properties should take lowest priority and should not override any existing property sources (including local config files). Default false. This will only have an effect when using config first bootstrap.
- `spring.cloud.config.override-system-properties` (Third party; type `Boolean`; default `true`): Flag to indicate that the external properties should override system properties. Default true.
- `spring.cloud.config.password` (Third party; type `String`): The password to use (HTTP Basic) when contacting the remote server.
- `spring.cloud.config.profile` (Third party; type `String`; default `default`): The default profile to use when fetching remote configuration (comma-separated). Default is "default".
- `spring.cloud.config.request-connect-timeout` (Third party; type `Integer`): timeout on waiting to connect to the Config Server.
- `spring.cloud.config.request-read-timeout` (Third party; type `Integer`): timeout on waiting to read data from the Config Server.
- `spring.cloud.config.resource-client.max-size` (Third party; type `DataSize`; default `1MB`): Maximum size of a file read from the config server; a larger file is rejected instead of being loaded into memory. Zero disables the limit.
- `spring.cloud.config.retry.enabled` (Third party; type `Boolean`; default `false`): Whether to retry failed requests to the Config Server. Only has an effect when spring.cloud.config.fail-fast is enabled.
- `spring.cloud.config.retry.initial-interval` (Third party; type `Long`; default `1000`): Initial retry interval in milliseconds.
- `spring.cloud.config.retry.max-attempts` (Third party; type `Integer`; default `6`): Maximum number of attempts.
- `spring.cloud.config.retry.max-interval` (Third party; type `Long`; default `2000`): Maximum interval for backoff.
- `spring.cloud.config.retry.multiplier` (Third party; type `Double`; default `1.1`): Multiplier for next interval.
- `spring.cloud.config.retry.use-random-policy` (Third party; type `Boolean`; deprecation `WARNING`, no replacement; default `false`): Use a random exponential backoff policy.
- `spring.cloud.config.send-all-labels` (Third party; type `Boolean`; default `false`): If set to true the client will send all labels to the server instead of sending one at a time. Support for this would require a config server version of 4.2.0 or higher.
- `spring.cloud.config.send-state` (Third party; type `Boolean`; default `true`): Flag to indicate whether to send state. Default true.
- `spring.cloud.config.server.accept-empty` (Third party; type `Boolean`; default `true`): Flag to indicate that If HTTP 404 needs to be sent if Application is not Found.
- `spring.cloud.config.server.aws-secretsmanager.default-label` (Third party; type `String`): The default staging label to be used to fetch the secret values. If unset, an active version of the secret will be fetched (AWSCURRENT).
- `spring.cloud.config.server.aws-secretsmanager.endpoint` (Third party; type `String`): The endpoint to be used by AWS Secrets Manager client. This can be used to specify an alternate endpoint for the API requests.
- `spring.cloud.config.server.aws-secretsmanager.ignore-label` (Third party; type `Boolean`; default `false`): Do not set staging label when fetching the secret values.
- `spring.cloud.config.server.aws-secretsmanager.order` (Third party; type `Integer`): The order of the environment repository.
- `spring.cloud.config.server.aws-secretsmanager.origin` (Third party; type `String`; default `aws:secrets:`): Prefix which indicates the origin of the property. Defaults to "aws:secrets:".
- `spring.cloud.config.server.aws-secretsmanager.prefix` (Third party; type `String`): Prefix indicating first level for every property loaded from AWS Secrets Manager. Value must start with a forward slash followed by a valid path segment or be empty. Defaults to "/secret".
- `spring.cloud.config.server.aws-secretsmanager.profile-separator` (Third party; type `String`; default `-`): String that separates profile from the application name.
- `spring.cloud.config.server.aws-secretsmanager.region` (Third party; type `String`): The region to be used by AWS Secrets Manager client.
- `spring.cloud.config.server.awsparamstore.decrypt-values` (Third party; type `Boolean`; default `true`): Flag to indicate the retrieval of all AWS parameters in a hierarchy with their value decrypted. Defaults to "true".
- `spring.cloud.config.server.awsparamstore.endpoint` (Third party; type `String`): The service endpoint to be used by the AWS Parameter Store client.
- `spring.cloud.config.server.awsparamstore.max-results` (Third party; type `Integer`; default `10`): The maximum number of items to return for an AWS Parameter Store API call. Defaults to "10".
- `spring.cloud.config.server.awsparamstore.order` (Third party; type `Integer`): The order of the environment repository.
- `spring.cloud.config.server.awsparamstore.origin` (Third party; type `String`; default `aws:ssm:parameter:`): Prefix indicating the property's origin. Defaults to "aws:ssm:parameter:".
- `spring.cloud.config.server.awsparamstore.prefix` (Third party; type `String`): Prefix indicating first level for every property loaded from the AWS Parameter Store. Value must start with a forward slash followed by one or more valid path segments or be empty. Defaults to "/config".
- `spring.cloud.config.server.awsparamstore.profile-separator` (Third party; type `String`; default `-`): String that separates an appended profile from the context name. Note that an AWS parameter name can only contain dots, dashes and underscores next to alphanumeric characters. Defaults to "-".
- `spring.cloud.config.server.awsparamstore.recursive` (Third party; type `Boolean`; default `true`): Flag to indicate the retrieval of all AWS parameters within a hierarchy. Defaults to "true".
- `spring.cloud.config.server.awsparamstore.region` (Third party; type `String`): The region to be used by the AWS Parameter Store client.
- `spring.cloud.config.server.awss3.bucket` (Third party; type `String`): Name of the S3 bucket that contains config.
- `spring.cloud.config.server.awss3.endpoint` (Third party; type `String`): Adds the ability to override the baseUrl of the s3 client.
- `spring.cloud.config.server.awss3.order` (Third party; type `Integer`)
- `spring.cloud.config.server.awss3.region` (Third party; type `String`): AWS region that contains config.
- `spring.cloud.config.server.awss3.search-paths` (Third party; type `List<String>`): List of directory paths to search for profiles in the bucket. Analogous to `#searchPaths` in Git backend.
- `spring.cloud.config.server.awss3.use-directory-layout` (Third party; type `Boolean`; default `false`): Use application name as intermediate directory. Analogous to `#searchPaths` from Git backend.
- `spring.cloud.config.server.bootstrap` (Third party; type `Boolean`; default `false`): Flag indicating that the config server should initialize its own Environment with properties from the remote repository. Off by default because it delays startup but can be useful when embedding the server in another application.
- `spring.cloud.config.server.credhub.ca-cert-files` (Third party; type `String[]`)
- `spring.cloud.config.server.credhub.connection-timeout` (Third party; type `Duration`)
- `spring.cloud.config.server.credhub.default-label` (Third party; type `String`; default `master`): The default label to be used when is not provided by client applications.
- `spring.cloud.config.server.credhub.oauth2.registration-id` (Third party; type `String`)
- `spring.cloud.config.server.credhub.order` (Third party; type `Integer`)
- `spring.cloud.config.server.credhub.path` (Third party; type `String`): The common base path for credentials in CredHub. It is empty by default.
- `spring.cloud.config.server.credhub.read-timeout` (Third party; type `Duration`)
- `spring.cloud.config.server.credhub.url` (Third party; type `String`)
- `spring.cloud.config.server.default-application-name` (Third party; type `String`; default `application`): Default application name when incoming requests do not have a specific one.
- `spring.cloud.config.server.default-label` (Third party; type `String`): Default repository label when incoming requests do not have a specific label.
- `spring.cloud.config.server.default-profile` (Third party; type `String`; default `default`): Default application profile when incoming requests do not have a specific one.
- `spring.cloud.config.server.enabled` (Third party; type `Boolean`; default `true`): Flag indicating config server is enabled.
- `spring.cloud.config.server.encrypt.enabled` (Third party; type `Boolean`; default `true`): Enable decryption of environment properties before sending to client.
- `spring.cloud.config.server.encrypt.plain-text-encrypt` (Third party; type `Boolean`; default `false`): Enable decryption of environment properties served by plain text endpoint `org.springframework.cloud.config.server.resource.ResourceController`.
- `spring.cloud.config.server.fail-on-composite-error` (Third party; type `Boolean`; default `true`): Flag indicating that if there are any errors reading properties from a subordinate environment repository in a composite environment repository, then the entire composite read should fail. Useful when set to false when a Vault repository is in the composite to allow clients to still read properties from other repositories without providing a valid Vault token. Defaults to true, resulting in a failure on any error.
- `spring.cloud.config.server.gcp-parameter-manager.application-label` (Third party; type `String`; default `application`)
- `spring.cloud.config.server.gcp-parameter-manager.default-label` (Third party; type `String`; default `main`)
- `spring.cloud.config.server.gcp-parameter-manager.location` (Third party; type `String`; default `global`)
- `spring.cloud.config.server.gcp-parameter-manager.order` (Third party; type `Integer`)
- `spring.cloud.config.server.gcp-parameter-manager.profile-label` (Third party; type `String`; default `profile`)
- `spring.cloud.config.server.gcp-parameter-manager.project-id` (Third party; type `String`)
- `spring.cloud.config.server.gcp-secret-manager.allowed-project-ids` (Third party; type `List<String>`): When `token-mandatory` is `false`: only these project IDs may be used with client-supplied `X-Project-ID`. When empty, client-supplied project IDs are rejected. When `token-mandatory` is `true`, this list is ignored for the header; use `X-Config-Token` permission checks instead.
- `spring.cloud.config.server.gcp-secret-manager.application-label` (Third party; type `String`; default `application`)
- `spring.cloud.config.server.gcp-secret-manager.order` (Third party; type `Integer`)
- `spring.cloud.config.server.gcp-secret-manager.profile-label` (Third party; type `String`; default `profile`)
- `spring.cloud.config.server.gcp-secret-manager.project-id` (Third party; type `String`): Used when `X-Project-ID` is absent and the metadata server is unavailable (for example local development). Not validated against `#allowedProjectIds`.
- `spring.cloud.config.server.gcp-secret-manager.service-account` (Third party; type `String`)
- `spring.cloud.config.server.gcp-secret-manager.token-mandatory` (Third party; type `Boolean`; default `true`)
- `spring.cloud.config.server.gcp-secret-manager.version` (Third party; type `Integer`; default `1`)
- `spring.cloud.config.server.gcs.bucket` (Third party; type `String`)
- `spring.cloud.config.server.gcs.order` (Third party; type `Integer`)
- `spring.cloud.config.server.gcs.project-id` (Third party; type `String`)
- `spring.cloud.config.server.gcs.search-paths` (Third party; type `List<String>`)
- `spring.cloud.config.server.gcs.use-directory-layout` (Third party; type `Boolean`; default `false`)
- `spring.cloud.config.server.git.azure.identity.client-id` (Third party; type `String`): Client ID of the Azure managed identity used for Azure DevOps authentication.
- `spring.cloud.config.server.git.azure.identity.managed-identity-enabled` (Third party; type `Boolean`; default `false`): Whether managed identity authentication is enabled for Azure DevOps repositories.
- `spring.cloud.config.server.git.basedir` (Third party; type `File`): Base directory for local working copy of repository.
- `spring.cloud.config.server.git.clone-on-start` (Third party; type `Boolean`; default `false`): Flag to indicate that the repository should be cloned on startup (not on demand). Generally leads to slower startup but faster first query.
- `spring.cloud.config.server.git.clone-submodules` (Third party; type `Boolean`; default `false`): Flag to indicate that the submodules in the repository should be cloned.
- `spring.cloud.config.server.git.continue-on-multiple-label-failure` (Third party; type `Boolean`; default `false`): Flag to indicate whether to continue on multiple label failure. Defaults to false.
- `spring.cloud.config.server.git.default-label` (Third party; type `String`): The default label to be used with the remote repository.
- `spring.cloud.config.server.git.delete-untracked-branches` (Third party; type `Boolean`; default `false`): Flag to indicate that the branch should be deleted locally if it's origin tracked branch was removed.
- `spring.cloud.config.server.git.force-pull` (Third party; type `Boolean`; default `false`): Flag to indicate that the repository should force pull. If true discard any local changes and take from remote repository.
- `spring.cloud.config.server.git.host-key` (Third party; type `String`): Valid SSH host key. Must be set if hostKeyAlgorithm is also set.
- `spring.cloud.config.server.git.host-key-algorithm` (Third party; type `String`): One of ssh-dss, ssh-rsa, ssh-ed25519, ecdsa-sha2-nistp256, ecdsa-sha2-nistp384, or ecdsa-sha2-nistp521. Must be set if hostKey is also set.
- `spring.cloud.config.server.git.ignore-local-ssh-settings` (Third party; type `Boolean`; default `false`): If true, use property-based instead of file-based SSH config.
- `spring.cloud.config.server.git.known-hosts-file` (Third party; type `String`): Location of custom .known_hosts file.
- `spring.cloud.config.server.git.order` (Third party; type `Integer`): The order of the environment repository.
- `spring.cloud.config.server.git.passphrase` (Third party; type `String`): Passphrase for unlocking your ssh private key.
- `spring.cloud.config.server.git.password` (Third party; type `String`): Password for authentication with remote repository.
- `spring.cloud.config.server.git.preferred-authentications` (Third party; type `String`): Override server authentication method order. This should allow for evading login prompts if server has keyboard-interactive authentication before the publickey method.
- `spring.cloud.config.server.git.private-key` (Third party; type `String`): Valid SSH private key. Must be set if ignoreLocalSshSettings is true and Git URI is SSH format.
- `spring.cloud.config.server.git.proxy` (Third party; type `Map<ProxyHostProperties.ProxyForScheme,ProxyHostProperties>`): HTTP proxy configuration.
- `spring.cloud.config.server.git.refresh-rate` (Third party; type `Integer`; default `0`): Time (in seconds) between refresh of the git repository.
- `spring.cloud.config.server.git.repos` (Third party; type `Map<String,MultipleJGitEnvironmentProperties.PatternMatchingJGitEnvironmentProperties>`): Map of repository identifier to location and other properties.
- `spring.cloud.config.server.git.search-paths` (Third party; type `String[]`): Search paths to use within local working copy. By default searches only the root.
- `spring.cloud.config.server.git.skip-ssl-validation` (Third party; type `Boolean`; default `false`): Flag to indicate that SSL certificate validation should be bypassed when communicating with a repository served over an HTTPS connection.
- `spring.cloud.config.server.git.strict-host-key-checking` (Third party; type `Boolean`; default `true`): If false, ignore errors with host key.
- `spring.cloud.config.server.git.timeout` (Third party; type `Integer`; default `5`): Timeout (in seconds) for obtaining HTTP or SSH connection (if applicable), defaults to 5 seconds.
- `spring.cloud.config.server.git.try-master-branch` (Third party; type `Boolean`; default `true`): To maintain compatibility we will try the master branch in addition to main when we try to fetch the default branch.
- `spring.cloud.config.server.git.uri` (Third party; type `String`): URI of remote repository.
- `spring.cloud.config.server.git.username` (Third party; type `String`): Username for authentication with remote repository.
- `spring.cloud.config.server.health.accept-empty` (Third party; type `Boolean`; default `false`)
- `spring.cloud.config.server.health.down-health-status` (Third party; type `String`)
- `spring.cloud.config.server.health.repositories` (Third party; type `Map<String,ConfigServerHealthIndicator.Repository>`)
- `spring.cloud.config.server.ignore-unresolvable-nested-placeholders` (Third party; type `Boolean`; default `false`): Flag indicating that unresolvable nested placeholders should be ignored when resolving placeholders for YAML and JSON responses.
- `spring.cloud.config.server.jdbc.default-label` (Third party; type `String`; default `master`)
- `spring.cloud.config.server.jdbc.enabled` (Third party; type `Boolean`; default `true`): Flag to indicate that JDBC environment repository configuration is enabled.
- `spring.cloud.config.server.jdbc.fail-on-error` (Third party; type `Boolean`; default `true`): Flag to determine how to handle query exceptions.
- `spring.cloud.config.server.jdbc.order` (Third party; type `Integer`)
- `spring.cloud.config.server.jdbc.sql` (Third party; type `String`; default `SELECT "KEY", "VALUE" from PROPERTIES where APPLICATION=? and PROFILE=? and LABEL=?`): SQL used to query database for keys and values.
- `spring.cloud.config.server.jdbc.sql-without-profile` (Third party; type `String`; default `SELECT "KEY", "VALUE" from PROPERTIES where APPLICATION=? and PROFILE is null and LABEL=?`): SQL used to query database for keys and values when profile is null.
- `spring.cloud.config.server.mongodb.collection` (Third party; type `String`; default `properties`): Name of the MongoDB collection to query for configuration properties.
- `spring.cloud.config.server.mongodb.default-label` (Third party; type `String`; default `master`): Default label to use if none is specified.
- `spring.cloud.config.server.mongodb.enabled` (Third party; type `Boolean`; default `true`): Flag to indicate that MongoDB environment repository configuration is enabled.
- `spring.cloud.config.server.mongodb.fail-on-error` (Third party; type `Boolean`; default `true`): Flag to determine how to handle query exceptions.
- `spring.cloud.config.server.mongodb.order` (Third party; type `Integer`): Order of the MongoDB environment repository.
- `spring.cloud.config.server.monitor.bitbucket.enabled` (Third party; type `Boolean`; default `true`)
- `spring.cloud.config.server.monitor.bitbucket.validation-enabled` (Third party; type `Boolean`; default `true`)
- `spring.cloud.config.server.monitor.bitbucket.webhook-secret` (Third party; type `String`)
- `spring.cloud.config.server.monitor.endpoint.path` (Third party; type `String`; default `/monitor`)
- `spring.cloud.config.server.monitor.gitea.enabled` (Third party; type `Boolean`; default `true`)
- `spring.cloud.config.server.monitor.gitea.validation-enabled` (Third party; type `Boolean`; default `true`)
- `spring.cloud.config.server.monitor.gitea.webhook-secret` (Third party; type `String`)
- `spring.cloud.config.server.monitor.gitee.enabled` (Third party; type `Boolean`; default `true`)
- `spring.cloud.config.server.monitor.gitee.validation-enabled` (Third party; type `Boolean`; default `true`)
- `spring.cloud.config.server.monitor.gitee.webhook-secret` (Third party; type `String`)
- `spring.cloud.config.server.monitor.github.enabled` (Third party; type `Boolean`; default `true`)
- `spring.cloud.config.server.monitor.github.validation-enabled` (Third party; type `Boolean`; default `true`)
- `spring.cloud.config.server.monitor.github.webhook-secret` (Third party; type `String`)
- `spring.cloud.config.server.monitor.gitlab.enabled` (Third party; type `Boolean`; default `true`)
- `spring.cloud.config.server.monitor.gitlab.validation-enabled` (Third party; type `Boolean`; default `true`)
- `spring.cloud.config.server.monitor.gitlab.webhook-secret` (Third party; type `String`)
- `spring.cloud.config.server.monitor.gogs.enabled` (Third party; type `Boolean`; default `true`)
- `spring.cloud.config.server.monitor.gogs.validation-enabled` (Third party; type `Boolean`; default `true`)
- `spring.cloud.config.server.monitor.gogs.webhook-secret` (Third party; type `String`)
- `spring.cloud.config.server.monitor.http.endpoints` (Third party; type `Map<String,String>`)
- `spring.cloud.config.server.monitor.ignored-paths` (Third party; type `List<String>`)
- `spring.cloud.config.server.monitor.max-dashes` (Third party; type `Integer`; default `20`): Upper bound on the number of dash-separated segments processed when guessing a service name from a notification path. Prevents a long, heavily dashed path from producing an unbounded number of candidate service names (and refresh events).
- `spring.cloud.config.server.monitor.max-paths` (Third party; type `Integer`; default `20`)
- `spring.cloud.config.server.native.add-label-locations` (Third party; type `Boolean`; default `true`): Flag to determine whether label locations should be added.
- `spring.cloud.config.server.native.default-label` (Third party; type `String`; default `master`)
- `spring.cloud.config.server.native.fail-on-error` (Third party; type `Boolean`; default `false`): Flag to determine how to handle exceptions during decryption (default false).
- `spring.cloud.config.server.native.last-label-wins` (Third party; type `Boolean`; default `true`): Flag to determine whether, when several labels are requested, the last label takes precedence over the earlier ones (default true). If false, the first label takes precedence, as it did before this property was introduced.
- `spring.cloud.config.server.native.order` (Third party; type `Integer`)
- `spring.cloud.config.server.native.search-locations` (Third party; type `String[]`): Locations to search for configuration files. Defaults to the same as a Spring Boot app so [classpath:/,classpath:/config/,file:./,file:./config/].
- `spring.cloud.config.server.native.version` (Third party; type `String`): Version string to be reported for native repository.
- `spring.cloud.config.server.overrides` (Third party; type `Map<String,String>`): Extra map for a property source to be sent to all clients unconditionally.
- `spring.cloud.config.server.prefix` (Third party; type `String`): Prefix for configuration resource paths (default is empty). Useful when embedding in another application when you don't want to change the context path or servlet path.
- `spring.cloud.config.server.redis.order` (Third party; type `Integer`)
- `spring.cloud.config.server.reverse-location-order` (Third party; type `Boolean`; default `false`): By default the location order we use in GenericResourceRepository is the order in which they are listed. Prior to Hoxton.SR11 the order used to be reverse. If this property is set to true then we will reverse the order like it used to be prior to Hoxton.SR11.
- `spring.cloud.config.server.strip-document-from-yaml` (Third party; type `Boolean`; default `true`): Flag to indicate that YAML documents that are text or collections (not a map) should be returned in "native" form.
- `spring.cloud.config.server.svn.basedir` (Third party; type `File`): Base directory for local working copy of repository.
- `spring.cloud.config.server.svn.continue-on-multiple-label-failure` (Third party; type `Boolean`; default `false`): Flag to indicate whether to continue on multiple label failure. Defaults to false.
- `spring.cloud.config.server.svn.default-label` (Third party; type `String`): The default label to be used with the remote repository.
- `spring.cloud.config.server.svn.order` (Third party; type `Integer`): The order of the environment repository.
- `spring.cloud.config.server.svn.passphrase` (Third party; type `String`): Passphrase for unlocking your ssh private key.
- `spring.cloud.config.server.svn.password` (Third party; type `String`): Password for authentication with remote repository.
- `spring.cloud.config.server.svn.search-paths` (Third party; type `String[]`): Search paths to use within local working copy. By default searches only the root.
- `spring.cloud.config.server.svn.strict-host-key-checking` (Third party; type `Boolean`; default `true`): Reject incoming SSH host keys from remote servers not in the known host list.
- `spring.cloud.config.server.svn.uri` (Third party; type `String`): URI of remote repository.
- `spring.cloud.config.server.svn.username` (Third party; type `String`): Username for authentication with remote repository.
- `spring.cloud.config.server.vault.app-role.app-role-path` (Third party; type `String`; default `approle`): Mount path of the AppRole authentication backend.
- `spring.cloud.config.server.vault.app-role.role` (Third party; type `String`): Name of the role, optional, used for pull-mode.
- `spring.cloud.config.server.vault.app-role.role-id` (Third party; type `String`): The RoleId.
- `spring.cloud.config.server.vault.app-role.secret-id` (Third party; type `String`): The SecretId.
- `spring.cloud.config.server.vault.authentication` (Third party; type `VaultEnvironmentProperties.AuthenticationMethod`)
- `spring.cloud.config.server.vault.aws-ec2.aws-ec2-path` (Third party; type `String`; default `aws-ec2`): Mount path of the AWS-EC2 authentication backend.
- `spring.cloud.config.server.vault.aws-ec2.identity-document` (Third party; type `String`; default `http://169.254.169.254/latest/dynamic/instance-identity/pkcs7`): URL of the AWS-EC2 PKCS7 identity document.
- `spring.cloud.config.server.vault.aws-ec2.nonce` (Third party; type `String`): Nonce used for AWS-EC2 authentication. An empty nonce defaults to nonce generation.
- `spring.cloud.config.server.vault.aws-ec2.role` (Third party; type `String`): Name of the role, optional.
- `spring.cloud.config.server.vault.aws-iam.aws-path` (Third party; type `String`; default `aws`): Mount path of the AWS authentication backend.
- `spring.cloud.config.server.vault.aws-iam.endpoint-uri` (Third party; type `URI`): STS server URI. @since 2.2
- `spring.cloud.config.server.vault.aws-iam.role` (Third party; type `String`): Name of the role, optional. Defaults to the friendly IAM name if not set.
- `spring.cloud.config.server.vault.aws-iam.server-name` (Third party; type `String`): Name of the server used to set `X-Vault-AWS-IAM-Server-ID` header in the headers of login requests.
- `spring.cloud.config.server.vault.azure-msi.azure-path` (Third party; type `String`; default `azure`): Mount path of the Azure MSI authentication backend.
- `spring.cloud.config.server.vault.azure-msi.identity-token-service` (Third party; type `String`): URI to the Azure MSI Identity Service.
- `spring.cloud.config.server.vault.azure-msi.metadata-service` (Third party; type `String`): URI to the Azure MSI Metadata Service.
- `spring.cloud.config.server.vault.azure-msi.role` (Third party; type `String`): Name of the role.
- `spring.cloud.config.server.vault.backend` (Third party; type `String`; default `secret`): Vault backend. Defaults to secret.
- `spring.cloud.config.server.vault.default-key` (Third party; type `String`; default `application`): The key in vault shared by all applications. Defaults to application. Set to empty to disable.
- `spring.cloud.config.server.vault.default-label` (Third party; type `String`; default `main`)
- `spring.cloud.config.server.vault.enable-label` (Third party; type `Boolean`; default `false`): Flag to indicate that the repository should use 'label' as well as 'application-name' and 'profile', for vault secrets. By default, the vault secrets are expected to be in 'application-name,profile' path. When this flag enabled, they are expected to be in 'application-name,profile,label' path. To maintain compatibility this flag is not enabled by default.
- `spring.cloud.config.server.vault.full-key-path` (Third party; type `Boolean`; default `false`): Include the full key path in the PropertySource name. Defaults to false
- `spring.cloud.config.server.vault.gcp-gce.gcp-path` (Third party; type `String`; default `gcp`): Mount path of the Kubernetes authentication backend.
- `spring.cloud.config.server.vault.gcp-gce.role` (Third party; type `String`): Name of the role against which the login is being attempted.
- `spring.cloud.config.server.vault.gcp-gce.service-account` (Third party; type `String`): Optional service account id. Using the default id if left unconfigured.
- `spring.cloud.config.server.vault.gcp-iam.credentials.encoded-key` (Third party; type `String`): The base64 encoded contents of an OAuth2 account private key in JSON format.
- `spring.cloud.config.server.vault.gcp-iam.credentials.location` (Third party; type `Resource`): Location of the OAuth2 credentials private key. Since this is a Resource, the private key can be in a multitude of locations, such as a local file system, classpath, URL, etc.
- `spring.cloud.config.server.vault.gcp-iam.gcp-path` (Third party; type `String`; default `gcp`): Mount path of the Kubernetes authentication backend.
- `spring.cloud.config.server.vault.gcp-iam.jwt-validity` (Third party; type `Duration`; default `15m`): Validity of the JWT token.
- `spring.cloud.config.server.vault.gcp-iam.project-id` (Third party; type `String`): Overrides the GCP project Id.
- `spring.cloud.config.server.vault.gcp-iam.role` (Third party; type `String`): Name of the role against which the login is being attempted.
- `spring.cloud.config.server.vault.gcp-iam.service-account-id` (Third party; type `String`): Overrides the GCP service account Id.
- `spring.cloud.config.server.vault.host` (Third party; type `String`; default `127.0.0.1`): Vault host. Defaults to 127.0.0.1.
- `spring.cloud.config.server.vault.kubernetes.kubernetes-path` (Third party; type `String`; default `kubernetes`): Mount path of the Kubernetes authentication backend.
- `spring.cloud.config.server.vault.kubernetes.role` (Third party; type `String`): Name of the role against which the login is being attempted.
- `spring.cloud.config.server.vault.kubernetes.service-account-token-file` (Third party; type `String`; default `/var/run/secrets/kubernetes.io/serviceaccount/token`): Path to the service account token file.
- `spring.cloud.config.server.vault.kv-version` (Third party; type `Integer`; default `1`): Value to indicate which version of Vault kv backend is used. Defaults to 1.
- `spring.cloud.config.server.vault.namespace` (Third party; type `String`): The value of the Vault X-Vault-Namespace header. Defaults to null. This a Vault Enterprise feature only.
- `spring.cloud.config.server.vault.order` (Third party; type `Integer`)
- `spring.cloud.config.server.vault.path-to-key` (Third party; type `String`): KV2 API required "data" after "mount-path". There could be folder/path structure, where the keys/applications are grouped. This property is the path after mount-path, under which application(s) are located (appended after "data") Default value is blank, which means all grouped applications are located right under the mount-path
- `spring.cloud.config.server.vault.pcf.instance-certificate` (Third party; type `Resource`): Path to the instance certificate (PEM). Defaults to `CF_INSTANCE_CERT` env variable.
- `spring.cloud.config.server.vault.pcf.instance-key` (Third party; type `Resource`): Path to the instance key (PEM). Defaults to `CF_INSTANCE_KEY` env variable.
- `spring.cloud.config.server.vault.pcf.pcf-path` (Third party; type `String`; default `pcf`): Mount path of the Kubernetes authentication backend.
- `spring.cloud.config.server.vault.pcf.role` (Third party; type `String`): Name of the role against which the login is being attempted.
- `spring.cloud.config.server.vault.port` (Third party; type `Integer`; default `8200`): Vault port. Defaults to 8200.
- `spring.cloud.config.server.vault.profile-separator` (Third party; type `String`; default `,`): Vault profile separator. Defaults to comma.
- `spring.cloud.config.server.vault.proxy` (Third party; type `Map<ProxyHostProperties.ProxyForScheme,ProxyHostProperties>`): HTTP proxy configuration.
- `spring.cloud.config.server.vault.scheme` (Third party; type `String`; default `http`): Vault scheme. Defaults to http.
- `spring.cloud.config.server.vault.skip-ssl-validation` (Third party; type `Boolean`; default `false`): Flag to indicate that SSL certificate validation should be bypassed when communicating with a repository served over an HTTPS connection.
- `spring.cloud.config.server.vault.ssl.cert-auth-path` (Third party; type `String`; default `cert`): Mount path of the TLS cert authentication backend.
- `spring.cloud.config.server.vault.ssl.key-store` (Third party; type `Resource`): Trust store that holds certificates and private keys.
- `spring.cloud.config.server.vault.ssl.key-store-password` (Third party; type `String`): Password used to access the key store.
- `spring.cloud.config.server.vault.ssl.trust-store` (Third party; type `Resource`): Trust store that holds SSL certificates.
- `spring.cloud.config.server.vault.ssl.trust-store-password` (Third party; type `String`): Password used to access the trust store.
- `spring.cloud.config.server.vault.timeout` (Third party; type `Integer`; default `5`): Timeout (in seconds) for obtaining HTTP connection, defaults to 5 seconds.
- `spring.cloud.config.server.vault.token` (Third party; type `String`): Static vault token. Required if `#authentication` is `TOKEN`.
- `spring.cloud.config.tls` (Third party; type `TlsProperties`): TLS properties.
- `spring.cloud.config.token` (Third party; type `String`): Security Token passed thru to underlying environment repository.
- `spring.cloud.config.uri` (Third party; type `String[]`; default `["http://localhost:8888"]`): The URI of the remote server (default http://localhost:8888).
- `spring.cloud.config.username` (Third party; type `String`): The username to use (HTTP Basic) when contacting the remote server.

**Hibernate & JDBC**

Control global properties that are relevant to Hibernate, when CAS attempts to employ and utilize database resources, connections and queries.

- `cas.jdbc.gen-ddl` (Optional; type `Boolean`; default `true`): Whether to generate DDL after the EntityManagerFactory has been initialized creating/updating all relevant tables.
- `cas.jdbc.physical-table-names` (Optional; type `Map<String,String>`): Indicate a physical table name to be used by the hibernate naming strategy in case table names need to be customized for the specific type of database. The key here indicates the CAS-provided table name and the value is the translate physical name for the database. If a match is not found for the CAS-provided table name, then that name will be used by default.
- `cas.jdbc.show-sql` (Optional; type `Boolean`; default `false`): Whether SQL queries should be displayed in the console/logs.

Remember that configuration server serves property sources from `/{name}/{profile}/{label}` to applications, where the default bindings in the client app are the application name for `name`, the active profiles for `profile` and `master` for `label`.

All of them can be overridden in the Spring Cloud Config client settings listed above. The “label” is useful for rolling back to previous versions of configuration; with the default Config Server implementation it can be a git label, branch name or commit id. Label can also be provided as a comma-separated list, in which case the items in the list are tried on-by-one until one succeeds. This can be useful when working on a feature branch, for instance, when you might want to align the config label with your branch, but make it optional (e.g. a label of `myfeature,develop`).

To lean more about how CAS allows you to reload configuration changes, please [review this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Reload.html).

## Spring Cloud Configuration Server Sources

Various configuration profiles exist to determine how configuration server should retrieve properties and settings.

- [Default](https://apereo.github.io/cas/development/configuration/Configuration-Server-Management-SpringCloud-Default.html)
- [Native](https://apereo.github.io/cas/development/configuration/Configuration-Server-Management-SpringCloud-Native.html)
- [REST](https://apereo.github.io/cas/development/configuration/Configuration-Server-Management-SpringCloud-REST.html)
- [Amazon S3](https://apereo.github.io/cas/development/configuration/Configuration-Server-Management-SpringCloud-AmazonS3.html)
- [Amazon Secret Manager](https://apereo.github.io/cas/development/configuration/Configuration-Server-Management-SpringCloud-AmazonSecretManager.html)
- [Amazon SSM](https://apereo.github.io/cas/development/configuration/Configuration-Server-Management-SpringCloud-AmazonSSM.html)
- [Azure KeyVault](https://apereo.github.io/cas/development/configuration/Configuration-Server-Management-SpringCloud-AzureKeyVault.html)
- [DynamoDb](https://apereo.github.io/cas/development/configuration/Configuration-Server-Management-SpringCloud-DynamoDb.html)
- [Etcd](https://apereo.github.io/cas/development/configuration/Configuration-Server-Management-SpringCloud-Etcd.html)
- [HashiCorp Consul](https://apereo.github.io/cas/development/configuration/Configuration-Server-Management-SpringCloud-HashiCorpConsul.html)
- [HashiCorp Vault](https://apereo.github.io/cas/development/configuration/Configuration-Server-Management-SpringCloud-HashiCorpVault.html)
- [JDBC](https://apereo.github.io/cas/development/configuration/Configuration-Server-Management-SpringCloud-JDBC.html)
- [MongoDb](https://apereo.github.io/cas/development/configuration/Configuration-Server-Management-SpringCloud-MongoDb.html)
- [ZooKeeper](https://apereo.github.io/cas/development/configuration/Configuration-Server-Management-SpringCloud-ZooKeeper.html)
- [GCP Secret Manager](https://apereo.github.io/cas/development/configuration/Configuration-Server-Management-SpringCloud-GCP-SecretManager.html)

The cloud configuration modules provided above may also be used verbatim inside a CAS server overlay. Remember that the primary objective for these modules is to retrieve settings and properties from a source. While they are mostly and primarily useful when activated inside the Spring Cloud Configuration server and can be set to honor profiles and such, they nonetheless may also be used inside a CAS server overlay directly to fetch settings from a source while running in standalone mode. In such scenarios, all sources of configuration regardless of format or syntax will work alongside each other to retrieve settings and you can certainly mix and match as you see fit.

### Spring Cloud Configuration Server Composite Sources

In some scenarios you may wish to pull configuration data from multiple environment repositories. To do this just enable multiple profiles in your configuration server’s application properties or YAML file. If, for example, you want to pull configuration data from a Git repository as well as a SVN repository you would set the following properties for your configuration server.

```yml
spring:
  profiles:
    active: git, svn
  cloud:
    config:
      server:
        svn:
          uri: file:///path/to/svn/repo
          order: 2
        git:
          uri: file:///path/to/git/repo
          order: 1
```

In addition to each repo specifying a URI, you can also specify an `order` property. The `order` property allows you to specify the priority order for all your repositories. The lower the numerical value of the order property the higher priority it will have. The priority order of a repository will help resolve any potential conflicts between repositories that contain values for the same properties.

### Spring Cloud Configuration Server Property Overrides

The configuration server has an “overrides” feature that allows the operator to provide configuration properties to all applications that cannot be accidentally changed by the application using the normal change events and hooks. To declare overrides add a map of name-value pairs to the overrides of the configuration server.

For example:

```yml
spring:
  cloud:
    config:
      server:
        overrides:
          foo: bar
```

This will cause the CAS server (as the client of the configuration server) to read `foo=bar` independent of its own configuration.
