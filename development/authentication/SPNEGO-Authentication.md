# SPNEGO Authentication

Source: https://apereo.github.io/cas/development/authentication/SPNEGO-Authentication.html

[SPNEGO](http://en.wikipedia.org/wiki/SPNEGO) is an authentication technology that is primarily used to provide transparent CAS authentication to browsers running on Windows running under Active Directory domain credentials. There are three actors involved: the client, the CAS server, and the Active Directory Domain Controller/KDC.

1. Client sends CAS: HTTP `GET` to CAS for cas protected page
2. CAS responds: HTTP `401` - Access Denied `WWW-Authenticate: Negotiate`
3. Client sends ticket request: Kerberos(`KRB_TGS_REQ`) Requesting ticket for `HTTP/cas.example.com@REALM`
4. Kerberos KDC responds: Kerberos(`KRB_TGS_REP`) Granting ticket for `HTTP/cas.example.com@REALM`
5. Client sends CAS: HTTP `GET` Authorization: Negotiate w/SPNEGO Token
6. CAS responds: HTTP `200` - OK `WWW-Authenticate` w/SPNEGO response + requested page.

The above interaction occurs only for the first request, when there is no CAS SSO session. Once CAS grants a ticket-granting ticket, the SPNEGO process will not happen again until the CAS ticket expires.

## Requirements

- Client is logged in to a Windows Active Directory domain.
- Supported browser.
- CAS is running MIT kerberos against the AD domain controller.

> **JCE Requirement**
>
> It's safe to make sure you have the proper JCE bundle installed in your Java environment that is used by CAS, specially if you need to consume encrypted payloads issued by ADFS. Be sure to pick the right version of the JCE for your Java version. Java versions can be detected via the `java -version` command.

> **Large Kerberos Tickets**
>
> If organization users have large kerberos tickets, likely cause by being a member of a large number of groups, the Tomcat connector will need to have the `maxHttpHeaderSize` value increased from the default amount to allow the ticket to be passed to the CAS Server application.

## Components

SPNEGO support is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-spnego-webflow"
}
```

### JCIFS SDK

Note that `jcifs-ext` library is no longer published to a public Maven repository. This means that you will need to download the necessary JAR files and include them in your build configuration. The SDK may be downloaded from [the CAS codebase](https://github.com/apereo/cas/blob/master/support/cas-server-support-spnego/lib/jcifs-ext.jar). Then, assuming the SDK is placed inside a `lib` directory of the [WAR overlay](https://apereo.github.io/cas/development/installation/WAR-Overlay-Installation.html) directory, it can be referenced in the build configuration as such:

```gradle
implementation files("${projectDir}/lib/jcifs-ext.jar")
```

## Configuration

The following steps are required to turn on the SPNEGO functionality.

### Create SPN Account

Create an Active Directory account for the Service Principal Name (SPN) and record the username. Password will be overwritten by the next step.

### Create Keytab File

The keytab file enables a trust link between the CAS server and the Key Distribution Center (KDC); an Active Directory domain controller serves the role of KDC in this context. The [`ktpass` tool](http://technet.microsoft.com/en-us/library/cc753771.aspx) is used to generate the keytab file, which contains a cryptographic key. Be sure to execute the command from an Active Directory domain controller as administrator (a member of domain administrators will not be able to use `ktpass` successfully).

Example:

```bash
C:\Users\administrator.DOMAIN>ktpass /out myspnaccount.keytab /princ HTTP/cas.example.com@REALM /pass * /mapuser domain-account@YOUR.REALM /ptype KRB5_NT_PRINCIPAL /crypto AES256-SHA1
Targeting domain controller: DC.YOUR.REALM
Successfully mapped HTTP/cas.example.com to domain-account.
Type the password for HTTP/cas.example.com:
Type the password again to confirm:
Password successfully set!
Key created.
Output keytab to myspnaccount.keytab:
Keytab version: 0x502
keysize 93 HTTP/cas.example.com@REALM ptype 1 (KRB5_NT_PRINCIPAL) vno 5 etype 0x12 (AES256-SHA1) keylength 32
(0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff)
```

Using `ktpass` requires Active Directory admin permissions. If that is not an option, you may be able to use `ktab.exe` from `%JAVA_HOME%\bin\ktab.exe` that is provided by the JDK:

```bash
%JAVA_HOME%\bin\ktab.exe -a service_xxx -n 0 -k cas.keytab
```

`-k` specifies key tab output file name and `-n 0` specifies the KNVO number if available and found for the user account. This value may match the `msDS-KeyVersionNumber` on the user account.

Also note that the keytab file must be regenerated after password changes, if any.

### Test SPN Account

Install and configure MIT Kerberos V on the CAS server host(s). The following sample `krb5.conf` file may be used as a reference.

```ini
[logging]
 default = FILE:/var/log/krb5libs.log
 kdc = FILE:/var/log/krb5kdc.log
 admin_server = FILE:/var/log/kadmind.log

[libdefaults]
 ticket_lifetime = 24000
 default_realm = YOUR.REALM.HERE
 default_keytab_name = /home/cas/kerberos/myspnaccount.keytab
 dns_lookup_realm = false
 dns_lookup_kdc = false
 default_tkt_enctypes = rc4-hmac
 default_tgs_enctypes = rc4-hmac

[realms]
 YOUR.REALM.HERE = {
  kdc = your.kdc.your.realm.here:88
 }

[domain_realm]
 .your.realm.here = YOUR.REALM.HERE
 your.realm.here = YOUR.REALM.HERE
```

It is important to note that `myspnaccount.keytab` is declared as default keytab, otherwise CAS may not be able to find it and will raise an exception similar to

```bash
KrbException: Invalid argument (400) - Cannot find key of appropriate type to decrypt AP REP -RC4 with HMAC`
```

Then verify that your are able to **read** the keytab file:

```bash
klist -k
Keytab name: FILE:/home/cas/kerberos/myspnaccount.keytab
KVNO Principal
---- --------------------------------------------------------------------------
   3 HTTP/cas.example.com@REALM
```

Then verify that your are able to **use** the keytab file:

```bash
kinit -k HTTP/cas.example.com@REALM
klist
Ticket cache: FILE:/tmp/krb5cc_999
Default principal: HTTP/cas.example.com@REALM

Valid starting       Expires              Service principal
12/08/2016 10:52:00  12/08/2016 20:52:00  krbtgt/REALM@REALM
    renew until 12/08/2016 20:52:00
```

### Browser Configuration

