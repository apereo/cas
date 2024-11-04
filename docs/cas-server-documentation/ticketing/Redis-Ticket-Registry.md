---
layout: default
title: CAS - Redis Ticket Registry
category: Ticketing
---

{% include variables.html %}

# Redis Ticket Registry

Redis integration is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-redis-ticket-registry" %}

This registry stores tickets in one or more [Redis](https://redis.io/) instances. CAS presents and uses Redis as a
key/value store that accepts `String` keys and CAS ticket documents as values. The key is started with `CAS_TICKET:`.

The Redis ticket registry supports Redis Sentinel, which provides high availability for Redis. In 
practical terms this means that using Sentinel you can create a Redis deployment that resists 
without human intervention to certain kind of failures. Redis Sentinel also provides other 
collateral tasks such as monitoring, notifications and acts as a configuration provider for clients.

## Configuration

{% include_cached casproperties.html properties="cas.ticket.registry.redis" %}
  
## Indexing & Search

See [this guide](Redis-Ticket-Registry-RediSearch.html) for more information.

## Caching & Messaging

The Redis ticket registry layers an in-memory cache on top of Redis to assist with performance, particularly
when it comes to fetching ticket objects from Redis using `SCAN` or `KEYS` operations that execute pattern matching.
This cache is specific and isolated to the CAS server node's memory, and is able to clean up after itself with a dedicated
expiration policy that is constructed off of the ticket's expiration policy. Each cache inside an individual CAS server node
will attempt to synchronize ticket changes and updates with other CAS server nodes via a message-based mechanism backed by 
Redis itself. Note that you can always entirely disable the caching mechanism by forcing its maximum capacity to be at zero
via dedicated CAS settings.

{% include_cached casproperties.html properties="cas.ticket.registry.redis.cache" %}

### Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="redisTicketsCache" %}

## Eviction Policy

Redis manages the internal eviction policy of cached objects via its time-alive settings.
The timeout is the ticket's `timeToLive` value. So you need to ensure the cache is alive long enough to support the
individual expiration policy of tickets, and let CAS clean the tickets as part of its own cleaner if necessary.

## Ticket Registry Locking

This ticket registry implementation automatically supports [distributed locking](../ticketing/Ticket-Registry-Locking.html).
The schemas and structures that track locking operations should be automatically created by CAS using
[Spring Integration](https://spring.io/projects/spring-integration) Redis support.
