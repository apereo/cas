---
name: cas-storage-backends
description: "Backend-specific rules for MongoDB (connection factory, client lifecycle, ticket and service registry documents, indexes, change streams), Redis/Lettuce (pipelines, SCAN, streams), JPA/Hibernate 7 and DynamoDB. Use when touching any MongoDB, Redis, JPA or DynamoDB backed module."
---


# Storage backends (MongoDB, Redis, JPA, DynamoDB)

## MongoDB backends (service registry and ticket registry)

- Three modules carry everything: `cas-server-support-mongo-core` (`MongoDbConnectionFactory`,
  `BaseConverters`, `DefaultCasMongoTemplate`, `MongoDbClusterTopologyManager`),
  `cas-server-support-mongo-service-registry`, and `cas-server-support-mongo-ticket-registry`
  (registry, `MongoDbTicketDocument`, `MongoDbTicketRegistryFacilitator`, catalog provider).
  Findings in the core factory apply to all ~19 `*-mongo` support modules at once; findings in a
  registry usually do not generalize.
- `MongoDbConnectionFactory.buildMongoDbClient` has two branches and they are not equivalent. The
  `client-uri` branch applies the connection string and nothing else: no `CasSSLContext`, no
  `ZonedDateTimeCodecProvider`, no pool/socket/server settings, no read or write concern, no
  `retryWrites`. A connection string cannot express a custom trust store, so a deployment using
  `client-uri` silently falls back to the JVM default trust material. Check which branch a setting
  lives in before describing it as configurable. (Still open.)
- Read the defaults from the operator's point of view before judging a flow: `ssl-enabled=false`,
  ticket-registry `crypto.enabled=false`, `write-concern=ACKNOWLEDGED`, `read-concern=AVAILABLE`,
  `retry-writes=false`. With the cipher off, `AbstractTicketRegistry.collectAndDigestTicketAttributes`
  returns attributes undigested, so the Mongo document carries the principal, the service and every
  authentication attribute in clear beside the ticket JSON. (Still open.)
- Durability is part of the security argument here. `deleteTicket` is a compare-and-swap only as far
  as the write concern makes the delete durable; `w:1` plus a replica-set failover can roll back the
  removal of a one-time-use ticket, code or nonce. Evaluate single-use state against the write
  concern, not just against the code path. (Still open.)
- Bean-construction side effects in a refresh-scoped bean are runtime operations, not startup
  operations: the bean method runs again on every `/actuator/refresh`. Destructive setup
  (`drop-collection`, `drop-indexes`) therefore lives in a plain-singleton `InitializingBean` that
  the refresh-scoped registry declares with `@DependsOn`, so singleton scope is what limits it to
  one execution per context. Express "startup only" through bean scope, not through a flag the
  configuration class retains; a configuration class holding mutable state to remember whether it
  has run is the shape to avoid. Do not mark those initializers `@Lazy(false)` either -- the
  `@DependsOn` edge already orders them, and eager marking would move Mongo I/O onto the startup
  path of an application that otherwise initializes lazily.
- Do NOT close a Mongo client from a bean's disposal here, and treat the leaked clients as a known
  cost rather than a bug to patch locally. CAS declares ~2400 beans
  `@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)`, and `ScopedProxyMode.DEFAULT` means NO scoped
  proxy: consumers hold the instance itself, not a proxy that re-resolves. `RefreshScope.refreshAll()`
  therefore disposes objects that live consumers are still using. Making `DefaultCasMongoTemplate` a
  `DisposableBean` that closed its client broke `/actuator/health` in the
  `mongodb-ticket-service-registry` scenario: `BeanContainer.destroy()` disposed the templates inside
  `mongoHealthIndicatorTemplate`, while Spring Boot's health registry -- not refresh-scoped, so
  outside the refresh cascade -- still held the `CompositeHealthIndicator` built over them, and the
  next check failed with `ClientSessionException: state should be: open`. The login path survived only
  because its whole chain is refresh-scoped and was rebuilt together, which is what makes this failure
  mode easy to miss.
- Carry two rules from that. A resource held behind a refresh-scoped bean cannot be released by
  disposal alone -- the owner's lifetime has to match its consumers', which is a scoped-proxy
  decision, not a `destroy()` one. And never reason about refresh behaviour from the annotation
  alone: `ScopedProxyMode.DEFAULT` is the opposite of what the name suggests.
- For the record, Spring Data will not release these clients either:
  `SimpleMongoClientDatabaseFactory(MongoClient, String)` records the client as externally managed and
  `MongoDatabaseFactorySupport.destroy()` is `if (mongoInstanceCreated) { closeClient(); }`. CAS always
  hands in its own client, because a connection string cannot express the `MongoClientSettings` it
  needs. So the clients really are never closed -- that is the cost of the model above, not an
  oversight at the template.
- `BeanContainer` extends `DisposableBean`, and `ListBeanContainer.destroy()` disposes every
  `DisposableBean` entry and closes every `Closeable` one. That is exactly how the health templates
  came to be closed: a container of resources is not a leak, it is a disposal amplifier.
