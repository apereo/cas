---
layout: default
title: CAS - pac4j Authentication
---

<p align="center">
  <img src="https://pac4j.github.io/pac4j/img/logo-cas.png" width="300" />
</p>

## Overview
The [pac4j](https://github.com/pac4j/pac4j) project is a security engine with specific authentication mechanisms, called authenticators, for MongoDB, LDAP, JWT, RDBMS...

Using the `cas-server-integration-pac4j` module, a pac4j authenticator (and profile creator) can be wrapped in a CAS authentication handler and used for authentication.

## Dependency
Support is added by including the following dependency in the Maven WAR overlay:

{% highlight xml %}
<dependency>
  <groupId>org.jasig.cas</groupId>
  <artifactId>cas-server-integration-pac4j</artifactId>
  <version>${cas.version}</version>
</dependency>
{% endhighlight %}

## Configuration
You can use the first implementation for CAS username/password credentials: `org.jasig.cas.support.pac4j.authentication.handler.support.UsernamePasswordWrapperAuthenticationHandler` (extended from `AbstractWrapperAuthenticationHandler`). In the `deployerConfigContext.xml` file:

{% highlight xml %}
<bean id="simpleAuthenticator" class="org.pac4j.http.credentials.authenticator.test.SimpleTestUsernamePasswordAuthenticator" />

<bean id="primaryAuthenticationHandler" class="org.jasig.cas.support.pac4j.authentication.handler.support.UsernamePasswordWrapperAuthenticationHandler">
    <property name="authenticator" ref="simpleAuthenticator" />
</bean>
{% endhighlight %}
