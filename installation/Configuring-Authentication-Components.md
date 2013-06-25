---
layout: default
title: CAS - Configuring Authentication Components
---
# Configuring Authentication Components
The CAS authentication process is performed by several related components:

1. `AuthenticationManager` - Entry point into authentication subsystem. It accepts one or more credentials
and delegates authentication to configured `AuthenticationHandler` components.
It collects the results of each attempt and determines effective security policy.
2. `AuthenticationHandler` - Authenticates a single credential and reports one of three possible results:
success, failure, not attempted.
3. `PrincipalResolver` - Converts information in the authentication credential into a security principal that
commonly contains additional metadata attributes
(i.e. user details such as affiliations, group membership, email, display name).
4. `AuthenticationMetaDataPopulator` - Strategy component for setting arbitrary metadata about a successful
authentication event; these are commonly used to set protocol-specific data.

## AuthenticationManager
CAS ships with a single yet flexible authentication manager, `PolicyBasedAuthenticationManager`, that should be
sufficient for most needs. It performs authentication according to the following contract.

For each given credential do the following:

1. Iterate over all configured authentication handlers.
2. Attempt to authenticate a credential if a handler supports it.
3. On success attempt to resolve a principal.
    1. Check whether a resolver is configured for the handler that authenticated the credential.
    2. If a suitable resolver is found, attempt to resolve the principal.
    3. If a suitable resolver is not found, use the principal resolved by the authentication handler.
4. Check whether the security policy (e.g. any, all) is satisfied.
    1. If security policy is met return immediately.
    2. Continue if security policy is not met.
5. After all credentials have been attempted check security policy again and throw `AuthenticationException`
if not satisfied.

There is an implicit security policy that requires at least one handler to successfully authenticate a credential,
but the behavior can be further controlled by setting `#setAuthenticationPolicy(AuthenticationPolicy)`
with one of the following policies.

* `AnyAuthenticationPolicy` - (Default) Satisfied if any handler succeeds. Supports a `tryAll` flag to avoid short
circuiting at step 4.1 above and try every handler even if one prior succeeded.
* `AllAuthenticationPolicy` - Satisfied if and only if all given credentials are successfully authenticated.
Support for multiple credentials is new in CAS 4.0 and this handler would only be acceptable in a multi-factor
authentication situation.
* `RequiredHandlerAuthenticationPolicy` - Satisfied if an only if a specified handler successfully authenticates
its credential. Supports a `tryAll` flag to avoid short circuiting at step 4.1 above and try every handler even
if one prior succeeded. This policy could be used to support a multi-factor authentication situation, for example,
where username/password authentication is required but an additional OTP is optional.

The following configuration snippet demonstrates how to configure `PolicyBasedAuthenticationManager` for a
straightforward multi-factor authentication case where username/password authentication is required and an additional
OTP credential is optional; in both cases principals are resolved from LDAP.

{% highlight xml %}
<bean id="passwordHandler"
      class="org.jasig.cas.authentication.LdapAuthenticationHandler">
      <!-- Details elided for simplicity -->
</bean>

<bean id="oneTimePasswordHandler"
      class="com.example.cas.authentication.CustomOTPAuthenticationHandler"
      p:name="oneTimePasswordHandler" />

<bean id="authenticationPolicy"
      class="org.jasig.cas.authentication.RequiredHandlerAuthenticationPolicyFactory"
      c:requiredHandlerName="passwordHandler"
      p:tryAll="true" />

<bean id="ldapPrincipalResolver"
      class="org.jasig.cas.authentication.principal.CredentialsToLdapAttributePrincipalResolver">
      <!-- Details elided for simplicity -->
</bean>

<bean id="authenticationManager"
      class="org.jasig.cas.authentication.PolicyBasedAuthenticationManager"
      p:authenticationPolicy-ref="authenticationPolicy">
    <constructor-arg>
        <map>
            <entry key-ref="passwordHandler" value-ref="ldapPrincipalResolver"/>
            <entry key-ref="oneTimePasswordHandler" value-ref="ldapPrincipalResolver" />
        </map>
    </constructor-arg>
    <property name="authenticationMetaDataPopulators">
        <bean class="org.jasig.cas.authentication.SuccessfulHandlerMetaDataPopulator" />
    </property>
