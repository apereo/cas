---
layout: default
title: CAS - Apache Tomcat - Embedded Servlet Container Session Initialization
category: Installation
---
{% include variables.html %}

# Apache Tomcat - Embedded Servlet Container Session Initialization

Turn on the Tomcat filter that creates the HTTP session at the start of every request, so it exists before later processing needs it.

{% include_cached casproperties.html properties="cas.server.tomcat.session-initialization." %}
