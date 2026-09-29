---
layout: default
title: CAS - Servlet Container
category: Installation
---
{% include variables.html %}

# Apache Tomcat - Embedded Servlet Container Rewrite Valve

The rewrite valve rewrites request URLs using `mod_rewrite`-style rules before they reach CAS.

{% include_cached casproperties.html properties="cas.server.tomcat.rewrite-valve." %}


