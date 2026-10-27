---
name: cas-ticket-registries
description: "Review and change ticket registries: stateless (compact tickets), queue-backed (Kafka, AMQP, Pulsar, GCP Pub/Sub), JPA (locking, MySQL/Postgres specifics, streams), Hazelcast, and multi-node scenario checks. Use when touching support/cas-server-support-*-ticket-registry or ticket compactors."
---

# Ticket registries

## Stateless ticket registry review discipline

- The ticket id is the ticket: prefix + base64url(AES-GCM(header + compact string)). The header is one byte for raw or
  raw-deflated (whichever is smaller), then the length and bytes of the prefix the ticket was issued under; reads reject
  a prefix mismatch, since the prefix outside the ciphertext picks the compactor and some layouts have the same field
  count (AT/RT, PT/PGT). The compact string is `CompactTicketCodec`: `1;` then `<length>:<value>` per
  field, lists nested the same way, so values are never escaped. Store enums by `name()`, never by ordinal. Compactors add fields in `compactFields` and read them
  with `parse(value, exactCount)`; bump `CompactTicketCodec.VERSION` when a layout changes, and say in the release notes
  that tickets issued before the upgrade are unreadable.
- Module layout: `TicketCompactor`, `CompactTicketCodec`, `CompactTicketAuthentication` and the core compactors live in
  `org.apereo.cas.ticket.registry.compact`, with `StatelessTicketRegistry` and `ShortenedServiceMatchingStrategy`, in
  `support/cas-server-support-stateless-ticket-registry-api`; the `stateless-ticket-registry` module keeps the
  auto-configuration. Modules that ship their own compactors (OAuth core, Simple MFA core) depend on the API module
  `compileOnly`, so their stateless bean configurations carry `@ConditionalOnClass(StatelessTicketRegistry.class)` next to
  the feature condition (the feature is enabled by default, so without the class check every deployment without the
  stateless module fails to start). Native hints for compactors: `CasStatelessTicketRegistryRuntimeHints` in the API module.
- Authentication in ST, PT, PGT and OAuth tickets is `CompactTicketAuthentication`: principal id, authentication date,
  handlers, credential types, remember-me and the retained authentication attributes (`clientName`, MFA context,
  trusted device) as text. Other attributes are not kept (documented caveat).
- `expand` creates tickets through the ticket factories, then sets the creation time and a
  `FixedInstantExpirationPolicy`; the registry then sets the id it was looked up by.
- The TGT compact form is its authentication only, as typed JSON with principal attributes stripped; updates of
  `services`, `proxyGrantingTickets` or `descendantTickets` never reach the TGC, and single logout is documented as unsupported.
- No local decode cache: expanded tickets are mutable per request, the crypto and inflate cost is small next to the JSON
  parse, and a cache is server-side state.
- There is no delete: `deleteSingleTicket` is the base no-op, so `deleteTicket(...)` returns 0 and nothing is ever
  consumed. Code that treats `delete > 0` as the single-use decision fails closed here; code that uses a deterministic
  TST id as a replay marker (`TransientSessionTicketFactory.normalizeTicketId`: DPoP, client assertions, Heimdall)
  never finds it and fails open. Callers must use the ticket `addTicket` returns: the stored id is re-encoded.
- `TransientSessionTicketCompactor` stringifies properties, so only flat string properties survive, and a one-element list
  comes back as a plain string (read list properties with `CollectionUtils.toCollection`); object-valued TSTs
  (VP transactions and results) do not. It keeps the whole service as typed JSON (class, original URL, source, format,
  tenant, attributes): flows resume with that service (delegation back to the SAML2 IdP callback with
  `srid`/`entityId`; a SAML1 `SamlService` must come back as one, or the application gets `ticket=` instead of
  `SAMLart`). A bare id (tickets compacted before) is rebuilt with the service factory. `getShortenedId` is only for
  tickets that are validated against a presented service (ST, PT, PGT, OAuth).
- Maintainer decision: the stateless registry stays 100% stateless, in the spirit of the Shibboleth IdP client-side
  storage. Never propose a server-side replay, nonce or single-use store as the fix for anything here; fixes must be
  expressible in the ticket or client storage itself (encoding, binding, lifetimes, key versioning).
