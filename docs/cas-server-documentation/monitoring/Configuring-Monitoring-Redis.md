---
layout: default
title: CAS - Monitoring
category: Monitoring & Statistics
---

{% include variables.html %}

# Redis Monitoring

Monitor the status and availability of Redis databases. This monitor is made available
with the usage of a Redis-based module that would for instance manage [tickets](../ticketing/Redis-Ticket-Registry.html)
or [application definitions](../services/Redis-Service-Management.html), etc and is able to provide health data 
and statistics for each feature and/or active connection to the Redis database.

{% include_cached actuators.html endpoints="health" healthIndicators="redis,redisHealthIndicator" %}
