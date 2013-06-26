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

Unless otherwise noted, the configuration for all authentication components is handled in `deployerConfigContext.xml`.

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
* [JAAS](#jaas)
* [LDAP](#ldap)
* [OpenID](#openid)
* [OAuth 1.0/2.0](#oauth)
* [RADIUS](#radius)
* [Windows (SPNEGO)](#spnego)
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

### JAAS
[JAAS](http://docs.oracle.com/javase/6/docs/technotes/guides/security/jaas/JAASRefGuide.html) is a Java standard
authentication and authorization API. JAAS is configured via externalized plain text configuration file.
Using JAAS with CAS allows modification of the authentication process without having to rebuild and redeploy CAS
and allows for PAM-style multi-module "stacked" authentication.

#### JaasAuthenticationHandler
`JaasAuthenticationHandler` delegates to the built-in JAAS subsystem to perform authentication according to the
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
or OpenLDAP. The following configuration snippet provides a template for LDAP authentication where it should be
sufficient for most deployments to simply provide values for property placeholders. The example makes use of LDAP
connection pooling, which is _strongly_ recommended for all environments.

#### LdapAuthenticationHandler Example
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

Please see the [ldaptive documentation](http://www.ldaptive.org/) for more information or to accommodate more
complex configurations.

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

#### Configuration Parameters
`RadiusAuthenticationHandler` accepts username/password credentials and delegates authentication to one or more RADIUS
servers. It supports two types of failovers: failover on an authentication failure, and failover on a server exception.

* `failoverOnAuthenticationFailure` - True to continue to the next configured RADIUS server on authentication failure,
false otherwise. This flag is typically set when user accounts are spread across one or more RADIUS servers.
* `failoverOnException` - True to continue to next configured RADIUS server on an error other than authentication
failure, false otherwise. This flag is typically set to support highly available deployments where authentication
should proceed in the face of one or more RADIUS server failures.
* `servers` - Array of RADIUS servers to delegate to for authentication.

The `JRadiusServerImpl` component representing a RADIUS server has the following configuration properties.

* `hostName` - the hostname of the RADIUS server.
* `sharedSecret` - the secret key used to communicate with the server.
* `radiusAuthenticator` - the RADIUS authenticator to use. Defaults to PAP.
* `authenticationPort` - the authentication port this server uses.
* `accountingPort` - the accounting port that this server uses.
* `socketTimeout` - the amount of time to wait before timing out.
* `retries` - the number of times to keep retrying a particular server on communication failure/timeout.

#### RadiusAuthenticationHandler Example
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
SPNEGO support is enabled by including the following dependency in the Maven WAR overlay:

    <dependency>
      <groupId>org.jasig.cas</groupId>
      <artifactId>cas-server-support-spnego</artifactId>
      <version>${cas.version}</version>
    </dependency>

#### SPNEGO Overview
There are three actors involved: the client, the CAS server, and the Active Directory Domain Controller/KDC.

Assumptions:
* Client is logged in to a windows domain.
* Client is Windows XP pro SP2 or greater running IE 6 or IE 7. (SPNEGO will not work with IE8 and JDK1.6 before 6u19.)
* CAS is running on a UNIX server configured for kerberos against the AD server in the windows domain.

SPNEGO Authentication Process:

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

##### JCIFSSpnegoAuthenticationHandler
The authentication handler that provides SPNEGO support in both Kerberos and NTLM flavors. NTLM is disabled by default.
Configuration properties:

* `principalWithDomainName` - True to include the domain name in the CAS principal ID, false otherwise.
* `NTLMallowed` - True to enable NTLM support, false otherwise. (Disabled by default.)

##### JCIFSConfig
Configuration helper for JCIFS and the Spring framework. Configuration properties:

* `jcifsServicePrincipal` - service principal name.
* `jcifsServicePassword` - service principal password.
* `kerberosDebug` - True to enable kerberos debugging, false otherwise.
* `kerberosRealm` - Kerberos realm name.
* `kerberosKdc` - Kerberos KDC address.
* `loginConf` - Path to the login.conf JAAS configuration file.


##### SpnegoNegociateCredentialsAction
CAS login Webflow action that begins the SPNEGO authenticaiton process. The action checks the `Authorization` request
header for a suitable value (`Negotiate` for Kerberos or `NTLM`). If the check is successful, flow continues to the
`SpnegoCredentialsAction` state; otherwise a 401 (not authorized) response is returned.

##### SpnegoCredentialsAction
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
    <bean class="org.jasig.cas.authentication.SuccessfulHandlerMetaDataPopulator" />
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