- `CompactTicketCodecTests`, `StatelessTicketRegistryTests.verifyServiceTicketFieldsCannotBeInjectedThroughService`,
  `verifyServiceTicketForDistinguishedNamePrincipal` and `verifyExpandedTicketsCarryTheirStatelessIds` guard the format.
- The stateless TGT is carried by the TGC exactly as with any other registry: `SendTicketGrantingTicketAction` sets the
  TGC with the (compacted, encrypted) TGT id, and the TGC value manager signs and encrypts it as usual. Maintainer rule:
  use the existing TGC behavior unchanged; no parallel cookies, bindings, digests or browser-storage copies of the TGT,
  and no stateless-only webflow wiring for the SSO session. Browser storage remains for Duo and the SAML IdP only.
- Browsers drop cookies over 4096 bytes, and a Duo TGT makes the default (encrypted and signed) TGC about 4.3 KB. The
  documented remedy is `cas.tgc.crypto.signing-enabled=false` (TGC crypto uses
  `EncryptionOptionalSigningOptionalJwtCryptographyProperties`): the JWE is `dir` + `A256CBC-HS512`, already authenticated,
  and dropping the JWS layer saves about a quarter. A defined `cas.tgc.crypto.signing.key` keeps signing on
  (`BaseStringCipherExecutor`). Scenario `mfa-duo-universal-login-stateless` runs with it.
- `TicketGrantingTicketCompactor` keeps only the TGT's authentication (typed JSON via `BaseJacksonSerializer.forType`),
  with principal attributes removed from the principal and the handler-result principals. On expand it always re-resolves
  them through `defaultPrincipalResolver` with a `BasicIdentifiableCredential` of the principal id, keeping that id (like
  `DefaultCentralAuthenticationService.rebuildStatelessTicketPrincipal`; attribute repository results are cached), and
  creates the TGT through the `TicketGrantingTicketFactory`, then sets the creation time and a `FixedInstantExpirationPolicy`
  from the compact fields. Maintainer: no ticket impl classes and no hand-built tickets in compactors; use the factories.
  Every compactor (core and OAuth) takes an `ObjectProvider<TicketFactory>` (the OAuth factories depend on the ticket
  registry) and creates the ticket through its factory with a null parent ticket, then sets the creation time and a
  `FixedInstantExpirationPolicy`. Proxy-granting and proxy tickets go through a service ticket created by the service
  ticket factory. Factories look up registered services on every expansion; expected.
  SSO-time decisions only see attribute-repository attributes; handler-only attributes (Duo, delegated claims) are gone.
- VC issuance works statelessly (maintainer decision): `OAuth20CodeCompactor` keeps `authorizationDetails` and
  `OAuth20AccessTokenCompactor` keeps `credentialConfigurationIds` and `authorizationDetails`, both as untyped JSON that
  expands to maps (`OidcVerifiableCredentialEndpointController` reads `credential_configuration_id` from either form). The
  pre-authorized code and the c_nonce are not single use there: `consumePreAuthorizationCode` returns a stateless code
  without deleting it, and the nonce service accepts a valid stateless nonce when the delete finds nothing. Offer
  transactions reference the stored code id, the nonce endpoint returns the stored id, the offer service reads a stateless
  transaction back by id, and the token response customizer reads a stateless access token back for `authorization_details`.
  VP is unsupported there. `OidcVerifiableCredentialIssuanceTests.StatelessTicketRegistryTests` covers the issuance.
- `getTicket(id).getId()` must equal `id`, as with every other registry: callers such as `InitialFlowSetupAction` put
  `ticket.getId()` into scope and look it up again. The stateless registry sets every expanded ticket's id to the id it was
  looked up by, unless the compactor's `isTicketIdRetained()` is true (device user codes, whose id is the user code). No
  compact layout may contain the ticket's own id, or updates nest the previous id.
- Expanded TGTs get a `FixedInstantExpirationPolicy` at the original policy's maximum expiration time, so an idle
  timeout is not enforced. Maintainer: no idle timeout, no single use and no revocation, by design (deployment trade-offs,
  as with the Shibboleth IdP); keys are created or copied by hand, as with any registry. A sliding idle timeout was built and
  rejected, do not reintroduce it. Non-happy paths are reviewed last.
- Scenarios `stateless-ticket-registry`, `stateless-ticket-registry-saml2-idp`, `oauth2-login-stateless`,
  `oidc-login-stateless`, `mfa-duo-universal-login-stateless`, `ticket-validation-casv3-pgt-stateless` cover happy
  paths only; none covers VC/VP, DPoP, private_key_jwt, replay or delimiter input.
