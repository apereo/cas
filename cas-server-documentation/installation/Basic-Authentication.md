---
layout: default
title: CAS - Basic Authentication
---

# Basic Authentication
Verify and authenticate credentials using Basic Authentication.

Support is enabled by including the following dependency in the Maven WAR overlay:

{% highlight xml %}
<dependency>
  <groupId>org.jasig.cas</groupId>
  <artifactId>cas-server-support-basic</artifactId>
  <version>${cas.version}</version>
</dependency>
{% endhighlight %}

To access a CAS-protected application using a command-line client such as `curl`, the following command may be used:

{% highlight xml %}
curl <APPLICATION-URL> -L -u <USER>:<PASSWORD>
{% endhighlight %}

Use `--insecure -v` flags to bypass certificate validation and receive additional logs from `curl`. 
