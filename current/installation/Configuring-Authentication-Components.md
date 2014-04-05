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

* [Database](Database-Authentication.html)
* [JAAS](JAAS-Authentication.html)
* [LDAP](LDAP-Authentication.html)
* [Legacy](Legacy-Authentication.html)
* [OpenID](OpenID-Authentication.html)
* [OAuth 1.0/2.0](OAuth-Authentication.html)
* [RADIUS](RADIUS-Authentication.html)
* [SPNEGO](SPNEGO-Authentication.html) (Windows)
* [Trusted](Trusted-Authentication.html) (REMOTE_USER)
* [X.509](X509-Authentication.html) (client SSL certificate)

There are some additional handlers for small deployments and special cases:
* [Whilelist](Whitelist-Authentication.html)
* [Blacklist](Blacklist-Authentication.html)


##Argument Extractors
Extractors are responsible to examine the http request received for parameters that describe the authentication request such as the requesting `service`, etc. Extractors exist for a number of supported authentication protocols and each create appropriate instances of `WebApplicationService` that contains the results of the extraction. 

Argument extractor configuration is defined at `src/main/webapp/WEB-INF/spring-configuration/argumentExtractorsConfiguration.xml`. Here's a brief sample:

{% highlight xml %}
<bean id="casArgumentExtractor"	class="org.jasig.cas.web.support.CasArgumentExtractor" />

<util:list id="argumentExtractors">
	<ref bean="casArgumentExtractor" />
</util:list>
{% endhighlight %}


###Components


####`ArgumentExtractor`
Strategy parent interface that defines operations needed to extract arguments from the http request.


####`CasArgumentExtractor`
Argument extractor that maps the request based on the specifications of the CAS protocol.


####`GoogleAccountsArgumentExtractor`
Argument extractor to be used to enable Google Apps integration and SAML v2 specification.


####`SamlArgumentExtractor`
Argument extractor compliant with SAML v1.1 specification.


####`OpenIdArgumentExtractor`
Argument extractor compliant with OpenId protocol.


## Principal Resolution
A CAS principal contains a unique identifier by which the authenticated user will be known to all requesting
services. A principal also contains optional [attributes that may be released](../integration/Attribute-Release.html)
to services to support authorization and personalization. Principal resolution is a requisite part of the
authentication process that happens after credential authentication.

CAS 4 `AuthenticationHandler` components provide simple principal resolution machinery by default. For example,
the `LdapAuthenticationHandler` component supports fetching attributes and setting the principal ID attribute from
an LDAP query. In all cases principals are resolved from the same store as that which provides authentication.

In many cases it is necessary to perform authentication by one means and resolve principals by another.
The `PrincipalResolver` component provides this functionality. A common use case for this this mix-and-match strategy
arises with X.509 authentication. It is common to store certificates in an LDAP directory and query the directory to
resolve the principal ID and attributes from directory attributes. The `X509CertificateAuthenticationHandler` may
be be combined with an LDAP-based principal resolver to accommodate this case.


### PrincipalResolver Components


