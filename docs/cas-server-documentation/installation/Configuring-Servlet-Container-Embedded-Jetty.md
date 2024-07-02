---
layout: default
title: CAS - Servlet Container
category: Installation
---
{% include variables.html %}

# Jetty - Embedded Servlet Container Configuration

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-webapp-jetty" bundled="true" %}

### Embedded Jetty Container

{% include_cached casproperties.html properties="cas.server.jetty" thirdPartyStartsWith="server.jetty" %}
