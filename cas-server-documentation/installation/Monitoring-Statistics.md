---
layout: default
title: CAS - Monitoring & Statistics
---

#Monitoring
The CAS server exposes a `/status` endpoint that may be used to inquire about the health and general state of the software. Access to the endpoint is secured by Spring Security at `src/main/webapp/WEB-INF/spring-configuration/securityContext.xml`:

{% highlight xml %}
<sec:http pattern="/status/**" entry-point-ref="notAuthorizedEntryPoint" use-expressions="true" auto-config="true">
    <sec:intercept-url pattern="/status" access="${cas.securityContext.status.access}" />
</sec:http>
{% endhighlight %}

Access is granted the following settings in `cas.properties` file:

{% highlight bash %}
# Spring Security's EL-based access rules for the /status URI of CAS that exposes health check information
cas.securityContext.status.access=hasIpAddress('127.0.0.1')

{% endhighlight %}


##Sample Output

{% highlight bash %}
Health: OK

	1.MemoryMonitor: OK - 322.13MB free, 495.09MB total.
{% endhighlight %}


#Statistics
Furthermore, the CAS web application has the ability to present statistical data about the runtime environment as well as ticket registry's performance.

The CAS server exposes a `/statistics` endpoint that may be used to inquire about the runtime state of the software. Access to the endpoint is secured by Spring Security at `src/main/webapp/WEB-INF/spring-configuration/securityContext.xml`:

{% highlight xml %}
<sec:http pattern="/statistics/**" entry-point-ref="notAuthorizedEntryPoint" use-expressions="true" auto-config="true">
 <sec:intercept-url pattern="/statistics" access="${cas.securityContext.statistics.access}" />
</sec:http>
{% endhighlight %}

Access is granted the following settings in `cas.properties` file:

{% highlight bash %}
# Spring Security's EL-based access rules for the /statistics URI of CAS that exposes stats about the CAS server
cas.securityContext.statistics.access=hasIpAddress('127.0.0.1')

{% endhighlight %}

![](http://i.imgur.com/8CXPgOC.png)
