---
layout: default
title: CAS - Trusted Authentication
category: Authentication
---
{% include variables.html %}

# Trusted Authentication

The trusted authentication handler provides support for trusting authentication performed by some other component
in the HTTP request handling chain. Proxies (including Apache in a reverse proxy scenario) are the most common
components that perform authentication in front of CAS.

Trusted authentication handler support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-trusted-webflow" %}

Trusted authentication is able to extract the remote authenticated user via the following ways:

1. Username may be extracted from `HttpServletRequest#getRemoteUser()`
2. Username may be extracted from `HttpServletRequest#getUserPrincipal()`
3. Username may be extracted from a request header whose name is defined in CAS settings.

{% include_cached casproperties.html properties="cas.authn.trusted" %}
