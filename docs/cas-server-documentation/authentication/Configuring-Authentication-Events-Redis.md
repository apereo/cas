---
layout: default
title: CAS - Configuring Authentication Events
category: Authentication
---
{% include variables.html %}

# MongoDb Authentication Events

Stores authentication events into a Redis database.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-events-redis" %}

{% include_cached casproperties.html properties="cas.events.redis" %}

