---
layout: default
title: CAS - Redis Ticket Registry
category: Ticketing
---

{% include variables.html %}

# Redis Ticket Registry

Redis integration is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-redis-ticket-registry" %}

This registry stores tickets in one or more [Redis](http://redis.io/) instances. The
[spring data redis](http://projects.spring.io/spring-data-redis/) library used by this component presents Redis as a
key/value store that accepts `String` keys and CAS ticket objects as values. The key is started with `CAS_TICKET:`.

The Redis ticket registry supports Redis Sentinel, which provides high availability for Redis. In 
practical terms this means that using Sentinel you can create a Redis deployment that resists 
without human intervention to certain kind of failures. Redis Sentinel also provides other 
collateral tasks such as monitoring, notifications and acts as a configuration provider for clients.

## Configuration

{% include_cached casproperties.html properties="cas.ticket.registry.redis" %}

### Eviction Policy

Redis manages the internal eviction policy of cached objects via its time-alive settings.
The timeout is the ticket's `timeToLive` value. So you need to ensure the cache is alive long enough to support the
individual expiration policy of tickets, and let CAS clean the tickets as part of its own cleaner if necessary.

## Ticket Registry Locking

This ticket registry implementation automatically supports [distributed locking](../ticketing/Ticket-Registry-Locking.html).
The schemas and structures that track locking operations should be automatically created by CAS using
[Spring Integration](https://spring.io/projects/spring-integration) Redis support.