- Scenario `rest-protocol-tickets-stateless`: REST TGT and ST, ticket status, p3 validation with a stub attribute,
  reuse of the ST, tampered tickets rejected (404 / validation failure), DELETE does not revoke the TGT.
- Scenario `delegated-login-stateless`: delegation to an external CAS (`CasClient`) and SimpleSAMLphp (`SAML2Client`),
  SSO afterwards, `clientName` plus a stub repository attribute released (identity provider attributes are not kept),
  SAML1 validation, cookie sizes under 4096. The SAML2 `RelayState` is the stateless TST id, so it is far longer than
  the 80 bytes the SAML binding allows; the scenario logs its length. Identity providers that enforce the limit will
  reject delegation with this registry.
- Scenario `stateless-ticket-registry-load` combines interrupt notifications, the SAML2 identity provider, OIDC, proxy
  tickets and delegation to the simplesamlphp SAML2 IdP, checks unhappy paths (tampered or forged tickets and cookies,
  expired and reused service tickets, cookie replay after logout, attribute-based access at single sign-on) and runs a load
  over plain HTTP (`STATELESS_LOAD_ITERATIONS`, `STATELESS_LOAD_CONCURRENCY`, `STATELESS_LOAD_BROWSERS`). It also runs
  Simple MFA by email (mockmock mail server from `init.sh`): a code typed in another user's flow and a wrong code fail,
  the right code passes, single sign-on does not ask again; replicated sessions are on for OAuth and pac4j.
- Simple MFA with the stateless registry (maintainer: short code typed, stateless id held by the flow).
  `CasSimpleMultifactorAuthenticationService.store` returns the stored ticket; the send-token and verify-email actions
  put its id on the flow credential (`CasSimpleMultifactorTokenCredential.ticketId`; the view binds `token` only). Read the
  credential from the flow scope, not `WebUtils.getCredential`, which returns null while the token is still blank.
  Lookup loads the ticket by that id and compares the typed code in constant time with
  `CasSimpleMultifactorAuthenticationTicket.getCode` (`code` property, else the id); with no id, or on mismatch, the code
  is looked up as a ticket id, which keeps the stateful behavior (another principal submitting a code burns it, as
  `simple-mfa-login` expects). `CasSimpleMultifactorAuthenticationTicketCompactor` keeps service, code and principal id.
  The token endpoint answers with the stored id. Tokens are not single-use with the stateless registry; the collision
  check in `generate` and the wrong-code fallback each log a warning from the stateless registry's failed decode.
- Maintainer: callers that hand a ticket id to a browser or another party read it from the ticket `addTicket` returns;
  the registry does not change the ticket passed in, and reading the ticket back after adding it was rejected (cost,
  type-specific code). Fix call sites as scenarios need them, not all at once. Delegation: the webflow manager keeps the
  built transient ticket in the flow (it carries the request properties) and hands the stored ticket to
  `DelegatedClientSessionManager.trackIdentifier(WebContext, Ticket, Client)` and the CAS client session key. Still
  open: UMA permission tickets and WS-Federation (no compactors; not listed as supported). Password reset
  (`DefaultPasswordResetUrlBuilder` puts the stored TST id in the link; scenario `forgot-password-stateless`), account
  registration (`SubmitAccountRegistrationAction` puts the stored TST id in the activation link; scenario
  `account-mgmt-registration-stateless`) and dynamic client registration
  (`OidcDynamicClientRegistrationEndpointController.generateRegistrationAccessToken` returns the stored token, resolved
  through `resolveAccessToken`; scenario `oidc-client-registration-stateless`) are done. Reset and activation links are
  not single-use.
- CIBA on the stateless registry is push only (maintainer decision; poll and ping documented as unsupported: readiness
  is server-side state and `consumeCibaRequest` needs `deleteTicket` to return 1). `OidcCibaRequestCompactor`
  (oidc-core-api) keeps client id, scopes, resources, principal id, notification token, user code, binding message and
  tenant, rebuilds through the CIBA factory, and keeps the principal id with attributes from the principal resolver (as the TGT compactor does; a `NullPrincipal` from the resolver must not replace the id). A stateless request's
  `getEncodedId()` is its own (already encrypted) id, so `auth_req_id` is the stored id with no second encoding;
  `OidcCibaController.recordCibaRequest` reads the stored request back, and `fetchOidcCibaRequest` looks up `CIBA-` ids
  as is (the encoded-id equality check still rejects raw stateful ids). The ID token `auth_req_id` claim comes from
  `IdTokenGenerationContext.cibaRequestId` (set from the response result), since the stateless access token does not
  keep that attribute. Scenario `oidc-login-ciba-stateless`.
