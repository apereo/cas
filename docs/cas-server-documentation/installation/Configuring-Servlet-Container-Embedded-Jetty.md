---
layout: default
title: CAS - Jetty - Embedded Servlet Container Configuration
category: Installation
---
{% include variables.html %}

# Jetty - Embedded Servlet Container Configuration

Run CAS on an embedded Jetty server instead of Apache Tomcat by building the overlay with the Jetty web application module.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-webapp-jetty" bundled="true" %}

### Embedded Jetty Container

{% include_cached casproperties.html properties="cas.server.jetty" thirdPartyStartsWith="server.jetty" %}
