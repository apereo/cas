---
name: cas-service-registries
description: "Review and change service registries and the services manager: lookups vs loads, caching, replication (Kafka/Hazelcast streams), chaining, JPA service registry, URL matching snapshots. Use when touching ServicesManager, ServiceRegistry implementations or support/cas-server-support-*-service-registry*."
---


# Service registries and services manager

## Services manager lookups and cache

- `ServicesManager.findServiceBy(Service)` matches against the services cache only while the background
  reload is enabled and the cache size is above zero; it consults the registry on a miss only when the
  scheduler is off or the cache is disabled. A test that writes straight to a `ServiceRegistry` and then
  looks the service up by URL must call `servicesManager.load()` first. Lookups by id or name still fall back
  to the registry. Registry events published by anything other than a services manager (the JSON/YAML
  watchers, Redis, S3) update the cache through `DefaultRegisteredServicesEventListener`, synchronously.
  `findServiceBy(Predicate)` reads the registry and must never write to the cache or index.
- `CacheableServicesManager.cacheRegisteredService` prepares a definition the way `load()` does (skips unsupported
  types, applies templates) before caching it, for saves, registry watchers and change notifications alike. Inside
  `AbstractServicesManager`, code that already ran `prepareServiceFromRegistry` caches through the private
  `cachePreparedRegisteredService` so templates are not merged twice.
- Do not override `ServiceRegistry.findServiceBy(String)` with a database filter on `serviceId`: definitions are regular
  expressions or other matching strategies evaluated in Java, so a `LIKE` on the requested URL misses them. Override
  `getServicesStream()` instead when the default `load()` is expensive or has side effects (events, a `git pull`).
- URL lookups match against `AbstractServicesManager.getSortedRegisteredServices`, a snapshot reused until the cache
  version changes or the cache size differs from it. Any new code that adds or replaces entries in the services cache
  must go through `cacheRegisteredService`/`cacheRegisteredServices` (or bump the version itself); a direct
  `getServicesCache().put` leaves URL lookups on a stale snapshot. Read paths must not re-cache or re-index services
  the cache already holds, and the returned list is shared and unmodifiable.
- The logout flow looks its `service` parameter up once per request: `AbstractLogoutAction` remembers the outcome,
  found or not, in the webflow request scope for the other logout actions of that request, and
  `DefaultLogoutRedirectionStrategy` looks the service up only when `cas.logout.follow-service-redirects` is on, then asks
  the chained single logout URL builders `supports(...)` instead of `isServiceAuthorized(...)`, which looks the service up
  again in every builder. Build new logout checks on the result already resolved; crawlers sending
  `/logout?service=<unknown>` pay for every lookup.

## Service registries and replication

- `ServiceRegistry.getServicesStream()` and `size()` are lookups, not loads: they must not publish
  `CasRegisteredServiceLoadedEvent`, which service replication and the OAuth client secret expiration listener
  consume. Only `load()` publishes it. `DefaultChainingServiceRegistry.getServicesStream()` delegates to each
  registry's stream; the interface default falls back to `load()`, so a registry with a cheaper read overrides it. `RestfulServiceRegistry.size()` logs and returns zero for an unreachable
  registry (the initializer counts at startup), while `load()` throws so the services manager keeps the cached services.
- Replication (`cas-server-support-service-registry-stream*`) applies only to file-based registries.
  `DefaultRegisteredServiceReplicationStrategy.updateLoadedRegisteredServicesFromCache` never clears the
  replication cache (late joiners read it); it keeps the newest entry per service id, removes older ones (renames
  leave them behind) and replaces the loaded service with the cached one instead of adding it. The publisher
  skips a loaded service that equals its cache entry, so steady state sends nothing; saves and deletes are always
  sent. Kafka keeps a node-local map fed by the topic, Hazelcast a shared `IMap` keyed by id, name and service id.
  Deletion markers (`event=CasRegisteredServiceDeletedEvent`) are never removed when applied: each node deletes its
  local copy (only if it has one, so no repeated delete events) and drops it from the loaded list, otherwise the
  load would publish it again over the marker. A save of the same id replaces the marker. The Kafka listener stores
  every foreign entry, markers included. Lookups (`getRegisteredServiceFromCacheByPredicate`) ignore markers: they run inside
  `save` and the create watcher, where acting on a marker would delete the definition being re-added.
- With `cache-size=0`, listing (`getAllServices`, `getAllServicesOfType`) must not cache, index or bump the cache
  version: a bump discards the sorted snapshot and the next lookup streams the registry. The id lookup and the
  unknown-URL fallback still index and bump on purpose, since that is what makes a newly found service visible to
  later lookups while the cache is off.
- `DefaultChainingServiceRegistry.findServiceBy(String)` asks every registry and picks the best match by
  evaluation order (registry order breaks ties); the exact-id and exact-name lookups still stop at the first hit.
- `HttpExecutionRequest.responseTimeout` sets a per-request response and connection-request timeout (applied in
  `HttpUtils.prepareHttpRequest` as a request `RequestConfig`, which replaces the client's, so it repeats the redirect
  setting); connect timeouts stay global. `RemoteEndpointServiceAccessStrategy` calls once (`maximumRetryAttempts(0)`)
  with its fixed `TIMEOUT`; `maximumRetryAttempts` counts retries after the first attempt, so the default 3 means 4 calls.
- Groovy SSO participation policies and Groovy activation criteria call `resolveScriptIfNeeded()` per evaluation:
  the script is resolved again only when missing, closed (expired from the script cache) or when `groovyScript`
  changed (`resolvedGroovyScript`). Tests prove reuse by swapping in a mock `ExecutableCompiledScript`.
- Kafka and Hazelcast replication caches key services by id, name and service id (`buildKey`); the `Mappable`
  default is `toString()`, which changes with every edit.
- Kafka replication consumers use a group per node named after the publisher id, a new UUID on every start, so the
  listener factory sets `auto.offset.reset=earliest` (`KafkaObjectFactory.setAutoOffsetReset`) and a node replays the
  compacted topic (records keyed by `buildKey`) on start. Without it, a node that starts after another node's startup
  broadcast never receives those services, since unchanged services are no longer sent again
  (`service-registry-replication-kafka` caught this).
- The Kafka stream test classes share one Spring context and so one cache manager; never `clear()` it in a test.
  Assert on the test's own (unique) service only.
- Attribute-based activation criteria (access strategy and attribute release) use `asMatchPredicate`, like
  required and rejected attributes in `RegisteredServiceAccessStrategyEvaluator`; patterns match whole values.

## JPA service registry

- `JpaServiceRegistry` reads through `SELECT r.id, r.body` projections, never entities: a read-only hint on the entity
  that `saveInternal` later finds and merges into would make Hibernate skip the update without an error. `load` and
  `getServicesStream` reuse the service built last time when the stored body is unchanged (`loadedServices`), so tests
  that need a fresh instance must change and save the definition; `delete`/`deleteAll` evict from that map.
  `JpaServiceRegistryTests` asserts identity reuse, zero managed entities after reads, and a bulk-save persistence
  context capped at `batch-size`.
- `JpaRegisteredServiceEntity` has no generated id: `saveInternal` calls `assignIdIfNecessary()` (random, below 2^53, as
  the JSON, in-memory, DynamoDB and REST registries do) and stores explicit ids as given, so imported ids are kept.
  Do not bring back `@GeneratedValue`: Hibernate then refuses or replaces assigned ids. `service-registry-jpa` runs
  `cas.verifyServiceRegistry` against MySQL through `docker exec mysql-server mysql ...` (2-second reload schedule).
