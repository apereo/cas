# Password Management - LDAP

Source: https://apereo.github.io/cas/development/password_management/Password-Management-LDAP.html

The account password and security questions may be stored inside an LDAP server.

LDAP support is enabled by including the following dependencies in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-pm-ldap"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.pm.ldap[0].account-locked-attribute` (Required; type `String`; default `pwdLockout`): Name of LDAP attribute that represents the account locked status. The value of the attribute is typically set to `"true"` if the account is ever updated to indicated a locked status. For Active Directory, this attribute might be called `lockoutTime`.
- `cas.authn.pm.ldap[0].base-dn` (Required; type `String`): Base DN to use. There may be scenarios where different parts of a single LDAP tree could be considered as base-dns. Rather than duplicating the LDAP configuration block for each individual base-dn, each entry can be specified and joined together using a special delimiter character. The user DN is retrieved using the combination of all base-dn and DN resolvers in the order defined. DN resolution should fail if multiple DNs are found. Otherwise the first DN found is returned. Usual syntax is: `subtreeA,dc=example,dc=net|subtreeC,dc=example,dc=net`.
- `cas.authn.pm.ldap[0].bind-credential` (Required; type `String`): The bind credential to use when connecting to LDAP.
- `cas.authn.pm.ldap[0].bind-dn` (Required; type `String`): The bind DN to use when connecting to LDAP. LDAP connection configuration injected into the LDAP connection pool can be initialized with the following parameters: - `bindDn/bindCredential` provided - Use the provided credentials to bind when initializing connections. - `bindDn/bindCredential` set to `*` - Use a fast-bind strategy to initialize the pool. - `bindDn/bindCredential` set to blank - Skip connection initializing; perform operations anonymously. - SASL mechanism provided - Use the given SASL mechanism to bind when initializing connections.
- `cas.authn.pm.ldap[0].ldap-url` (Required; type `String`): The LDAP url to the server. More than one may be specified, separated by space and/or comma.
- `cas.authn.pm.ldap[0].principal-transformation.groovy.location` (Required; type `Resource`): Transform usernames using a Groovy resource. The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.pm.ldap[0].search-filter` (Required; type `String`): User filter to use for searching. Syntax is `cn={user}` or `cn={0}`. You may also provide an external groovy script in the syntax of `file:/path/to/GroovyScript.groovy` to fully build the final filter template dynamically.
- `cas.authn.pm.ldap[0].type` (Required; type `LdapType`; default `AD`): The specific variant of LDAP based on which update operations will be constructed. Accepted values are: * - `AD` - `GENERIC` - `FreeIPA` - `EDirectory`
- `cas.authn.pm.ldap[0].username-attribute` (Required; type `String`; default `uid`): Username attribute required by LDAP.
- `cas.authn.pm.ldap` (Optional; type `List<LdapPasswordManagementProperties>`): Manage account passwords in LDAP.
- `cas.authn.pm.ldap[0].account-unlocked-attribute-values` (Optional; type `String`; default `[FALSE]`): When CAS is about to unlock the user account, it will use the `#accountLockedAttribute` setting to locate the appropriate attribute for the user entry. This attribute will then be assigned the value(s) defined here to unlock the account. For Active Directory and in scenarios where `#accountLockedAttribute` is set to `lockoutTime`, this value might be set to zero. A value of zero means that the account is not currently locked out. Note that the value defined here may be treated as case sensitive by the LDAP server.
- `cas.authn.pm.ldap[0].allow-multiple-dns` (Optional; type `boolean`): Whether search/query results are allowed to match on multiple DNs, or whether a single unique DN is expected for the result.
- `cas.authn.pm.ldap[0].allow-multiple-entries` (Optional; type `boolean`): Set if multiple Entries are allowed.
- `cas.authn.pm.ldap[0].binary-attributes` (Optional; type `List<String>`): Indicate the collection of attributes that are to be tagged and processed as binary attributes by the underlying search resolver.
- `cas.authn.pm.ldap[0].block-wait-time` (Optional; type `String`): The length of time the pool will block. By default the pool will block indefinitely and there is no guarantee that waiting threads will be serviced in the order in which they made their request. This option should be used with a blocking connection pool when you need to control the exact number of connections that can be created
- `cas.authn.pm.ldap[0].connect-timeout` (Optional; type `String`): Sets the maximum amount of time that connects will block.
- `cas.authn.pm.ldap[0].connection-strategy` (Optional; type `String`): If multiple URLs are provided as the ldapURL this describes how each URL will be processed. - `ACTIVE_PASSIVE` First LDAP will be used for every request unless it fails and then the next shall be used. - `ROUND_ROBIN` For each new connection the next url in the list will be used. - `RANDOM` For each new connection a random LDAP url will be selected. - `DNS_SRV` LDAP urls based on DNS SRV records of the configured/given LDAP url will be used.
- `cas.authn.pm.ldap[0].disable-pooling` (Optional; type `boolean`): Whether to use a pooled connection factory in components.
- `cas.authn.pm.ldap[0].fail-fast` (Optional; type `boolean`): Attempt to populate the connection pool early on startup and fail quickly if something goes wrong.
- `cas.authn.pm.ldap[0].follow-referrals` (Optional; type `boolean`): Set if search referrals should be followed.
- `cas.authn.pm.ldap[0].hostname-verifier` (Optional; type `AbstractLdapProperties.LdapHostnameVerifierOptions`): Hostname verification options. Available values are as follows: - `DEFAULT`: Default option, forcing verification. - `ANY`: Skip hostname verification and allow all.
- `cas.authn.pm.ldap[0].idle-time` (Optional; type `String`): Removes connections from the pool based on how long they have been idle in the available queue. Prunes connections that have been idle for more than the indicated amount.
- `cas.authn.pm.ldap[0].keystore` (Optional; type `String`): Path to the keystore used for SSL connections. Typically contains SSL certificates for the LDAP server.
- `cas.authn.pm.ldap[0].keystore-password` (Optional; type `String`): Keystore password.
- `cas.authn.pm.ldap[0].keystore-type` (Optional; type `String`): The type of keystore. `PKCS12` or `JKS`. If left blank, defaults to the default keystore type indicated by the underlying Java platform.
- `cas.authn.pm.ldap[0].max-pool-size` (Optional; type `int`): Maximum LDAP connection pool size which the pool can use to grow.
- `cas.authn.pm.ldap[0].min-pool-size` (Optional; type `int`): Minimum LDAP connection pool size. Size the pool should be initialized to and pruned to
- `cas.authn.pm.ldap[0].name` (Optional; type `String`): Name of the LDAP handler.
- `cas.authn.pm.ldap[0].page-size` (Optional; type `int`): Request that the server return results in batches of a specific size. See [RFC 2696](http://www.ietf.org/rfc/rfc2696.txt). This control is often used to work around server result size limits. A negative/zero value disables paged requests.
- `cas.authn.pm.ldap[0].pool-passivator` (Optional; type `String`): You may receive unexpected LDAP failures, when CAS is configured to authenticate using `DIRECT` or `AUTHENTICATED` types and LDAP is locked down to not allow anonymous binds/searches. Every second attempt with a given LDAP connection from the pool would fail if it was on the same connection as a failed login attempt, and the regular connection validator would similarly fail. When a connection is returned back to a pool, it still may contain the principal and credentials from the previous attempt. Before the next bind attempt using that connection, the validator tries to validate the connection again but fails because it’s no longer trying with the configured bind credentials but with whatever user DN was used in the previous step. Given the validation failure, the connection is closed and CAS would deny access by default. Passivators attempt to reconnect to LDAP with the configured bind credentials, effectively resetting the connection to what it should be after each bind request. Furthermore if you are seeing errors in the logs that resemble a 'Operation exception encountered, reopening connection' type of message, this usually is an indication that the connection pool’s validation timeout established and created by CAS is greater than the timeout configured in the LDAP server, or more likely, in the load balancer in front of the LDAP servers. You can adjust the LDAP server session’s timeout for connections, or you can teach CAS to use a validity period that is equal or less than the LDAP server session’s timeout. Accepted values are: - `NONE`: No passivation takes place. - `BIND`: The default behavior which passivates a connection by performing a bind operation on it. This option requires the availability of bind credentials when establishing connections to LDAP.
- `cas.authn.pm.ldap[0].principal-transformation.blocking-pattern` (Optional; type `String`): A regular expression that will be used against the username to match for blocking/forbidden values. If a match is found, an exception will be thrown and principal transformation will fail.
- `cas.authn.pm.ldap[0].principal-transformation.case-conversion` (Optional; type `PrincipalTransformationProperties.CaseConversion`; default `NONE`): Indicate whether the principal identifier should be transformed into upper-case, lower-case, etc. Available values are as follows: - `NONE`: No conversion. - `LOWERCASE`: Lowercase conversion. - `UPPERCASE`: Uppercase conversion.
- `cas.authn.pm.ldap[0].principal-transformation.pattern` (Optional; type `String`): A regular expression that will be used against the provided username for username extractions. On a successful match, the first matched group in the pattern will be used as the extracted username.
- `cas.authn.pm.ldap[0].principal-transformation.prefix` (Optional; type `String`): Prefix to add to the principal id prior to authentication.
- `cas.authn.pm.ldap[0].principal-transformation.suffix` (Optional; type `String`): Suffix to add to the principal id prior to authentication.
- `cas.authn.pm.ldap[0].prune-period` (Optional; type `String`): Removes connections from the pool based on how long they have been idle in the available queue. Run the pruning process at the indicated interval.
- `cas.authn.pm.ldap[0].response-timeout` (Optional; type `String`): Duration of time to wait for responses.
- `cas.authn.pm.ldap[0].sasl-authorization-id` (Optional; type `String`): SASL authorization id.
- `cas.authn.pm.ldap[0].sasl-mechanism` (Optional; type `String`): The SASL mechanism.
- `cas.authn.pm.ldap[0].sasl-mutual-auth` (Optional; type `Boolean`): Whether SASL mutual authentication is enabled.
- `cas.authn.pm.ldap[0].sasl-quality-of-protection` (Optional; type `String`): SASL quality of protected.
- `cas.authn.pm.ldap[0].sasl-realm` (Optional; type `String`): The SASL realm.
- `cas.authn.pm.ldap[0].sasl-security-strength` (Optional; type `String`): SASL security strength.
- `cas.authn.pm.ldap[0].search-entry-handlers.case-change.attribute-name-case-change` (Optional; type `String`): The Attribute name case change.
- `cas.authn.pm.ldap[0].search-entry-handlers.case-change.attribute-names` (Optional; type `List<String>`): The Attribute names.
- `cas.authn.pm.ldap[0].search-entry-handlers.case-change.attribute-value-case-change` (Optional; type `String`): The Attribute value case change.
- `cas.authn.pm.ldap[0].search-entry-handlers.case-change.dn-case-change` (Optional; type `String`): The Dn case change.
- `cas.authn.pm.ldap[0].search-entry-handlers.dn-attribute.add-if-exists` (Optional; type `boolean`): The Add if exists.
- `cas.authn.pm.ldap[0].search-entry-handlers.dn-attribute.dn-attribute-name` (Optional; type `String`; default `entryDN`): The Dn attribute name.
- `cas.authn.pm.ldap[0].search-entry-handlers.merge-attribute.attribute-names` (Optional; type `List<String>`): The Attribute names.
- `cas.authn.pm.ldap[0].search-entry-handlers.merge-attribute.merge-attribute-name` (Optional; type `String`): The Merge attribute name.
- `cas.authn.pm.ldap[0].search-entry-handlers.primary-group-id.base-dn` (Optional; type `String`): The Base dn.
- `cas.authn.pm.ldap[0].search-entry-handlers.primary-group-id.group-filter` (Optional; type `String`; default `(&(objectClass=group)(objectSid={0}))`): The Group filter.
- `cas.authn.pm.ldap[0].search-entry-handlers.recursive.merge-attributes` (Optional; type `List<String>`): The Merge attributes.
- `cas.authn.pm.ldap[0].search-entry-handlers.recursive.search-attribute` (Optional; type `String`): The Search attribute.
- `cas.authn.pm.ldap[0].search-entry-handlers.search-referral.limit` (Optional; type `int`; default `10`): The default referral limit.
- `cas.authn.pm.ldap[0].search-entry-handlers.search-result.limit` (Optional; type `int`; default `10`): The default referral limit.
- `cas.authn.pm.ldap[0].search-entry-handlers.type` (Optional; type `LdapSearchEntryHandlersProperties.SearchEntryHandlerTypes`): The type of search entry handler to choose. Available values are as follows: - `FOLLOW_SEARCH_REFERRAL`: Provides handling of an ldap referral for search operations. - `FOLLOW_SEARCH_RESULT_REFERENCE`: Provides handling of an ldap continuation reference for search operations. - `ACTIVE_DIRECTORY`: Process the entry results fetched from active directory and check for account status controls for disabled/expired accounts, etc. - `OBJECT_GUID`: Object guid search entry handler. Handles the `objectGUID` attribute fetching and conversion. - `OBJECT_SID`: Object sid search entry handler. Handles the `objectSid` attribute fetching and conversion. - `CASE_CHANGE`: Case change search entry handler. Provides the ability to modify the case of search entry DNs, attribute names, and attribute values. - `DN_ATTRIBUTE_ENTRY`: DN attribute entry handler. Adds the entry DN as an attribute to the result set. Provides a client side implementation of RFC 5020. - `MERGE`: Merge search entry handler. Merges the values of one or more attributes into a single attribute. - `PRIMARY_GROUP`: Primary group search handler. Constructs the primary group SID and then searches for that group and puts it's DN in the `memberOf` attribute of the original search entry. - `RANGE_ENTRY`: Range entry search handler. Rewrites attributes returned from Active Directory to include all values by performing additional searches. - `RECURSIVE_ENTRY`: Recursive entry search handler. This recursively searches based on a supplied attribute and merges those results into the original entry. - `MERGE_ENTRIES`: Merge entries handler. Merges the values of one or more attributes in all entries into a single attribute. The merged attribute may or may not already exist on the entry. If it does exist it's existing values will remain intact.
- `cas.authn.pm.ldap[0].search-entry-handlers[0].case-change.attribute-name-case-change` (Optional; type `String`): The Attribute name case change.
- `cas.authn.pm.ldap[0].search-entry-handlers[0].case-change.attribute-names` (Optional; type `List<String>`): The Attribute names.
- `cas.authn.pm.ldap[0].search-entry-handlers[0].case-change.attribute-value-case-change` (Optional; type `String`): The Attribute value case change.
- `cas.authn.pm.ldap[0].search-entry-handlers[0].case-change.dn-case-change` (Optional; type `String`): The Dn case change.
- `cas.authn.pm.ldap[0].search-entry-handlers[0].dn-attribute.add-if-exists` (Optional; type `boolean`): The Add if exists.
- `cas.authn.pm.ldap[0].search-entry-handlers[0].dn-attribute.dn-attribute-name` (Optional; type `String`; default `entryDN`): The Dn attribute name.
- `cas.authn.pm.ldap[0].search-entry-handlers[0].merge-attribute.attribute-names` (Optional; type `List<String>`): The Attribute names.
- `cas.authn.pm.ldap[0].search-entry-handlers[0].merge-attribute.merge-attribute-name` (Optional; type `String`): The Merge attribute name.
- `cas.authn.pm.ldap[0].search-entry-handlers[0].primary-group-id.base-dn` (Optional; type `String`): The Base dn.
- `cas.authn.pm.ldap[0].search-entry-handlers[0].primary-group-id.group-filter` (Optional; type `String`; default `(&(objectClass=group)(objectSid={0}))`): The Group filter.
- `cas.authn.pm.ldap[0].search-entry-handlers[0].recursive.merge-attributes` (Optional; type `List<String>`): The Merge attributes.
- `cas.authn.pm.ldap[0].search-entry-handlers[0].recursive.search-attribute` (Optional; type `String`): The Search attribute.
- `cas.authn.pm.ldap[0].search-entry-handlers[0].search-referral.limit` (Optional; type `int`; default `10`): The default referral limit.
- `cas.authn.pm.ldap[0].search-entry-handlers[0].search-result.limit` (Optional; type `int`; default `10`): The default referral limit.
- `cas.authn.pm.ldap[0].search-entry-handlers[0].type` (Optional; type `LdapSearchEntryHandlersProperties.SearchEntryHandlerTypes`): The type of search entry handler to choose. Available values are as follows: - `FOLLOW_SEARCH_REFERRAL`: Provides handling of an ldap referral for search operations. - `FOLLOW_SEARCH_RESULT_REFERENCE`: Provides handling of an ldap continuation reference for search operations. - `ACTIVE_DIRECTORY`: Process the entry results fetched from active directory and check for account status controls for disabled/expired accounts, etc. - `OBJECT_GUID`: Object guid search entry handler. Handles the `objectGUID` attribute fetching and conversion. - `OBJECT_SID`: Object sid search entry handler. Handles the `objectSid` attribute fetching and conversion. - `CASE_CHANGE`: Case change search entry handler. Provides the ability to modify the case of search entry DNs, attribute names, and attribute values. - `DN_ATTRIBUTE_ENTRY`: DN attribute entry handler. Adds the entry DN as an attribute to the result set. Provides a client side implementation of RFC 5020. - `MERGE`: Merge search entry handler. Merges the values of one or more attributes into a single attribute. - `PRIMARY_GROUP`: Primary group search handler. Constructs the primary group SID and then searches for that group and puts it's DN in the `memberOf` attribute of the original search entry. - `RANGE_ENTRY`: Range entry search handler. Rewrites attributes returned from Active Directory to include all values by performing additional searches. - `RECURSIVE_ENTRY`: Recursive entry search handler. This recursively searches based on a supplied attribute and merges those results into the original entry. - `MERGE_ENTRIES`: Merge entries handler. Merges the values of one or more attributes in all entries into a single attribute. The merged attribute may or may not already exist on the entry. If it does exist it's existing values will remain intact.
- `cas.authn.pm.ldap[0].security-questions-attributes` (Optional; type `Map<String, String>`): Collection of attribute names that indicate security questions answers. This is done via a key-value structure where the key is the attribute name for the security question and the value is the attribute name for the answer linked to the question.
- `cas.authn.pm.ldap[0].subtree-search` (Optional; type `boolean`): Whether subtree searching is allowed.
- `cas.authn.pm.ldap[0].trust-certificates` (Optional; type `String`): Path of the trust certificates to use for the SSL connection. Ignores keystore-related settings when activated and used.
- `cas.authn.pm.ldap[0].trust-manager` (Optional; type `String`): Trust Manager options. Trust managers are responsible for managing the trust material that is used when making LDAP trust decisions, and for deciding whether credentials presented by a peer should be accepted. Accepted values are: - `DEFAULT`: Enable and force the default JVM trust managers. - `ANY`: Trust any client or server.
- `cas.authn.pm.ldap[0].trust-store` (Optional; type `String`): Path to the keystore used to determine which certificates or certificate authorities should be trusted. Used when connecting to an LDAP server via LDAPS or startTLS connection. If left blank, the default truststore for the Java runtime is used.
- `cas.authn.pm.ldap[0].trust-store-password` (Optional; type `String`): Password needed to open the truststore.
- `cas.authn.pm.ldap[0].trust-store-type` (Optional; type `String`): The type of trust keystore that determines which certificates or certificate authorities are trusted. Types depend on underlying java platform, typically `PKCS12` or `JKS`. If left blank, defaults to the default keystore type indicated by the underlying Java platform.
- `cas.authn.pm.ldap[0].use-start-tls` (Optional; type `boolean`): Whether TLS should be used and enabled when establishing the connection.
- `cas.authn.pm.ldap[0].validate-on-checkout` (Optional; type `boolean`): Whether connections should be validated when loaned out from the pool.
- `cas.authn.pm.ldap[0].validate-period` (Optional; type `String`): Period at which pool should be validated.
- `cas.authn.pm.ldap[0].validate-periodically` (Optional; type `boolean`): Whether connections should be validated periodically when the pool is idle.
- `cas.authn.pm.ldap[0].validate-timeout` (Optional; type `String`): Period at which validation operations may time out.
- `cas.authn.pm.ldap[0].validator.attribute-name` (Optional; type `String`; default `objectClass`): Attribute name to use for the compare validator.
- `cas.authn.pm.ldap[0].validator.attribute-value` (Optional; type `String`; default `top`): Attribute values to use for the compare validator.
- `cas.authn.pm.ldap[0].validator.base-dn` (Optional; type `String`): Base DN to use for the search request of the search validator.
- `cas.authn.pm.ldap[0].validator.dn` (Optional; type `String`): DN to compare to use for the compare validator.
- `cas.authn.pm.ldap[0].validator.scope` (Optional; type `String`; default `OBJECT`): Search scope to use for the search request of the search validator.
- `cas.authn.pm.ldap[0].validator.search-filter` (Optional; type `String`; default `(objectClass=*)`): Search filter to use for the search request of the search validator.
- `cas.authn.pm.ldap[0].validator.type` (Optional; type `String`; default `search`): Determine the LDAP validator type. The following LDAP validators can be used to test connection health status: - `search`: Validates a connection is healthy by performing a search operation. Validation is considered successful if the search result size is greater than zero. - `none`: No validation takes place. - `compare`: Validates a connection is healthy by performing a compare operation.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

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

**Principal transformation**

Authentication handlers that generally deal with username-password credentials can be configured to transform the user id prior to executing the authentication sequence. Each authentication strategy in CAS provides settings to properly transform the principal. Refer to the relevant settings for the authentication strategy at hand to learn more.

Authentication handlers as part of principal transformation may also be provided a path to a Groovy script to transform the provided username. The outline of the script may take on the following form:

```groovy
String run(final Object... args) {
    def (providedUsername,logger) = args
    return providedUsername.concat("SomethingElse")
}
```

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development//integration/Apache-Groovy-Scripting.html).
