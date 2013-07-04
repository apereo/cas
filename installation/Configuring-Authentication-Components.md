---
layout: default
title: CAS - Configuring Authentication Components
---
# Configuring Authentication Components
The CAS authentication process is performed by several related components:

######`AuthenticationManager`
Entry point into authentication subsystem. It accepts one or more credentials and delegates authentication to
configured `AuthenticationHandler` components. It collects the results of each attempt and determines effective
security policy.

######`AuthenticationHandler`
Authenticates a single credential and reports one of three possible results: success, failure, not attempted.

######`PrincipalResolver`
Converts information in the authentication credential into a security principal that commonly contains additional
metadata attributes (i.e. user details such as affiliations, group membership, email, display name).

######`AuthenticationMetaDataPopulator`
Strategy component for setting arbitrary metadata about a successful authentication event; these are commonly used
to set protocol-specific data.

Unless otherwise noted, the configuration for all authentication components is handled in `deployerConfigContext.xml`.

## Authentication Manager
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

######`AnyAuthenticationPolicy`
Satisfied if any handler succeeds. Supports a `tryAll` flag to avoid short circuiting at step 4.1 above and try every
handler even if one prior succeeded. This policy is the default and provides backward-compatible behavior with the
`AuthenticationManagerImpl` component of CAS 3.x.

######`AllAuthenticationPolicy`
Satisfied if and only if all given credentials are successfully authenticated. Support for multiple credentials is
new in CAS 4.0 and this handler would only be acceptable in a multi-factor authentication situation.

######`RequiredHandlerAuthenticationPolicy`
Satisfied if an only if a specified handler successfully authenticates its credential. Supports a `tryAll` flag to
avoid short circuiting at step 4.1 above and try every handler even if one prior succeeded. This policy could be
used to support a multi-factor authentication situation, for example, where username/password authentication is
required but an additional OTP is optional.

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
    <list>
      <bean class="org.jasig.cas.authentication.SuccessfulHandlerMetaDataPopulator" />
    </list>
  </property>
</bean>
{% endhighlight %}

## Authentication Handlers
CAS ships with support for authenticating against many common kinds of authentication systems.
The following list provides a complete list of supported authentication technologies; jump to the section(s) of
interest.

