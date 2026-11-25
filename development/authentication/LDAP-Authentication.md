# LDAP Authentication

Source: https://apereo.github.io/cas/development/authentication/LDAP-Authentication.html

LDAP integration is enabled by including the following dependency in the overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-ldap"
}
```

## Configuration

CAS authenticates a username/password against an LDAP directory such as Active Directory or OpenLDAP. There are numerous directory architectures and we provide configuration for four common cases.

Note that CAS will automatically create the appropriate components internally based on the settings specified below. If you wish to authenticate against more than one LDAP server, increment the index and specify the settings for the next LDAP server.

**Note:** Attributes retrieved as part of LDAP authentication are merged with all attributes retrieved from other attribute repository sources, if any. Attributes retrieved directly as part of LDAP authentication trump all other attributes.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.ldap[0].base-dn` (Required; type `String`): Base DN to use. There may be scenarios where different parts of a single LDAP tree could be considered as base-dns. Rather than duplicating the LDAP configuration block for each individual base-dn, each entry can be specified and joined together using a special delimiter character. The user DN is retrieved using the combination of all base-dn and DN resolvers in the order defined. DN resolution should fail if multiple DNs are found. Otherwise the first DN found is returned. Usual syntax is: `subtreeA,dc=example,dc=net|subtreeC,dc=example,dc=net`.
- `cas.authn.ldap[0].bind-credential` (Required; type `String`): The bind credential to use when connecting to LDAP.
- `cas.authn.ldap[0].bind-dn` (Required; type `String`): The bind DN to use when connecting to LDAP. LDAP connection configuration injected into the LDAP connection pool can be initialized with the following parameters: - `bindDn/bindCredential` provided - Use the provided credentials to bind when initializing connections. - `bindDn/bindCredential` set to `*` - Use a fast-bind strategy to initialize the pool. - `bindDn/bindCredential` set to blank - Skip connection initializing; perform operations anonymously. - SASL mechanism provided - Use the given SASL mechanism to bind when initializing connections.
- `cas.authn.ldap[0].ldap-url` (Required; type `String`): The LDAP url to the server. More than one may be specified, separated by space and/or comma.
- `cas.authn.ldap[0].password-encoder.encoding-algorithm` (Required; type `String`): The encoding algorithm to use such as `MD5`. Relevant when the type used is `DEFAULT` or `GLIBC_CRYPT`. When used with `PasswordEncoderTypes#PBKDF2`, it should be one of `PBKDF2WithHmacSHA1`, `PBKDF2WithHmacSHA256` or `PBKDF2WithHmacSHA512`.
- `cas.authn.ldap[0].password-encoder.type` (Required; type `String`; default `NONE`): Define the password encoder type to use. Type may be specified as blank or `NONE` to disable password encoding. It may also refer to a fully-qualified class name that implements the Spring Security's `PasswordEncoder` interface if you wish you define your own encoder. The following types may be used: - `NONE`: No password encoding (i.e. plain-text) takes place. - `DEFAULT`: Use the `DefaultPasswordEncoder` of CAS. For message-digest algorithms via `character-encoding` and `encoding-algorithm`. - `BCRYPT`: Use the `BCryptPasswordEncoder` based on the strength provided and an optional secret. - `SCRYPT`: Use the `SCryptPasswordEncoder`. - `PBKDF2`: Use the `Pbkdf2PasswordEncoder` based on the strength provided and an optional secret. - `STANDARD`: Use the `StandardPasswordEncoder` based on the secret provided. - `SSHA`: Use the `LdapShaPasswordEncoder` supports Ldap SHA and SSHA (salted-SHA). The values are base-64 encoded and have the label {SHA} or {SSHA} prepended to the encoded hash. - `GLIBC_CRYPT`: Use the `GlibcCryptPasswordEncoder` based on the `encoding-algorithm`, strength provided and an optional secret. - `org.example.MyEncoder`: An implementation of `PasswordEncoder` of your own choosing. - `file:///path/to/script.groovy`: Path to a Groovy script charged with handling password encoding operations.
- `cas.authn.ldap[0].password-policy.groovy.location` (Required; type `Resource`): Handle password policy via Groovy script. The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.ldap[0].principal-transformation.groovy.location` (Required; type `Resource`): Transform usernames using a Groovy resource. The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.ldap[0].search-filter` (Required; type `String`): User filter to use for searching. Syntax is `cn={user}` or `cn={0}`. You may also provide an external groovy script in the syntax of `file:/path/to/GroovyScript.groovy` to fully build the final filter template dynamically.
- `cas.authn.ldap[0].type` (Required; type `AbstractLdapAuthenticationProperties.AuthenticationTypes`): The authentication type. - `AD` - Users authenticate with `sAMAccountName`. - `AUTHENTICATED` - Manager bind/search type of authentication. If {@code} principalAttributePassword} is empty then a user simple bind is done to validate credentials. Otherwise the given attribute is compared with the given `principalAttributePassword` using the `SHA` encrypted value of it. - `ANONYMOUS`: Similar semantics as `AUTHENTICATED` except no `bindDn` and `bindCredential` may be specified to initialize the connection. If `principalAttributePassword` is empty then a user simple bind is done to validate credentials. Otherwise the given attribute is compared with the given `principalAttributePassword` using the `SHA` encrypted value of it. - DIRECT: Direct Bind - Compute user DN from format string and perform simple bind. This is relevant when no search is required to compute the DN needed for a bind operation. Use cases for this type are: 1) All users are under a single branch in the directory, `e.g. ou=Users,dc=example,dc=org.` 2) The username provided on the CAS login form is part of the DN, e.g. `uid=%s,ou=Users,dc=example,dc=org`. Available values are as follows: - `AD`: Active Directory. - `AUTHENTICATED`: Authenticated Search. - `DIRECT`: Direct Bind. - `ANONYMOUS`: Anonymous Search.
- `cas.authn.ldap` (Optional; type `List<LdapAuthenticationProperties>`): Collection of settings related to LDAP authentication. These settings are required to be indexed (i.e. ldap[0].xyz).
- `cas.authn.ldap[0].additional-attributes` (Optional; type `List<String>`): List of additional attributes to retrieve, if any.
- `cas.authn.ldap[0].allow-missing-principal-attribute-value` (Optional; type `boolean`; default `true`): Flag to indicate whether CAS should block authentication if a specific/configured principal id attribute is not found.
- `cas.authn.ldap[0].allow-multiple-dns` (Optional; type `boolean`): Whether search/query results are allowed to match on multiple DNs, or whether a single unique DN is expected for the result.
- `cas.authn.ldap[0].allow-multiple-entries` (Optional; type `boolean`): Set if multiple Entries are allowed.
- `cas.authn.ldap[0].allow-multiple-principal-attribute-values` (Optional; type `boolean`): Sets a flag that determines whether multiple values are allowed for the `#principalAttributeId`. This flag only has an effect if `#principalAttributeId` is configured. If multiple values are detected when the flag is false, the first value is used and a warning is logged. If multiple values are detected when the flag is true, an exception is raised.
- `cas.authn.ldap[0].binary-attributes` (Optional; type `List<String>`): Indicate the collection of attributes that are to be tagged and processed as binary attributes by the underlying search resolver.
- `cas.authn.ldap[0].block-wait-time` (Optional; type `String`): The length of time the pool will block. By default the pool will block indefinitely and there is no guarantee that waiting threads will be serviced in the order in which they made their request. This option should be used with a blocking connection pool when you need to control the exact number of connections that can be created
- `cas.authn.ldap[0].collect-dn-attribute` (Optional; type `boolean`): When entry DN should be called as an attribute and stored into the principal.
- `cas.authn.ldap[0].connect-timeout` (Optional; type `String`): Sets the maximum amount of time that connects will block.
- `cas.authn.ldap[0].connection-strategy` (Optional; type `String`): If multiple URLs are provided as the ldapURL this describes how each URL will be processed. - `ACTIVE_PASSIVE` First LDAP will be used for every request unless it fails and then the next shall be used. - `ROUND_ROBIN` For each new connection the next url in the list will be used. - `RANDOM` For each new connection a random LDAP url will be selected. - `DNS_SRV` LDAP urls based on DNS SRV records of the configured/given LDAP url will be used.
- `cas.authn.ldap[0].credential-criteria` (Optional; type `String`): A number of authentication handlers are allowed to determine whether they can operate on the provided credential and as such lend themselves to be tried and tested during the authentication handler selection phase. The credential criteria may be one of the following options: - 1) A regular expression pattern that is tested against the credential identifier. - 2) A fully qualified class name of your own design that implements `Predicate`. - 3) Path to an external Groovy script that implements the same interface.
- `cas.authn.ldap[0].deref-aliases` (Optional; type `String`): Define how aliases are de-referenced. Accepted values are: - `NEVER` - `SEARCHING`: dereference when searching the entries beneath the starting point but not when searching for the starting entry. - `FINDING`: dereference when searching for the starting entry but not when searching the entries beneath the starting point. - `ALWAYS`: dereference when searching for the starting entry and when searching the entries beneath the starting point.
- `cas.authn.ldap[0].disable-pooling` (Optional; type `boolean`): Whether to use a pooled connection factory in components.
- `cas.authn.ldap[0].dn-format` (Optional; type `String`): Specify the dn format accepted by the AD authenticator, etc. Example format might be `uid=%s,ou=people,dc=example,dc=org`.
- `cas.authn.ldap[0].enhance-with-entry-resolver` (Optional; type `boolean`): Whether specific search entry resolvers need to be set on the authenticator, or the default should be used.
- `cas.authn.ldap[0].fail-fast` (Optional; type `boolean`): Attempt to populate the connection pool early on startup and fail quickly if something goes wrong.
- `cas.authn.ldap[0].follow-referrals` (Optional; type `boolean`): Set if search referrals should be followed.
- `cas.authn.ldap[0].hostname-verifier` (Optional; type `AbstractLdapProperties.LdapHostnameVerifierOptions`): Hostname verification options. Available values are as follows: - `DEFAULT`: Default option, forcing verification. - `ANY`: Skip hostname verification and allow all.
- `cas.authn.ldap[0].idle-time` (Optional; type `String`): Removes connections from the pool based on how long they have been idle in the available queue. Prunes connections that have been idle for more than the indicated amount.
- `cas.authn.ldap[0].keystore` (Optional; type `String`): Path to the keystore used for SSL connections. Typically contains SSL certificates for the LDAP server.
- `cas.authn.ldap[0].keystore-password` (Optional; type `String`): Keystore password.
- `cas.authn.ldap[0].keystore-type` (Optional; type `String`): The type of keystore. `PKCS12` or `JKS`. If left blank, defaults to the default keystore type indicated by the underlying Java platform.
- `cas.authn.ldap[0].max-pool-size` (Optional; type `int`): Maximum LDAP connection pool size which the pool can use to grow.
- `cas.authn.ldap[0].min-pool-size` (Optional; type `int`): Minimum LDAP connection pool size. Size the pool should be initialized to and pruned to
- `cas.authn.ldap[0].name` (Optional; type `String`): Name of the LDAP handler.
- `cas.authn.ldap[0].order` (Optional; type `Integer`): Order of the authentication handler in the chain.
- `cas.authn.ldap[0].page-size` (Optional; type `int`): Request that the server return results in batches of a specific size. See [RFC 2696](http://www.ietf.org/rfc/rfc2696.txt). This control is often used to work around server result size limits. A negative/zero value disables paged requests.
- `cas.authn.ldap[0].password-encoder.character-encoding` (Optional; type `String`; default `UTF-8`): The encoding algorithm to use such as 'UTF-8'. Relevant when the type used is `DEFAULT`.
- `cas.authn.ldap[0].password-encoder.hash-length` (Optional; type `int`; default `16`): When used by `PasswordEncoderTypes#ARGON2`, it indicates the hash strength/length.
- `cas.authn.ldap[0].password-encoder.iterations` (Optional; type `int`; default `310000`): When used by `PasswordEncoderTypes#PBKDF2`, it indicates the required number of iterations.
- `cas.authn.ldap[0].password-encoder.secret` (Optional; type `String`): Secret to use with `PasswordEncoderTypes#STANDARD`, `PasswordEncoderTypes#PBKDF2`, `PasswordEncoderTypes#BCRYPT`, `PasswordEncoderTypes#GLIBC_CRYPT` password encoders. Secret usually is an optional setting.
- `cas.authn.ldap[0].password-encoder.strength` (Optional; type `int`; default `16`): Strength or number of iterations to use for password hashing. Usually relevant when dealing with `PasswordEncoderTypes#BCRYPT`, `PasswordEncoderTypes#PBKDF2` or `PasswordEncoderTypes#GLIBC_CRYPT`. When used by `PasswordEncoderTypes#ARGON2` or `PasswordEncoderTypes#PBKDF2`, it indicates the salt strength.
- `cas.authn.ldap[0].password-policy.account-state-handling-enabled` (Optional; type `boolean`; default `true`): Indicates whether account state handling should be enabled to process warnings or errors reported back from the authentication response, produced by the source.
- `cas.authn.ldap[0].password-policy.custom-policy-class` (Optional; type `String`): An implementation of a policy class that knows how to handle LDAP responses. The class must be an implementation of `org.ldaptive.auth.AuthenticationResponseHandler`.
- `cas.authn.ldap[0].password-policy.display-warning-on-match` (Optional; type `boolean`; default `true`): Indicates if warning should be displayed, when the ldap attribute value matches the `#warningAttributeValue`.
- `cas.authn.ldap[0].password-policy.enabled` (Optional; type `boolean`; default `true`): Whether password policy should be enabled.
- `cas.authn.ldap[0].password-policy.login-failures` (Optional; type `int`; default `5`): When dealing with FreeIPA, indicates the number of allows login failures.
- `cas.authn.ldap[0].password-policy.password-expiration-number-of-days` (Optional; type `int`; default `180`): This is used to calculate an expiration period for the account password. When defined, LDAP password policy handling will use the `pwdLastSet` attribute which must be returned from the LDAP authentication attempt. LDAP password policy handling will emit a warning for the `pwdLastSet` value plus the expiration amount. A negative value will disable the operations that calculate the expiration period.
- `cas.authn.ldap[0].password-policy.policy-attributes` (Optional; type `Map<String, Class<? extends LoginException>>`): Key-value structure (Map) that indicates a list of boolean attributes as keys. If either attribute value is true, indicating an account state is flagged, the corresponding error can be thrown. Example `accountLocked=javax.security.auth.login.AccountLockedException`
- `cas.authn.ldap[0].password-policy.strategy` (Optional; type `PasswordPolicyProperties.PasswordPolicyHandlingOptions`; default `DEFAULT`): Decide how authentication should handle password policy changes. Available values are as follows: - `DEFAULT`: Default option to handle policy changes. - `GROOVY`: Handle account password policies via Groovy. - `REJECT_RESULT_CODE`: Strategy to only activate password policy if the authentication response code is not blocked.
- `cas.authn.ldap[0].password-policy.type` (Optional; type `AbstractLdapProperties.LdapType`; default `GENERIC`): LDAP type.
- `cas.authn.ldap[0].password-policy.warn-all` (Optional; type `boolean`): Always display the password expiration warning regardless.
- `cas.authn.ldap[0].password-policy.warning-attribute-name` (Optional; type `String`): Used by an account state handling policy that only calculates account warnings in case the entry carries this attribute.
- `cas.authn.ldap[0].password-policy.warning-attribute-value` (Optional; type `String`): Used by an account state handling policy that only calculates account warnings in case the entry carries an attribute `#warningAttributeName` whose value matches this field.
- `cas.authn.ldap[0].password-policy.warning-days` (Optional; type `int`; default `30`): This is used to calculate a warning period to see if account expiry is within the calculated window.
- `cas.authn.ldap[0].pool-passivator` (Optional; type `String`): You may receive unexpected LDAP failures, when CAS is configured to authenticate using `DIRECT` or `AUTHENTICATED` types and LDAP is locked down to not allow anonymous binds/searches. Every second attempt with a given LDAP connection from the pool would fail if it was on the same connection as a failed login attempt, and the regular connection validator would similarly fail. When a connection is returned back to a pool, it still may contain the principal and credentials from the previous attempt. Before the next bind attempt using that connection, the validator tries to validate the connection again but fails because it’s no longer trying with the configured bind credentials but with whatever user DN was used in the previous step. Given the validation failure, the connection is closed and CAS would deny access by default. Passivators attempt to reconnect to LDAP with the configured bind credentials, effectively resetting the connection to what it should be after each bind request. Furthermore if you are seeing errors in the logs that resemble a 'Operation exception encountered, reopening connection' type of message, this usually is an indication that the connection pool’s validation timeout established and created by CAS is greater than the timeout configured in the LDAP server, or more likely, in the load balancer in front of the LDAP servers. You can adjust the LDAP server session’s timeout for connections, or you can teach CAS to use a validity period that is equal or less than the LDAP server session’s timeout. Accepted values are: - `NONE`: No passivation takes place. - `BIND`: The default behavior which passivates a connection by performing a bind operation on it. This option requires the availability of bind credentials when establishing connections to LDAP.
- `cas.authn.ldap[0].principal-attribute-id` (Optional; type `String`): The attribute to use as the principal identifier built during and upon a successful authentication attempt.
- `cas.authn.ldap[0].principal-attribute-list` (Optional; type `List<String>`): List of attributes to retrieve from LDAP. Attributes can be virtually remapped to multiple names. Example `cn:commonName,givenName,eduPersonTargettedId:SOME_IDENTIFIER`. To fetch and resolve attributes that carry tags/options, consider tagging the mapped attribute as such: `homePostalAddress:homePostalAddress;`.
- `cas.authn.ldap[0].principal-attribute-password` (Optional; type `String`): If principalAttributePassword is empty then a user simple bind is done to validate credentials otherwise the given attribute is compared with the given principalAttributePassword using the SHA encrypted value of it. For the anonymous authentication type, if principalAttributePassword is empty then a user simple bind is done to validate credentials otherwise the given attribute is compared with the given principalAttributePassword using the SHA encrypted value of it.
- `cas.authn.ldap[0].principal-dn-attribute-name` (Optional; type `String`; default `principalLdapDn`): Name of attribute to be used for principal's DN.
- `cas.authn.ldap[0].principal-transformation.blocking-pattern` (Optional; type `String`): A regular expression that will be used against the username to match for blocking/forbidden values. If a match is found, an exception will be thrown and principal transformation will fail.
- `cas.authn.ldap[0].principal-transformation.case-conversion` (Optional; type `PrincipalTransformationProperties.CaseConversion`; default `NONE`): Indicate whether the principal identifier should be transformed into upper-case, lower-case, etc. Available values are as follows: - `NONE`: No conversion. - `LOWERCASE`: Lowercase conversion. - `UPPERCASE`: Uppercase conversion.
- `cas.authn.ldap[0].principal-transformation.pattern` (Optional; type `String`): A regular expression that will be used against the provided username for username extractions. On a successful match, the first matched group in the pattern will be used as the extracted username.
- `cas.authn.ldap[0].principal-transformation.prefix` (Optional; type `String`): Prefix to add to the principal id prior to authentication.
- `cas.authn.ldap[0].principal-transformation.suffix` (Optional; type `String`): Suffix to add to the principal id prior to authentication.
- `cas.authn.ldap[0].prune-period` (Optional; type `String`): Removes connections from the pool based on how long they have been idle in the available queue. Run the pruning process at the indicated interval.
- `cas.authn.ldap[0].resolve-from-attribute` (Optional; type `String`): If this attribute is set, the value found in the first attribute value will be used in place of the DN.
- `cas.authn.ldap[0].response-timeout` (Optional; type `String`): Duration of time to wait for responses.
- `cas.authn.ldap[0].sasl-authorization-id` (Optional; type `String`): SASL authorization id.
- `cas.authn.ldap[0].sasl-mechanism` (Optional; type `String`): The SASL mechanism.
- `cas.authn.ldap[0].sasl-mutual-auth` (Optional; type `Boolean`): Whether SASL mutual authentication is enabled.
- `cas.authn.ldap[0].sasl-quality-of-protection` (Optional; type `String`): SASL quality of protected.
- `cas.authn.ldap[0].sasl-realm` (Optional; type `String`): The SASL realm.
- `cas.authn.ldap[0].sasl-security-strength` (Optional; type `String`): SASL security strength.
- `cas.authn.ldap[0].search-entry-handlers.case-change.attribute-name-case-change` (Optional; type `String`): The Attribute name case change.
- `cas.authn.ldap[0].search-entry-handlers.case-change.attribute-names` (Optional; type `List<String>`): The Attribute names.
- `cas.authn.ldap[0].search-entry-handlers.case-change.attribute-value-case-change` (Optional; type `String`): The Attribute value case change.
- `cas.authn.ldap[0].search-entry-handlers.case-change.dn-case-change` (Optional; type `String`): The Dn case change.
- `cas.authn.ldap[0].search-entry-handlers.dn-attribute.add-if-exists` (Optional; type `boolean`): The Add if exists.
- `cas.authn.ldap[0].search-entry-handlers.dn-attribute.dn-attribute-name` (Optional; type `String`; default `entryDN`): The Dn attribute name.
- `cas.authn.ldap[0].search-entry-handlers.merge-attribute.attribute-names` (Optional; type `List<String>`): The Attribute names.
- `cas.authn.ldap[0].search-entry-handlers.merge-attribute.merge-attribute-name` (Optional; type `String`): The Merge attribute name.
- `cas.authn.ldap[0].search-entry-handlers.primary-group-id.base-dn` (Optional; type `String`): The Base dn.
- `cas.authn.ldap[0].search-entry-handlers.primary-group-id.group-filter` (Optional; type `String`; default `(&(objectClass=group)(objectSid={0}))`): The Group filter.
- `cas.authn.ldap[0].search-entry-handlers.recursive.merge-attributes` (Optional; type `List<String>`): The Merge attributes.
- `cas.authn.ldap[0].search-entry-handlers.recursive.search-attribute` (Optional; type `String`): The Search attribute.
- `cas.authn.ldap[0].search-entry-handlers.search-referral.limit` (Optional; type `int`; default `10`): The default referral limit.
- `cas.authn.ldap[0].search-entry-handlers.search-result.limit` (Optional; type `int`; default `10`): The default referral limit.
- `cas.authn.ldap[0].search-entry-handlers.type` (Optional; type `LdapSearchEntryHandlersProperties.SearchEntryHandlerTypes`): The type of search entry handler to choose. Available values are as follows: - `FOLLOW_SEARCH_REFERRAL`: Provides handling of an ldap referral for search operations. - `FOLLOW_SEARCH_RESULT_REFERENCE`: Provides handling of an ldap continuation reference for search operations. - `ACTIVE_DIRECTORY`: Process the entry results fetched from active directory and check for account status controls for disabled/expired accounts, etc. - `OBJECT_GUID`: Object guid search entry handler. Handles the `objectGUID` attribute fetching and conversion. - `OBJECT_SID`: Object sid search entry handler. Handles the `objectSid` attribute fetching and conversion. - `CASE_CHANGE`: Case change search entry handler. Provides the ability to modify the case of search entry DNs, attribute names, and attribute values. - `DN_ATTRIBUTE_ENTRY`: DN attribute entry handler. Adds the entry DN as an attribute to the result set. Provides a client side implementation of RFC 5020. - `MERGE`: Merge search entry handler. Merges the values of one or more attributes into a single attribute. - `PRIMARY_GROUP`: Primary group search handler. Constructs the primary group SID and then searches for that group and puts it's DN in the `memberOf` attribute of the original search entry. - `RANGE_ENTRY`: Range entry search handler. Rewrites attributes returned from Active Directory to include all values by performing additional searches. - `RECURSIVE_ENTRY`: Recursive entry search handler. This recursively searches based on a supplied attribute and merges those results into the original entry. - `MERGE_ENTRIES`: Merge entries handler. Merges the values of one or more attributes in all entries into a single attribute. The merged attribute may or may not already exist on the entry. If it does exist it's existing values will remain intact.
- `cas.authn.ldap[0].search-entry-handlers[0].case-change.attribute-name-case-change` (Optional; type `String`): The Attribute name case change.
- `cas.authn.ldap[0].search-entry-handlers[0].case-change.attribute-names` (Optional; type `List<String>`): The Attribute names.
- `cas.authn.ldap[0].search-entry-handlers[0].case-change.attribute-value-case-change` (Optional; type `String`): The Attribute value case change.
- `cas.authn.ldap[0].search-entry-handlers[0].case-change.dn-case-change` (Optional; type `String`): The Dn case change.
- `cas.authn.ldap[0].search-entry-handlers[0].dn-attribute.add-if-exists` (Optional; type `boolean`): The Add if exists.
- `cas.authn.ldap[0].search-entry-handlers[0].dn-attribute.dn-attribute-name` (Optional; type `String`; default `entryDN`): The Dn attribute name.
- `cas.authn.ldap[0].search-entry-handlers[0].merge-attribute.attribute-names` (Optional; type `List<String>`): The Attribute names.
- `cas.authn.ldap[0].search-entry-handlers[0].merge-attribute.merge-attribute-name` (Optional; type `String`): The Merge attribute name.
- `cas.authn.ldap[0].search-entry-handlers[0].primary-group-id.base-dn` (Optional; type `String`): The Base dn.
- `cas.authn.ldap[0].search-entry-handlers[0].primary-group-id.group-filter` (Optional; type `String`; default `(&(objectClass=group)(objectSid={0}))`): The Group filter.
- `cas.authn.ldap[0].search-entry-handlers[0].recursive.merge-attributes` (Optional; type `List<String>`): The Merge attributes.
- `cas.authn.ldap[0].search-entry-handlers[0].recursive.search-attribute` (Optional; type `String`): The Search attribute.
- `cas.authn.ldap[0].search-entry-handlers[0].search-referral.limit` (Optional; type `int`; default `10`): The default referral limit.
- `cas.authn.ldap[0].search-entry-handlers[0].search-result.limit` (Optional; type `int`; default `10`): The default referral limit.
- `cas.authn.ldap[0].search-entry-handlers[0].type` (Optional; type `LdapSearchEntryHandlersProperties.SearchEntryHandlerTypes`): The type of search entry handler to choose. Available values are as follows: - `FOLLOW_SEARCH_REFERRAL`: Provides handling of an ldap referral for search operations. - `FOLLOW_SEARCH_RESULT_REFERENCE`: Provides handling of an ldap continuation reference for search operations. - `ACTIVE_DIRECTORY`: Process the entry results fetched from active directory and check for account status controls for disabled/expired accounts, etc. - `OBJECT_GUID`: Object guid search entry handler. Handles the `objectGUID` attribute fetching and conversion. - `OBJECT_SID`: Object sid search entry handler. Handles the `objectSid` attribute fetching and conversion. - `CASE_CHANGE`: Case change search entry handler. Provides the ability to modify the case of search entry DNs, attribute names, and attribute values. - `DN_ATTRIBUTE_ENTRY`: DN attribute entry handler. Adds the entry DN as an attribute to the result set. Provides a client side implementation of RFC 5020. - `MERGE`: Merge search entry handler. Merges the values of one or more attributes into a single attribute. - `PRIMARY_GROUP`: Primary group search handler. Constructs the primary group SID and then searches for that group and puts it's DN in the `memberOf` attribute of the original search entry. - `RANGE_ENTRY`: Range entry search handler. Rewrites attributes returned from Active Directory to include all values by performing additional searches. - `RECURSIVE_ENTRY`: Recursive entry search handler. This recursively searches based on a supplied attribute and merges those results into the original entry. - `MERGE_ENTRIES`: Merge entries handler. Merges the values of one or more attributes in all entries into a single attribute. The merged attribute may or may not already exist on the entry. If it does exist it's existing values will remain intact.
- `cas.authn.ldap[0].state` (Optional; type `AuthenticationHandlerStates`; default `ACTIVE`): Define the scope and state of this authentication handler and the lifecycle in which it can be invoked or activated. Available values are as follows: - `ACTIVE`: Active authentication handler, and is invoked by default automatically to verify credentials globally. - `STANDBY`: Authentication handler is in a semi-enabled state, waiting to be called only on-demand when explicitly asked for.
- `cas.authn.ldap[0].subtree-search` (Optional; type `boolean`): Whether subtree searching is allowed.
- `cas.authn.ldap[0].trust-certificates` (Optional; type `String`): Path of the trust certificates to use for the SSL connection. Ignores keystore-related settings when activated and used.
- `cas.authn.ldap[0].trust-manager` (Optional; type `String`): Trust Manager options. Trust managers are responsible for managing the trust material that is used when making LDAP trust decisions, and for deciding whether credentials presented by a peer should be accepted. Accepted values are: - `DEFAULT`: Enable and force the default JVM trust managers. - `ANY`: Trust any client or server.
- `cas.authn.ldap[0].trust-store` (Optional; type `String`): Path to the keystore used to determine which certificates or certificate authorities should be trusted. Used when connecting to an LDAP server via LDAPS or startTLS connection. If left blank, the default truststore for the Java runtime is used.
- `cas.authn.ldap[0].trust-store-password` (Optional; type `String`): Password needed to open the truststore.
- `cas.authn.ldap[0].trust-store-type` (Optional; type `String`): The type of trust keystore that determines which certificates or certificate authorities are trusted. Types depend on underlying java platform, typically `PKCS12` or `JKS`. If left blank, defaults to the default keystore type indicated by the underlying Java platform.
- `cas.authn.ldap[0].use-start-tls` (Optional; type `boolean`): Whether TLS should be used and enabled when establishing the connection.
- `cas.authn.ldap[0].validate-on-checkout` (Optional; type `boolean`): Whether connections should be validated when loaned out from the pool.
- `cas.authn.ldap[0].validate-period` (Optional; type `String`): Period at which pool should be validated.
- `cas.authn.ldap[0].validate-periodically` (Optional; type `boolean`): Whether connections should be validated periodically when the pool is idle.
- `cas.authn.ldap[0].validate-timeout` (Optional; type `String`): Period at which validation operations may time out.
- `cas.authn.ldap[0].validator.attribute-name` (Optional; type `String`; default `objectClass`): Attribute name to use for the compare validator.
- `cas.authn.ldap[0].validator.attribute-value` (Optional; type `String`; default `top`): Attribute values to use for the compare validator.
- `cas.authn.ldap[0].validator.base-dn` (Optional; type `String`): Base DN to use for the search request of the search validator.
- `cas.authn.ldap[0].validator.dn` (Optional; type `String`): DN to compare to use for the compare validator.
- `cas.authn.ldap[0].validator.scope` (Optional; type `String`; default `OBJECT`): Search scope to use for the search request of the search validator.
- `cas.authn.ldap[0].validator.search-filter` (Optional; type `String`; default `(objectClass=*)`): Search filter to use for the search request of the search validator.
- `cas.authn.ldap[0].validator.type` (Optional; type `String`; default `search`): Determine the LDAP validator type. The following LDAP validators can be used to test connection health status: - `search`: Validates a connection is healthy by performing a search operation. Validation is considered successful if the search result size is greater than zero. - `none`: No validation takes place. - `compare`: Validates a connection is healthy by performing a compare operation.

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

**Authentication**

Certain authentication handlers are allowed to determine whether they can operate on the provided credential and as such lend themselves to be tried and tested during the authentication handler selection phase. The credential criteria may be one of the following options:

- A regular expression pattern that is tested against the credential identifier.
- A fully qualified class name of your own design that looks similar to the below example:

```java
import java.util.function.Predicate;
import org.apereo.cas.authentication.Credential;

public class PredicateExample implements Predicate<Credential> {
    @Override
    public boolean test(final Credential credential) {
        // Examine the credential and return true/false
    }
}
```

- Path to an external Groovy script that looks similar to the below example:

```groovy
import org.apereo.cas.authentication.Credential
import java.util.function.Predicate

class PredicateExample implements Predicate<Credential> {
    @Override
    boolean test(final Credential credential) {
        // test and return result
    }
}
```

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development//integration/Apache-Groovy-Scripting.html).

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

**Password policy**

**Password Policy Strategies**

If the password policy strategy is to be handed off to a Groovy script, the outline of the script may be as follows:

```groovy
import java.util.*
import org.ldaptive.auth.*
import org.apereo.cas.*
import org.apereo.cas.authentication.*
import org.apereo.cas.authentication.support.*

List<MessageDescriptor> run(final Object... args) {
    def (response,configuration,logger,applicationContext) = args
    logger.info("Handling password policy [{}] via ${configuration.getAccountStateHandler()}", response)

    def accountStateHandler = configuration.getAccountStateHandler()
    return accountStateHandler.handle(response, configuration)
}
```

The parameters passed are as follows:

| Parameter | Description |
| --- | --- |
| `response` | The LDAP authentication response of type `org.ldaptive.auth.AuthenticationResponse` |
| `configuration` | The LDAP password policy configuration carrying the account state handler defined. |
| `logger` | The object responsible for issuing log messages such as `logger.info(...)`. |
| `applicationContext` | The Spring `ApplicationContext` that allows one to interact with the runtime. |

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

## Password Policy Enforcement

To learn how to enforce a password policy for LDAP, please [review this guide](https://apereo.github.io/cas/development/installation/Password-Policy-Enforcement.html).

## Password Synchronization

You may also be interested in synchronizing account passwords with one or more LDAP servers. To learn more, please [review this guide](https://apereo.github.io/cas/development/password_management/Password-Synchronization.html).

## Multitenancy

Configuration settings for LDAP authentication can be specified in a multitenant environment. Please [review this guide](https://apereo.github.io/cas/development/multitenancy/Multitenancy-Tenant-Properties.html) for more information.

## Troubleshooting

To enable additional logging, modify the logging configuration file to add the following:

```xml
<Logger name="org.ldaptive" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
```