- `mongoDbTicketRegistryTemplate` is `@Primary`. Any module injecting `MongoTemplate` by type gets
  the ticket-registry database; check the qualifier before assuming a module talks to its own.
  (Still open.)
- The ticket registry propagates storage failures rather than returning a benign value:
  `addSingleTicket` and `updateTicket` declare `throws Exception`, and a ticket definition missing
  from the catalog throws. Only a genuine miss returns null. Do not reintroduce a `catch (Throwable)`
  that logs and continues — that is what made a failed insert look like a successful login. When
  reviewing any storage backend, read the catch blocks before the happy path.
- Multi-collection reads go through `MongoDbTicketRegistry.streamTicketDocuments`, which records
  every cursor it opens and closes them from the returned stream's `onClose`; a consumer that
  short-circuits never lets `flatMap` close the inner stream it was reading. Callers must close the
  stream. Per-collection queries are bounded by `limitQuery` to `from + count`, because no single
  collection can contribute more than that to the result, while the global skip/limit stays in the
  JVM since it spans collections — do not push `skip` down, it does not mean the same thing there.
- Collection names are resolved once per call and `distinct()`-ed, so definitions sharing a storage
  name are read and counted once.
- Writer and reader must agree on key mapping. `MappingMongoConverter` is configured with
  `MongoDbConnectionFactory.MAP_KEY_DOT_REPLACEMENT`, and `digestAttributeKey` applies the same
  replacement when a query addresses `attributes.<key>`. Check the converter's configuration
  whenever new code addresses a map field by path.
- `IDX_PRINCIPAL` is created on every ticket collection, not only on the ticket-granting ticket
  collection, because `deleteTicketsFor` and the principal criteria run against all of them.
- Ticket reads project to `json` and `type` (`includeTicketContent`); `query(decode=false)` projects to `ticketId` and
  `principal`. A new read path that needs another field must add it to the projection, or the field comes back null.
- Registry-wide counts (`countTickets`, `sessionCount`, `serviceTicketCount`) use `estimatedCount`; filtered counts
  keep `count(query)`. Removing a ticket-granting ticket deletes its `getServices()` tickets with one `$in` remove per
  collection (`deleteServiceTickets` override) without reading them back; linked proxy-granting tickets still take
  the inherited path because each needs its parent updated.
- Which collection gets what is decided once, by `MongoDbTicketRegistry.getSessionTicketDefinitions` (the
  ticket-granting ticket definition) and `getServiceTicketDefinitions` (`ServiceAwareTicket` implementations). The
  registry writes `attributes` only into session collections and runs service lookups only against service
  collections; the facilitator builds `IDX_ATTRIBUTES`/`IDX_SERVICE` on the same sets and drops them elsewhere. Change
  readers, writers and indexes together, or a lookup ends up scanning an unindexed collection.
- The ticket id is the document `_id` (`MongoDbTicketDocument.ticketId` is `@MongoId(FieldType.STRING)`, so a value
  that happens to look like an ObjectId is never converted; `FIELD_NAME_ID` is `"_id"`). Pass `MongoDbTicketDocument.class`
  to every `remove`/`updateFirst`/`find` that addresses the id, so the query is mapped with that field type. There is no
  `IDX_ID` any more; the facilitator drops one it finds. `migrateTicketDocuments` (also run per collection at startup)
  converts documents from earlier versions (`ObjectId` `_id` plus a `ticketId` field) with one `$merge` into the same
  collection (`keepExisting`) followed by deleting the `ObjectId` documents; it touches nothing else, so tests may call
  it on a shared database. Mixed-version clusters are not supported across this change.
- `createOrUpdateIndexes` drops an index whose options changed before recreating it, and restores the dropped index when
  the replacement fails, so a collection is never left without it.
- `MongoDbServiceRegistry` applies post-load listeners on every read path (`load`, `findServiceById`, exact lookups,
  `getServicesStream`); a new read path must do the same, or OIDC services lose their scope-based release policies.
- `MongoDbServiceRegistry` exact lookups (`findServiceByExactServiceId`/`Name`) return the first match in natural
  order, sorted in the JVM: the comparator leads with `getEvaluationPriority()`, which is type-derived and never
  stored. `size()` is `estimatedCount`; `delete` is a single remove by id.