* [Database](#database)
* [JAAS](#jaas)
* [LDAP](#ldap)
* [OpenID](#openid)
* [OAuth 1.0/2.0](#oauth)
* [RADIUS](#radius)
* [SPNEGO](#spnego) (Windows)
* [Trusted](#trusted) (REMOTE_USER)
* [X.509](#x_509) (client SSL certificate)

There are some additional handlers for small deployments and special cases:
* Whilelist
* Blacklist

### Database
Database authentication components are enabled by including the following dependencies in the Maven WAR overlay:

    <dependency>
         <groupId>org.jasig.cas</groupId>
         <artifactId>cas-server-support-ldap</artifactId>
         <version>${cas.version}</version>
    </dependency>
    <dependency>
        <groupId>c3p0</groupId>
        <artifactId>c3p0</artifactId>
        <version>0.9.1.2</version>
    </dependency>

#### Connection Pooling
All database authentication components require a `DataSource` for acquiring connections to the underlying database.
The use of connection pooling is _strongly_ recommended, and the [c3p0 library](http://www.mchange.com/projects/c3p0/)
is a good choice that we discuss here.
[Tomcat JDBC Pool](http://tomcat.apache.org/tomcat-7.0-doc/jdbc-pool.html) is another competent alternative.
Note that the connection pool dependency mentioned above should be modified according to the choice of connection pool
components.

##### Pooled Data Source Example
A bean named `dataSource` must be defined for CAS components that use a database. A bean like the following should be
defined in `deployerConfigContext.xml`.
{% highlight xml %}
<bean id="dataSource"
  class="com.mchange.v2.c3p0.ComboPooledDataSource"
  p:driverClass="${database.driverClass}"
  p:jdbcUrl="${database.url}"
  p:user="${database.user}"
  p:password="${database.password}"
  p:initialPoolSize="${database.pool.minSize}"
  p:minPoolSize="${database.pool.minSize}"
  p:maxPoolSize="${database.pool.maxSize}"
  p:maxIdleTimeExcessConnections="${database.pool.maxIdleTime}"
  p:checkoutTimeout="${database.pool.maxWait}"
  p:acquireIncrement="${database.pool.acquireIncrement}"
  p:acquireRetryAttempts="${database.pool.acquireRetryAttempts}"
  p:acquireRetryDelay="${database.pool.acquireRetryDelay}"
  p:idleConnectionTestPeriod="${database.pool.idleConnectionTestPeriod}"
  p:preferredTestQuery="${database.pool.connectionHealthQuery}" />
{% endhighlight %}

The following properties may be used as a starting point for connection pool configuration/tuning.

    # == Basic database connection pool configuration ==
    database.driverClass=org.postgresql.Driver
    database.url=jdbc:postgresql://database.example.com/cas?ssl=true
    database.user=somebody
    database.password=meaningless
    database.pool.minSize=6
    database.pool.maxSize=18
     
    # Maximum amount of time to wait in ms for a connection to become
    # available when the pool is exhausted
    database.pool.maxWait=10000
     
    # Amount of time in seconds after which idle connections
    # in excess of minimum size are pruned.
    database.pool.maxIdleTime=120
     
    # Number of connections to obtain on pool exhaustion condition.
    # The maximum pool size is always respected when acquiring
    # new connections.
    database.pool.acquireIncrement=6
     
    # == Connection testing settings ==
     
    # Period in s at which a health query will be issued on idle
    # connections to determine connection liveliness.
    database.pool.idleConnectionTestPeriod=30
     
    # Query executed periodically to test health
    database.pool.connectionHealthQuery=select 1
     
    # == Database recovery settings ==
     
    # Number of times to retry acquiring a _new_ connection
    # when an error is encountered during acquisition.
    database.pool.acquireRetryAttempts=5
     
    # Amount of time in ms to wait between successive aquire retry attempts.
    database.pool.acquireRetryDelay=2000


#### Database Components
CAS provides 3 components to accommodate different database authentication needs.

######`QueryDatabaseAuthenticationHandler`
Authenticates a user by comparing the (hashed) user password against the password on record determined by a
configurable database query. `QueryDatabaseAuthenticationHandler` is by far the most flexible and easiest to
configure for anyone proficient with SQL, but `SearchModeSearchDatabaseAuthenticationHandler` provides an alternative
for simple queries based solely on username and password and builds the SQL query using straightforward inputs.

The following database schema for user data is assumed in the following two examples that leverage SQL queries
to authenticate users.

    create table users (
        username varchar(50) not null,
        password varchar(50) not null,
        active bit not null );

The following example uses an MD5 hash algorithm and searches exclusively for _active_ users.
{% highlight xml %}
<bean id="passwordEncoder"
      class="org.jasig.cas.authentication.handler.DefaultPasswordEncoder"
      c:encodingAlgorithm="MD5"
      p:characterEncoding="UTF-8" />

<bean id="dbAuthHandler"
      class="org.jasig.cas.adaptors.jdbc.QueryDatabaseAuthenticationHandler"
      p:dataSource-ref="dataSource"
      p:passwordEncoder-ref="passwordEncoder"
      p:sql="select password from users where username=? and active=1" />
{% endhighlight %}

######`SearchModeSearchDatabaseAuthenticationHandler`
Searches for a user record by querying against a username and (hashed) password; the user is authenticated if at
least one result is found.

The following example uses a SHA1 hash algorithm to authenticate users.
{% highlight xml %}
<bean id="passwordEncoder"
      class="org.jasig.cas.authentication.handler.DefaultPasswordEncoder"
      c:encodingAlgorithm="SHA1"
      p:characterEncoding="UTF-8" />

<bean id="dbAuthHandler"
      class="org.jasig.cas.adaptors.jdbc.SearchModeSearchDatabaseAuthenticationHandler"
      p:dataSource-ref="dataSource"
      p:passwordEncoder-ref="passwordEncoder"
      p:tableUsers="users"
      p:fieldUser="username"
      p:fieldPassword="password" />
{% endhighlight %}

######`BindModeSearchDatabaseAuthenticationHandler`
Authenticates a user by attempting to create a database connection using the username and (hashed) password.

The following example does not perform any password encoding since most JDBC drivers natively encode plaintext
passwords to the appropriate format required by the underlying database. Note authentication is equivalent to the
ability to establish a connection with username/password credentials. This handler is the easiest to configure
(usually none required), but least flexible, of the database authentication components.
{% highlight xml %}
<bean id="dbAuthHandler"
      class="org.jasig.cas.adaptors.jdbc.BindModeSearchDatabaseAuthenticationHandler"
      p:dataSource-ref="dataSource" />
{% endhighlight %}

### JAAS
[JAAS](http://docs.oracle.com/javase/6/docs/technotes/guides/security/jaas/JAASRefGuide.html) is a Java standard
authentication and authorization API. JAAS is configured via externalized plain text configuration file.
Using JAAS with CAS allows modification of the authentication process without having to rebuild and redeploy CAS
and allows for PAM-style multi-module "stacked" authentication.

#### JAAS Components
JAAS components are provided in the CAS core module and require no additional dependencies to use.

######`JaasAuthenticationHandler`
The JAAS handler delegates to the built-in JAAS subsystem to perform authentication according to the
directives in the JAAS config file. The handler only exposes a single configuration property:

* `realm` - JAAS realm name. (Defaults to _CAS_.)

The following configuration excerpt demonstrates how to configure the JAAS handler in `deployerConfigContext.xml`:

{% highlight xml %}
<bean class="org.jasig.cas.authentication.handler.support.JaasAuthenticationHandler"
      p:realm="CustomCasRealm" />
{% endhighlight %}

#### JAAS Configuration File
The default JAAS configuration file is located at `$JRE_HOME/lib/security/java.security`. It's important to note
that JAAS configuration applies to the entire JVM. The path to the JAAS configuration file in effect may be altered
by setting the `java.security.auth.login.config` system property to an alternate file path.
A sample JAAS configuration file is provided for reference.

    /**
      * Login Configuration for JAAS. First try Kerberos, then LDAP, then AD
      * Note that a valid krb5.conf must be supplied to the JVM for Kerberos auth
      * -Djava.security.krb5.conf=/etc/krb5.conf
      */
    CAS {
      com.ibm.security.auth.module.Krb5LoginModule sufficient
        debug=FALSE;
        edu.uconn.netid.jaas.LDAPLoginModule sufficient
        java.naming.provider.url="ldap://ldap.my.org:389/dc=my,dc=org"
        java.naming.security.principal="uid=cas,dc=my,dc=org"
        java.naming.security.credentials="password"
        Attribute="uid"
        startTLS="true";
      edu.uconn.netid.jaas.LDAPLoginModule sufficient
        java.naming.provider.url="ldaps://ad.my.org:636/dc=ad,dc=my,dc=org"
        java.naming.security.principal="cas@ad.my.org"
        java.naming.security.credentials="password"
        Attribute="sAMAccountName";
    };

### LDAP
LDAP integration is enabled by including the following dependency in the Maven WAR overlay:

    <dependency>
         <groupId>org.jasig.cas</groupId>
         <artifactId>cas-server-support-ldap</artifactId>
         <version>${cas.version}</version>
    </dependency>

`LdapAuthenticationHandler` authenticates a username/password against an LDAP directory such as Active Directory
or OpenLDAP. There are numerous directory architectures and we provide configuration for four common cases:

1. [Active Directory](#active_directory_authentication) - Users authenticate with sAMAAccount name.
2. [Authenticated Search](#ldap_requiring_authenticated_search) - Manager bind/search followed by user simple bind.
3. [Anonymous Search](#ldap_supporting_anonymous_search) - Anonymous search followed by user simple bind.
4. [Direct Bind](#ldap_supporting_direct_bind) - Compute user DN from format string and perform simple bind.

See the [ldaptive documentation](http://www.ldaptive.org/) for more information or to accommodate other situations.

#### Active Directory Authentication
The following configuration authenticates users with the [Fast Bind](http://www.ldaptive.org/docs/guide/ad/fastbind)
mechanism, which should be sufficient for most Active Directory deployments. Simply copy the configuration to
`deployerConfigContext.xml` and provide values for property placeholders.
{% highlight xml %}
<bean id="ldapAuthenticationHandler"
      class="org.jasig.cas.authentication.LdapAuthenticationHandler"
      p:principalIdAttribute="sAMAccountName"
      c:authenticator-ref="authenticator">
  <property name="principalAttributeMap">
      <map>
          <!--
             | This map provides a simple attribute resolution mechanism.
             | Keys are LDAP attribute names, values are CAS attribute names.
             | Use this facility instead of a PrincipalResolver if LDAP is
             | the only attribute source.
             --> 
          <entry key="displayName" value="displayName" />
          <entry key="mail" value="mail" />
          <entry key="memberOf" value="memberOf" />
      </map>
  </property>
</bean>

<bean id="authenticator" class="org.ldaptive.auth.Authenticator"
      c:resolver-ref="dnResolver"
      c:handler-ref="authHandler" />

<bean id="dnResolver"
      class="org.ldaptive.auth.FormatDnResolver"
      c:format="${ldap.authn.format}" />

<bean id="authHandler" class="org.ldaptive.auth.PooledBindAuthenticationHandler"
      p:connectionFactory-ref="pooledLdapConnectionFactory" />

<bean id="pooledLdapConnectionFactory"
      class="org.ldaptive.pool.PooledConnectionFactory"
      p:connectionPool-ref="connectionPool" />

<bean id="connectionPool"
      class="org.ldaptive.pool.BlockingConnectionPool"
      init-method="initialize"
      p:poolConfig-ref="ldapPoolConfig"
      p:blockWaitTime="${ldap.pool.blockWaitTime}"
      p:validator-ref="searchValidator"
      p:pruneStrategy-ref="pruneStrategy"
      p:connectionFactory-ref="connectionFactory" />

<bean id="ldapPoolConfig" class="org.ldaptive.pool.PoolConfig"
      p:minPoolSize="${ldap.pool.minSize}"
      p:maxPoolSize="${ldap.pool.maxSize}"
      p:validateOnCheckOut="${ldap.pool.validateOnCheckout}"
      p:validatePeriodically="${ldap.pool.validatePeriodically}"
      p:validatePeriod="${ldap.pool.validatePeriod}" />

<bean id="connectionFactory" class="org.ldaptive.DefaultConnectionFactory"
      p:connectionConfig-ref="connectionConfig" />

<bean id="connectionConfig" class="org.ldaptive.ConnectionConfig"
      p:ldapUrl="${ldap.url}"
      p:connectTimeout="${ldap.connectTimeout}"
      p:useStartTLS="${ldap.useStartTLS}"
      p:connectionInitializer-ref="fastBindConnectionInitializer" />

<bean id="fastBindConnectionInitializer"
      class="org.ldaptive.ad.extended.FastBindOperation.FastBindConnectionInitializer" />

<bean id="pruneStrategy" class="org.ldaptive.pool.IdlePruneStrategy"
      p:prunePeriod="${ldap.pool.prunePeriod}"
      p:idleTime="${ldap.pool.idleTime}" />

<bean id="searchValidator" class="org.ldaptive.pool.SearchValidator" />
{% endhighlight %}

#### LDAP Requiring Authenticated Search
The following configuration snippet provides a template for LDAP authentication performed with manager credentials
followed by a bind. Copy the configuration to `deployerConfigContext.xml` and provide values for property placeholders.
{% highlight xml %}
<bean id="ldapAuthenticationHandler"
      class="org.jasig.cas.authentication.LdapAuthenticationHandler"
      p:principalIdAttribute="uid"
      c:authenticator-ref="authenticator">
  <property name="principalAttributeMap">
      <map>
          <!--
             | This map provides a simple attribute resolution mechanism.
             | Keys are LDAP attribute names, values are CAS attribute names.
             | Use this facility instead of a PrincipalResolver if LDAP is
             | the only attribute source.
             --> 
          <entry key="member" value="member" />
          <entry key="eduPersonAffiliation" value="affiliation" />
          <entry key="mail" value="mail" />
          <entry key="displayName" value="displayName" />
      </map>
  </property>
</bean>

<bean id="authenticator" class="org.ldaptive.auth.Authenticator"
      c:resolver-ref="dnResolver"
      c:handler-ref="authHandler" />

<bean id="dnResolver" class="org.ldaptive.auth.PooledSearchDnResolver"
      p:baseDn="${ldap.authn.baseDn}"
      p:allowMultipleDns="false"
      p:connectionFactory-ref="searchPooledLdapConnectionFactory"
      p:userFilter="${ldap.authn.searchFilter}" />

<bean id="searchPooledLdapConnectionFactory"
      class="org.ldaptive.pool.PooledConnectionFactory"
      p:connectionPool-ref="searchConnectionPool" />

<bean id="searchConnectionPool" parent="abstractConnectionPool"
      p:connectionFactory-ref="searchConnectionFactory" />

<bean id="searchConnectionFactory"
      class="org.ldaptive.DefaultConnectionFactory"
      p:connectionConfig-ref="searchConnectionConfig" />

<bean id="searchConnectionConfig" parent="abstractConnectionConfig"
      p:connectionInitializer-ref="bindConnectionInitializer" />

<bean id="bindConnectionInitializer"
      class="org.ldaptive.BindConnectionInitializer"
      p:bindDn="${ldap.authn.managerDN}">
  <property name="credential">
    <bean class="org.ldaptive.Credential"
          c:password="${ldap.authn.managerPassword}" />
  </property>
</bean>

<bean id="abstractConnectionPool" abstract="true"
      class="org.ldaptive.pool.BlockingConnectionPool"
      init-method="initialize"
      p:poolConfig-ref="ldapPoolConfig"
      p:blockWaitTime="${ldap.pool.blockWaitTime}"
      p:validator-ref="searchValidator"
      p:pruneStrategy-ref="pruneStrategy" />

<bean id="abstractConnectionConfig" abstract="true"
      class="org.ldaptive.ConnectionConfig"
      p:ldapUrl="${ldap.url}"
      p:connectTimeout="${ldap.connectTimeout}"
      p:useStartTLS="${ldap.useStartTLS}" />

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

<bean id="authHandler" class="org.ldaptive.auth.PooledBindAuthenticationHandler"
      p:connectionFactory-ref="bindPooledLdapConnectionFactory" />

<bean id="bindPooledLdapConnectionFactory"
      class="org.ldaptive.pool.PooledConnectionFactory"
      p:connectionPool-ref="bindConnectionPool" />

<bean id="bindConnectionPool" parent="abstractConnectionPool"
      p:connectionFactory-ref="bindConnectionFactory" />

<bean id="bindConnectionFactory"
      class="org.ldaptive.DefaultConnectionFactory"
      p:connectionConfig-ref="bindConnectionConfig" />

<bean id="bindConnectionConfig" parent="abstractConnectionConfig" />
{% endhighlight %}


#### LDAP Supporting Anonymous Search
The following configuration snippet provides a template for LDAP authentication performed with an anonymous search
followed by a bind. Copy the configuration to `deployerConfigContext.xml` and provide values for property placeholders.
{% highlight xml %}
<bean id="ldapAuthenticationHandler"
      class="org.jasig.cas.authentication.LdapAuthenticationHandler"
      p:principalIdAttribute="uid"
      c:authenticator-ref="authenticator">
  <property name="principalAttributeMap">
      <map>
          <!--
             | This map provides a simple attribute resolution mechanism.
             | Keys are LDAP attribute names, values are CAS attribute names.
             | Use this facility instead of a PrincipalResolver if LDAP is
             | the only attribute source.
             --> 
          <entry key="member" value="member" />
          <entry key="eduPersonAffiliation" value="affiliation" />
          <entry key="mail" value="mail" />
          <entry key="displayName" value="displayName" />
      </map>
  </property>
</bean>

<bean id="authenticator" class="org.ldaptive.auth.Authenticator"
      c:resolver-ref="dnResolver"
      c:handler-ref="authHandler" />

<bean id="dnResolver" class="org.ldaptive.auth.PooledSearchDnResolver"
      p:baseDn="${ldap.authn.baseDn}"
      p:allowMultipleDns="false"
      p:connectionFactory-ref="searchPooledLdapConnectionFactory"
      p:userFilter="${ldap.authn.searchFilter}" />

<bean id="searchPooledLdapConnectionFactory"
      class="org.ldaptive.pool.PooledConnectionFactory"
      p:connectionPool-ref="searchConnectionPool" />

<bean id="searchConnectionPool" parent="abstractConnectionPool" />

<bean id="abstractConnectionPool" abstract="true"
      class="org.ldaptive.pool.BlockingConnectionPool"
      init-method="initialize"
      p:poolConfig-ref="ldapPoolConfig"
      p:blockWaitTime="${ldap.pool.blockWaitTime}"
      p:validator-ref="searchValidator"
      p:pruneStrategy-ref="pruneStrategy"
      p:connectionFactory-ref="connectionFactory" />

<bean id="ldapPoolConfig" class="org.ldaptive.pool.PoolConfig"
      p:minPoolSize="${ldap.pool.minSize}"
      p:maxPoolSize="${ldap.pool.maxSize}"
      p:validateOnCheckOut="${ldap.pool.validateOnCheckout}"
      p:validatePeriodically="${ldap.pool.validatePeriodically}"
      p:validatePeriod="${ldap.pool.validatePeriod}" />

<bean id="connectionFactory" class="org.ldaptive.DefaultConnectionFactory"
      p:connectionConfig-ref="connectionConfig" />

<bean id="connectionConfig" class="org.ldaptive.ConnectionConfig"
      p:ldapUrl="${ldap.url}"
      p:connectTimeout="${ldap.connectTimeout}"
      p:useStartTLS="${ldap.useStartTLS}" />

<bean id="pruneStrategy" class="org.ldaptive.pool.IdlePruneStrategy"
      p:prunePeriod="${ldap.pool.prunePeriod}"
      p:idleTime="${ldap.pool.idleTime}" />

<bean id="searchValidator" class="org.ldaptive.pool.SearchValidator" />

<bean id="authHandler" class="org.ldaptive.auth.PooledBindAuthenticationHandler"
      p:connectionFactory-ref="bindPooledLdapConnectionFactory" />

<bean id="bindPooledLdapConnectionFactory"
      class="org.ldaptive.pool.PooledConnectionFactory"
      p:connectionPool-ref="bindConnectionPool" />

<bean id="bindConnectionPool" parent="abstractConnectionPool" />
{% endhighlight %}

#### LDAP Supporting Direct Bind
The following configuration snippet provides a template for LDAP authentication where no search is required to
compute the DN needed for a bind operation. There are two requirements for this use case:

1. All users are under a single branch in the directory, e.g. `ou=Users,dc=example,dc=org`.
2. The username provided on the CAS login form is part of the DN, e.g. `uid=%s,ou=Users,dc=exmaple,dc=org`.

Copy the configuration to `deployerConfigContext.xml` and provide values for property placeholders.
{% highlight xml %}
<bean id="ldapAuthenticationHandler"
      class="org.jasig.cas.authentication.LdapAuthenticationHandler"
      p:principalIdAttribute="uid"
      c:authenticator-ref="authenticator">
  <property name="principalAttributeMap">
      <map>
          <!--
             | This map provides a simple attribute resolution mechanism.
             | Keys are LDAP attribute names, values are CAS attribute names.
             | Use this facility instead of a PrincipalResolver if LDAP is
             | the only attribute source.
             --> 
          <entry key="member" value="member" />
          <entry key="eduPersonAffiliation" value="affiliation" />
          <entry key="mail" value="mail" />
          <entry key="displayName" value="displayName" />
      </map>
  </property>
</bean>

<bean id="authenticator" class="org.ldaptive.auth.Authenticator"
      c:resolver-ref="dnResolver"
      c:handler-ref="authHandler" />

<bean id="dnResolver"
      class="org.ldaptive.auth.FormatDnResolver"
      c:format="${ldap.authn.format}" />

<bean id="authHandler" class="org.ldaptive.auth.PooledBindAuthenticationHandler"
      p:connectionFactory-ref="pooledLdapConnectionFactory" />

<bean id="pooledLdapConnectionFactory"
      class="org.ldaptive.pool.PooledConnectionFactory"
      p:connectionPool-ref="connectionPool" />

<bean id="connectionPool"
      class="org.ldaptive.pool.BlockingConnectionPool"
      init-method="initialize"
      p:poolConfig-ref="ldapPoolConfig"
      p:blockWaitTime="${ldap.pool.blockWaitTime}"
      p:validator-ref="searchValidator"
      p:pruneStrategy-ref="pruneStrategy"
      p:connectionFactory-ref="connectionFactory" />

<bean id="ldapPoolConfig" class="org.ldaptive.pool.PoolConfig"
      p:minPoolSize="${ldap.pool.minSize}"
      p:maxPoolSize="${ldap.pool.maxSize}"
      p:validateOnCheckOut="${ldap.pool.validateOnCheckout}"
      p:validatePeriodically="${ldap.pool.validatePeriodically}"
      p:validatePeriod="${ldap.pool.validatePeriod}" />

<bean id="connectionFactory" class="org.ldaptive.DefaultConnectionFactory"
      p:connectionConfig-ref="connectionConfig" />

<bean id="connectionConfig" class="org.ldaptive.ConnectionConfig"
      p:ldapUrl="${ldap.url}"
      p:connectTimeout="${ldap.connectTimeout}"
      p:useStartTLS="${ldap.useStartTLS}" />

<bean id="pruneStrategy" class="org.ldaptive.pool.IdlePruneStrategy"
      p:prunePeriod="${ldap.pool.prunePeriod}"
      p:idleTime="${ldap.pool.idleTime}" />

<bean id="searchValidator" class="org.ldaptive.pool.SearchValidator" />
{% endhighlight %}

#### LDAP Properties Starter
The following LDAP configuration properties provide a reasonable starting point for configuring the LDAP
authentication handler. The `ldap.url` property must be changed at a minumum. LDAP properties may be added to the
`cas.properties` configuration file; alternatively they may be isolated in an `ldap.properties` file and loaded
into the Spring application context by modifying the `propertyFileConfigurer.xml` configuration file.

    #========================================
    # General properties
    #========================================
    ldap.url=ldap://directory.ldaptive.org

    # LDAP connection timeout in milliseconds
    ldap.connectTimeout=3000

    # Whether to use StartTLS (probably needed if not SSL connection)
    ldap.useStartTLS=true

    #========================================
    # LDAP connection pool configuration
    #========================================
    ldap.pool.minSize=3
    ldap.pool.maxSize=10
    ldap.pool.validateOnCheckout=false
    ldap.pool.validatePeriodically=true

    # Amount of time in milliseconds to block on pool exhausted condition
    # before giving up.
    ldap.pool.blockWaitTime=3000

    # Frequency of connection validation in seconds
    # Only applies if validatePeriodically=true
    ldap.pool.validatePeriod=300

    # Attempt to prune connections every N seconds
    ldap.pool.prunePeriod=300

    # Maximum amount of time an idle connection is allowed to be in
    # pool before it is liable to be removed/destroyed
    ldap.pool.idleTime=600

    #========================================
    # Authentication
    #========================================

    # Base DN of users to be authenticated
    ldap.authn.baseDn=ou=Users,dc=example,dc=org

    # Manager DN for authenticated searches
    ldap.authn.managerDN=uid=manager,ou=Users,dc=example,dc=org

    # Manager password for authenticated searches
    ldap.authn.managerPassword=nonsense

    # Search filter used for configurations that require searching for DNs
    #ldap.authn.searchFilter=(&(uid={user})(accountState=active))
    ldap.authn.searchFilter=(uid={user})
    
    # Search filter used for configurations that require searching for DNs
    #ldap.authn.format=uid=%s,ou=Users,dc=example,dc=org
    ldap.authn.format=%s@example.com

### OpenID
_TBD_: @leleuj

### OAuth
_TBD_: @leleuj

### RADIUS
RADIUS support is enabled by including the following dependency in the Maven WAR overlay:

    <dependency>
      <groupId>org.jasig.cas</groupId>
      <artifactId>cas-server-support-radius</artifactId>
      <version>${cas.version}</version>
    </dependency>

#### RADIUS Components
######`RadiusAuthenticationHandler`
The RADIUS handler accepts username/password credentials and delegates authentication to one or more RADIUS
servers. It supports two types of failovers: failover on an authentication failure, and failover on a server exception.

* `failoverOnAuthenticationFailure` - True to continue to the next configured RADIUS server on authentication failure,
false otherwise. This flag is typically set when user accounts are spread across one or more RADIUS servers.
* `failoverOnException` - True to continue to next configured RADIUS server on an error other than authentication
failure, false otherwise. This flag is typically set to support highly available deployments where authentication
should proceed in the face of one or more RADIUS server failures.
* `servers` - Array of RADIUS servers to delegate to for authentication.

######`JRadiusServerImpl`
Component representing a RADIUS server has the following configuration properties.

* `hostName` - the hostname of the RADIUS server.
* `sharedSecret` - the secret key used to communicate with the server.
* `radiusAuthenticator` - the RADIUS authenticator to use. Defaults to PAP.
* `authenticationPort` - the authentication port this server uses.
* `accountingPort` - the accounting port that this server uses.
* `socketTimeout` - the amount of time to wait before timing out.
* `retries` - the number of times to keep retrying a particular server on communication failure/timeout.

#### RADIUS Configuration Example
{% highlight xml %}
<bean id="papAuthenticator" class="net.jradius.client.auth.PAPAuthenticator" />

<bean id="abstractServer" class="org.jasig.cas.adaptors.radius.JRadiusServerImpl" abstract="true"
      c:sharedSecret="32_or_more_random_characters"
      c:radiusAuthenticator-ref="papAuthenticator"
      c:authenticationPort="1812"
      c:accountingPort="1813"
      c:socketTimeout="5"
      c:retries="3" />

<bean class="org.jasig.cas.adaptors.radius.authentication.handler.support.RadiusAuthenticationHandler"
      p:failoverOnAuthenticationFailure="false"
      p:failoverOnException="true">
  <property name="servers">
    <list>
      <bean parent="abstractServer" c:hostName="radius1.example.org" />
      <bean parent="abstractServer" c:hostName="radius2.example.org" />
  </property>
</bean>
{% endhighlight %}

### SPNEGO
[SPNEGO](http://en.wikipedia.org/wiki/SPNEGO) is an authentication technology that is primarily used to provide
transparent CAS authentication to browsers running on Windows running under Active Directory domain credentials.
There are three actors involved: the client, the CAS server, and the Active Directory Domain Controller/KDC.

#### SPNEGO Requirements
* Client is logged in to a Windows Active Directory domain.
* Supported browser and JDK.
* CAS is running MIT kerberos against the AD domain controller.

#### SPNEGO Authentication Process
    1. Client sends CAS:               HTTP GET to CAS  for cas protected page
    2. CAS responds:                   HTTP 401 - Access Denied WWW-Authenticate: Negotiate
    3. Client sends ticket request:    Kerberos(KRB_TGS_REQ) Requesting ticket for HTTP/cas.example.com@REALM
    4. Kerberos KDC responds:          Kerberos(KRB_TGS_REP) Granting ticket for HTTP/cas.example.com@REALM
    5. Client sends CAS:               HTTP GET Authorization: Negotiate w/SPNEGO Token
    6. CAS responds:                   HTTP 200 - OK WWW-Authenticate w/SPNEGO response + requested page.

The above interaction occurs only for the first request, when there is no CAS ticket-granting ticket associated with
the user session. Once CAS grants a ticket-granting ticket, the SPNEGO process will not happen again until the CAS
ticket expires.

#### SPNEGO Components
SPNEGO support is enabled by including the following dependency in the Maven WAR overlay:

    <dependency>
      <groupId>org.jasig.cas</groupId>
      <artifactId>cas-server-support-spnego</artifactId>
      <version>${cas.version}</version>
    </dependency>

######`JCIFSSpnegoAuthenticationHandler`
The authentication handler that provides SPNEGO support in both Kerberos and NTLM flavors. NTLM is disabled by default.
Configuration properties:

* `principalWithDomainName` - True to include the domain name in the CAS principal ID, false otherwise.
* `NTLMallowed` - True to enable NTLM support, false otherwise. (Disabled by default.)

######`JCIFSConfig`
Configuration helper for JCIFS and the Spring framework. Configuration properties:

* `jcifsServicePrincipal` - service principal name.
* `jcifsServicePassword` - service principal password.
* `kerberosDebug` - True to enable kerberos debugging, false otherwise.
* `kerberosRealm` - Kerberos realm name.
* `kerberosKdc` - Kerberos KDC address.
* `loginConf` - Path to the login.conf JAAS configuration file.


######`SpnegoNegociateCredentialsAction`
CAS login Webflow action that begins the SPNEGO authenticaiton process. The action checks the `Authorization` request
header for a suitable value (`Negotiate` for Kerberos or `NTLM`). If the check is successful, flow continues to the
`SpnegoCredentialsAction` state; otherwise a 401 (not authorized) response is returned.

######`SpnegoCredentialsAction`
Constructs CAS credentials from the encoded GSSAPI data in the `Authorization` request header. The standard CAS
authentication process proceeds as usual after this step: authentication is attempted with a suitable handler,
`JCIFSSpnegoAuthenticationHandler` in this case. The action also sets response headers accordingly based on whether
authentication succeeded or failed.

#### SPNEGO Configuration

##### Create SPN Account
Create an Active Directory account for the Service Principal Name (SPN) and record the username and password, which
will be used subsequently to configure the `JCIFSConfig` component.

##### Create Keytab File
The keytab file enables a trust link between the CAS server and the Key Distribution Center (KDC); an Active Directory
domain controller serves the role of KDC in this context.
The [`ktpass` tool](http://technet.microsoft.com/en-us/library/cc753771.aspx) is used to generate the keytab file,
which contains a cryptographic key. Be sure to execute the command from an Active Directory domain controller as
administrator.

##### Test SPN Account
Install and configure MIT Kerberos V on the CAS server host(s). The following sample `krb5.conf` file may be used
as a reference.

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

Then verify that your are able to read the keytab file:

    klist -k

Then verify that your are able to read the keytab file:

    kinit a_user_in_the_realm@YOUR.REALM.HERE
    klist

##### Browser Configuration
* Internet Explorer - Enable _Integrated Windows Authentication_ and add the CAS server URL to the _Local Intranet_
zone.
* Firefox - Set the `network.negotiate-auth.trusted-uris` configuration parameter in `about:config` to the CAS server
URL, e.g. https://cas.example.com.

##### CAS Component Configuration
Define two new action states in `login-webflow.xml` before the `viewLoginForm` state:

{% highlight xml %}
<action-state id="startAuthenticate">
  <evaluate expression="negociateSpnego" />
  <transition on="success" to="spnego" />
</action-state>
 
<action-state id="spnego">
  <evaluate expression="spnego" />
  <transition on="success" to="sendTicketGrantingTicket" />
  <transition on="error" to="viewLoginForm" />
</action-state>
{% endhighlight %}

Additionally, two existing states need to be modified:
1. `gatewayRequestCheck` - replace `viewLoginForm` with `startAuthenticate`
2. `renewRequestCheck` - replace `viewLoginForm` with `startAuthenticate`


Add two bean definitions in `cas-servlet.xml`:

{% highlight xml %}
<bean id="negociateSpnego" class="org.jasig.cas.support.spnego.web.flow.SpnegoNegociateCredentialsAction" />
 
<bean id="spnego" class="org.jasig.cas.support.spnego.web.flow.SpnegoCredentialsAction"
      p:centralAuthenticationService-ref="centralAuthenticationService" />
{% endhighlight %}

Update `deployerConfigContext.xml` according to the following template:

{% highlight xml %}
<bean id="jcifsConfig"
      class="org.jasig.cas.support.spnego.authentication.handler.support.JCIFSConfig"
      p:jcifsServicePrincipal="HTTP/cas.example.com@EXAMPLE.COM"
      p:kerberosDebug="false"
      p:kerberosRealm="EXAMPLE.COM"
      p:kerberosKdc="172.10.1.10"
      p:loginConf="/path/to/login.conf" />

<bean id="spnegoAuthentication" class="jcifs.spnego.Authentication" />

<bean id="spnegoHandler"
      class="org.jasig.cas.support.spnego.authentication.handler.support.JCIFSSpnegoAuthenticationHandler"
      p:authentication-ref="spnegoAuthentication"
      p:principalWithDomainName="false"
      p:NTLMallowed="true" />

<bean id="spnegoPrincipalResolver"
      class="org.jasig.cas.support.spnego.authentication.principal.SpnegoPrincipalResolver" />

<bean id="authenticationManager"
      class="org.jasig.cas.authentication.PolicyBasedAuthenticationManager">
  <constructor-arg>
    <map>
      <entry key-ref="spnegoHandler" value-ref="spnegoPrincipalResolver"/>
    </map>
  </constructor-arg>
  <property name="authenticationMetaDataPopulators">
    <list>
      <bean class="org.jasig.cas.authentication.SuccessfulHandlerMetaDataPopulator" />
    </list>
  </property>
</bean>
{% endhighlight %}

Provide a JAAS `login.conf` file in a location that agrees with the `loginConf` property of the `JCIFSConfig` bean
above.

    jcifs.spnego.initiate {
       com.sun.security.auth.module.Krb5LoginModule required storeKey=true useKeyTab=true keyTab="/home/cas/kerberos/myspnaccount.keytab";
    };
    jcifs.spnego.accept {
       com.sun.security.auth.module.Krb5LoginModule required storeKey=true useKeyTab=true keyTab="/home/cas/kerberos/myspnaccount.keytab";
    };

### Trusted
The trusted authentication handler provides support for trusting authentication performed by some other component
in the HTTP request handling chain. Proxies (including Apache in a reverse proxy scenario) are the most common
components that perform authentication in front of CAS.

Trusted authentication handler support is enabled by including the following dependency in the Maven WAR overlay:

    <dependency>
      <groupId>org.jasig.cas</groupId>
      <artifactId>cas-server-support-trusted</artifactId>
      <version>${cas.version}</version>
    </dependency>

#### Configure Trusted Authentication Handler
Modify `deployerConfigContext.xml` according to the following template:

{% highlight xml %}
<bean id="trustedHandler"
      class="org.jasig.cas.adaptors.trusted.authentication.handler.support.PrincipalBearingCredentialsAuthenticationHandler" />

<bean id="trustedPrincipalResolver"
      class="org.jasig.cas.adaptors.trusted.authentication.principal.PrincipalBearingPrincipalResolver" />

<bean id="authenticationManager"
      class="org.jasig.cas.authentication.PolicyBasedAuthenticationManager">
  <constructor-arg>
    <map>
      <entry key-ref="trustedHandler" value-ref="trustedPrincipalResolver"/>
    </map>
  </constructor-arg>
  <property name="authenticationMetaDataPopulators">
    <list>
      <bean class="org.jasig.cas.authentication.SuccessfulHandlerMetaDataPopulator" />
    </list>
  </property>
</bean>
{% endhighlight %}

#### Configure Webflow Components
Add an additional state to `login-webflow.xml`:

{% highlight xml %}
<action-state id="remoteAuthenticate">
  <evaluate expression="principalFromRemoteAction" />.
  <transition on="success" to="sendTicketGrantingTicket" />
  <transition on="error" to="viewLoginForm" />
</action-state>
{% endhighlight %}

Replace references to `viewLoginForm` in existing states with `remoteAuthenticate`.

Install the Webflow action into the Spring context by adding the following bean to `cas-servlet.xml`:

{% highlight xml %}
<bean id="principalFromRemoteAction"
      class="org.jasig.cas.adaptors.trusted.web.flow.PrincipalFromRequestRemoteUserNonInteractiveCredentialsAction"
      p:centralAuthenticationService-ref="centralAuthenticationService" />
{% endhighlight %}

### X.509
CAS X.509 authentication components provide a mechanism to authenticate users who present client certificates during
the SSL/TLS handshake process. The X.509 components require configuration ouside the CAS application since the
SSL handshake happens outside the servlet layer where the CAS application resides. There is no particular requirement
on deployment architecture (i.e. Apache reverse proxy, load balancer SSL termination) other than any client
certificate presented in the SSL handshake be accessible to the servlet container as a request attribute named
`javax.servlet.request.X509Certificate`. This happens naturally for configurations that terminate SSL connections
directly at the servlet container and when using Apache/mod_jk; for other architectures it may be necessary to do
additional work.

#### X.509 Components
X.509 support is enabled by including the following dependency in the Maven WAR overlay:

    <dependency>
      <groupId>org.jasig.cas</groupId>
      <artifactId>cas-server-support-x509</artifactId>
      <version>${cas.version}</version>
    </dependency>

CAS provides an X.509 authentication handler, a handful of X.509-specific prinicpal resolvers, some certificate
revocation machinery, and some Webflow actions to provide for non-interactive authentication.

######`X509CredentialsAuthenticationHandler`
The X.509 handler technically performs additional checks _after_ the real SSL client authentication process performed
by the Web server terminating the SSL connection. Since an SSL peer may be configured to accept a wide range of
certificates, the CAS X.509 handler provides a number of properties that place additional restrictions on
acceptable client certificates.

* `regExTrustedIssuerDnPattern` - Regular expression defining allowed issuer DNs. (must be specified)
* `regExSubjectDnPattern` - Regular expression defining allowed subject DNs. (default=`.*`)
* `maxPathLength` - Maximum number of certs allowed in certificate chain. (default=1)
* `maxPathLengthAllowUnspecified` - True to allow unspecified path length, false otherwise. (default=false)
* `checkKeyUsage` - True to enforce certificate `keyUsage` field (if present), false otherwise. (default=false)
* `requireKeyUsage` - True to require the existence of a `keyUsage` certificate field, false otherwise. (default=false)
* `revocationChecker` - Instance of `RevocationChecker` used for certificate expiration checks.
(default=`NoOpRevocationChecker`)

##### Principal Resolvers

######`X509SubjectPrincipalResolver`
Creates a principal ID from a format string composed of components from the subject distinguished name.
The following configuration snippet produces prinicpals of the form _cn@example.com_. For example, given a
certificate with the subject _DC=edu, DC=vt/UID=jacky, CN=Jascarnella Ellagwonto_ it would produce the ID
_jacky@vt.edu_.

{% highlight xml %}
<bean id="x509SubjectResolver"
      class="org.jasig.cas.adaptors.x509.authentication.principal.X509SubjectPrincipalResolver"
      p:descriptor="$CN@$DC.$DC" />
{% endhighlight %}

See the Javadocs for a thorough discussion of the format string specification.

######`X509SubjectDNPrincipalResolver`
Creates a principal ID from the certificate subject distinguished name.

######`X509SerialNumberPrincipalResolver`
Creates a principal ID from the certificate serial number.

######`X509SerialNumberAndIssuerDNPrincipalResolver`
Creates a principal ID by concatenating the certificate serial number, a delimiter, and the issuer DN.
The serial number may be prefixed with an optional string. See the Javadocs for more information.

##### RevocationChecker
CAS provides a flexible policy engine for certificate revocation checking. This facility arose due to lack of
configurability in the revocation machinery built into the JSSE.

######`ResourceCRLRevocationChecker`
Performs a certificate revocation check against a CRL hosted at a fixed location. Any resource type supported by the
Spring [`Resource`]() class may be specified for the CRL resource. The CRL is fetched at periodic intervals and cached.

Configuration properties:

* `crl` - Spring resource describing the location/kind of CRL resource. (must be specified)
* `refreshInterval` - Periodic CRL refresh interval in seconds. (default=3600)
* `unavailableCRLPolicy` - Policy applied when CRL data is unavailable upon fetching. (default=`DenyRevocationPolicy`)
* `expiredCRLPolicy` - Policy applied when CRL data is expired. (default=`ThresholdExpiredCRLRevocationPolicy`)

The following policies are available by default:

* `AllowRevocationPolicy` - Deny policy
* `DenyRevocationPolicy` - Deny policy
* `ThresholdExpiredCRLRevocationPolicy` - Deny if CRL is more than X seconds expired.

`ResourceCRLRevocationChecker` Example:
{% highlight xml %}
<bean id="crlResource"
      class="org.springframework.core.io.UrlResource"
      c:path="https://pki.example.com/exampleca/crl" />

<bean id="allowPolicy"
      class="org.jasig.cas.adaptors.x509.authentication.handler.support.AllowRevocationPolicy" />

<bean id="thresholdPolicy"
      class="org.jasig.cas.adaptors.x509.authentication.handler.support.ThresholdExpiredCRLRevocationPolicy"
      p:threshold="3600" />

<bean id="revocationChecker"
      class="org.jasig.cas.adaptors.x509.authentication.handler.support.ResourceCRLRevocationChecker"
      c:crl-ref="crlResource"
      p:refreshInterval="600"
      p:unavailableCRLPolicy-ref="allowPolicy"
      p:thresholdPolicy-ref="thresholdPolicy" />
{% endhighlight %}

######`CRLDistributionPointRevocationChecker`
Performs certificate revocation checking against the CRL URI(s) mentioned in the certificate _cRLDistributionPoints_
extension field. The component leverages a cache to prevent excessive IO against CRL endpoints; CRL data is fetched
if does not exist in the cache or if it is expired.

Configuration properties:

* `cache` - Ehcache `Cache` component.
* `unavailableCRLPolicy` - Policy applied when CRL data is unavailable upon fetching. (default=`DenyRevocationPolicy`)
* `expiredCRLPolicy` - Policy applied when CRL data is expired. (default=`ThresholdExpiredCRLRevocationPolicy`)

`CRLDistributionPointRevocationChecker` Example:
{% highlight xml %}
<!-- timeToLive, timeToIdle are in seconds -->
<bean id="crlCache" class="org.springframework.cache.ehcache.EhCacheFactoryBean"
      p:cacheName="CRLCache"
      p:eternal="false"
      p:overflowToDisk="false"
      p:maxElementsInMemory="100"
      p:timeToLive="36000"
      p:timeToIdle="36000">
  <property name="cacheManager">
    <bean class="org.springframework.cache.ehcache.EhCacheManagerFactoryBean" />
  </property>
</bean>

<bean id="denyPolicy"
      class="org.jasig.cas.adaptors.x509.authentication.handler.support.DenyRevocationPolicy" />

<bean id="thresholdPolicy"
      class="org.jasig.cas.adaptors.x509.authentication.handler.support.ThresholdExpiredCRLRevocationPolicy"
      p:threshold="3600" />

<bean id="revocationChecker"
      class="org.jasig.cas.adaptors.x509.authentication.handler.support.CRLDistributionPointRevocationChecker"
      c:cache-ref="crlCache"
      p:unavailableCRLPolicy-ref="denyPolicy"
      p:thresholdPolicy-ref="thresholdPolicy" />
{% endhighlight %}

#### Webflow Components
A single Webflow component, `X509CertificateCredentialsNonInteractiveAction`, is required to extract the certificate
from the HTTP request context and perform non-interactive authentication.

#### X.509 Configuration
X.509 configuration requires substantial configuration outside the CAS Web application. The configuration of Web
server SSL components varies dramatically with software and is outside the scope of this document. We offer some
general advice for SSL configuration:

* Configuring SSL components for optional client certificate behavior generally provides better user experience.
Requiring client certificates prevents SSL negotiation in cases where the certificate is not present, which prevents
user-friendly server-side error messages.
* Accept certificates only from trusted issuers, generally those within your PKI.
* Specify all certificates in the certificate chain(s) of allowed issuers.

##### Configure Authentication Components
Use the following template to configure authentication in `deployerConfigContext.xml`:
{% highlight xml %}
<bean id="crlResource"
      class="org.springframework.core.io.UrlResource"
      c:path="https://pki.example.com/exampleca/crl" />

<bean id="allowPolicy"
      class="org.jasig.cas.adaptors.x509.authentication.handler.support.AllowRevocationPolicy" />

<bean id="thresholdPolicy"
      class="org.jasig.cas.adaptors.x509.authentication.handler.support.ThresholdExpiredCRLRevocationPolicy"
      p:threshold="3600" />

<bean id="revocationChecker"
      class="org.jasig.cas.adaptors.x509.authentication.handler.support.ResourceCRLRevocationChecker"
      c:crl-ref="crlResource"
      p:refreshInterval="600"
      p:unavailableCRLPolicy-ref="allowPolicy"
      p:thresholdPolicy-ref="thresholdPolicy" />

<bean id="x509Handler"
      class="org.jasig.cas.adaptors.x509.authentication.handler.support.X509CredentialsAuthenticationHandler"
      p:trustedIssuerDnPattern="CN=(DEV )*Virginia Tech [A-Za-z ]*User CA.*"
      p:maxPathLength="2147483647"
      p:maxPathLengthAllowUnspecified="true"
      p:checkKeyUsage="true"
      p:requireKeyUsage="true"
      p:revocationChecker-ref="revocationChecker">      

<bean id="x509PrincipalResolver"
      class="org.jasig.cas.adaptors.x509.authentication.principal.X509SubjectPrincipalResolver"
      p:descriptor="$UID" />

<bean id="authenticationManager"
      class="org.jasig.cas.authentication.PolicyBasedAuthenticationManager">
  <constructor-arg>
    <map>
      <entry key-ref="x509Handler" value-ref="x509PrincipalResolver"/>
    </map>
  </constructor-arg>
  <property name="authenticationMetaDataPopulators">
    <list>
      <bean class="org.jasig.cas.authentication.SuccessfulHandlerMetaDataPopulator" />
    </list>
  </property>
</bean>
{% endhighlight %}

##### X.509 Webflow Configuration
Uncomment the `startAuthenticate` state in `login-webflow.xml`:

{% highlight xml %}
<action-state id="startAuthenticate">
  <action bean="x509Check" />
  <transition on="success" to="sendTicketGrantingTicket" />
  <transition on="warn" to="warn" />
  <transition on="error" to="generateLoginTicket" />
</action-state>
{% endhighlight %} 

Replace all instances of the `generateLoginTicket` transition in other states with `startAuthenticate`.

Define the `x509Check` bean in `cas-servlet.xml`:
{% highlight xml %}
<bean id="x509Check"
   class="org.jasig.cas.adaptors.x509.web.flow.X509CertificateCredentialsNonInteractiveAction"
   p:centralAuthenticationService-ref="centralAuthenticationService" />
{% endhighlight %}

## Principal Resolution
TBD: @serac

## Authentication Metadata
TBD: @serac

## Long Term Authentication
This feature, also known as *Remember Me*, extends the length of the SSO session beyond the typical period of hours
such that users can go days or weeks without having to log in to CAS.

TBD: @serac

## Proxy Authentication
TBD: @serac

## Multi-factor Authentication
TBD: @serac

## Login Throttling
TBD: @serac
