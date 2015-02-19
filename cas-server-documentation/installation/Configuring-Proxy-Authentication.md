---
layout: default
title: CAS - Proxy Authentication
---

# Proxy Authentication

Proxy authentication support for CAS v1+ protocols is enabled by default, thus it is entirely a matter of CAS
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


## Proxy Handlers
Components responsible to determine what needs to be done to handle proxies.


###`CAS10ProxyHandler`
Proxy handler compliant with CAS v1 protocol that is designed to not handle proxy requests and simply return nothing as proxy support in the protocol is absent.

{% highlight xml %}
<bean id="proxy10Handler" class="org.jasig.cas.ticket.proxy.support.Cas10ProxyHandler"/>
{% endhighlight %}


###`CAS20ProxyHandler`
Protocol handler compliant with CAS v2 protocol that is responsible to callback the URL provided and give it a pgtIou and a pgtId. 

{% highlight xml %}
<bean id="proxy20Handler" class="org.jasig.cas.ticket.proxy.support.Cas20ProxyHandler"
          p:httpClient-ref="httpClient"
          p:uniqueTicketIdGenerator-ref="proxy20TicketUniqueIdGenerator"/>
{% endhighlight %}

####Handling SSL-enabled Proxy URLs
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

