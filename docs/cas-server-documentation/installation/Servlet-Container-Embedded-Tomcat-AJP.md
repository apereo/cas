---
layout: default
title: CAS - Apache Tomcat - Embedded Servlet Container AJP
category: Installation
---
{% include variables.html %}

# Apache Tomcat - Embedded Servlet Container AJP

Open an AJP connector on the embedded Apache Tomcat, for deployments where a web server such as Apache httpd forwards requests to CAS over AJP. AJP is off by default.

{% include_cached casproperties.html properties="cas.server.tomcat.ajp." %}
