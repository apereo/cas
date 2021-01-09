### LDAP Configuration

The following options apply to features that integrate with an LDAP server (i.e. authentication, attribute resolution, etc):

```properties
# {{ include.configKey }}.ldap-url=ldaps://ldap1.example.edu ldaps://ldap2.example.edu
# {{ include.configKey }}.bind-dn=cn=-directory -manager,dc=example,dc=org
# {{ include.configKey }}.bind-credential=Password

# {{ include.configKey }}.pool-passivator=NONE|BIND
# {{ include.configKey }}.connection-strategy=
# {{ include.configKey }}.connect-timeout=PT5S
# {{ include.configKey }}.trust-certificates=
# {{ include.configKey }}.trust-store=
# {{ include.configKey }}.trust-store-password=
# {{ include.configKey }}.trust-store-type=JKS|JCEKS|PKCS12
# {{ include.configKey }}.keystore=
# {{ include.configKey }}.keystore-password=
# {{ include.configKey }}.keystore-type=JKS|JCEKS|PKCS12
# {{ include.configKey }}.disable-pooling=false
# {{ include.configKey }}.min-pool-size=3
# {{ include.configKey }}.max-pool-size=10
# {{ include.configKey }}.validate-on-checkout=true
# {{ include.configKey }}.validate-periodically=true
# {{ include.configKey }}.validate-period=PT5M
# {{ include.configKey }}.validate-timeout=PT5S
# {{ include.configKey }}.fail-fast=true
# {{ include.configKey }}.idle-time=PT10M
# {{ include.configKey }}.prune-period=PT2H
# {{ include.configKey }}.block-wait-time=PT3S

# {{ include.configKey }}.use-start-tls=false
# {{ include.configKey }}.response-timeout=PT5S
# {{ include.configKey }}.allow-multiple-dns=false
# {{ include.configKey }}.allow-multiple-entries=false
# {{ include.configKey }}.follow-referrals=false
# {{ include.configKey }}.binary-attributes=objectGUID,someOtherAttribute
# {{ include.configKey }}.name=
```

### LDAP Connection Initialization

LDAP connection configuration injected into the LDAP connection pool can be initialized with the following parameters:

| Behavior                               | Description
|----------------------------------------|-------------------------------------------------------------------
| `bindDn`/`bindCredential` provided     | Use the provided credentials to bind when initializing connections.
| `bindDn`/`bindCredential` set to `*`   | Use a fast-bind strategy to initialize the pool.
| `bindDn`/`bindCredential` set to blank | Skip connection initializing; perform operations anonymously.
| SASL mechanism provided                | Use the given SASL mechanism to bind when initializing connections.

### LDAP Passivators

The following options can be used to passivate objects when they are checked back into the LDAP connection pool:

| Type                    | Description
|-------------------------|----------------------------------------------------------------------------------------------------
| `NONE`                  | No passivation takes place.
| `BIND`                  | The default behavior which passivates a connection by performing a bind operation on it. This option requires the availability of bind credentials when establishing connections to LDAP.

You may receive unexpected LDAP failures, when CAS is configured to authenticate 
using `DIRECT` or `AUTHENTICATED` types and LDAP is locked down to not allow anonymous 
binds/searches. Every second attempt with a given LDAP connection from the pool would 
fail if it was on the same connection as a failed login attempt, and the regular connection 
validator would similarly fail. When a connection is returned back to a pool, it 
still may contain the principal and credentials from the previous attempt. Before the next 
bind attempt using that connection, the validator tries to validate the connection again 
but fails because it's no longer trying with the configured bind credentials but with 
whatever user DN was used in the previous step. Given the validation failure, the 
connection is closed and CAS would deny access by default. Passivators attempt to 
reconnect to LDAP with the configured bind credentials, effectively resetting the 
connection to what it should be after each bind request.

Furthermore if you are seeing errors in the logs that resemble a *<Operation exception 
encountered, reopening connection>* type of message, this usually is an indication 
that the connection pool's validation timeout established and created by CAS is 
greater than the timeout configured in the LDAP server, or more likely, in the 
load balancer in front of the LDAP servers. You can adjust the LDAP server session's 
timeout for connections, or you can teach CAS to use a validity period that is 
equal or less than the LDAP server session's timeout.

### LDAP Connection Strategies

If multiple URLs are provided as the LDAP url, this describes how each URL will be processed.

| Provider              | Description
|-----------------------|-----------------------------------------------------------------------------------------------
| `ACTIVE_PASSIVE`      | First LDAP will be used for every request unless it fails and then the next shall be used.
| `ROUND_ROBIN`         | For each new connection the next url in the list will be used.
| `RANDOM`              | For each new connection a random LDAP url will be selected.
| `DNS_SRV`             | LDAP urls based on DNS SRV records of the configured/given LDAP url will be used.