</bean>
{% endhighlight %}

## AuthenticationHandler
CAS ships with support for authenticating against many common kinds of authentication systems.
The following list provides a complete list of supported authentication technologies; jump to the section(s) of
interest.

* [Database](#database)
* [LDAP](#ldap)
* OpenID
* OAuth 1.0/2.0
* RADIUS
* Windows (SPNEGO, NTLM)
* X.509 (client SSL certificate)

There are some additional handlers for small deployments and special cases:
* Whilelist
* Blacklist

### Database
Database authentication components are enabled by including the following dependency in the Maven WAR overlay:

    <dependency>
         <groupId>org.jasig.cas</groupId>
         <artifactId>cas-server-support-ldap</artifactId>
         <version>${cas.version}</version>
    </dependency>

CAS provides 3 components to accommodate different database authentication needs:
* `QueryDatabaseAuthenticationHandler` - Authenticates a user by comparing the (hashed) user password against the
password on record determined by an configurable database query.
* `SearchModeSearchDatabaseAuthenticationHandler` - Searches for a user record by querying against a username and
(hashed) password; the user is authenticated if at least one result is found.
* `BindModeSearchDatabaseAuthenticationHandler` - Authenticates a user by attempting to create a database connection
using the username and (hashed) password.

`QueryDatabaseAuthenticationHandler` is by far the most flexible and easiest to configure for anyone proficient with
SQL, but `SearchModeSearchDatabaseAuthenticationHandler` provides a limited alternative for simple queries based
solely on username and password and builds the SQL query using straightforward inputs. The following database schema
for user information is assumed in subsequent examples:

    create table users (
        username varchar(50) not null,
        password varchar(50) not null,
        active bit not null );

#### QueryDatabaseAuthenticationHandler Example
The following example uses an MD5 hash algorithm and searches exclusively for _active_ users.

{% highlight xml %}
<bean id="passwordEncoder" class="org.jasig.cas.authentication.handler.DefaultPasswordEncoder"
        c:encodingAlgorithm="MD5"
        p:characterEncoding="UTF-8" />

<bean id="dbAuthHandler" class="org.jasig.cas.adaptors.jdbc.QueryDatabaseAuthenticationHandler"
      p:passwordEncoder-ref="passwordEncoder"
      p:sql="select password from users where username=? and active=1" />
{% endhighlight %}

#### SearchModeSearchDatabaseAuthenticationHandler Example
The following example uses a SHA1 hash algorithm to authenticate users.

{% highlight xml %}
<bean id="passwordEncoder" class="org.jasig.cas.authentication.handler.DefaultPasswordEncoder"
        c:encodingAlgorithm="SHA1"
        p:characterEncoding="UTF-8" />

<bean id="dbAuthHandler" class="org.jasig.cas.adaptors.jdbc.SearchModeSearchDatabaseAuthenticationHandler"
      p:passwordEncoder-ref="passwordEncoder"
      p:tableUsers="users"
      p:fieldUser="username"
      p:fieldPassword="password" />
{% endhighlight %}

#### BindModeSearchDatabaseAuthenticationHandler
The following example does not perform any password encoding since most JDBC drivers natively encode plaintext
passwords to the appropriate format required by the underlying database. Note authentication is equivalent to the
ability to establish a connection with username/password credentials. This handler is the easiest to configure
(usually none required), but least flexible, of the database authentication components.
    
{% highlight xml %}
<bean id="dbAuthHandler" class="org.jasig.cas.adaptors.jdbc.BindModeSearchDatabaseAuthenticationHandler"/>
{% endhighlight %}

### LDAP
LDAP integration is enabled by including the following dependency in the Maven WAR overlay:

    <dependency>
         <groupId>org.jasig.cas</groupId>
         <artifactId>cas-server-support-ldap</artifactId>
         <version>${cas.version}</version>
    </dependency>

`LdapAuthenticationHandler` authenticates a username/password against an LDAP directory such as Active Directory
or OpenLDAP. The following configuration snippet provides a template for LDAP authentication where it should be
sufficient for most deployments to simply provide values for property placeholders. The example makes use of LDAP
connection pooling, which is _strongly_ recommended for all environments.

#### LdapAuthenticationHandler Example
{% highlight xml %}
<bean id="ldapAuthenticationHandler"
      class="org.jasig.cas.authentication.LdapAuthenticationHandler"
      p:principalIdAttribute="uid">
    <constructor-arg ref="authenticator" />
    <property name="principalAttributeMap">
        <map>
            <!--
               | This map provides a simple attribute resolution mechanism.
               | Keys are LDAP attribute names, values are CAS attribute names.
               | This facility can be used instead or in addition to PrincipalResolver
               | components.
               --> 
            <entry key="member" value="memberOf" />
            <entry key="eduPersonAffiliation" value="affiliation" />
            <entry key="mail" value="mail" />
            <entry key="displayName" value="displayName" />
        </map>
    </property>
</bean>

<!--
   | NOTE:
   | The sslConfig property provides a route to configure custom key/trust stores.
   | The connectionInitializer property provides a means (possibly in addition to sslConfig)
   | to support SASL EXTERNAL binds.
   | See http://www.ldaptive.org/docs/guide/connections for more information.
   -->
<bean id="connectionConfig" class="org.ldaptive.ConnectionConfig"
      p:ldapUrl="${ldap.url}"
      p:connectTimeout="${ldap.connectTimeout}"
      p:useStartTLS="${ldap.useStartTLS}" />

<bean id="connectionFactory" class="org.ldaptive.DefaultConnectionFactory"
    p:connectionConfig-ref="connectionConfig" />

<bean id="ldapPoolConfig" class="org.ldaptive.pool.PoolConfig"
      p:minPoolSize="${ldap.pool.minSize}"
      p:maxPoolSize="${ldap.pool.maxSize}"
      p:validateOnCheckOut="${ldap.pool.validateOnCheckout}"
      p:validatePeriodically="${ldap.pool.validatePeriodically}"
      p:validatePeriod="${ldap.pool.validatePeriod}" />

<bean id="pruneStrategy" class="org.ldaptive.pool.IdlePruneStrategy"
      p:prunePeriod="${ldap.pool.prunePeriod}"
      p:idleTime="${ldap.pool.idleTime}" />

<bean id="searchValidator" class="org.ldaptive.pool.SearchValidator" />

<bean id="connectionPool" class="org.ldaptive.pool.BlockingConnectionPool"
      init-method="initialize"
      p:poolConfig-ref="ldapPoolConfig"
      p:blockWaitTime="${ldap.pool.blockWaitTime}"
      p:validator-ref="searchValidator"
      p:pruneStrategy-ref="pruneStrategy"
      p:connectionFactory-ref="connectionFactory" />

<bean id="pooledLdapConnectionFactory" class="org.ldaptive.pool.PooledConnectionFactory"
      p:connectionPool-ref="connectionPool" />

<!--
   | This configuration uses a connection pool for both search and bind operations.
   | Pooling all operations is strongly recommended.
   -->
<bean id="authenticator" class="org.ldaptive.auth.Authenticator"
      c:resolver-ref="pooledSearchDnResolver"
      c:handler-ref="pooledBindHandler" />

<!--
   | Contrast with org.ldaptive.auth.FormatDnResolver, which constructs bind DN
   | based on a format string using the username as input.
   | FormatDnResolver is preferable for directories that support it, such as Active Directory.
   -->
<bean id="pooledSearchDnResolver" class="org.ldaptive.auth.PooledSearchDnResolver"
      p:baseDn="${ldap.authn.baseDn}"
      p:allowMultipleDns="false"
      p:connectionFactory-ref="pooledLdapConnectionFactory"
      p:userFilter="${ldap.authn.searchFilter}" />

<bean id="pooledBindHandler" class="org.ldaptive.auth.PooledBindAuthenticationHandler"
      p:connectionFactory-ref="pooledLdapConnectionFactory"
      p:authenticationControls-ref="authControls" />
{% endhighlight %}

Please see the [ldaptive documentation](http://www.ldaptive.org/) for more information or to accommodate more
complex configurations. Active Directory users should review 
