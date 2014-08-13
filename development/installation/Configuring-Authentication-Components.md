---
layout: default
title: CAS - Configuring Authentication Components
---

# Configuring Authentication Components
The CAS authentication process is performed by several related components:

######`PrincipalNameTransformer`
Transforms the user id string that is typed into the login form into a tentative Principal Name to be
validated by a specific type of Authentication Handler.

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
new in CAS and this handler would only be acceptable in a multi-factor authentication situation.


######`RequiredHandlerAuthenticationPolicy`
Satisfied if an only if a specified handler successfully authenticates its credential. Supports a `tryAll` flag to
avoid short circuiting at step 4.1 above and try every handler even if one prior succeeded. This policy could be
used to support a multi-factor authentication situation, for example, where username/password authentication is
required but an additional OTP is optional.

The following configuration snippet demonstrates how to configure `PolicyBasedAuthenticationManager` for a
straightforward multi-factor authentication case where username/password authentication is required and an additional OTP credential is optional; in both cases principals are resolved from LDAP.

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
* [OAuth 1.0/2.0, OpenID](OAuth-OpenId-Authentication.html)
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

CAS `AuthenticationHandler` components provide simple principal resolution machinery by default. For example,
the `LdapAuthenticationHandler` component supports fetching attributes and setting the principal ID attribute from
an LDAP query. In all cases principals are resolved from the same store as that which provides authentication.

In many cases it is necessary to perform authentication by one means and resolve principals by another.
The `PrincipalResolver` component provides this functionality. A common use case for this this mix-and-match strategy
arises with X.509 authentication. It is common to store certificates in an LDAP directory and query the directory to
resolve the principal ID and attributes from directory attributes. The `X509CertificateAuthenticationHandler` may
be be combined with an LDAP-based principal resolver to accommodate this case.

### PrincipalNameTransformer Components

######`NoOpPrincipalNameTransformer`
Default transformer, that actually does no transformation on the user id.

######`PrefixSuffixPrincipalNameTransformer`
Transforms the user id by adding a postfix or suffix.

######`ConvertCasePrincipalNameTransformer`
A transformer that converts the form uid to either lowercase or uppercase. The result is also trimmed. The transformer is also able
to accept and work on the result of a previous transformer that might have modified the uid, such that the two can be chained.

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

######`X509SubjectPrincipalResolver`
Creates a principal ID from a format string composed of components from the subject distinguished name.
See the [X.509 principal resolver](#x_509) section for more information.


######`X509SubjectDNPrincipalResolver`
Creates a principal ID from the certificate subject distinguished name.


### PrincipalResolver vs. AuthenticationHandler
The principal resolution machinery provided by `AuthenticationHandler` components should be used in preference to
`PrincipalResolver` in any situation where the former provides adequate functionality.
If the principal that is resolved by the authentication handler
suffices, then a `null` value may be passed in place of the resolver bean id:

{% highlight xml %}
<bean id="authenticationManager"
      class="org.jasig.cas.authentication.PolicyBasedAuthenticationManager"
      p:authenticationPolicy-ref="authenticationPolicy">
  <constructor-arg>
      <map>
          <entry key-ref="passwordHandler" value="#{null}"/>
      </map>
  </constructor-arg>
</bean>
{% endhighlight %}

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
CAS has support for long term Ticket Granting Tickets, a feature that is also referred to as _"Remember Me"_
to extends the length of the SSO session beyond the typical configuration.
Please [see this guide](Configuring-LongTerm-Authentication.html) for more details.

## Proxy Authentication
Proxy authentication support for CASv2 and CASv3 protocols is enabled by default, thus it is entirely a matter of CAS
client configuration to leverage proxy authentication features.


<div class="alert alert-info"><strong>Service Configuration</strong><p>
Note that each registered application in the registry must explicitly be configured
to allow for proxy authentication. See <a href="Service-Management.html">this guide</a>
to learn about registering services in the registry.
</p></div>

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

#####Handling SSL-enabled Proxy URLs
By default, CAS ships with a bundled HTTP client that is partly responsible to callback the URL
for proxy authentication. Note that this URL need also be authorized by the CAS service registry
before the callback can be made. [See this guide](Service-Management.md) for more info.

If the callback URL is authorized by the service registry, and if the endpoint is under HTTPS
and protected by an SSL certificate, CAS will also attempt to verify the validity of the endpoint's
certificate before it can establish a successful connection. If the certificate is invalid, expired,
missing a step in its chain, self-signed or otherwise, CAS will fail to execute the callback.

The HTTP client of CAS does present a local trust store that is similar to that of the Java platform.
It is recommended that this trust store be used to handle the management of all certificates that need
to be imported into the platform to allow CAS to execute the callback URL successfully. While by default, 
the local trust store to CAS is empty, CAS will still utilize **both** the default and the local trust store.
The local trust store should only be used for CAS-related functionality of course, and the trust store file
can be carried over across CAS and Java upgrades, and certainly managed by the source control system that should
host all CAS configuration. 

The trust store configuration is inside the `applicationContext.xml` file, as such:

{% highlight xml %}
<bean id="trustStoreSslSocketFactory"
          class="org.jasig.cas.authentication.FileTrustStoreSslSocketFactory"
          c:trustStoreFile="${http.client.truststore.file:classpath:truststore.jks}"
          c:trustStorePassword="${http.client.truststore.psw:changeit}" />
{% endhighlight %}


## Multi-factor Authentication (MFA)
CAS provides a framework for multi-factor authentication (MFA). The design philosophy for MFA support follows from
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
Factory that produces policy objects based on the security context of the service requesting a ticket. In particular the security context is based on the required authentication handlers that must have successfully validated credentials in order to access the service. A clarifying example is helpful; assume the following authentication components are defined in `deployerConfigContext.xml`:

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
      <entry key-ref="passwordHandler" value="#{ null }" />
      <entry key-ref="oneTimePasswordHandler" value="#{ null }" />
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

With the above configuration in mind, the [service management facility](../Service-Management.html)
may now be leveraged to register services that require specific kinds of credentials be used to access the service.
The kinds of required credentials are specified by naming the authentication handlers that accept them, for example,
`ldapHandler` and `oneTimePasswordHandler`. Thus a service could be registered that imposes security constraints like
the following:

_Only permit users with SSO sessions created from both a username/password and OTP token to access this service._


## Login Throttling
CAS provides a facility for limiting failed login attempts to support password guessing and related abuse scenarios.
Please [see this guide](Configuring-Authentication-Throttling.html) for additional details on login throttling.
