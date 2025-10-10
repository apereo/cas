---
layout: default
title: CAS - Servlet Container
category: Installation
---
{% include variables.html %}

# Undertow - Embedded Servlet Container Configuration

<div class="alert alert-warning">:warning: <strong>Usage Warning!</strong><p>
This functionality is not yet compatible with the CAS ecosystem of libraries, and/or Spring Boot. As a result, 
support for this feature is dropped. Please consider using alternatives and keep an eye on future releases of CAS
to see if support for this feature is re-introduced.
</p></div>

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-webapp-undertow" bundled="true" %}

{% include_cached casproperties.html thirdPartyStartsWith="server.undertow" %}