- `MongoDbServiceRegistryChangeStreamWatcher` (bean in the inner `MongoDbServiceRegistryChangeStreamConfiguration` of `CasMongoDbServiceRegistryAutoConfiguration`) is a plain
  singleton `SmartLifecycle`, deliberately not `@RefreshScope`: a refresh would dispose of it and, since scoped beans
  are recreated only on access, nothing would start a new one. It listens for `RefreshScopeRefreshedEvent` and
  restarts itself instead (stop, then `start()` re-reads the settings). `start()` checks `change-stream.enabled` and
  `isReplicaSetDefined` (no I/O), then a virtual thread asks the server `hello` for `setName` and stops for good
  without one. Activation is decided at runtime on purpose; a class-level `@Conditional` on properties is evaluated
  at AOT build time for native images. It re-resolves `mongoDbServiceRegistryTemplate` from its `ObjectProvider` on
  every poll and reopens the stream when a refresh produced a new instance. On change it calls
  `ServicesManager.load()` after the quiet period rather than patching the cache, so templates, environment
  filtering, expiration and registry post-load listeners all apply. Its own node's writes come back as events too;
  the reload is idempotent. The scenario needs the replica set from `run-mongodb-server-clustered.sh` (ports 37017-37019).
- `MongoDbConnectionFactory.getMappingBasePackages()` is empty by default, so building a template scans nothing; the
  mapping context registers entity types on first use. Pool settings apply only to the host/port branch.
- `casTicketRegistryLockRepository` exists for Redis and JPA and does not exist for Mongo, so
  `LockRepository` is JVM-local on a Mongo cluster. `updateTicket` is an unconditional `updateFirst`
  with no version check, so every read-modify-write invariant is last-writer-wins across nodes.
  (Still open.)
- Scenario `mongodb-service-registry-change-stream` runs the service registry against the replica set
  (`run-mongodb-server-clustered.sh`), with the scheduled reload an hour away, and creates, changes and deletes a definition
  with `mongosh` inside the container (`execFileSync`, no shell, so `$set` survives); a URL is only accepted or refused
  afterwards if the change stream reloaded. It does not initialize from JSON: the JSON registry would stay in the chain
  and keep serving its own copy of the definition after Mongo changes. The rest of this paragraph is about the ticket registry.
- Ticket registry puppeteer coverage is `mongodb-ticket-service-registry` only. It refreshes the context first, then
  clears sessions, logs in once and asserts exactly one ticket-granting ticket, the health indicator
  and the ticket-registry cleaner — so it exercises the refresh path but asserts nothing about what
  survives a refresh. There is no crypto-enabled variant, no TLS and no concurrency, so a change in
  this area needs a new scenario rather than a rerun. `http-session-mongodb`, `authn-events-mongodb`,
  `configuration-properties-mongodb`, `simple-mfa-trusted-device-mongodb`,
  `saml2-idp-login-sp-metadata-mongodb` and `delegated-login-saml2-mongodb-metadata` exercise the
  shared connection factory and are the ones to re-run for any `cas-server-support-mongo-core` change.
- `MongoDbTicketRegistryTests` clears the whole registry in `@BeforeEach` while JUnit runs its
  methods concurrently, so any new test there must assert on identifiers it created itself (a UUID
  principal or attribute value) rather than on registry-wide counts.

## Redis, JPA/Hibernate, DynamoDB and MongoDB indexes

- JPA with `@GeneratedValue` ids (Hibernate 7): `merge()` of an entity whose id is set but not in the table throws `OptimisticLockException`; reset the id to `0` to insert. The JPA GAuth repository does this when `update()` inserts a missing device.
- `DynamoDbTableUtils.createTable(..., globalSecondaryIndexes)` creates GSIs with a new table and adds missing ones to an existing table with `UpdateTable`; pass every GSI key attribute in the attribute definitions.
- MongoDB: a query with a collation uses only indexes with the same collation. Give such an index an explicit name, so it does not clash with a plain index on the same field. Avoid `MongoDbConnectionFactory.createOrUpdateIndexes` for collated indexes: the server returns the collation expanded, the options never compare equal, and the index is dropped and rebuilt at every startup.
- Spring Data Redis with Lettuce: `executePipelined` opens a dedicated connection for the pipeline (a new TCP connection when there is no pool), so a pipeline is slower than a few plain commands on the shared connection. Pipeline only bulk work such as batched deletes, never a per-login write.
- `CasRedisTemplate.scan(pattern, count)` treats `count` as a cap on the results, not as the `SCAN COUNT` hint; for a hint, use `RedisTemplate.scan(ScanOptions)` and close the cursor.
- Hibernate 7 JPQL bulk deletes (`DELETE FROM Entity e WHERE ...`) also delete the matching rows of the entity's element-collection tables (`DELETE ... WHERE id IN (SELECT ...)`), so they need no native SQL. The physical naming strategy turns table names into snake case (`google_authenticator_registration_record`), so native SQL must not use the `@Table` name as written.
- A Java `Stream` built with `onClose(...)` releases nothing unless it is closed: wrap any stream that holds a cursor or connection, such as `CasRedisTemplate.scan(...)`, in try-with-resources, even for a terminal `count()` or `collect()`. When pooling is enabled, CAS turns off Lettuce native-connection sharing, so every leaked Redis connection is a pooled one. To catch such a leak in a test, enable the pool with a small `max-active` and call the method more times than the pool allows. The fakeredis emulator in the VM cannot run the Redis ticket-registry suites that need RediSearch.