- SAML IdP SSO path (`buildResponseBasedSingleSignOnSession`): the service ticket id is only the assertion's
  `SessionIndex` and is never read back (SAML2 SLO is unsupported here), so the raw id is fine.
- Duo `TICKET_REGISTRY` session storage is unsupported with the stateless registry (maintainer decision: document only,
  no code). The TST holds the whole flow (authentication, result builder, all webflow scopes) as objects, Duo's SDK
  rejects a `state` over 1024 characters, and the Duo webflow is wired at startup by storage type, so a runtime fallback
  onto browser storage does not work (no `restore` transition, no storage write). Users must set `BROWSER_STORAGE`.
- Session stores on the ticket registry (`TicketRegistryCasSessionStore`, also behind pac4j through
  `CasSessionStorePac4jAdapter`; Spring Session
  `TicketRegistrySessionRepository`) keep only text properties, since the transient ticket compactor stringifies values:
  other values go in as base64 Java serialization text, times as ISO-8601. The default in-memory registry hands the
  same ticket instance to concurrent requests, so never add or remove keys of a stored ticket's properties map while
  saving a session: Spring Session keeps a fixed set of properties (all attributes under one `attributes` property)
  and only replaces their values. The session cookie (and the Spring Session id)
  follow the id of the ticket the registry returns on add and update. With the stateless registry the whole session
  rides in that cookie, so keep sessions small; the session ticket expires at a fixed instant from its creation.
  pac4j saves the request to resume as a `FoundAction`/`OkAction` (exceptions); the pac4j store serializes exceptions
  without their stack trace, which otherwise pushes the session cookie past 4096 bytes and the browser drops it (the
  OAuth callback then lands on the redirect URI without a code). The load scenario fails on any oversized cookie.
- PAR on the stateless registry: `OidcPushedAuthorizationRequestCompactor` (oidc-core-api) keeps a slim form of the
  request context (maintainer choice over the serialized request); the PAR authentication keeps its principal id, date
  and all attributes as text, since the authorize step merges those attributes into the user's authentication. The
  registered service is looked up again by client id; client credentials are dropped from the parameters; the client's
  pac4j profile is not kept. `request_uri` is read from the ticket `addTicket` returns.
- The OAuth replicated session cookie path is configured in `OAuth20HandlerInterceptorAdapter.preHandle`
  (`OAuth20ConfigurationContext.configureSessionReplicationCookiePath`) before the pac4j security interceptor can write
  the cookie; configuring it only in the controllers left a `Path=/` cookie from the first request after startup.
- Scenarios that start an external SAML2 IdP from `readyScript` (after CAS is up) must call `/cas/sp/idp/metadata`
  before the first delegated login: the pac4j client failed to load the IdP metadata at startup, and redirecting to it
  fails with a `NullPointerException` in `ChainingMetadataResolver.setResolvers` until that endpoint forces a reload.
- `addTicket` and `createTicketGrantingTicket` hand back a `DefaultEncodedTicket` here, which has no creation time and cannot be compacted again: `updateTicket` on it fails with a `NullPointerException` in `TicketCompactor.compact`. Guard updates of tickets that came back from the registry with `isStateless()`, as `OAuth20DefaultTokenGenerator` does for codes, refresh tokens and the parent ticket-granting ticket.
- Test fixtures such as `AbstractOidcTests.getAccessToken` return Mockito mocks: `markTicketStateless()` does nothing on them, so stub `isStateless()` instead, and make the resolved ticket a different object than the stored one so the test can tell them apart.

## Queue-backed ticket registries (Kafka, AMQP, Pulsar, GCP Pub/Sub)

