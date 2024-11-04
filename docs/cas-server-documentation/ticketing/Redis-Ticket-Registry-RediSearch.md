---
layout: default
title: CAS - Redis Ticket Registry
category: Ticketing
---

{% include variables.html %}

# Redis Ticket Registry - Indexing & Search

For better performance, it's best for the Redis server deployment to turn up and enable [RediSearch](https://github.com/RediSearch/RediSearch).
RediSearch is a Redis module that enables querying, secondary indexing, and full-text search for Redis. These features allow CAS
to build particular indexes for ticket documents for faster querying and search operations. In certain cases, this would
significantly improve the performance of *lookup* operations.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-redis-modules" %}
