---
layout: default
title: CAS - Apache Tomcat - Embedded Servlet Container Remote User
category: Installation
---
{% include variables.html %}

# Apache Tomcat - Embedded Servlet Container Remote User

Accept the authenticated user name from an HTTP header set by a trusted proxy, optionally only from allowed IP addresses, using a Tomcat remote user valve.

{% include_cached casproperties.html properties="cas.server.tomcat.remote-user-valve." %}
