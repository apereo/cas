---
layout: default
title: CAS - RADIUS Authentication
---
<a name="RADIUSAuthentication">  </a>
# RADIUS Authentication
RADIUS support is enabled by including the following dependency in the Maven WAR overlay:

    <dependency>
      <groupId>org.jasig.cas</groupId>
      <artifactId>cas-server-support-radius</artifactId>
      <version>${cas.version}</version>
    </dependency>

<a name="RADIUSComponents">  </a>
## RADIUS Components
<a name="RadiusAuthenticationHandler">  </a>
######`RadiusAuthenticationHandler`
The RADIUS handler accepts username/password credentials and delegates authentication to one or more RADIUS
servers. It supports two types of failovers: failover on an authentication failure, and failover on a server exception.

* `failoverOnAuthenticationFailure` - True to continue to the next configured RADIUS server on authentication failure,
false otherwise. This flag is typically set when user accounts are spread across one or more RADIUS servers.
* `failoverOnException` - True to continue to next configured RADIUS server on an error other than authentication
failure, false otherwise. This flag is typically set to support highly available deployments where authentication
should proceed in the face of one or more RADIUS server failures.
* `servers` - Array of RADIUS servers to delegate to for authentication.

<a name="JRadiusServerImpl">  </a>
######`JRadiusServerImpl`
Component representing a RADIUS server has the following configuration properties.

* `hostName` - the hostname of the RADIUS server.
* `sharedSecret` - the secret key used to communicate with the server.
* `radiusAuthenticator` - the RADIUS authenticator to use. Defaults to PAP.
* `authenticationPort` - the authentication port this server uses.
* `accountingPort` - the accounting port that this server uses.
* `socketTimeout` - the amount of time to wait before timing out.
* `retries` - the number of times to keep retrying a particular server on communication failure/timeout.

<a name="RADIUSConfigurationExample">  </a>
## RADIUS Configuration Example
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
