---
layout: default
title: CAS - Servlet Container
category: Installation
---
{% include variables.html %}

# Undertow - Embedded Servlet Container Configuration

<div class="alert alert-warning">:warning: <strong>Usage Warning!</strong><p>
Spring Boot requires a Servlet 6.1 baseline, with which Undertow is not yet compatible. As a result, 
Undertow support is dropped, including the Undertow starter and the ability to use Undertow as an embedded server.
Please consider using Tomcat or Jetty as an alternative embedded server until Undertow adds support for Servlet 6.1.
</p></div>

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-webapp-undertow" bundled="true" %}

{% include_cached casproperties.html thirdPartyStartsWith="server.undertow" %}
