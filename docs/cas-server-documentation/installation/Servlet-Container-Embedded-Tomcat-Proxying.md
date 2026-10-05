---
layout: default
title: CAS - Apache Tomcat - Embedded Servlet Container Proxying
category: Installation
---
{% include variables.html %}

# Apache Tomcat - Embedded Servlet Container Proxying

Open an additional plain HTTP connector on the embedded Apache Tomcat, for example when a proxy or load balancer terminates TLS and forwards requests to CAS over HTTP.

{% include_cached casproperties.html properties="cas.server.tomcat.http" %}
             