- Internet Explorer - Enable `Integrated Windows Authentication` and add the CAS server URL to the `Local Intranet` zone.
- Firefox - Set the `network.negotiate-auth.trusted-uris` configuration parameter in `about:config` to the CAS server URL, e.g. `https://cas.example.com`.

### Authentication Configuration

Make sure you have at least specified the JCIFS Service Principal in the CAS configuration.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.spnego.ldap.base-dn` (Required; type `String`): Base DN to use. There may be scenarios where different parts of a single LDAP tree could be considered as base-dns. Rather than duplicating the LDAP configuration block for each individual base-dn, each entry can be specified and joined together using a special delimiter character. The user DN is retrieved using the combination of all base-dn and DN resolvers in the order defined. DN resolution should fail if multiple DNs are found. Otherwise the first DN found is returned. Usual syntax is: `subtreeA,dc=example,dc=net|subtreeC,dc=example,dc=net`.
- `cas.authn.spnego.ldap.bind-credential` (Required; type `String`): The bind credential to use when connecting to LDAP.
- `cas.authn.spnego.ldap.bind-dn` (Required; type `String`): The bind DN to use when connecting to LDAP. LDAP connection configuration injected into the LDAP connection pool can be initialized with the following parameters: - `bindDn/bindCredential` provided - Use the provided credentials to bind when initializing connections. - `bindDn/bindCredential` set to `*` - Use a fast-bind strategy to initialize the pool. - `bindDn/bindCredential` set to blank - Skip connection initializing; perform operations anonymously. - SASL mechanism provided - Use the given SASL mechanism to bind when initializing connections.
- `cas.authn.spnego.ldap.ldap-url` (Required; type `String`): The LDAP url to the server. More than one may be specified, separated by space and/or comma.
- `cas.authn.spnego.ldap.search-filter` (Required; type `String`): User filter to use for searching. Syntax is `cn={user}` or `cn={0}`. You may also provide an external groovy script in the syntax of `file:/path/to/GroovyScript.groovy` to fully build the final filter template dynamically.
- `cas.authn.spnego.principal-transformation.groovy.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.spnego.principal.principal-transformation.groovy.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.spnego.properties[0].jcifs-service-principal` (Required; type `String`; default `HTTP/cas.example.com@EXAMPLE.COM`): The Jcifs service principal.
- `cas.authn.spnego.alternative-remote-host-attribute` (Optional; type `String`; default `alternateRemoteHeader`): Alternative header name to use in order to find the host address.
- `cas.authn.spnego.dns-timeout` (Optional; type `String`; default `PT2S`): When validating clients, specifies the DNS timeout used to look up an address.
- `cas.authn.spnego.host-name-client-action-strategy` (Optional; type `String`; default `hostnameSpnegoClientAction`): The bean id of a webflow action whose job is to evaluate the client host to see if the request is authorized for spnego. Supported strategies include `hostnameSpnegoClientAction` where CAS checks to see if the request’s remote hostname matches a predefine pattern. and `ldapSpnegoClientAction` where CAS checks an LDAP instance for the remote hostname, to locate a pre-defined attribute whose mere existence would allow the webflow to resume to SPNEGO.
- `cas.authn.spnego.host-name-pattern-string` (Optional; type `String`; default `.+`): A regex pattern that indicates whether the client host name is allowed for spnego.
- `cas.authn.spnego.ips-to-check-pattern` (Optional; type `String`; default `127.+`): A regex pattern that indicates whether the client IP is allowed for spnego.
- `cas.authn.spnego.ldap.allow-multiple-dns` (Optional; type `Boolean`; default `false`): Whether search/query results are allowed to match on multiple DNs, or whether a single unique DN is expected for the result.
- `cas.authn.spnego.ldap.allow-multiple-entries` (Optional; type `Boolean`; default `false`): Set if multiple Entries are allowed.
- `cas.authn.spnego.ldap.binary-attributes` (Optional; type `List<String>`): Indicate the collection of attributes that are to be tagged and processed as binary attributes by the underlying search resolver.
- `cas.authn.spnego.ldap.block-wait-time` (Optional; type `String`; default `PT3S`): The length of time the pool will block. By default the pool will block indefinitely and there is no guarantee that waiting threads will be serviced in the order in which they made their request. This option should be used with a blocking connection pool when you need to control the exact number of connections that can be created
- `cas.authn.spnego.ldap.connect-timeout` (Optional; type `String`; default `PT5S`): Sets the maximum amount of time that connects will block.
- `cas.authn.spnego.ldap.connection-strategy` (Optional; type `String`): If multiple URLs are provided as the ldapURL this describes how each URL will be processed. - `ACTIVE_PASSIVE` First LDAP will be used for every request unless it fails and then the next shall be used. - `ROUND_ROBIN` For each new connection the next url in the list will be used. - `RANDOM` For each new connection a random LDAP url will be selected. - `DNS_SRV` LDAP urls based on DNS SRV records of the configured/given LDAP url will be used.
- `cas.authn.spnego.ldap.disable-pooling` (Optional; type `Boolean`; default `false`): Whether to use a pooled connection factory in components.
- `cas.authn.spnego.ldap.fail-fast` (Optional; type `Boolean`; default `true`): Attempt to populate the connection pool early on startup and fail quickly if something goes wrong.
- `cas.authn.spnego.ldap.follow-referrals` (Optional; type `Boolean`; default `true`): Set if search referrals should be followed.
- `cas.authn.spnego.ldap.hostname-verifier` (Optional; type `AbstractLdapProperties.LdapHostnameVerifierOptions`; default `DEFAULT`): Hostname verification options. Available values are as follows: - `DEFAULT`: Default option, forcing verification. - `ANY`: Skip hostname verification and allow all.
- `cas.authn.spnego.ldap.idle-time` (Optional; type `String`; default `PT10M`): Removes connections from the pool based on how long they have been idle in the available queue. Prunes connections that have been idle for more than the indicated amount.
- `cas.authn.spnego.ldap.keystore` (Optional; type `String`): Path to the keystore used for SSL connections. Typically contains SSL certificates for the LDAP server.
- `cas.authn.spnego.ldap.keystore-password` (Optional; type `String`): Keystore password.
- `cas.authn.spnego.ldap.keystore-type` (Optional; type `String`): The type of keystore. `PKCS12` or `JKS`. If left blank, defaults to the default keystore type indicated by the underlying Java platform.
- `cas.authn.spnego.ldap.max-pool-size` (Optional; type `Integer`; default `10`): Maximum LDAP connection pool size which the pool can use to grow.
- `cas.authn.spnego.ldap.min-pool-size` (Optional; type `Integer`; default `3`): Minimum LDAP connection pool size. Size the pool should be initialized to and pruned to
- `cas.authn.spnego.ldap.name` (Optional; type `String`): Name of the LDAP handler.
- `cas.authn.spnego.ldap.page-size` (Optional; type `Integer`; default `0`): Request that the server return results in batches of a specific size. See [RFC 2696](http://www.ietf.org/rfc/rfc2696.txt). This control is often used to work around server result size limits. A negative/zero value disables paged requests.
- `cas.authn.spnego.ldap.pool-passivator` (Optional; type `String`; default `BIND`): You may receive unexpected LDAP failures, when CAS is configured to authenticate using `DIRECT` or `AUTHENTICATED` types and LDAP is locked down to not allow anonymous binds/searches. Every second attempt with a given LDAP connection from the pool would fail if it was on the same connection as a failed login attempt, and the regular connection validator would similarly fail. When a connection is returned back to a pool, it still may contain the principal and credentials from the previous attempt. Before the next bind attempt using that connection, the validator tries to validate the connection again but fails because it’s no longer trying with the configured bind credentials but with whatever user DN was used in the previous step. Given the validation failure, the connection is closed and CAS would deny access by default. Passivators attempt to reconnect to LDAP with the configured bind credentials, effectively resetting the connection to what it should be after each bind request. Furthermore if you are seeing errors in the logs that resemble a 'Operation exception encountered, reopening connection' type of message, this usually is an indication that the connection pool’s validation timeout established and created by CAS is greater than the timeout configured in the LDAP server, or more likely, in the load balancer in front of the LDAP servers. You can adjust the LDAP server session’s timeout for connections, or you can teach CAS to use a validity period that is equal or less than the LDAP server session’s timeout. Accepted values are: - `NONE`: No passivation takes place. - `BIND`: The default behavior which passivates a connection by performing a bind operation on it. This option requires the availability of bind credentials when establishing connections to LDAP.
- `cas.authn.spnego.ldap.prune-period` (Optional; type `String`; default `PT2H`): Removes connections from the pool based on how long they have been idle in the available queue. Run the pruning process at the indicated interval.
- `cas.authn.spnego.ldap.response-timeout` (Optional; type `String`; default `PT5S`): Duration of time to wait for responses.
- `cas.authn.spnego.ldap.sasl-authorization-id` (Optional; type `String`): SASL authorization id.
- `cas.authn.spnego.ldap.sasl-mechanism` (Optional; type `String`): The SASL mechanism.
- `cas.authn.spnego.ldap.sasl-mutual-auth` (Optional; type `Boolean`): Whether SASL mutual authentication is enabled.
- `cas.authn.spnego.ldap.sasl-quality-of-protection` (Optional; type `String`): SASL quality of protected.
- `cas.authn.spnego.ldap.sasl-realm` (Optional; type `String`): The SASL realm.
- `cas.authn.spnego.ldap.sasl-security-strength` (Optional; type `String`): SASL security strength.
- `cas.authn.spnego.ldap.search-entry-handlers` (Optional; type `List<LdapSearchEntryHandlersProperties>`): Search handlers.
- `cas.authn.spnego.ldap.search-entry-handlers[0].case-change.attribute-name-case-change` (Optional; type `String`): The Attribute name case change.
- `cas.authn.spnego.ldap.search-entry-handlers[0].case-change.attribute-names` (Optional; type `List<String>`): The Attribute names.
- `cas.authn.spnego.ldap.search-entry-handlers[0].case-change.attribute-value-case-change` (Optional; type `String`): The Attribute value case change.
- `cas.authn.spnego.ldap.search-entry-handlers[0].case-change.dn-case-change` (Optional; type `String`): The Dn case change.
- `cas.authn.spnego.ldap.search-entry-handlers[0].dn-attribute.add-if-exists` (Optional; type `boolean`): The Add if exists.
- `cas.authn.spnego.ldap.search-entry-handlers[0].dn-attribute.dn-attribute-name` (Optional; type `String`; default `entryDN`): The Dn attribute name.
- `cas.authn.spnego.ldap.search-entry-handlers[0].merge-attribute.attribute-names` (Optional; type `List<String>`): The Attribute names.
- `cas.authn.spnego.ldap.search-entry-handlers[0].merge-attribute.merge-attribute-name` (Optional; type `String`): The Merge attribute name.
- `cas.authn.spnego.ldap.search-entry-handlers[0].primary-group-id.base-dn` (Optional; type `String`): The Base dn.
- `cas.authn.spnego.ldap.search-entry-handlers[0].primary-group-id.group-filter` (Optional; type `String`; default `(&(objectClass=group)(objectSid={0}))`): The Group filter.
- `cas.authn.spnego.ldap.search-entry-handlers[0].recursive.merge-attributes` (Optional; type `List<String>`): The Merge attributes.
- `cas.authn.spnego.ldap.search-entry-handlers[0].recursive.search-attribute` (Optional; type `String`): The Search attribute.
- `cas.authn.spnego.ldap.search-entry-handlers[0].search-referral.limit` (Optional; type `int`; default `10`): The default referral limit.
- `cas.authn.spnego.ldap.search-entry-handlers[0].search-result.limit` (Optional; type `int`; default `10`): The default referral limit.
- `cas.authn.spnego.ldap.search-entry-handlers[0].type` (Optional; type `LdapSearchEntryHandlersProperties.SearchEntryHandlerTypes`): The type of search entry handler to choose. Available values are as follows: - `FOLLOW_SEARCH_REFERRAL`: Provides handling of an ldap referral for search operations. - `FOLLOW_SEARCH_RESULT_REFERENCE`: Provides handling of an ldap continuation reference for search operations. - `ACTIVE_DIRECTORY`: Process the entry results fetched from active directory and check for account status controls for disabled/expired accounts, etc. - `OBJECT_GUID`: Object guid search entry handler. Handles the `objectGUID` attribute fetching and conversion. - `OBJECT_SID`: Object sid search entry handler. Handles the `objectSid` attribute fetching and conversion. - `CASE_CHANGE`: Case change search entry handler. Provides the ability to modify the case of search entry DNs, attribute names, and attribute values. - `DN_ATTRIBUTE_ENTRY`: DN attribute entry handler. Adds the entry DN as an attribute to the result set. Provides a client side implementation of RFC 5020. - `MERGE`: Merge search entry handler. Merges the values of one or more attributes into a single attribute. - `PRIMARY_GROUP`: Primary group search handler. Constructs the primary group SID and then searches for that group and puts it's DN in the `memberOf` attribute of the original search entry. - `RANGE_ENTRY`: Range entry search handler. Rewrites attributes returned from Active Directory to include all values by performing additional searches. - `RECURSIVE_ENTRY`: Recursive entry search handler. This recursively searches based on a supplied attribute and merges those results into the original entry. - `MERGE_ENTRIES`: Merge entries handler. Merges the values of one or more attributes in all entries into a single attribute. The merged attribute may or may not already exist on the entry. If it does exist it's existing values will remain intact.
- `cas.authn.spnego.ldap.subtree-search` (Optional; type `Boolean`; default `true`): Whether subtree searching is allowed.
- `cas.authn.spnego.ldap.trust-certificates` (Optional; type `String`): Path of the trust certificates to use for the SSL connection. Ignores keystore-related settings when activated and used.
- `cas.authn.spnego.ldap.trust-manager` (Optional; type `String`): Trust Manager options. Trust managers are responsible for managing the trust material that is used when making LDAP trust decisions, and for deciding whether credentials presented by a peer should be accepted. Accepted values are: - `DEFAULT`: Enable and force the default JVM trust managers. - `ANY`: Trust any client or server.
- `cas.authn.spnego.ldap.trust-store` (Optional; type `String`): Path to the keystore used to determine which certificates or certificate authorities should be trusted. Used when connecting to an LDAP server via LDAPS or startTLS connection. If left blank, the default truststore for the Java runtime is used.
- `cas.authn.spnego.ldap.trust-store-password` (Optional; type `String`): Password needed to open the truststore.
- `cas.authn.spnego.ldap.trust-store-type` (Optional; type `String`): The type of trust keystore that determines which certificates or certificate authorities are trusted. Types depend on underlying java platform, typically `PKCS12` or `JKS`. If left blank, defaults to the default keystore type indicated by the underlying Java platform.
- `cas.authn.spnego.ldap.use-start-tls` (Optional; type `Boolean`; default `false`): Whether TLS should be used and enabled when establishing the connection.
- `cas.authn.spnego.ldap.validate-on-checkout` (Optional; type `Boolean`; default `true`): Whether connections should be validated when loaned out from the pool.
- `cas.authn.spnego.ldap.validate-period` (Optional; type `String`; default `PT5M`): Period at which pool should be validated.
- `cas.authn.spnego.ldap.validate-periodically` (Optional; type `Boolean`; default `true`): Whether connections should be validated periodically when the pool is idle.
- `cas.authn.spnego.ldap.validate-timeout` (Optional; type `String`; default `PT5S`): Period at which validation operations may time out.
- `cas.authn.spnego.ldap.validator.attribute-name` (Optional; type `String`; default `objectClass`): Attribute name to use for the compare validator.
- `cas.authn.spnego.ldap.validator.attribute-value` (Optional; type `String`; default `top`): Attribute values to use for the compare validator.
- `cas.authn.spnego.ldap.validator.base-dn` (Optional; type `String`): Base DN to use for the search request of the search validator.
- `cas.authn.spnego.ldap.validator.dn` (Optional; type `String`): DN to compare to use for the compare validator.
- `cas.authn.spnego.ldap.validator.scope` (Optional; type `String`; default `OBJECT`): Search scope to use for the search request of the search validator.
- `cas.authn.spnego.ldap.validator.search-filter` (Optional; type `String`; default `(objectClass=*)`): Search filter to use for the search request of the search validator.
- `cas.authn.spnego.ldap.validator.type` (Optional; type `String`; default `search`): Determine the LDAP validator type. The following LDAP validators can be used to test connection health status: - `search`: Validates a connection is healthy by performing a search operation. Validation is considered successful if the search result size is greater than zero. - `none`: No validation takes place. - `compare`: Validates a connection is healthy by performing a compare operation.
- `cas.authn.spnego.mixed-mode-authentication` (Optional; type `Boolean`; default `false`): If true, does not terminate authentication and allows CAS to resume and fallback to normal authentication means such as uid/psw via the login page. If disallowed, considers spnego authentication to be final in the event of failures.
- `cas.authn.spnego.name` (Optional; type `String`): Name of the authentication handler.
- `cas.authn.spnego.ntlm-allowed` (Optional; type `Boolean`; default `true`): Allows authentication if spnego credential is marked as NTLM.
- `cas.authn.spnego.order` (Optional; type `Integer`): The order of the authentication handler in the chain.
- `cas.authn.spnego.pool-size` (Optional; type `Integer`; default `10`): The size of the pool used to validate SPNEGO tokens. A pool is used to provider better performance than what was previously offered by the simple Lombok `Synchronized` annotation.
- `cas.authn.spnego.pool-timeout` (Optional; type `String`; default `PT2S`): The timeout of the pool used to validate SPNEGO tokens.
- `cas.authn.spnego.principal-transformation.blocking-pattern` (Optional; type `String`): A regular expression that will be used against the username to match for blocking/forbidden values. If a match is found, an exception will be thrown and principal transformation will fail.
- `cas.authn.spnego.principal-transformation.case-conversion` (Optional; type `PrincipalTransformationProperties.CaseConversion`; default `NONE`): Indicate whether the principal identifier should be transformed into upper-case, lower-case, etc. Available values are as follows: - `NONE`: No conversion. - `LOWERCASE`: Lowercase conversion. - `UPPERCASE`: Uppercase conversion.
- `cas.authn.spnego.principal-transformation.pattern` (Optional; type `String`): A regular expression that will be used against the provided username for username extractions. On a successful match, the first matched group in the pattern will be used as the extracted username.
- `cas.authn.spnego.principal-transformation.prefix` (Optional; type `String`): Prefix to add to the principal id prior to authentication.
- `cas.authn.spnego.principal-transformation.suffix` (Optional; type `String`): Suffix to add to the principal id prior to authentication.
- `cas.authn.spnego.principal-with-domain-name` (Optional; type `Boolean`; default `false`): If specified, will create the principal by this name on successful authentication.
- `cas.authn.spnego.principal.active-attribute-repository-ids` (Optional; type `String`; default `*`): Activated attribute repository identifiers that should be used for fetching attributes if attribute resolution is enabled. The list here may include identifiers separated by comma.
- `cas.authn.spnego.principal.attribute-repository-selection` (Optional; type `Map<String,String>`): Control the behavior of the attribute repository selection by authentication method or handler. The map here is keyed by the authentication handler name, and the value is the attribute repository identifiers separated by comma. When the authentication handler is executed, the attribute repositories assigned to this handler will be selected to fetch attributes. Note that the resolution engine will always favor attribute repositories assigned to the service definition, if any and as part of its authentication policy, over this global setting.
- `cas.authn.spnego.principal.attribute-resolution-enabled` (Optional; type `TriStateBoolean`; default `undefined`): Whether attribute repositories should be contacted to fetch person attributes. Defaults to true if not set. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.authn.spnego.principal.principal-attribute` (Optional; type `String`): Attribute name to use to indicate the identifier of the principal constructed. If the attribute is blank or has no values, the default principal id will be used determined by the underlying authentication engine. The principal id attribute usually is removed from the collection of attributes collected, though this behavior depends on the schematics of the underlying authentication strategy.
- `cas.authn.spnego.principal.principal-resolution-conflict-strategy` (Optional; type `String`; default `last`): In the event that the principal resolution engine resolves more than one principal, (especially if such principals in the chain have different identifiers), this setting determines strategy by which the principal id would be chosen from the chain. Accepted values are: `last`, `first`.
- `cas.authn.spnego.principal.principal-resolution-failure-fatal` (Optional; type `TriStateBoolean`; default `undefined`): When true, throws an error back indicating that principal resolution has failed and no principal can be found based on the authentication requirements. Otherwise, logs the condition as an error without raising a catastrophic error. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.authn.spnego.principal.principal-transformation.blocking-pattern` (Optional; type `String`): A regular expression that will be used against the username to match for blocking/forbidden values. If a match is found, an exception will be thrown and principal transformation will fail.
- `cas.authn.spnego.principal.principal-transformation.case-conversion` (Optional; type `PrincipalTransformationProperties.CaseConversion`; default `NONE`): Indicate whether the principal identifier should be transformed into upper-case, lower-case, etc. Available values are as follows: - `NONE`: No conversion. - `LOWERCASE`: Lowercase conversion. - `UPPERCASE`: Uppercase conversion.
- `cas.authn.spnego.principal.principal-transformation.pattern` (Optional; type `String`): A regular expression that will be used against the provided username for username extractions. On a successful match, the first matched group in the pattern will be used as the extracted username.
- `cas.authn.spnego.principal.principal-transformation.prefix` (Optional; type `String`): Prefix to add to the principal id prior to authentication.
- `cas.authn.spnego.principal.principal-transformation.suffix` (Optional; type `String`): Suffix to add to the principal id prior to authentication.
- `cas.authn.spnego.principal.return-null` (Optional; type `TriStateBoolean`; default `undefined`): Return a null principal object if no attributes can be found for the principal. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.authn.spnego.principal.use-existing-principal-id` (Optional; type `TriStateBoolean`; default `undefined`): Uses an existing principal id that may have already been established in order to run person directory queries. This is generally useful in situations where authentication is delegated to an external identity provider and a principal is first established to then query an attribute source. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.authn.spnego.properties` (Optional; type `List<SpnegoAuthenticationProperties>`): Individual authentication settings for spengo that are grouped and fed to the spnego authentication object to form a collection.
- `cas.authn.spnego.properties[0].cache-policy` (Optional; type `long`; default `600`): Jcifs Netbios cache policy.
- `cas.authn.spnego.properties[0].jcifs-domain` (Optional; type `String`): The Jcifs domain.
- `cas.authn.spnego.properties[0].jcifs-domain-controller` (Optional; type `String`): The Jcifs domain controller.
- `cas.authn.spnego.properties[0].jcifs-netbios-wins` (Optional; type `String`): The Jcifs netbios wins.
- `cas.authn.spnego.properties[0].jcifs-password` (Optional; type `String`): The Jcifs password.
- `cas.authn.spnego.properties[0].jcifs-service-password` (Optional; type `String`): The Jcifs service password, used to log the service principal in when `cas.authn.spnego.system.use-subject-creds-only` is `true`.
- `cas.authn.spnego.properties[0].jcifs-username` (Optional; type `String`): The Jcifs username.
- `cas.authn.spnego.properties[0].timeout` (Optional; type `String`; default `PT5M`): Spnego JCIFS timeout.
- `cas.authn.spnego.send401-on-authentication-failure` (Optional; type `Boolean`; default `true`): If the authenticated principal cannot be determined from the spegno credential, will set the http status code to 401.
- `cas.authn.spnego.spnego-attribute-name` (Optional; type `String`; default `distinguishedName`): In case LDAP is used to validate clients, this is the attribute that indicates the host.
- `cas.authn.spnego.supported-browsers` (Optional; type `String`; default `MSIE,Trident,Firefox,AppleWebKit`): Begins negotiating spnego if the user-agent is one of the supported browsers.
- `cas.authn.spnego.system.kerberos-conf` (Optional; type `String`): The Kerberos conf. As with all Kerberos installations, a Kerberos Key Distribution Center (KDC) is required. It needs to contain the user name and password you will use to be authenticated to Kerberos. As with most Kerberos installations, a Kerberos configuration file krb5.conf is consulted to determine such things as the default realm and KDC. Typically, the default realm and the KDC for that realm are indicated in the Kerberos krb5.conf configuration file. The path to the configuration file must typically be defined as an absolute path.
- `cas.authn.spnego.system.kerberos-debug` (Optional; type `String`): The Kerberos debug.
- `cas.authn.spnego.system.kerberos-kdc` (Optional; type `String`; default `172.10.1.10`): The Kerberos kdc, set as the `java.security.krb5.kdc` system property. When both this and the Kerberos realm are set, the JDK ignores the Kerberos configuration file, and a KDC listening on a port other than 88 cannot be reached since the property carries no port. Leave both blank to take the realm and KDC from the Kerberos configuration file.
- `cas.authn.spnego.system.kerberos-realm` (Optional; type `String`; default `EXAMPLE.COM`): The Kerberos realm, set as the `java.security.krb5.realm` system property. See the Kerberos kdc setting for how both interact with the Kerberos configuration file.
- `cas.authn.spnego.system.login-conf` (Optional; type `String`): The Login conf.Absolute path to the jaas login configuration file. This should define the spnego authentication details. Make sure you have at least specified the JCIFS Service Principal defined.
- `cas.authn.spnego.system.use-subject-creds-only` (Optional; type `Boolean`; default `false`): Sets the `javax.security.auth.useSubjectCredsOnly` system property. When `true`, CAS logs the service principal in through the `jcifs.spnego.accept` entry of the JAAS login configuration, with the JCIFS service principal and service password, and uses the credentials of that login to accept SPNEGO tokens. When `false`, the JCIFS service password and the `jcifs.spnego.accept` entry are not used; the JDK finds the service keys on its own, typically from the `default_keytab_name` keytab of the Kerberos configuration file.
- `cas.authn.spnego.webflow.enabled` (Optional; type `Boolean`; default `true`): Whether webflow auto-configuration should be enabled.
- `cas.authn.spnego.webflow.order` (Optional; type `Integer`; default `0`): The order in which the webflow is configured.

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

You may provide a JAAS `login.conf` file:

```bash
jcifs.spnego.initiate {
   com.sun.security.auth.module.Krb5LoginModule \
    required storeKey=true useKeyTab=true keyTab="/home/cas/kerberos/myspnaccount.keytab";
};
jcifs.spnego.accept {
   com.sun.security.auth.module.Krb5LoginModule \
    required storeKey=true useKeyTab=true keyTab="/home/cas/kerberos/myspnaccount.keytab";
};
```

## Client Selection Strategy

CAS provides a set of components that attempt to activate the SPNEGO flow conditionally, in case deployers need a configurable way to decide whether SPNEGO should be applied to the current authentication/browser request. The state that is available to the webflow is `evaluateClientRequest` which will attempt to start SPNEGO authentication or resume normally, depending on the client action strategy chosen below.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.spnego.alternative-remote-host-attribute` (Optional; type `String`; default `alternateRemoteHeader`): Alternative header name to use in order to find the host address.
- `cas.authn.spnego.dns-timeout` (Optional; type `String`; default `PT2S`): When validating clients, specifies the DNS timeout used to look up an address.
- `cas.authn.spnego.host-name-client-action-strategy` (Optional; type `String`; default `hostnameSpnegoClientAction`): The bean id of a webflow action whose job is to evaluate the client host to see if the request is authorized for spnego. Supported strategies include `hostnameSpnegoClientAction` where CAS checks to see if the request’s remote hostname matches a predefine pattern. and `ldapSpnegoClientAction` where CAS checks an LDAP instance for the remote hostname, to locate a pre-defined attribute whose mere existence would allow the webflow to resume to SPNEGO.
- `cas.authn.spnego.host-name-pattern-string` (Optional; type `String`; default `.+`): A regex pattern that indicates whether the client host name is allowed for spnego.
- `cas.authn.spnego.ips-to-check-pattern` (Optional; type `String`; default `127.+`): A regex pattern that indicates whether the client IP is allowed for spnego.