### LDAP SASL Mechanisms

```properties
# {{ include.configKey }}.sasl-mechanism=GSSAPI|DIGEST_MD5|CRAM_MD5|EXTERNAL
# {{ include.configKey }}.sasl-realm=EXAMPLE.COM
# {{ include.configKey }}.sasl-authorization-id=
# {{ include.configKey }}.sasl-mutual-auth=
# {{ include.configKey }}.sasl-quality-of-protection=
# {{ include.configKey }}.sasl-security-strength=
```

### LDAP Connection Validators

The following LDAP validators can be used to test connection health status:

| Type                    | Description
|-------------------------|----------------------------------------------------------------------
| `NONE`                  | No validation takes place.
| `SEARCH`                | Validates a connection is healthy by performing a search operation. Validation is considered successful if the search result size is greater than zero.
| `COMPARE`               | Validates a connection is healthy by performing a compare operation.

```properties
# {{ include.configKey }}.validator.type=NONE|SEARCH|COMPARE
# {{ include.configKey }}.validator.base-dn=
# {{ include.configKey }}.validator.search-filter=(object-class=*)
# {{ include.configKey }}.validator.scope=OBJECT|ONELEVEL|SUBTREE
# {{ include.configKey }}.validator.attribute-name=objectClass
# {{ include.configKey }}.validator.attribute-value=top
# {{ include.configKey }}.validator.dn=

```

### LDAP SSL Hostname Verification

The following LDAP validators can be used to test connection health status:

| Type                    | Description
|-------------------------|--------------------------------------------------------------------
| `DEFAULT`               | Default option to enable and force hostname verification of the LDAP SSL configuration.
| `ANY`                   | Skip and ignore the hostname verification of the LDAP SSL configuration.

```properties
#{{ include.configKey }}.hostname-verifier=DEFAULT|ANY
```

### LDAP SSL Trust Managers

Trust managers are responsible for managing the trust material that is used when making LDAP trust decisions,
and for deciding whether credentials presented by a peer should be accepted.

| Type                    | Description
|-------------------------|---------------------------------------------------------------------------------------------
| `DEFAULT`               | Enable and force the default JVM trust managers.
| `ANY`                   | Trust any client or server.

```properties
#{{ include.configKey }}.trust-manager=DEFAULT|ANY
```

### LDAP Types

A number of components/features in CAS allow you to explicitly indicate a `type` for 
the LDAP server, specially in cases where CAS needs to update an attribute, etc in 
LDAP (i.e. consent, password management, etc). The relevant setting would be:

```properties
#{{ include.configKey }}.type=AD|FreeIPA|EDirectory|Generic
```

The following types are supported:

| Type                    | Description
|-------------------------|--------------------------------------------------
| `AD`                                                     | Active Directory.
| `FreeIPA`                                    | FreeIPA Directory Server.
| `EDirectory`                         | NetIQ eDirectory.
| `GENERIC`                              | All other directory servers (i.e OpenLDAP, etc).

### LDAP Authentication/Search Settings

In addition to common LDAP connection settings above, there are cases where CAS simply need to execute
authenticate against an LDAP server to fetch an account or set of attributes or execute a search query in general.
The following  options apply  given the provider's *configuration key*:

**Note:** Failure to specify adequate properties such as `type`, `ldap-url`, etc will
simply deactivate LDAP altogether silently.

```properties
# {{ include.configKey }}.type=AD|AUTHENTICATED|DIRECT|ANONYMOUS

# {{ include.configKey }}.base-dn=dc=example,dc=org
# {{ include.configKey }}.subtree-search=true
# {{ include.configKey }}.search-filter=cn={user}
# {{ include.configKey }}.page-size=0

# {{ include.configKey }}.enhance-with-entry-resolver=true
# {{ include.configKey }}.deref-aliases=NEVER|SEARCHING|FINDING|ALWAYS
# {{ include.configKey }}.dn-format=uid=%s,ou=people,dc=example,dc=org
# {{ include.configKey }}.principal-attribute-password=password
```

The following authentication types are supported:

