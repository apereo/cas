---
layout: default
title: CAS - Whitelist Authentication
---

<a name="WhitelistAuthentication">  </a>
# Whitelist Authentication
Whitelist authentication components fall into two categories: Those that accept a set of credentials stored directly in the configuration and those that accept a set of credentials from a file resource on the server.

These are:
* `AcceptUsersAuthenticationHandler`
* `FileAuthenticationHandler`

<a name="AuthenticationComponents">  </a>
## Authentication Components
Support is enabled by including the following dependency in the Maven WAR overlay:

    <dependency>
      <groupId>org.jasig.cas</groupId>
      <artifactId>cas-server-support-generic</artifactId>
      <version>${cas.version}</version>
    </dependency>

<a name="AcceptUsersAuthenticationHandler">  </a>
###`AcceptUsersAuthenticationHandler`
{% highlight xml %}
<bean class="org.jasig.cas.adaptors.generic.AcceptUsersAuthenticationHandler">
    <property name="users">
       <map>
          <entry key="scott" value="password" />
       </map>
    </property>
</bean>
{% endhighlight %}


<a name="FileAuthenticationHandler">  </a>
###`FileAuthenticationHandler`
{% highlight xml %}
<bean class="org.jasig.cas.adaptors.generic.FileAuthenticationHandler"
   p:fileName="file:/opt/cas/file_of_passwords.txt" />
{% endhighlight %}

<a name="ExamplePasswordFile">  </a>
####Example Password File
{% highlight bash %}
scott::password
bob::password2
{% endhighlight %}


