---
layout: default
title: CAS - Attribute Resolution
category: Attributes
---

{% include variables.html %}

# Redis Attribute Resolution
     
The following configuration describes how to fetch and retrieve attributes from Redis attribute repositories.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-redis-authentication" %}

{% include_cached casproperties.html properties="cas.authn.attribute-repository.redis" %}