######`PersonDirectoryPrincipalResolver`
Uses the Jasig Person Directory library to provide a flexible principal resolution services against a number of data
sources. The key to configuring `PersonDirectoryPrincipalResolver` is the definition of an `IPersonAttributeDao` object.
The [Person Directory documentation](https://wiki.jasig.org/display/PDM15/Person+Directory+1.5+Manual) provides
configuration for two common examples:

* [Database (JDBC)](https://wiki.jasig.org/x/bBjP)
* [LDAP](https://wiki.jasig.org/x/iBjP)

We present a stub configuration here that can be modified accordingly by consulting the Person Directory documentation.
{% highlight xml %}
<bean id="attributeRepository"
      class="org.jasig.services.persondir.support.StubPersonAttributeDao">
  <property name="backingMap">
    <map>
      <entry key="uid" value="username"/>
      <entry key="eduPersonAffiliation" value="affiliation"/>
      <entry key="member" value="member"/>
    </map>
  </property>
</bean>

<bean id="principalResolver"
      class="org.jasig.cas.authentication.principal.PersonDirectoryPrincipalResolver"
      p:principalAttributeName="username"
      p:attributeRepository-ref="attributeRepository"
      p:returnNullIfNoAttributes="true" />
{% endhighlight %}


######`OpenIdPrincipalResolver`
Extension of `PersonDirectoryPrincipalResolver` that is specifically for use with OpenID credentials. The configuration
of this component is identical to that of `PersonDirectoryPrincipalResolver`.


######`SpnegoPrincipalResolver`
Extension of `PersonDirectoryPrincipalResolver` that is specifically for use with SPNEGO credentials. The configuration
is the same as that of `PersonDirectoryPrincipalResolver` but with an additional property, `transformPrincipalId`,
that provides a simple case transform on the principal ID. The following values are supported:

* NONE
* LOWERCASE
* UPPERCASE

{% highlight xml %}
<bean id="principalResolver"
      class="org.jasig.cas.authentication.principal.PersonDirectoryPrincipalResolver"
      p:principalAttributeName="username"
      p:attributeRepository-ref="attributeRepository"
      p:returnNullIfNoAttributes="true"
      p:transformPrincipalId="UPPERCASE" />
{% endhighlight %}


######`CredentialsToLdapAttributePrincipalResolver`
Provides an LDAP resolver based on the ldaptive library. This components provides a two-phase principal resolution
strategy:

1. Delegate to a (presumably simple) resolver to obtain a principal ID to use as the basis of an LDAP query.
2. Execute an LDAP query for attributes, with the option to use a returned attribute as the final principal ID.

TODO: provide configuration example
@mmoayyed


######`X509SubjectPrincipalResolver`
Creates a principal ID from a format string composed of components from the subject distinguished name.
See the [X.509 principal resolver](#x_509) section for more information.


######`X509SubjectDNPrincipalResolver`
Creates a principal ID from the certificate subject distinguished name.


### PrincipalResolver Versus AuthenticationHandler
The principal resolution machinery provided by `AuthenticationHandler` components should be used in preference to
`PrincipalResolver` in any situation where the former provides adequate functionality.


## Authentication Metadata
`AuthenticationMetaDataPopulator` components provide a pluggable strategy for injecting arbitrary metadata into the
authentication subsystem for consumption by other subsystems or external components. Some notable uses of metadata
populators:

* Supports the long term authentication feature
* SAML protocol support
* OAuth and OpenID protocol support.

The default authentication metadata populators should be sufficient for most deployments. Where the components are
required to support optional CAS features, they will be explicitly identified and configuration will be provided.


## Long Term Authentication
This feature, also known as *Remember Me*, extends the length of the SSO session beyond the typical period of hours
such that users can go days or weeks without having to log in to CAS. See the
[security guide](../planning/Security-Guide.html)
for discussion of security concerns related to long term authentication.


### Policy and Deployment Considerations
While users can elect to establish a long term authentication session, the duration is established through
configuration as a matter of security policy. Deployers must determine the length of long term authentication sessions
by weighing convenience against security risks. The length of the long term authentication session is configured
(somewhat unhelpfully) in seconds, but the Google calculator provides a convenient converter:

[2 weeks in seconds](https://www.google.com/search?q=2+weeks+in+seconds&oq=2+weeks+in+seconds)

The use of long term authentication sessions dramatically increases the length of time ticket-granting tickets are
stored in the ticket registry. Loss of a ticket-granting ticket corresponding to a long-term SSO session would require
the user to reauthenticate to CAS. A security policy that requires that long term authentication sessions MUST NOT
be terminated prior to their natural expiration would mandate a ticket registry component that provides for durable storage. Memcached is a notable example of a store that has no facility for durable storage. In many cases loss of
ticket-granting tickets is acceptable, even for long term authentication sessions.

It's important to note that ticket-granting tickets and service tickets can be stored in separate registries, where
the former provides durable storage for persistent long-term authentication tickets and the latter provides less
durable storage for ephemeral service tickets. Thus deployers could mix `JpaTicketRegistry` and
`MemcachedTicketRegistry`, for example, to take advantage of their strengths, durability and speed respectively.


### Component Configuration
Long term authentication requires configuring CAS components in Spring configuration, modification of the CAS login
webflow, and UI customization of the login form. The length of the long term authentication session is represented
in following sections by the following property:

    # Long term authentication session length in seconds
    rememberMeDuration=1209600

The duration of the long term authentication session is configured in two different places:
1. `ticketExpirationPolicies.xml`
2. `ticketGrantingTicketCookieGenerator.xml`

Update the ticket-granting ticket expiration policy in `ticketExpirationPolicies.xml` to accommodate both long term
and stardard sessions.
{% highlight xml %}
<!--
   | The following policy applies to standard CAS SSO sessions.
   | Default 2h (7200s) sliding expiration with default 8h (28800s) maximum lifetime.
   -->
<bean id="standardSessionTGTExpirationPolicy"
      class="org.jasig.cas.ticket.support.TicketGrantingTicketExpirationPolicy"
      p:maxTimeToLiveInSeconds="${tgt.maxTimeToLiveInSeconds:28800}"
      p:timeToKillInSeconds="${tgt.timeToKillInSeconds:7200}"/>

<!--
   | The following policy applies to long term CAS SSO sessions.
   | Default duration is two weeks (1209600s).
   -->
<bean id="longTermSessionTGTExpirationPolicy"
      class="org.jasig.cas.ticket.support.TimeoutExpirationPolicy"
      c:timeToKillInMilliSeconds="#{ ${rememberMeDuration:1209600} * 1000 }" />

<bean id="grantingTicketExpirationPolicy"
      class="org.jasig.cas.ticket.support.RememberMeDelegatingExpirationPolicy"
      p:sessionExpirationPolicy-ref="standardSessionTGTExpirationPolicy"
      p:rememberMeExpirationPolicy-ref="longTermSessionTGTExpirationPolicy" />
{% endhighlight %}

Update the CASTGC cookie expiration in `ticketGrantingTicketCookieGenerator.xml` to match the long term authentication
duration:
{% highlight xml %}
<bean id="ticketGrantingTicketCookieGenerator" class="org.jasig.cas.web.support.CookieRetrievingCookieGenerator"
      p:cookieSecure="true"
      p:cookieMaxAge="-1"
      p:rememberMeMaxAge="${rememberMeDuration:1209600}"
      p:cookieName="CASTGC"
      p:cookiePath="/cas" />
{% endhighlight %}

Modify the `PolicyBasedAuthenticationManager` bean in `deployerConfigContext.xml` to include the
`RememberMeAuthenticationMetaDataPopulator` component that flags long-term SSO sessions:
{% highlight xml %}
<bean id="authenticationManager"
      class="org.jasig.cas.authentication.PolicyBasedAuthenticationManager">
  <constructor-arg>
    <map>
      <entry key-ref="passwordHandler" value-ref="ldapPrincipalResolver"/>
    </map>
  </constructor-arg>
  <property name="authenticationMetaDataPopulators">
    <list>
      <bean class="org.jasig.cas.authentication.SuccessfulHandlerMetaDataPopulator" />
      <bean class="org.jasig.cas.authentication.principal.RememberMeAuthenticationMetaDataPopulator" />
    </list>
  </property>
</bean>
{% endhighlight %}


### Webflow Configuration
Two sections of `login-webflow.xml` require changes:
1. `credentials` variable declaration
2. `viewLoginForm` action state

Change the `credentials` variable declaration as follows:
{% highlight xml %}
<var name="credentials" class="org.jasig.cas.authentication.principal.RememberMeUsernamePasswordCredential" />
{% endhighlight %}

Change the `viewLoginForm` action state as follows:
{% highlight xml %}
<view-state id="viewLoginForm" view="casLoginView" model="credential">
  <binder>
    <binding property="username" />
    <binding property="password" />
    <binding property="rememberMe" />
  </binder>
  <on-entry>
    <set name="viewScope.commandName" value="'credential'" />
  </on-entry>
  <transition on="submit" bind="true" validate="true" to="realSubmit">
    <evaluate expression="authenticationViaFormAction.doBind(flowRequestContext, flowScope.credential)" />
  </transition>
</view-state>
{% endhighlight %}


### User Interface Customization
A checkbox or other suitable control must be added to the CAS login form to allow user selection of long term
authentication. We recommend adding a checkbox control to `casLoginView.jsp` as in the following code snippet.
The only functional consideration is that the name of the form element is _rememberMe_.
{% highlight xml %}
<input type="checkbox" name="rememberMe" id="rememberMe" value="true" />
<label for="rememberMe">Remember Me</label>
{% endhighlight %}


## Proxy Authentication
Proxy authentication support for CASv2 and CASv3 protocols is enabled by default, thus it is entirely a matter of CAS
client configuration to leverage proxy authentication features.

Disabling proxy authentication components is recommended for deployments that wish to strategically avoid proxy
authentication as a matter of security policy. The simplest means of removing support is to remove support for the
`/proxy` and `/proxyValidate` endpoints on the CAS server. The relevant sections of `cas-servlet.xml` are listed
below and the aspects related to proxy authentication may either be commented out or removed altogether.

{% highlight xml %}
<bean
    id="handlerMappingC"
    class="org.springframework.web.servlet.handler.SimpleUrlHandlerMapping"
    p:alwaysUseFullPath="true">
  <property name="mappings">
    <util:properties>
      <prop key="/serviceValidate">serviceValidateController</prop>
      <prop key="/validate">legacyValidateController</prop>
      <prop key="/proxy">proxyController</prop>
      <prop key="/proxyValidate">proxyValidateController</prop>
      <prop key="/authorizationFailure.html">passThroughController</prop>
      <prop key="/status">healthCheckController</prop>
      <prop key="/statistics">statisticsController</prop>
    </util:properties>
  </property>
</bean>

<bean id="proxyController" class="org.jasig.cas.web.ProxyController"
      p:centralAuthenticationService-ref="centralAuthenticationService"/>

<bean id="proxyValidateController" class="org.jasig.cas.web.ServiceValidateController"
      p:centralAuthenticationService-ref="centralAuthenticationService"
      p:proxyHandler-ref="proxy20Handler"
      p:argumentExtractor-ref="casArgumentExtractor"/>
{% endhighlight %}


### Proxy Handlers
Components responsible to determine what needs to be done to handle proxies.


####`CAS10ProxyHandler`
Proxy handler compliant with CAS v1 protocol that is designed to not handle proxy requests and simply return nothing as proxy support in the protocol is absent.

{% highlight xml %}
<bean id="proxy10Handler" class="org.jasig.cas.ticket.proxy.support.Cas10ProxyHandler"/>
{% endhighlight %}


####`CAS20ProxyHandler`
Protocol handler compliant with CAS v2 protocol that is responsible to callback the URL provided and give it a pgtIou and a pgtId. 

{% highlight xml %}
<bean id="proxy20Handler" class="org.jasig.cas.ticket.proxy.support.Cas20ProxyHandler"
          p:httpClient-ref="httpClient"
          p:uniqueTicketIdGenerator-ref="proxy20TicketUniqueIdGenerator"/>
{% endhighlight %}



## Multi-factor Authentication (MFA)
CAS 4 provides a framework for multi-factor authentication (MFA). The design philosophy for MFA support follows from
the observation that institutional security policies with respect to MFA vary dramatically. We provide first class
API support for authenticating multiple credentials and a policy framework around authentication. The components
could be extended in a straightforward fashion to provide higher-level behaviors such as Webflow logic to assist,
for example, a credential upgrade scenario where a SSO session is started by a weaker credential but a particular
service demands reauthentication with a stronger credential.

The authentication subsystem in CAS natively supports handling multiple credentials. While the default login form
and Webflow tier are designed for the simple case of accepting a single credential, all core API components that
interface with the authentication subsystem accept one or more credentials to authenticate.

Beyond support for multiple credentials, an extensible policy framework is available to apply policy arbitrarily.
CAS ships with support for applying policy in the following areas:

* Credential authentication success and failure.
* Service-specific authentication requirements.


######`PolicyBasedAuthenticationManager`
CAS ships with an authentication manager component that is fundamentally MFA-aware. It supports a number of
policies, discussed above, that could facilitate a simple MFA design; for example, where multiple credentials are
invariably required to start a CAS SSO session.


######`ContextualAuthenticationPolicy`
Strategy pattern component for applying security policy in an arbitrary context. These components are assumed to be
stateful once created.


######`ContextualAuthenticationPolicyFactory`
Factory class for creating stateful instances of `ContextualAuthenticationPolicy` that apply to a particular context.


######`AcceptAnyAuthenticationPolicyFactory`
Simple factory class that produces contextual security policies that always pass. This component is configured by
default in some cases to provide backward compatibility with CAS 3.x.


######`RequiredHandlerAuthenticationPolicyFactory`
Factory that produces policy objects based on the security context of the service requesting a ticket. In particular
the security context is based on the required authentication handlers that must have successfully validated credentials
in order to access the service. A clarifying example is helpful; assume the following authentication components
are defined in `deployerConfigContext.xml`:
{% highlight xml %}
<bean id="ldapHandler"
      class="org.jasig.cas.authentication.LdapAuthenticationHandler"
      p:name="ldapHandler">
      <!-- Details elided for simplicity -->
</bean>

<bean id="oneTimePasswordHandler"
      class="com.example.cas.authentication.CustomOTPAuthenticationHandler"
      p:name="oneTimePasswordHandler" />

<bean id="authenticationManager"
      class="org.jasig.cas.authentication.PolicyBasedAuthenticationManager">
  <constructor-arg>
    <map>
      <entry key-ref="passwordHandler" />
      <entry key-ref="oneTimePasswordHandler" />
    </map>
  </constructor-arg>
</bean>
{% endhighlight %}

Assume also the following beans are defined in `applicationContext.xml`:
{% highlight xml %}
<bean id="centralAuthenticationService"
      class="org.jasig.cas.CentralAuthenticationServiceImpl"
      c:authenticationManager-ref="authenticationManager"
      c:logoutManager-ref="logoutManager"
      c:servicesManager-ref="servicesManager"
      c:serviceTicketExpirationPolicy-ref="neverExpiresExpirationPolicy"
      c:serviceTicketRegistry-ref="ticketRegistry"
      c:ticketGrantingTicketExpirationPolicy-ref="neverExpiresExpirationPolicy"
      c:ticketGrantingTicketUniqueTicketIdGenerator-ref="uniqueTicketIdGenerator"
      c:ticketRegistry-ref="ticketRegistry"
      c:uniqueTicketIdGeneratorsForService-ref="uniqueTicketIdGeneratorsForService"
      p:serviceContextAuthenticationPolicyFactory-ref="casAuthenticationPolicy" />

<bean id="casAuthenticationPolicy"
      class="org.jasig.cas.authentication.RequiredHandlerAuthenticationPolicyFactory" />
{% endhighlight %}

With the above configuration in mind, the [service management facility](../installation/Service-Management.html)
may now be leveraged to register services that require specific kinds of credentials be used to access the service.
The kinds of required credentials are specified by naming the authentication handlers that accept them, for example,
`ldapHandler` and `oneTimePasswordHandler`. Thus a service could be registered that imposes security constraints like
the following:

_Only permit users with SSO sessions created from both a username/password and OTP token to access this service._


## Login Throttling
CAS provides a facility for limiting failed login attempts to support password guessing and related abuse scenarios.
A couple strategies are provided for tracking failed attempts:

1. Source IP - Limit successive failed logins against any username from the same IP address.
2. Source IP and username - Limit succesive failed logins against a particular user from the same IP address.

It would be straightforward to develop new components that implement alternative strategies.

All login throttling components that ship with CAS limit successive failed login attempts that exceed a threshold
rate in failures per second. The following properties are provided to define the failure rate.

* `failureRangeInSeconds` - Period of time in seconds during which the threshold applies.
* `failureThreshold` - Number of failed login attempts permitted in the above period.

A failure rate of more than 1 per 3 seconds is indicative of an automated authentication attempt, which is a
reasonable basis for throttling policy. Regardless of policy care should be taken to weigh security against access;
overly restrictive policies may prevent legitimate authentication attempts.


### Throttling Components
The CAS login throttling components are listed below along with a sample configuration that implements a policy
preventing more than 1 failed login every 3 seconds.


######`InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter`
Uses a memory map to prevent successive failed login attempts from the same IP address.
{% highlight xml %}
<bean id="loginThrottle"
      class="org.jasig.cas.web.support.InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter"
      p:failureRangeInSeconds="3"
      p:failureThreshold="1" />
{% endhighlight %}


######`InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter`
Uses a memory map to prevent successive failed login attempts for a particular username from the same IP address.
{% highlight xml %}
<bean id="loginThrottle"
      class="org.jasig.cas.web.support.InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter"
      p:failureRangeInSeconds="3"
      p:failureThreshold="1" />
{% endhighlight %}


######`InspektrThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter`
Queries the data source used by the CAS audit facility to prevent successive failed login attempts for a particular
username from the same IP address. This component requires that the
[inspektr library](https://github.com/dima767/inspektr) used for CAS auditing be configured with
`JdbcAuditTrailManager`, which writes audit data to a database.
{% highlight xml %}
<bean id="loginThrottle"
      class="org.jasig.cas.web.support.InspektrThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter"
      c:auditTrailManager-ref="auditTrailManager"
      c:dataSource-ref="dataSource"
      p:failureRangeInSeconds="3"
      p:failureThreshold="1" />

<bean id="auditTrailManager"
      class="com.github.inspektr.audit.support.JdbcAuditTrailManager"
      c:transactionTemplate-ref="inspektrTransactionTemplate"
      p:dataSource-ref="dataSource" />

<bean id="inspektrTransactionManager"
      class="org.springframework.jdbc.datasource.DataSourceTransactionManager"
      p:dataSource-ref="dataSource" />

<bean id="inspektrTransactionTemplate"
      class="org.springframework.transaction.support.TransactionTemplate"
      p:transactionManager-ref="inspektrTransactionManager"
      p:isolationLevelName="ISOLATION_READ_COMMITTED"
      p:propagationBehaviorName="PROPAGATION_REQUIRED" />
{% endhighlight %}


### High Availability Considerations for Throttling
All of the throttling components are suitable for a CAS deployment that satisfies the
[recommended HA architecture](../planning/High-Availability-Guide.html). In particular deployments with multiple CAS
nodes behind a load balancer configured with session affinity can use either in-memory or _inspektr_ components. It is
instructive to discuss the rationale. Since load balancer session affinity is determined by source IP address, which
is the same criterion by which throttle policy is applied, an attacker from a fixed location should be bound to the
same CAS server node for successive authentication attempts. A distributed attack, on the other hand, where successive
request would be routed indeterminately, would cause haphazard tracking for in-memory CAS components since attempts
would be split across N systems. However, since the source varies, accurate accounting would be pointless since the
throttling components themselves assume a constant source IP for tracking purposes. The login throttling components
are simply not sufficient for detecting or preventing a distributed password brute force attack.

For stateless CAS clusters where there is no session affinity, the in-memory components may afford some protection but
they cannot apply the rate strictly since requests to CAS hosts would be split across N systems.
The _inspektr_ components, on the other hand, fully support stateless clusters.


### Configuring Login Throttling
Login throttling configuration consists of two core components:

1. A login throttle modeled as a Spring `HandlerInterceptorAdapter` component.
2. A scheduled task that periodically cleans up state to allow the throttle to relax.

The period of scheduled task execution MUST be less than that defined by `failureRangeInSeconds` for proper throttle policy enforcement. For example, if `failureRangeInSeconds` is 3, then the quartz trigger that drives the task would be configured for less than 3000 (ms).

It is convenient to place Spring configuration for login throttling components in `deployerConfigContext.xml`.
{% highlight xml %}
<bean id="loginThrottle"
      class="org.jasig.cas.web.support.InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter"
      p:failureRangeInSeconds="3"
      p:failureThreshold="1" />

<bean id="loginThrottleJobDetail"
      class="org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean"
      p:targetObject-ref="loginThrottle"
      p:targetMethod="decrementCounts"/>

<!-- A scheduler that drives all configured triggers is provided by default in applicationContext.xml. -->
<bean id="loginThrottleTrigger"
      class="org.springframework.scheduling.quartz.SimpleTriggerBean"
      p:jobDetail-ref="loginThrottleJobDetail"
      p:startDelay="1000"
      p:repeatInterval="1000"/>
{% endhighlight %}

Configure the throttle to fire during the login webflow by editing `cas-servlet.xml`:
{% highlight xml %}
<bean id="loginFlowHandlerMapping" class="org.springframework.webflow.mvc.servlet.FlowHandlerMapping"
      p:flowRegistry-ref="loginFlowRegistry"
      p:order="2">
  <property name="interceptors">
    <ref local="localeChangeInterceptor" />
    <ref local="loginThrottle" />
  </property>
</bean>
{% endhighlight %}