| Type                    | Description
|-------------------------|----------------------------------------------------------------------------------------------------
| `AD`                    | Active Directory - Users authenticate with `sAMAccountName` typically using a DN format.
| `AUTHENTICATED`         | Manager bind/search type of authentication. If `principalAttributePassword` is empty then a user simple bind is done to validate credentials. Otherwise the given attribute is compared with the given `principalAttributePassword` using the `SHA` encrypted value of it.
| `DIRECT`                | Compute user DN from a format string and perform simple bind. This is relevant when no search is required to compute the DN needed for a bind operation. This option is useful when all users are under a single branch in the directory, e.g. `ou=Users,dc=example,dc=org`, or the username provided on the CAS login form is part of the DN, e.g. `uid=%s,ou=Users,dc=exmaple,dc=org`
| `ANONYMOUS`             | Similar semantics as `AUTHENTICATED` except no `bindDn` and `bindCredential` may be specified to initialize the connection. If `principalAttributePassword` is empty then a user simple bind is done to validate credentials. Otherwise the given attribute is compared with the given `principalAttributePassword` using the `SHA` encrypted value of it.
      
### LDAP Scriptable Search Filter

LDAP search filters can point to an external Groovy script to dynamically construct the final filter template. 
  
```properties
# {{ include.configKey }}.search-filter=file:/path/to/LdapFilterQuery.groovy
```
                                                                      
The script itself may be designed as:

```groovy
import org.ldaptive.*
import org.springframework.context.*

def run(Object[] args) {
    def filter = (FilterTemplate) args[0]
    def parameters = (Map) args[1]
    def applicationContext = (ApplicationContext) args[2]
    def logger = args[3]

    logger.info("Configuring LDAP filter")
    filter.setFilter("uid=something")
}
```

The following parameters are passed to the script:

| Parameter             | Description
|---------------------------------------------------------------------------------------------------------
| `filter`                 | `FilterTemplate` to be updated by the script and used for the LDAP query.
| `parameters`            | Map of query parameters which may be used to construct the final filter.
| `applicationContext`    | Reference to the Spring `ApplicationContext` reference.
| `logger`                | The object responsible for issuing log messages such as `logger.info(...)`.

### LDAP Search Entry Handlers

```properties
# {{ include.configKey }}.search-entry-handlers[0].type=

# {{ include.configKey }}.search-entry-handlers[0].case-change.dn-case-change=NONE|LOWER|UPPER
# {{ include.configKey }}.search-entry-handlers[0].case-change.attribute-name-case-change=NONE|LOWER|UPPER
# {{ include.configKey }}.search-entry-handlers[0].case-change.attribute-value-case-change=NONE|LOWER|UPPER
# {{ include.configKey }}.search-entry-handlers[0].case-change.attribute-names=

# {{ include.configKey }}.search-entry-handlers[0].dn-attribute.dn-attribute-name=entryDN
# {{ include.configKey }}.search-entry-handlers[0].dn-attribute.add-if-exists=false

# {{ include.configKey }}.search-entry-handlers[0].primary-group-id.group-filter=(&(object-class=group)(object-sid={0}))
# {{ include.configKey }}.search-entry-handlers[0].primary-group-id.base-dn=

# {{ include.configKey }}.search-entry-handlers[0].merge-attribute.merge-attribute-name=
# {{ include.configKey }}.search-entry-handlers[0].merge-attribute.attribute-names=

# {{ include.configKey }}.search-entry-handlers[0].recursive.search-attribute=
# {{ include.configKey }}.search-entry-handlers[0].recursive.merge-attributes=
```

The following types are supported:

| Type                    | Description
|-------------------------|----------------------------------------------------------------------------------------------------
| `CASE_CHANGE` | Provides the ability to modify the case of search entry DNs, attribute names, and attribute values.
| `DN_ATTRIBUTE_ENTRY` | Adds the entry DN as an attribute to the result set. Provides a client side implementation of RFC 5020.
| `MERGE` | Merges the values of one or more attributes into a single attribute.
| `OBJECT_GUID` | Handles the `objectGUID` attribute fetching and conversion.
| `OBJECT_SID` | Handles the `objectSid` attribute fetching and conversion.
| `PRIMARY_GROUP` | Constructs the primary group SID and then searches for that group and puts it's DN in the 'memberOf' attribute of the original search entry.
| `RANGE_ENTRY` |  Rewrites attributes returned from Active Directory to include all values by performing additional searches.
| `RECURSIVE_ENTRY` | This recursively searches based on a supplied attribute and merges those results into the original entry.

### LDAP Multiple Base DNs

There may be scenarios where different parts of a single LDAP tree could be considered as base-dns. Rather than duplicating
the LDAP configuration block for each individual base-dn, each entry can be specified 
and joined together using a special delimiter character. The user DN is retrieved using the combination of all base-dn and DN 
resolvers in the order defined. DN resolution should fail if multiple DNs are found. Otherwise the first DN found is returned.

```properties
# {{ include.configKey }}.base-dn=subtreeA,dc=example,dc=net|subtreeC,dc=example,dc=net
```
