---
layout: default
title: CAS - Monitoring & Statistics
---

#Monitoring
The CAS server exposes a `/status` endpoint that may be used to inquire about the health and general state of the software. Access to the endpoint is secured by Spring Security at `src/main/webapp/WEB-INF/spring-configuration/securityContext.xml`:

{% highlight xml %}
<sec:http pattern="/status/**" entry-point-ref="notAuthorizedEntryPoint" use-expressions="true" auto-config="true">
    <sec:intercept-url pattern="/status" access="hasIpAddress('${cas.securityContext.status.allowedSubnet}')" />
</sec:http>
{% endhighlight %}

Access is granted the following settings in `cas.properties` file:

{% highlight bash %}
# IP address or CIDR subnet allowed to access the /status URI of CAS that exposes health check information
# IPv6 version
cas.securityContext.status.allowedSubnet=127.0.0.1

# IPv4 version
#cas.securityContext.status.allowedSubnet=127.0.0.1

{% endhighlight %}


##Sample Output

{% highlight bash %}
Health: OK

	1.MemoryMonitor: OK - 322.13MB free, 495.09MB total.
{% endhighlight %}


#Statistics
Furthermore, the `cas-management` web application has the ability to present statistical data about the runtime environment as well as ticket registry's performance:

![](http://i.imgur.com/ZkNC6l9.png)
