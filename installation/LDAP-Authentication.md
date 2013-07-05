---
layout: default
title: CAS - LDAP Authentication
---
# LDAP Authentication
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

## Active Directory Authentication
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

## LDAP Supporting Anonymous Search
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

## LDAP Supporting Direct Bind
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

## LDAP Properties Starter
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