### By Remote IP

Checks to see if the request’s remote ip address matches a predefine pattern.

### By Hostname

Checks to see if the request’s remote hostname matches a predefine pattern.

### By LDAP Attribute

Checks an LDAP instance for the remote hostname, to locate a pre-defined attribute whose mere existence would allow the webflow to resume to SPNEGO.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.spnego.ldap.base-dn` (Required; type `String`): Base DN to use. There may be scenarios where different parts of a single LDAP tree could be considered as base-dns. Rather than duplicating the LDAP configuration block for each individual base-dn, each entry can be specified and joined together using a special delimiter character. The user DN is retrieved using the combination of all base-dn and DN resolvers in the order defined. DN resolution should fail if multiple DNs are found. Otherwise the first DN found is returned. Usual syntax is: `subtreeA,dc=example,dc=net|subtreeC,dc=example,dc=net`.
- `cas.authn.spnego.ldap.bind-credential` (Required; type `String`): The bind credential to use when connecting to LDAP.
- `cas.authn.spnego.ldap.bind-dn` (Required; type `String`): The bind DN to use when connecting to LDAP. LDAP connection configuration injected into the LDAP connection pool can be initialized with the following parameters: - `bindDn/bindCredential` provided - Use the provided credentials to bind when initializing connections. - `bindDn/bindCredential` set to `*` - Use a fast-bind strategy to initialize the pool. - `bindDn/bindCredential` set to blank - Skip connection initializing; perform operations anonymously. - SASL mechanism provided - Use the given SASL mechanism to bind when initializing connections.
- `cas.authn.spnego.ldap.ldap-url` (Required; type `String`): The LDAP url to the server. More than one may be specified, separated by space and/or comma.
- `cas.authn.spnego.ldap.search-filter` (Required; type `String`): User filter to use for searching. Syntax is `cn={user}` or `cn={0}`. You may also provide an external groovy script in the syntax of `file:/path/to/GroovyScript.groovy` to fully build the final filter template dynamically.
- `cas.authn.spnego.ldap.allow-multiple-dns` (Optional; type `Boolean`; default `false`): Whether search/query results are allowed to match on multiple DNs, or whether a single unique DN is expected for the result.
- `cas.authn.spnego.ldap.allow-multiple-entries` (Optional; type `Boolean`; default `false`): Set if multiple Entries are allowed.
- `cas.authn.spnego.ldap.binary-attributes` (Optional; type `List<String>`): Indicate the collection of attributes that are to be tagged and processed as binary attributes by the underlying search resolver.
- `cas.authn.spnego.ldap.block-wait-time` (Optional; type `String`; default `PT3S`): The length of time the pool will block. By default the pool will block indefinitely and there is no guarantee that waiting threads will be serviced in the order in which they made their request. This option should be used with a blocking connection pool when you need to control the exact number of connections that can be created
- `cas.authn.spnego.ldap.connect-timeout` (Optional; type `String`; default `PT5S`): Sets the maximum amount of time that connects will block.
- `cas.authn.spnego.ldap.connection-strategy` (Optional; type `String`): If multiple URLs are provided as the ldapURL this describes how each URL will be processed. - `ACTIVE_PASSIVE` First LDAP will be used for every request unless it fails and then the next shall be used. - `ROUND_ROBIN` For each new connection the next url in the list will be used. - `RANDOM` For each new connection a random LDAP url will be selected. - `DNS_SRV` LDAP urls based on DNS SRV records of the configured/given LDAP url will be used.
- `cas.authn.spnego.ldap.disable-pooling` (Optional; type `Boolean`; default `false`): Whether to use a pooled connection factory in components.
- `cas.authn.spnego.ldap.fail-fast` (Optional; type `Boolean`; default `true`): Attempt to populate the connection pool early on startup and fail quickly if something goes wrong.
- `cas.authn.spnego.ldap.follow-referrals` (Optional; type `Boolean`; default `true`): Set if search referrals should be followed.
- `cas.authn.spnego.ldap.hostname-verifier` (Optional; type `AbstractLdapProperties.LdapHostnameVerifierOptions`; default `DEFAULT`): Hostname verification options. Available values are as follows: - `DEFAULT`: Default option, forcing verification. - `ANY`: Skip hostname verification and allow all.
- `cas.authn.spnego.ldap.idle-time` (Optional; type `String`; default `PT10M`): Removes connections from the pool based on how long they have been idle in the available queue. Prunes connections that have been idle for more than the indicated amount.
- `cas.authn.spnego.ldap.keystore` (Optional; type `String`): Path to the keystore used for SSL connections. Typically contains SSL certificates for the LDAP server.
- `cas.authn.spnego.ldap.keystore-password` (Optional; type `String`): Keystore password.
- `cas.authn.spnego.ldap.keystore-type` (Optional; type `String`): The type of keystore. `PKCS12` or `JKS`. If left blank, defaults to the default keystore type indicated by the underlying Java platform.
- `cas.authn.spnego.ldap.max-pool-size` (Optional; type `Integer`; default `10`): Maximum LDAP connection pool size which the pool can use to grow.
- `cas.authn.spnego.ldap.min-pool-size` (Optional; type `Integer`; default `3`): Minimum LDAP connection pool size. Size the pool should be initialized to and pruned to
- `cas.authn.spnego.ldap.name` (Optional; type `String`): Name of the LDAP handler.
- `cas.authn.spnego.ldap.page-size` (Optional; type `Integer`; default `0`): Request that the server return results in batches of a specific size. See [RFC 2696](http://www.ietf.org/rfc/rfc2696.txt). This control is often used to work around server result size limits. A negative/zero value disables paged requests.
- `cas.authn.spnego.ldap.pool-passivator` (Optional; type `String`; default `BIND`): You may receive unexpected LDAP failures, when CAS is configured to authenticate using `DIRECT` or `AUTHENTICATED` types and LDAP is locked down to not allow anonymous binds/searches. Every second attempt with a given LDAP connection from the pool would fail if it was on the same connection as a failed login attempt, and the regular connection validator would similarly fail. When a connection is returned back to a pool, it still may contain the principal and credentials from the previous attempt. Before the next bind attempt using that connection, the validator tries to validate the connection again but fails because it’s no longer trying with the configured bind credentials but with whatever user DN was used in the previous step. Given the validation failure, the connection is closed and CAS would deny access by default. Passivators attempt to reconnect to LDAP with the configured bind credentials, effectively resetting the connection to what it should be after each bind request. Furthermore if you are seeing errors in the logs that resemble a 'Operation exception encountered, reopening connection' type of message, this usually is an indication that the connection pool’s validation timeout established and created by CAS is greater than the timeout configured in the LDAP server, or more likely, in the load balancer in front of the LDAP servers. You can adjust the LDAP server session’s timeout for connections, or you can teach CAS to use a validity period that is equal or less than the LDAP server session’s timeout. Accepted values are: - `NONE`: No passivation takes place. - `BIND`: The default behavior which passivates a connection by performing a bind operation on it. This option requires the availability of bind credentials when establishing connections to LDAP.
- `cas.authn.spnego.ldap.prune-period` (Optional; type `String`; default `PT2H`): Removes connections from the pool based on how long they have been idle in the available queue. Run the pruning process at the indicated interval.
- `cas.authn.spnego.ldap.response-timeout` (Optional; type `String`; default `PT5S`): Duration of time to wait for responses.
- `cas.authn.spnego.ldap.sasl-authorization-id` (Optional; type `String`): SASL authorization id.
- `cas.authn.spnego.ldap.sasl-mechanism` (Optional; type `String`): The SASL mechanism.
- `cas.authn.spnego.ldap.sasl-mutual-auth` (Optional; type `Boolean`): Whether SASL mutual authentication is enabled.
- `cas.authn.spnego.ldap.sasl-quality-of-protection` (Optional; type `String`): SASL quality of protected.
- `cas.authn.spnego.ldap.sasl-realm` (Optional; type `String`): The SASL realm.
- `cas.authn.spnego.ldap.sasl-security-strength` (Optional; type `String`): SASL security strength.
- `cas.authn.spnego.ldap.search-entry-handlers` (Optional; type `List<LdapSearchEntryHandlersProperties>`): Search handlers.
- `cas.authn.spnego.ldap.search-entry-handlers[0].case-change.attribute-name-case-change` (Optional; type `String`): The Attribute name case change.
- `cas.authn.spnego.ldap.search-entry-handlers[0].case-change.attribute-names` (Optional; type `List<String>`): The Attribute names.
- `cas.authn.spnego.ldap.search-entry-handlers[0].case-change.attribute-value-case-change` (Optional; type `String`): The Attribute value case change.
- `cas.authn.spnego.ldap.search-entry-handlers[0].case-change.dn-case-change` (Optional; type `String`): The Dn case change.
- `cas.authn.spnego.ldap.search-entry-handlers[0].dn-attribute.add-if-exists` (Optional; type `boolean`): The Add if exists.
- `cas.authn.spnego.ldap.search-entry-handlers[0].dn-attribute.dn-attribute-name` (Optional; type `String`; default `entryDN`): The Dn attribute name.
- `cas.authn.spnego.ldap.search-entry-handlers[0].merge-attribute.attribute-names` (Optional; type `List<String>`): The Attribute names.
- `cas.authn.spnego.ldap.search-entry-handlers[0].merge-attribute.merge-attribute-name` (Optional; type `String`): The Merge attribute name.
- `cas.authn.spnego.ldap.search-entry-handlers[0].primary-group-id.base-dn` (Optional; type `String`): The Base dn.
- `cas.authn.spnego.ldap.search-entry-handlers[0].primary-group-id.group-filter` (Optional; type `String`; default `(&(objectClass=group)(objectSid={0}))`): The Group filter.
- `cas.authn.spnego.ldap.search-entry-handlers[0].recursive.merge-attributes` (Optional; type `List<String>`): The Merge attributes.
- `cas.authn.spnego.ldap.search-entry-handlers[0].recursive.search-attribute` (Optional; type `String`): The Search attribute.
- `cas.authn.spnego.ldap.search-entry-handlers[0].search-referral.limit` (Optional; type `int`; default `10`): The default referral limit.
- `cas.authn.spnego.ldap.search-entry-handlers[0].search-result.limit` (Optional; type `int`; default `10`): The default referral limit.
- `cas.authn.spnego.ldap.search-entry-handlers[0].type` (Optional; type `LdapSearchEntryHandlersProperties.SearchEntryHandlerTypes`): The type of search entry handler to choose. Available values are as follows: - `FOLLOW_SEARCH_REFERRAL`: Provides handling of an ldap referral for search operations. - `FOLLOW_SEARCH_RESULT_REFERENCE`: Provides handling of an ldap continuation reference for search operations. - `ACTIVE_DIRECTORY`: Process the entry results fetched from active directory and check for account status controls for disabled/expired accounts, etc. - `OBJECT_GUID`: Object guid search entry handler. Handles the `objectGUID` attribute fetching and conversion. - `OBJECT_SID`: Object sid search entry handler. Handles the `objectSid` attribute fetching and conversion. - `CASE_CHANGE`: Case change search entry handler. Provides the ability to modify the case of search entry DNs, attribute names, and attribute values. - `DN_ATTRIBUTE_ENTRY`: DN attribute entry handler. Adds the entry DN as an attribute to the result set. Provides a client side implementation of RFC 5020. - `MERGE`: Merge search entry handler. Merges the values of one or more attributes into a single attribute. - `PRIMARY_GROUP`: Primary group search handler. Constructs the primary group SID and then searches for that group and puts it's DN in the `memberOf` attribute of the original search entry. - `RANGE_ENTRY`: Range entry search handler. Rewrites attributes returned from Active Directory to include all values by performing additional searches. - `RECURSIVE_ENTRY`: Recursive entry search handler. This recursively searches based on a supplied attribute and merges those results into the original entry. - `MERGE_ENTRIES`: Merge entries handler. Merges the values of one or more attributes in all entries into a single attribute. The merged attribute may or may not already exist on the entry. If it does exist it's existing values will remain intact.
- `cas.authn.spnego.ldap.subtree-search` (Optional; type `Boolean`; default `true`): Whether subtree searching is allowed.
- `cas.authn.spnego.ldap.trust-certificates` (Optional; type `String`): Path of the trust certificates to use for the SSL connection. Ignores keystore-related settings when activated and used.
- `cas.authn.spnego.ldap.trust-manager` (Optional; type `String`): Trust Manager options. Trust managers are responsible for managing the trust material that is used when making LDAP trust decisions, and for deciding whether credentials presented by a peer should be accepted. Accepted values are: - `DEFAULT`: Enable and force the default JVM trust managers. - `ANY`: Trust any client or server.
- `cas.authn.spnego.ldap.trust-store` (Optional; type `String`): Path to the keystore used to determine which certificates or certificate authorities should be trusted. Used when connecting to an LDAP server via LDAPS or startTLS connection. If left blank, the default truststore for the Java runtime is used.
- `cas.authn.spnego.ldap.trust-store-password` (Optional; type `String`): Password needed to open the truststore.
- `cas.authn.spnego.ldap.trust-store-type` (Optional; type `String`): The type of trust keystore that determines which certificates or certificate authorities are trusted. Types depend on underlying java platform, typically `PKCS12` or `JKS`. If left blank, defaults to the default keystore type indicated by the underlying Java platform.
- `cas.authn.spnego.ldap.use-start-tls` (Optional; type `Boolean`; default `false`): Whether TLS should be used and enabled when establishing the connection.
- `cas.authn.spnego.ldap.validate-on-checkout` (Optional; type `Boolean`; default `true`): Whether connections should be validated when loaned out from the pool.
- `cas.authn.spnego.ldap.validate-period` (Optional; type `String`; default `PT5M`): Period at which pool should be validated.
- `cas.authn.spnego.ldap.validate-periodically` (Optional; type `Boolean`; default `true`): Whether connections should be validated periodically when the pool is idle.
- `cas.authn.spnego.ldap.validate-timeout` (Optional; type `String`; default `PT5S`): Period at which validation operations may time out.
- `cas.authn.spnego.ldap.validator.attribute-name` (Optional; type `String`; default `objectClass`): Attribute name to use for the compare validator.
- `cas.authn.spnego.ldap.validator.attribute-value` (Optional; type `String`; default `top`): Attribute values to use for the compare validator.
- `cas.authn.spnego.ldap.validator.base-dn` (Optional; type `String`): Base DN to use for the search request of the search validator.
- `cas.authn.spnego.ldap.validator.dn` (Optional; type `String`): DN to compare to use for the compare validator.
- `cas.authn.spnego.ldap.validator.scope` (Optional; type `String`; default `OBJECT`): Search scope to use for the search request of the search validator.
- `cas.authn.spnego.ldap.validator.search-filter` (Optional; type `String`; default `(objectClass=*)`): Search filter to use for the search request of the search validator.
- `cas.authn.spnego.ldap.validator.type` (Optional; type `String`; default `search`): Determine the LDAP validator type. The following LDAP validators can be used to test connection health status: - `search`: Validates a connection is healthy by performing a search operation. Validation is considered successful if the search result size is greater than zero. - `none`: No validation takes place. - `compare`: Validates a connection is healthy by performing a compare operation.
- `cas.authn.spnego.spnego-attribute-name` (Optional; type `String`; default `distinguishedName`): In case LDAP is used to validate clients, this is the attribute that indicates the host.

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

## Logging

To enable additional logging, configure the log4j configuration file to add the following levels:

```xml
...
<Logger name="jcifs.spnego" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
...
```

## Troubleshooting

- Failure unspecified at GSS-API level

```bash
Caused by: GSSException: Failure unspecified at GSS-API level (Mechanism level: Invalid argument (400) - Cannot find key of appropriate type to decrypt AP REP - AES256 CTS mode with HMAC SHA1-96)
        at sun.security.jgss.krb5.Krb5Context.acceptSecContext(Unknown Source)
        at sun.security.jgss.GSSContextImpl.acceptSecContext(Unknown Source)
        at sun.security.jgss.GSSContextImpl.acceptSecContext(Unknown Source)
        ... 280 more
Caused by: KrbException: Invalid argument (400) - Cannot find key of appropriate type to decrypt 
AP REP - AES256 CTS mode with HMAC SHA1-96
        at sun.security.krb5.KrbApReq.authenticate(Unknown Source)
        at sun.security.krb5.KrbApReq.<init>(Unknown Source)
        at sun.security.jgss.krb5.InitSecContextToken.<init>(Unknown Source)
        ... 283 more
```

It’s very likely that you have the wrong path to `.keytab` file. The KVNO in the keytab file must be identical with the KVNO stored in the Active Directory. Active Directory is raising the KVNO with every execution of ktpass, as part of its `msDS-KeyVersionNumber`.

Other possible causes include:

1. The service principal in the in the CAS configuration is not identical with that from the keytab. (param `/princ` from ktpass)
2. There is no key for the `enctype` sent with the ticket by Active Directory. (param `/crypto` from `ktpass` and set in the `krb5.conf/permitted_enctypes+default_tkt_enctypes`).
3. The KVNO from the ticket is different than the KVNO in the keytab (param `/kvno` from `ktpass`).
