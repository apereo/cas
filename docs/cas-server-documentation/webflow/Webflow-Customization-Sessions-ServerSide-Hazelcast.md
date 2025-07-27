---
layout: default
title: CAS - Web Flow Customization
category: Webflow Management
---

{% include variables.html %}

# Server-side Sessions - Hazelcast

If you don't wish to use the native container's strategy for session replication,
you can use CAS's support for Hazelcast session replication.

This feature is enabled via the following module:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-session-hazelcast" %}

{% include_cached casproperties.html 
properties="cas.webflow.session.server.hazelcast" 
thirdPartyStartsWith="spring.session.hazelcast,spring.session.hazelcast" %}
