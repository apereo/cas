---
layout: default
title: CAS - Shiro Authentication
---


# Shiro Authentication
CAS support handling the authentication event via [Apache Shiro](http://shiro.apache.org/). This is handled by an instance of `ShiroAuthenticationHandler`. 


## Authentication Components
Support is enabled by including the following dependency in the Maven WAR overlay:

{% highlight xml %}
<dependency>
  <groupId>org.jasig.cas</groupId>
  <artifactId>cas-server-support-generic</artifactId>
  <version>${cas.version}</version>
</dependency>
{% endhighlight %}

###Shiro Configuration
{% highlight xml %}
<bean class="org.jasig.cas.adaptors.generic.ShiroAuthenticationHandler"
	p:shiroConfiguration="classpath:shiro.ini"
	p:requiredRoles-ref="shiroRequiredRoles"
	p:requiredPermissions-ref="shiroRequiredPermissions" />
    
<util:set id="shiroRequiredRoles" />
<util:set id="shiroRequiredPermissions" />
{% endhighlight %}

Sample `shiro.ini` that needs be placed on the classpath based on the example above:

{% highlight ini %}
[main]
cacheManager = org.apache.shiro.cache.MemoryConstrainedCacheManager
securityManager.cacheManager = $cacheManager

[users]
casuser = Mellon, admin

[roles]
admin = system,admin,staff,superuser:*
{% endhighlight %}

###Roles and Permissions
Apache Shiro supports retrieving and checking roles and permissions for an authenticated 
subject. CAS exposes a modest configuration to enforce roles and permissions as part
of the authentication, so that in their absence, the authentication may fail.
While by default these settings are optional, you may configure roles and/or permissions
for the given authentication handler to check their presence and report back. 

An example might be:

{% highlight xml %}
<util:set id="shiroRequiredRoles">
	<value>admin</value>
</util:set>

<util:set id="shiroRequiredPermissions">
	<value>superuser:canDeleteAll</value>
</util:set>
{% endhighlight %} 

