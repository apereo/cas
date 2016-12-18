---
layout: default
title: CAS - Redis Ticket Registry
---

# Redis Ticket Registry
Redis integration is enabled by including the following dependency in the WAR overlay:

```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-redis-ticket-registry</artifactId>
    <version>${cas.version}</version>
</dependency>
```
`RedisTicketRegistry` stores tickets in one or more [Redis](http://redis.io/) instances. The
[spring data redis](http://projects.spring.io/spring-data-redis/) library used by this component presents Redis as a
key/value store that accepts `String` keys and Java `Object` values. The key is started with `CAS_TICKET:`.

## Configuration

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html).

### Eviction Policy

Redis manages the internal eviction policy of cached objects via the idle and alive settings.
The timeout is the ticket`s `timeToLive`,you need to ensure the cache is alive long enough to support the individual expiration policy of tickets, and let
CAS clean the tickets as part of its own cleaner. 
