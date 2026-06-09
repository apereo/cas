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
  
### Design & Performance

The Redis ticket registry treats Redis as the shared source of truth for CAS tickets, while each 
CAS node may keep a local in-memory cache as a first-level optimization. The local cache is per-node 
and is intended to reduce Redis reads and ticket deserialization for hot tickets. It should not be 
treated as authoritative. Ticket lookups may be served from the local cache, but ticket lifecycle 
operations still need to keep Redis and all node-local caches coherent.

Cache keys are derived from the Redis ticket key format, not simply from the clear 
ticket id in all cases. This matters when registry cryptography is enabled. In that mode, ticket 
identifiers and principal identifiers are digested before being used in Redis keys, so the local cache 
key for a ticket is also the digested form. Any cache invalidation mechanism must use the same canonical 
cache key that the registry uses for reads and writes. Invalidating by the clear ticket id will miss 
encrypted/digested cache entries and can leave stale tickets alive on other nodes.

Cluster cache coherence is handled through Redis-backed pub/sub messages. When one CAS node adds, 
updates, deletes, or clears tickets, it publishes a notification. Other CAS nodes receive that 
notification and update or invalidate their local cache. Nodes should ignore their own messages 
because the local registry operation has already updated the local cache directly. For deletes, 
the notification does not need to carry the full ticket object; it only needs enough information 
to identify the local cache entry, such as the Redis key or derived cache key. This avoids 
unnecessary Redis fetches, deserialization, and decryption during delete-heavy operations.

From a performance perspective, the important distinction is between operations that need 
the ticket body and operations that only need to remove state. Add and update messages naturally 
carry a ticket because peer caches may be populated with that object. Delete messages should avoid 
materializing the ticket when the registry already knows the Redis key being removed. Bulk user/session 
deletion paths are especially sensitive: scanning Redis, fetching each ticket, deserializing it, 
and decrypting it just to publish a cache invalidation can be very expensive. A better design is to 
scan only the required metadata, unlink matching Redis keys in batches, invalidate the local cache by 
canonical key, and publish delete notifications by key.

There are several operational caveats. Redis pub/sub is not durable; a node that is down, 
disconnected, or misconfigured when a message is published can miss the notification and retain 
stale local cache entries until expiration or manual cache clearing. CAS node queue identifiers 
must be unique per node, or left unset so they are generated uniquely; if multiple nodes share 
the same identifier, they may incorrectly treat each other’s notifications as self-published messages and 
ignore them. Administrative logout cannot remove a user’s browser cookie directly, so correctness depends 
on the server-side ticket deletion being authoritative across Redis and all local caches. For highly 
conservative deployments, disabling the local cache or setting its size to zero trades performance 
for simpler consistency semantics.

## Eviction Policy

Redis manages the internal eviction policy of cached objects via its time-alive settings.
The timeout is the ticket's `timeToLive` value. So you need to ensure the cache is alive long enough to support the
individual expiration policy of tickets, and let CAS clean the tickets as part of its own cleaner if necessary.

Redis removes expired keys in two ways. Passive expiration happens when CAS accesses a 
key and Redis notices that its TTL has elapsed; the key is deleted before the command proceeds. 
Active expiration runs periodically in the background: Redis samples keys that have TTLs, deletes 
the ones that are already expired, and repeats this work within a CPU-time budget. This means 
expired keys are usually removed quickly, but deletion is not a continuous full scan of the keyspace.

## Ticket Registry Locking

This ticket registry implementation automatically supports [distributed locking](../ticketing/Ticket-Registry-Locking.html).
The schemas and structures that track locking operations should be automatically created by CAS using
[Spring Integration](https://spring.io/projects/spring-integration) Redis support.