- Each node keeps its own copy of the registry and broadcasts every change, so the consuming side must be
  one consumer group, queue or subscription *per node*, named from the node's `PublisherIdentifier`
  (`cas.ticket.registry.core.queue-identifier`, random when unset). A name shared by the nodes turns the
  broadcast into load balancing: each change reaches one node. The AMQP registry (a queue per identifier) and
  the Kafka service registry stream (`groupId` = identifier) do it right, and the Kafka ticket registry now
  consumes in `<group-id>-<queue-identifier>`. Pulsar subscribes as `<subscription-name>-<queue-identifier>`
  (`CasPulsarTicketRegistryAutoConfiguration.subscriptionNameFor`, covered by the two-instance `pulsar-ticket-registry`
  scenario). Pulsar subscriptions are durable, so nodes need a stable queue identifier or every restart leaves an abandoned
  subscription accumulating backlog. GCP Pub/Sub subscribes as `<topic>Subscription-<queue-identifier>`
  (`CasGoogleCloudPubSubTicketRegistryAutoConfiguration.subscriptionNameFor`, matched by exact name, never by
  prefix, since every node's name shares the prefix) and has no two-instance scenario, since no emulator runs in CI.
- Pulsar commands travel as `String` (`SchemaType.STRING`) written and read with `PulsarTicketRegistryPublisher.MAPPER`
  (default typing on, as the Kafka registry's mapper), received by `PulsarTicketRegistryMessageListener`. Pulsar's
  `SchemaType.JSON` derives a schema from the declared class, which cannot describe `BaseMessageQueueCommand` subclasses
  carrying a `Ticket` known only at runtime. Any new broker-backed registry should ship its own JSON the same way.
- JPA ticket registry locking needs Spring Integration's `INT_LOCK` tables, which Spring only creates for embedded
  databases. Scenarios on MySQL/PostgreSQL with `cas.ticket.registry.core.enable-locking=true` must set
  `spring.integration.jdbc.initialize-schema=always`, or every lock fails ("Failed to lock for: TGT-...") and no service
  ticket is issued; a scenario that never asks for a service ticket will not notice.
- `JpaTicketRegistry` never leaves a managed entity behind: `updateTicket` is a bulk JPQL `UPDATE` (falling back to
  `persist` when no row matched), which bypasses the persistence context, so every read detaches what it loaded when a
  transaction is bound (`toDetachedTicket`) and adding flushes and detaches (`persistDetached`). A new read path must do
  the same, or a later read in the same transaction gets the copy from before the update. Reads also carry
  `HibernateHints.HINT_READ_ONLY`. `BaseJpaTicketRegistryTests` asserts zero managed entities after such sequences.
- MySQL caps index keys at 3072 bytes (768 `utf8mb4` characters) and `ddl-auto=update` only logs a refused index, so a
  ticket column that is indexed on MySQL must stay within that (`MySQLJpaTicketEntity` overrides the lengths;
  `MySQLJpaTicketRegistryTests` checks the indexes exist). MariaDB (10.11, generic entity) shortens a single-column index to
  a 768-character prefix on its own but refuses the `type,principalId` index.
- JPA `expirationTime` is the earliest expiry (`JpaTicketEntityFactory.toExpirationTime`): now when `isExpired()` at write
  time (marked expired, out of uses), else the earlier of `toMaximumExpirationTime` and `IdleExpirationPolicy` idle time,
  looking through `BaseDelegatingExpirationPolicy.getExpirationPolicyFor` (public for this). Do not make the delegating
  policy implement `IdleExpirationPolicy`: Redis calls `getIdleExpirationTime(...).toInstant()` assuming non-null.
  `TicketRegistryStreamCriteria.expiringBefore` asks for candidates; only JPA honors it. `DefaultTicketRegistryCleaner`
  full-scans on run 0 and every 60th run (`FULL_SCAN_INTERVAL`), so tests that need a candidate run call `clean()` once
  on a fresh cleaner first; mocks must stub `stream(any(TicketRegistryStreamCriteria.class))`, not `stream()`.
- MySQL ticket registry: `JpaTicketEntityFactory.withDialectDefaults` copies the settings, sets fetch size 0 for the
  persistence unit and adds `useCursorFetch`/`cachePrepStmts`/`prepStmtCacheSize`/`prepStmtCacheSqlLimit` unless set in
  `properties` or the URL; streaming queries set the fetch size as a hint (`streaming(...)`). New streaming queries in
  `JpaTicketRegistry` must use `streaming(...)`, lookups must not.
- Postgres ticket attributes are `jsonb` (`PostgresJpaTicketEntity`); the attribute lookup is
  `t.attributes @> jsonb_build_object(CAST(:k AS text), jsonb_build_array(CAST(:v AS text)))`, served by the GIN index
  `idx_ticket_attributes` (`jsonb_path_ops`). JPA `@Index` cannot declare GIN and CAS deliberately does not run DDL of
  its own (Misagh removed a startup initializer): the docs make the admin create the index once after the table exists;
  `PostgresJpaTicketRegistryTests` runs the documented statement inside `jpaTicketRegistryTransactionTemplate`: the ticket
  data source has autocommit off and Postgres DDL is transactional, so a bare `JdbcTemplate.execute` is rolled back
  when the pooled connection is returned and the index never appears. Schema migrations like this may move to Liquibase
  later. On an old `json` column `@>` fails; the docs recommend a clean ticket table for every new deployment or upgrade.
- `getSessionsWithAttributes` binds the ticket type and values; attribute names stay in the JSON paths as quoted literals
  (bound on Postgres via `->`, which is matched with `jsonb_exists` rather than the `?` operator Hibernate would read as a
  parameter). `build/jpa-ticket-registry-run.sh` runs the module against MySQL, MariaDB and Postgres in turn.
- JPA registry streams (`stream`, `getSessionsFor`, `getTicketsFor`) must be consumed inside a ticket transaction:
  outside one, MySQL fails on the first row with "Operation not allowed after ResultSet closed", because the connection
  goes back to the pool. Tests that read them wrap the read in `jpaTicketRegistryTransactionTemplate`.
  `getSessionsWithAttributes` is the exception: its results are small and its callers (delegated OIDC/SAML2 logout, MCP
  tools) run without one, so it collects the matches inside its own transaction and returns the list's stream.
- Tests in classes extending `BaseTicketRegistryTests` must be `@RepeatedTest`, never `@Test`: its `@BeforeEach` takes
  a `RepetitionInfo`.
- Two-node scenarios for any ticket registry call `cas.verifyTicketRegistryAcrossNodes()` (in `cas.js`): ST from node 1
  validates once on node 2, SSO is honored across nodes, and logout on node 2 ends the session on node 1 even when the old
  TGC is replayed. `pulsar-ticket-registry`, `kafka-ticket-registry`, `jpa-ticket-registry-loadbalanced` and
  `mongodb-ticket-registry-loadbalanced` are only that call plus `script.json` (`amqp-ticket-registry` adds its SAML2 login after it);
  the helper needs `cas.service-registry.core.init-from-json=true`, httpbin from the `bootstrapScript`, and a per-instance
  `cas.server.name`; give a new registry the same shape rather than a bespoke script.
- The symptom is a two-instance puppeteer scenario that fails about half its CI runs, with every in-process
  attempt of a failing run failing alike: partitions are assigned once when the instances start and the
  attempts reuse the running servers. `ci/tests/kafka/docker-compose.yml` sets `KAFKA_NUM_PARTITIONS=1`, so in
  a shared group a single consumer owns each topic, and a ticket never reaches the other node, which then
  rejects the TGC and clears it.

## Hazelcast ticket registry

- Entry lifetime comes from `ExpirationPolicy.getTimeToLive(ticket)`, never the no-argument `getTimeToLive()`: on delegating policies (remember-me) the latter is the default policy's value. The time-to-live is capped by `toMaximumExpirationTime(ticket)` so an update does not restart it. Write with the six-argument `IMap.set(key, value, ttl, unit, maxIdle, unit)`: the four-argument form keeps the map's max-idle, which is the default lifetime of the ticket type (the catalog's storage timeout) and evicts longer-lived tickets.
- The registry does not own `casTicketRegistryHazelcastInstance`; throttling and the Hazelcast monitor share it. Keep public `close()`/`shutdown()` methods and `DisposableBean`/`AutoCloseable` off the registry: Spring infers them as destroy methods of the `@Bean` and a registry refresh would shut the shared instance down. A mocked `HazelcastInstance` returns a `null` config, so a test that verifies `shutdown()` is not called must stub `getConfig()`, or the old code's NPE is swallowed and the test passes anyway.
- No CAS module uses Hazelcast replicated maps; every module opens its maps with `getMap()`. `HazelcastConfigurationFactory.buildMapConfig` always returns a `MapConfig`, and the `cluster.core.replicated`/`async-fillup` settings were removed (they only produced a `ReplicatedMapConfig` nobody opened, leaving the real map on defaults). Replicated maps would not suit the ticket registry or sessions anyway: they have no predicates, indexes, SQL or paging.
- `stream(criteria)` pages over the whole registry (maps in catalog order, keys within a map), not per map. Attribute queries use `Predicates.in("attributes[<digested key>]", values)` combined with `Predicates.or`; never build a `Predicates.sql` string from attribute values (crypto is off by default, so values arrive raw).
- Principal lookups (`countSessionsFor`, `getSessionsFor`, `deleteTicketsFor`) go through the `principal` index, which holds the digest of the lower-cased principal id (`digestPrincipal`). The registry runs no SQL and Hazelcast Jet is off: every query is an `IMap` predicate, aggregation or entry processor, and the `core.enable-jet` setting and the SQL mappings were removed (the `hazelcast-sql` dependency and its native hints are gone too). Bulk removal is `executeOnEntries(new HazelcastTicketDocumentRemovalProcessor(), predicate)` over the maps whose implementation class is authentication-aware, and the count is the processor's `true` results. Counts use `IMap.size()` or `aggregate(Aggregators.count(), predicate)`. Indexes exist only for what is queried: `principal` on authentication-aware ticket maps, `service` on service-aware ones; a new query on another field needs its index added in `CasHazelcastTicketRegistryAutoConfiguration`. Spring's `RuntimeHintsPredicates.reflection().onType(...)` is the check for `registerSerializationHints`, which registers Java serialization as a reflection hint.
- The ticket registry's map configs are part of the `Config` the instance is created with (static config); other modules sharing `casTicketRegistryHazelcastInstance`, such as throttling, still add theirs as dynamic config. A test can tell them apart through `((DynamicConfigurationAwareConfig) instance.getConfig()).getStaticConfig()`. Unbounded `stream()` reads keys and then `getAll` in batches; do not use `IMap.iterator(fetchSize)`, which fetches values one call at a time. A TGT's service tickets are removed with `executeOnKeys` per map (`deleteServiceTickets` override).
- `HazelcastTicketDocument` is stored with compact serialization (`HazelcastTicketDocumentSerializer`, registered on the ticket registry's `Config` before the instance starts) and is not `Serializable`: fields are `principal` and `service` (both digested like ticket ids when encryption is on), `ticket` (bytes of `SerializationUtils.serialize` of the possibly encoded ticket; read back through `toTicket`) and `attributes`, an array of `HazelcastTicketDocument.toAttributeEntry(name, value)` strings kept only for TGTs and queried with `Predicates.in("attributes[any]", ...)`. Adding a field means changing the serializer; stored entries from older versions are not readable. `hazelcast-sql` is no longer a dependency, so nothing may use Hazelcast SQL or Jet.
- `core.enable-compression` stays: it still compresses what other modules keep with Java serialization (throttling records on the shared ticket registry instance, streamed service definitions); tickets are compact and never compressed.
- Hazelcast map defaults: eviction `NONE` (size eviction silently logs users out), merge `LATEST_UPDATE`. Merge policies that compare per-entry statistics (`LATEST_UPDATE`, `LATEST_ACCESS`, `HIGHER_HITS`, `EXPIRATION_TIME`) need `MapConfig.setPerEntryStatsEnabled(true)`, or the member fails to start; `buildMapConfig` sets it. A config-only test does not catch that; start an instance. Expired TGTs reach `HazelcastTicketRegistry.handleExpiredTicket` through `HazelcastTicketExpirationListener`, a local entry listener with values registered on the TGT map config, so single logout happens once on the owning member; the listener looks the registry up lazily so a refreshed registry is used.
- Hazelcast test members set `tcpip-enabled=false` to stay alone; that only works because `HazelcastConfigurationFactory` turns auto-detection off. With auto-detection on, Hazelcast falls back to multicast and members of different test contexts and Gradle JVMs on a developer machine form one cluster (the VM has no multicast, so it never shows there). A member without a joiner also rejects members that join it ("Incompatible joiners"); a test that needs two members builds both itself with TCP/IP and its own cluster name. Never send a Java lambda as a Hazelcast `Predicate` or processor unless it is `Serializable` and only captures serializable state; evaluate plain `java.util.function.Predicate`s locally.
