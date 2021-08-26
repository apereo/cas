---
layout: default
title: CAS - Configuring Authentication Throttling
category: Authentication
---
{% include variables.html %}

# Redis Throttling Authentication Attempts

Queries a Redis data source used by the CAS audit facility to prevent successive failed login attempts 
for a particular username from the same IP address. This component requires and 
depends on the [CAS auditing functionality](../audits/Audits.html) via Redis.

Enable the following module in your configuration overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-throttle-redis" %}

{% include casproperties.html properties="cas.audit.redis" %}
