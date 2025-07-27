---
layout: default
title: CAS - Web Flow Customization
category: Webflow Management
---

{% include variables.html %}

# Server-side Sessions - Redis

If you don't wish to use the native container's strategy for session replication,
you can use CAS's support for Redis session replication.

This feature is enabled via the following module:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-session-redis" %}

{% include_cached casproperties.html 
thirdPartyStartsWith="spring.session.redis,spring.redis" %}
