---
layout: default
title: CAS - Stormpath Authentication
---

# Stormpath Authentication
Verify and authenticate credentials against the [Stormpath](https://stormpath.com/) Cloud Identity.

{% highlight xml %}
<alias name="stormpathAuthenticationHandler" alias="primaryAuthenticationHandler" />
{% endhighlight %}

Support is enabled by including the following dependency in the Maven WAR overlay:

{% highlight xml %}
<dependency>
  <groupId>org.jasig.cas</groupId>
  <artifactId>cas-server-support-stormpath</artifactId>
  <version>${cas.version}</version>
</dependency>
{% endhighlight %}

The following settings are applicable:

{% highlight properties %}
cas.authn.stormpath.api.key=
cas.authn.stormpath.app.id=
cas.authn.stormpath.secret.key=
{% endhighlight %}
