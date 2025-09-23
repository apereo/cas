---
layout: default
title: CAS - Servlet Container
category: Installation
---
{% include variables.html %}

# Apache Tomcat - Embedded Servlet Container Configuration

Note that by default, the embedded container attempts to enable the HTTP2 protocol.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-webapp-tomcat" bundled="true" %}

## Configuration

{% include_cached casproperties.html thirdPartyStartsWith="server.tomcat." %}

## IPv4 Configuration

In order to force Apache Tomcat to use IPv4, configure the following as a system property for your *run* command:

```bash
-Djava.net.preferIPv4Stack=true 
```

The same sort of configuration needs to be applied to your `$CATALINA_OPTS`
environment variable in case of an external container.
