---
layout: default
title: CAS - Blacklist Authentication
---
<a name="BlacklistAuthentication">  </a>
# Blacklist Authentication
Blacklist authentication components are those that specifically deny access to a set of credentials. Those that fail to match against the predefined set will blindly be accepted.
 
These are:
* `RejectUsersAuthenticationHandler`

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
<bean class="org.jasig.cas.adaptors.generic.RejectUsersAuthenticationHandler">
    <property name="users">
       <map>
          <entry key="scott" value="password" />
       </map>
    </property>
</bean>
{% endhighlight %}
