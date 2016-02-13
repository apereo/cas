---
layout: default
title: CAS - Blacklist Authentication
---

# Blacklist Authentication
Blacklist authentication components are those that specifically deny access to a set of credentials. Those that fail to match against the predefined set will blindly be accepted.

These are:

* `RejectUsersAuthenticationHandler`

## Authentication Components
Support is enabled by including the following dependency in the Maven WAR overlay:

{% highlight xml %}
<dependency>
  <groupId>org.jasig.cas</groupId>
  <artifactId>cas-server-support-generic</artifactId>
  <version>${cas.version}</version>
</dependency>
{% endhighlight %}

### `RejectUsersAuthenticationHandler`
{% highlight xml %}
<alias name="rejectUsersAuthenticationHandler" alias="primaryAuthenticationHandler" />
{% endhighlight %}

The following settings are applicable:

{% highlight properties %}
# reject.authn.users=user1,user2
{% endhighlight %}
