---
layout: default
title: CAS - ClearPass
---
# ClearPass: Credential Caching and Replay
To enable single sign-on into some legacy application it may be necessary to provide them with the actual cleartext password. While such approach inevitably increases security risk, at times this may be a necessary evil in order to integrate applications with CAS.

## Architecture
A service may obtain cleartext credentials for an authenticated user by presenting a valid proxy ticket obtained specifically for the CAS cleartext extension service end-point that is ClearPass. Tickets issued for a ClearPass response are validated in the same way you would access a traditional proxied service. ClearPass ensures this by just being another CAS Client. Credentials are cached inside an Ehcache-backed map with support for encryption of the obtained password in memory.

Upon receiving the request, ClearPass ensures that the following validation criteria are met:

* The proxy ticket was obtained for the URL specifying location of the ClearPass service
* The proxy is valid according to standard CAS spec
* The proxy ticket is indeed a proxy ticket, not a service ticket
* Each member of the proxy chain has been given explicit permission to receive cleartext credentials

## Validation Responses
Upon successful validation the ClearPass service provides credentials in the following response:

{% highlight xml %}
<cas:clearPassResponse xmlns:cas='http://www.yale.edu/tp/cas'>
    <cas:clearPassSuccess>
        <cas:credentials>actual_password</cas:credentials>
    </cas:clearPassSuccess>
</cas:clearPassResponse>
{% endhighlight %}

If the validation fails, the traditional response is a 403 Status code being returned. If there are failures for any other reason other than authorization, the following error response is returned:

{% highlight xml %}
<cas:clearPassResponse xmlns:cas='http://www.yale.edu/tp/cas'>
    <cas:clearPassFailure>description of the problem</cas:clearPassFailure>
</cas:clearPassResponse>
{% endhighlight %}

## Components
Support is enabled by including the following dependency in the Maven WAR overlay:

    <dependency>
       <groupId>org.jasig.cas</groupId>
       <artifactId>cas-server-extension-clearpass</artifactId>
       <version>${cas.version}</version>
       <scope>runtime</scope>
    </dependency>

## Configuration

###`AuthenticationMetaDataPopulator` in `deployerConfigContext.xml`
Uncomment the below element that is responsible for capturing and caching the password:

{% highlight xml %}
<property name="authenticationMetaDataPopulators">
  <list>
    <bean class="org.jasig.cas.extension.clearpass.CacheCredentialsMetaDataPopulator"
      c:credentialCache-ref="encryptedMap" />
    </bean>
  </list>
</property>
{% endhighlight %}

###Modifying `web.xml`
In your Maven overlay, modify the `web.xml` to include the following:
{% highlight xml %}
<servlet-mapping>
  <servlet-name>cas</servlet-name>
  <url-pattern>/clearPass</url-pattern>
</servlet-mapping>
{% endhighlight %}
 Be sure to put this snippet with the other servlet-mappings.

Next, add the following filter and filter-mapping:

{% highlight xml %}
<filter>
  <filter-name>clearPassFilterChainProxy</filter-name>
  <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
</filter>
 
<filter-mapping>
  <filter-name>clearPassFilterChainProxy</filter-name>
  <url-pattern>/clearPass</url-pattern>
</filter-mapping>
{% endhighlight %}

Be sure to put this snippet with the other filter and filter-mappings.

###Modifying `clearpass-configuration.xml`
Obtain a copy of the [`clearpass-configuration.xml`](https://github.com/Jasig/cas/blob/master/cas-server-webapp/src/main/webapp/WEB-INF/unused-spring-configuration/clearpass-configuration.xml) file inside the `WEB-INF/unused-spring-configuration` of the project. Place that in your project's `WEB-INF/spring-configuration` directory.

Next, declare the following bean inside the file:
{% highlight xml %}
<bean id="clearPassProxyList" class="org.jasig.cas.client.validation.ProxyList">
    <constructor-arg>
        <list>
            <value>https://proxy.server.edu</value>
            <value>...</value>
        </list>
    </constructor-arg>
</bean>
{% endhighlight %}

The above bean defines the list of proxying services authorized to obtain ClearPass credentials.  

Alternatively, you may replace:

    <property name="allowedProxyChains" ref="clearPassProxyList" />
with:

    <property name="acceptAnyProxy" value="true" />

...to allow all proxying services to be able to obtain ClearPass credentials.

<div class="alert alert-warning"><strong>Usage Warning!</strong><p>It's not appropriate in your environment to allow any service that can obtain a proxy ticket to proxy to ClearPass. Explicitly authorizing proxy chains to access ClearPass (and denying all unauthorized proxy chains) is an important access control on release of the end-user's password.</p></div>

###Modifying `ticketRegistry.xml`
Obtain a copy of the [`ticketRegistry.xml`](https://github.com/Jasig/cas/blob/master/cas-server-webapp/src/main/webapp/WEB-INF/spring-configuration/ticketRegistry.xml) file and place that in your project's `WEB-INF/spring-configuration` directory.

Replace:
    <bean id="ticketRegistry" class="org.jasig.cas.ticket.registry.DefaultTicketRegistry" />
with:
    <bean id="ticketRegistryValue" class="org.jasig.cas.ticket.registry.DefaultTicketRegistry" />