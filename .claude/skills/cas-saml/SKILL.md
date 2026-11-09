---
name: cas-saml
description: "Review and change SAML2 SP and IdP metadata handling: resolvers, MDQ, caching and backup files, signature trust, IdP metadata backends and generation, the shared HTTP client, the SAML2 metadata scenarios, and the pac4j-free session store of the SAML2 IdP. Use when touching support/cas-server-support-saml* code."
---

# SAML2 metadata

## SAML2 metadata review discipline

- Disabling `org.apereo.cas.util.io.PathWatcherService` does not directly disable plain XML SP metadata
  refresh in 7.3.x or master: `FileSystemResourceMetadataResolver` loads individual files through
  `InMemoryResourceMetadataResolver`, and the outer metadata cache controls reload after expiration or
  invalidation. Directory sources use OpenSAML's `LocalDynamicMetadataResolver`, not CAS PathWatcher.
  JSON metadata, Groovy sources, discovery feeds and watched service definitions are different paths;
  establish the actual metadata source before attributing stale XML to the global watcher switch.
- Local SP metadata files are change-detected by the caching resolver, not by a watcher: a resolver reports its files
  through `SamlRegisteredServiceMetadataResolver.getMetadataSourceFiles` (file system: every file or directory of the
  location; JSON: `saml-sp-metadata.json`), the loader snapshots their mtimes (a directory adds its direct children)
  into `CachedMetadataResolverResult`, and `locateAndCacheMetadataResolver` compares at most once per
  `file-system.change-detection-interval`, one caller per entry (CAS on a timestamp), then invalidates and reloads.
  A new file-backed resolver must report its files or edits wait for expiry. The JSON resolver has no
  `FileWatcherService` any more; it re-reads the file when its mtime changes, including a file created after startup.
- Operator visibility for the SP cache lives on the caching resolver interface (`getStatistics`, `getCachedEntries`,
  defaults empty) and per resolver (`describe(service, criteria)`, merged into health details and cache entries). The URL
  resolver reports the backup file, its age since the source last confirmed it (a `304` touches the mtime, a `200`
  rewrites it) and `metadataBackupStale` past `http.metadata-backup-max-age`; a stale backup is still served, logged at
  ERROR. `GET .../samlIdPRegisteredServiceMetadataCache/statistics` returns both lists, and the
  `samlRegisteredServiceMetadataCacheMeterBinder` bean publishes Caffeine `cache.*` meters tagged
  `cache=samlRegisteredServiceMetadata`. The per-service GET no longer drops entities failing `isValid()`; it adds `valid`.
- Whether cached SP metadata holds a single SP (the only case reloaded when it lacks the requested entity) is decided
  once by the loader (`CachedMetadataResolverResult.singleServiceProvider`): the predicate-only lookup walks every entity,
  so it must not run per request. Directory (`LocalDynamicMetadataResolver`) and most Groovy resolvers report 0 entities.
- Removed cache entries (refresh, D8 reload, expiry, eviction, invalidation) go through a Caffeine removal listener that
  destroys reloading/dynamic OpenSAML child resolvers after `metadataResolverRetirementDelay` (PT1M), skipping any
  instance a live entry still holds (a Groovy script may return a shared resolver). DOM/in-memory resolvers are left
  alone: destroying them buys nothing and breaks requests still holding them.
- `SamlRegisteredServiceMetadataExpirationPolicy` clamps a metadata `cacheDuration` to `core.minimum-cache-duration`/
  `maximum-cache-duration` (PT1M/P1D) and caps any lifetime at the earliest *future* `validUntil` of the entity (cache-key
  entity, else `serviceId`) and its ancestors; a past `validUntil` does not cap, or an expired aggregate would reload
  forever. Mockito returns `Duration.ZERO` for an unstubbed `getCacheDuration()`; stub `null` in policy tests.
- The SP cache key classifies the *SpEL-resolved* location, MDQ when any comma-separated part is, matching
  `MetadataQueryProtocolMetadataResolver.supports`; classifying the raw string collapsed SpEL or mirrored MDQ locations
  into one key for every entity.
- `AbstractSamlIdPMetadataLocator.fetch(service)` compares what `fetchInternal` returns with the cached global document
  (metadata, certificates, keys); a match means the store fell back, so the service is remembered in a key-only cache
  (10,000 entries, the document cache's expiry) and gets the global instance. Only services with their own documents take
  one of the 100 document-cache slots. `appliesTo` cannot tell them apart: the file-system locator stamps the service
  owner on the global artifacts. Never compute another key inside the Caffeine mapping function; fetch the global first.
- Redis SP metadata keeps `SamlMetadataDocumentIndex:<sha512(entityId)>` hashes (field = document key) next to the
  `SamlMetadataDocument:*` keys; entity lookups read the hash and `MGET`, never SCAN. Pre-index documents are indexed once,
  guarded by `SamlMetadataDocumentIndex:complete`; every delete path must also drop the index field. The prefix must not
  match `SamlMetadataDocument:*`, which `load()` and the scenario scan.
- There are two independent metadata subsystems and they share almost nothing. *SP metadata*
  runs `SamlRegisteredService` -> `SamlRegisteredServiceDefaultCachingMetadataResolver` ->
  `SamlRegisteredServiceMetadataResolverCacheLoader` -> the resolvers under
  `.../cache/resolver/**` -> OpenSAML filters. *IdP metadata* runs
  `SamlIdPMetadataGenerator` -> `SamlIdPMetadataLocator` (one per storage backend) ->
  `SamlIdPMetadataResolver` -> `SamlIdPMetadataCredentialResolver`. A third, `saml-mdui-core`,
  is a separate adapter stack that does not reuse either. Review each as a path.
- `SamlIdPMetadataResolver` is a singleton that calls `setMetadataRootElement` — which swaps
  `AbstractBatchMetadataResolver`'s backing store — on every cache miss, then reads it back.
  Anything reached through `SamlIdPMetadataCredentialResolver` inherits that shared state, so
  per-service IdP metadata is where concurrency defects in this area live. That swap-then-read
  pair is now held under a `CasReentrantLock` on the miss path only, with the cache re-checked
  inside the lock; `setMetadataRootElement` is `protected` and its javadoc carries the calling
  contract. Anything new that installs a backing store on that singleton has to take the same
  lock, and results handed out of the resolver must be materialized (`List.copyOf`) rather than
  left as a lazy view over a store a later resolution replaces. There is no puppeteer scenario
  for per-service IdP metadata — no scenario sets `idpMetadataLocation` — so this path is
  covered by `SamlIdPMetadataResolverTests` alone.
- CAS does not use OpenSAML's `HTTPMetadataResolver` / `FileBackedHTTPMetadataResolver`.
  `UrlResourceMetadataResolver` reimplements fetch-and-back-up by hand, which is why the usual
  guarantees are absent: no background refresh timer and no min/max refresh delay. Do not assume
  OpenSAML semantics when reading this code. The backup file is now treated the way that class
  would treat it: `force-metadata-refresh` (default `true`) means "do not serve the backup
  without checking the remote first", never "delete the backup", a successful download
  overwrites it in place, and `resolveMetadataLocations` falls back onto it once every location
  has failed (comma-separated locations are mirrors; a live mirror beats a stale copy). The two readers of the backup want different answers about root validity
  — reusing it instead of contacting the remote requires `Boolean.TRUE.equals(isRootValid())`,
  falling back onto it only requires `!Boolean.FALSE.equals(...)`, because a document with no
  `validUntil` reports its validity as unknown.
- The URL and MDQ resolvers share one flow: `resolveMetadataLocation` per location (a `304` is served
  from the backup, a 2xx is parsed from memory and replaces the backup, anything else is a failure
  with its reason), then the backup, then a `SamlException` listing every location and why it
  failed. They no longer return an empty list on failure, because that reason is what the D1
  failure cache remembers and what the cached-metadata endpoint answers; tests assert the
  exception. MDQ only adds the `Accept` header, basic authentication and the per-entity
  locations. Read the status with `HttpStatus.resolve`, never `valueOf`.
- Conditional requests: validators (`ETag`, `Last-Modified`) live in `<backup>.properties` next to the
  backup with a `digest` (SHA-256) of the document they came with, and are sent only while the
  backup still has that digest — services sharing a location share the backup, so the validators
  may otherwise describe a document another load has replaced. No extended file attributes.
- The metadata backup file is named `sha(metadataLocation)` while the Caffeine key is
  `serviceId|metadataLocation`. Two services pointing at one URL are two cache entries over one
  file, and nothing upstream serializes them. The sharing is deliberate — a federation aggregate
  should not be downloaded once per service — so the fix was to make the write atomic:
  `writeMetadataToBackupFile` writes a sibling temporary file and moves it into place, falling back
  to `REPLACE_EXISTING` where `ATOMIC_MOVE` is unsupported, rather than giving each service its own
  copy or holding a lock across a download. Anything new that writes into the backup directory goes
  through that method, and remember the `.tmp` siblings when writing directory assertions.
- Trust here is opt-in, and it used to fail open as well. A service without
  `metadataSignatureLocation` still gets no `SignatureValidationFilter` at all — that is the
  operator's choice, not a defect. What changed is the other half:
  `SamlUtils.buildSignatureValidationFilter` still returns `null` rather than throwing when the
  configured resource cannot be read, but `addSignatureValidationFilterIfNeeded` now treats that
  null as fatal and throws. It is only reached once something has established that the metadata is
  meant to be verified — the service defines a signature location, or the stored document carries a
  signature — so a null there means the verification that was asked for cannot be performed. Every
  resolver catches the failure and yields no metadata, which is the intended outcome; do not
  "fix" that by restoring the warning.
- `buildRequiredValidUntilFilterIfNeeded` only runs when `metadataMaxValidity > 0`, and
  `SamlRegisteredServiceMetadataExpirationPolicy` reads `cacheDuration` but never `validUntil`.
  Expiry and validity are two different mechanisms in this code; do not conflate them. The
  policy can also return a negative duration when a service expiration date is in the past.
- IdP metadata backends are not interchangeable, and what to check in each is whether the *global*
  lookup is scoped. JPA and MongoDB used an unfiltered "first row" query
  (`SELECT r FROM SamlIdPMetadataDocument r`, `findOne(new Query())`) and so could answer with a
  per-service document and its keys; both now query `appliesTo` for the global owner like any other.
  The rest were already scoped: Redis scans by `appliesTo`, DynamoDB queries it, GCP uses it as the
  blob id, S3 as the bucket, Git as the directory. REST sends `appliesTo` only for a service and
  leaves the global case to the remote server, which is that endpoint's contract rather than a CAS
  defect. `appliesTo` is `SamlIdPUtils.getSamlIdPMetadataOwner` = `name + '-' + id`, which also
  becomes a directory name on the file-system locator and a key elsewhere — unsanitized, and it
  changes when a service is renamed.
- Per-service IdP metadata is not something CAS generates. `SamlIdPMetadataLocator
  .shouldGenerateMetadataFor` is `registeredService.isEmpty()` and no backend overrides it, so the
  generator only ever mints the global document — at context startup, from each backend generator's
  `afterPropertiesSet`. A per-service document exists only where an operator provisioned one: a
  directory named by `idpMetadataLocation` for the file-system locator, or rows and documents
  inserted directly for JDBC and MongoDB.
- The guard in front of generation reinforces that. `AbstractSamlIdPMetadataLocator.exists` calls
  `fetch`, and a per-service `fetchInternal` must fall back to the global document when the service
  has none (JPA, MongoDB, Git and, since `saml2-idp-login-metadata-redis` showed every SAML2 response
  failing with "Unable to locate signing credentials", Redis; DynamoDB and Google Cloud Storage still
  look up only `appliesTo`), so `exists(service)` is true as soon as the *global* document exists. Both
  `BaseSamlIdPMetadataGenerator.generate` and `SamlIdPMetadataResolver.resolveMetadata` check
  `exists` before `shouldGenerateMetadataFor`, which means `generate(Optional.of(service))` is a
  no-op in a normally-started deployment and returns the global document. That fallback is the
  intended behaviour — a service without its own metadata uses the IdP's — not a defect.
- Two consequences worth carrying. Scoping the global lookup matters only for deployments that
  provision per-service rows, so do not describe it as reachable out of the box. And a test cannot
  create a per-service JDBC or MongoDB document through the generator: it has to insert one the way
  an operator would, through the entity manager or `mongoTemplate`, and on JDBC through the
  dialect-specific `JpaSamlIdPMetadataDocumentFactory`. `JpaSamlIdPMetadataGeneratorTests
  .verifyService` looks like it covers this and does not — its `assertNotNull`s are satisfied by the
  global fallback.
- The file-system generator writes the IdP signing and encryption private keys in the clear
  with default permissions; the other backends run the key through `metadataCipherExecutor`.
  Do not describe key-at-rest handling as uniform across backends.
- `/idp/metadata` is public and calls `generate(...)` on every request, including with a
  caller-supplied `service` parameter. Read that together with the two bullets above before
  calling it a resource-exhaustion vector, which is the mistake made once in this repo: generation
  is guarded by an `exists()` check that the global document already satisfies, so in a started
  deployment the endpoint mints nothing and simply returns the global metadata. What is genuinely
  unguarded is the first generation, before any document exists — no lock across threads or nodes,
  and two RSA keypairs at a default `key-size` of 4096.
  The controller answers `text/xml` unless `Accept` names `application/samlmetadata+xml`, sets a SHA-256 `ETag` with
  `Cache-Control: no-cache`, and answers `If-None-Match` (lists, `W/`, `*`) with `304`.
- The parser pool in `CasCoreSamlAutoConfiguration` is properly hardened (doctype disallowed,
  external entities off, secure processing on), so XXE is not the gap. Response size is:
  metadata bodies are read with `IOUtils.toString` with no bound.
- `HttpUtils.execute` used to build a `CloseableHttpClient`, with its own pooling connection
  manager, per call and never close it. The response handler buffers the entity into a
  `ByteArrayEntity`, so the connection did return to that pool — and the pool was then dropped with
  an idle socket in it, no evictor, and nothing that would ever lease from it again, so the socket
  outlived its TTL. Clients are now reused, keyed on everything that decides their construction:
  the `HttpClientFactory` identity (a sentinel when the request carries no CAS `HttpClient`),
  `redirectsEnabled`, and whether automatic retries are left on. Timeouts are static and so are not
  in the key. Anything with a proxy or DNS overrides still gets a client of its own and is closed
  when the request finishes; so is anything past the registry bound.
- Three things follow from that sharing, and all three are load-bearing. Shared clients carry
  explicit pool limits, because a per-call client had a pool to itself and the library's defaults
  of 25 total and 5 per route would otherwise become a ceiling. Shared connections revalidate on
  every lease, because a pooled connection can outlive the server it points at — a restarted
  service, a rotated load balancer, or a `MockWebServer` a test just closed. And it is only safe to
  close a client the moment `execute` returns because the handler already buffered the body; a
  change that streams the response instead would break that.
- Connection reuse is bounded by `connectionTimeToLive`, which defaults to five seconds
  (`org.apereo.cas.util.http.HttpUtils.connectionTimeToLive`). Bursts reuse connections; steady
  low-rate traffic will still reconnect. Raising it is an operator decision, not a CAS default to
  change quietly.
- Check MDQ against the SAML profile for the Metadata Query Protocol, not from memory: a compliant
  client MUST send `Accept: application/samlmetadata+xml`. CAS used to put
  `cas.authn.saml-idp.metadata.mdq.supported-content-type` on `Content-Type` of a GET and then send
  `Accept: */*`, so negotiation never happened; that property is now the `Accept` value and its
  default is the media type the profile requires. Signature verification is RECOMMENDED for servers
  there, not a client MUST, so do not report the optional filter as a spec violation — report it as
  fail-open.
- The MDQ resolver also used to drop `.httpClient(...)` from its request, so it fell back to a
  default SSL context instead of the deployment's trust store and hostname verifier while the plain
  URL resolver used them. `httpClient` is `protected` on `UrlResourceMetadataResolver` for that
  reason; any new request built in this family passes it.
- The two request-path amplifiers here are fixed, and the shape of each fix is worth keeping.
  `SamlRegisteredServiceMetadataHealthIndicator` asks each resolver about each metadata location
  once per poll (memoized by resolver and raw location, so every `jdbc://` service shares one
  probe), and the URL resolver answers from the metadata backup file when one
  exists and only probes the network for a service with no local copy — the question is whether CAS
  can serve the service, and a local copy answers it. `DynamicMetadataResolverAdapter` caches
  resolved entity descriptors *and* misses (bounded, expiring on
  `cas.saml-metadata-ui.schedule.repeat-interval`), because the entity id driving the fetch arrives
  on an unauthenticated login request; caching the misses is what bounds what a caller can provoke.
  Its rebuild replaces a resolver every request shares, so build-then-read is under a
  `CasReentrantLock`, the same remedy as `SamlIdPMetadataResolver`.
- `MetadataUIUtils` no longer has `isMetadataFoundForEntityId` or the overload of
  `locateMetadataUserInterfaceForEntityId` that takes an adapter. Both resolved the entity
  descriptor themselves, and the login action called one after the other, so every render resolved
  twice. The action resolves once and passes the descriptor down; do not reintroduce a helper that
  resolves internally.
- `FunctionUtils.doAndRetry` sleeps between attempts on the calling thread: Spring 7's `RetryPolicy.withMaxRetries(n)`
  is `FixedBackOff(1000ms, n)`, so the default `maximum-retry-attempts=3` means four attempts and three seconds of sleep.
  The SP caching resolver no longer retries at all: "this metadata does not contain the entity" is not transient, and the
  entity id driving the lookup arrives on unauthenticated requests. A failed load is remembered per cache key for
  `failure-cache-expiration` (Caffeine caches no failed load on its own), and metadata holding a single SP that lacks the
  entity is reloaded at most once per that window. `maximum-retry-attempts` now only governs HTTP fetches inside the URL
  and MDQ resolvers. Do not put a retry back around resolution; tests assert `SamlException`, not `RetryException`.
- The SP cache key is `id|metadataKey|fingerprint`. `metadataKey` is the location, or `location|entityId` for MDQ and
  for bare store sources (`jdbc://`, `mongodb://`, `redis://`, `dynamodb://`, `awss3://`, `git://`, `rest://`, `json://`),
  because those resolvers load only the requested entity. The fingerprint holds every service field that changes how
  metadata is filtered or cached (signature location, `requireSignedRoot`, criteria, max validity, certificate validation,
  expiration duration, proxy); a new such field on `SamlRegisteredService` belongs in it, or edits to it go unnoticed
  until the entry expires. Nothing listens for service events, deliberately: the fingerprint covers every node.
- The MDQ backup file (and its validators file) is named after the requested entity id, falling back to `serviceId` only
  when the criteria name none; an MDQ `serviceId` is a wildcard shared by every entity. Downloaded documents are parsed
  from memory, never by reading the backup file back.
- The service's `metadataExpirationDuration` defaults to `PT60M`, so for SP metadata the global `cache-expiration`
  (`PT24H`) only applies to services that blank it out. For IdP metadata `cache-expiration` is the real lifetime: both IdP
  caches expire after *write*, and `SamlIdPMetadataResolver` reuses a cached entry only while `locator.fetch(...)` returns
  the same document instance it was parsed from, so the published certificates follow the locator's cache. The
  file-system locator reads keys from disk on every call, bypassing that cache, so key and certificate can still disagree
  there for up to `cache-expiration` after a rotation on disk.
- Store-backed SP resolvers keep in-memory indexes instead of reading everything per load. S3 keeps
  key -> (ETag, entity id): a listing (paged; `listObjectsV2` alone stops at 1000) costs a `HEAD` only
  for new or changed objects, and only matching documents are downloaded; bucket checks use
  `headBucket`, never `listBuckets` (account-wide, needs `s3:ListAllMyBuckets`). Git keeps path ->
  (size, mtime, entity id) and pulls at most once per `git.schedule.repeat-interval`, single-flight,
  first load always pulling. The scheduled pull bean (`gitSamlRegisteredServiceRepositoryScheduler`)
  still exists and its `@Scheduled` placeholder defaults (`PT2H`, `PT60S`) differ from
  `SchedulingProperties` (`PT2M`, `PT15S`).
- Puppeteer coverage: `saml2-idp-metadata-caching`, `saml2-idp-login-idp-initiated-mdq`,
  `saml2-idp-login-sp-metadata-{directory,groovy,jdbc,json,mongodb}`,
  `saml2-idp-login-sp-override-metadata`, `saml2-idp-login-metadata-aws-s3`, `saml-mdui`,
  `delegated-login-saml2-mongodb-metadata`. All are single-threaded happy paths against a
  reachable metadata source, so nothing here covers concurrency, a metadata host that is down,
  signature-validation failure, or MDQ content negotiation. Changes in those areas need new
  scenarios rather than a rerun.

## SAML2 identity provider session state

- `saml-idp`, `saml-idp-core` and `saml-idp-web` (main and test) carry no pac4j dependency; keep it that way. Endpoints
  are `permitAll` in the Spring Security chain and the controllers do the login handshake themselves (redirect to
  `/login`, `InternalTicketValidator` on the callback), so pac4j was only ever the store for pending requests.
- Pending AuthnRequests live in `samlIdPDistributedSessionStore`; the store implementations, their contract and the
  pac4j adapter are in the `cas-session-stores` skill.
- The one place that needs both the IdP and pac4j, `SamlIdPDelegatedClientAuthenticationRequestCustomizer`, lives in
  `cas-server-support-pac4j-saml` and looks the session store up by bean name when it runs, because delegated
  authentication contexts (including the pac4j-saml tests) can run without the IdP auto-configuration.
- Test AuthnRequests come from `BaseSamlIdPConfigurationTests.getAuthnRequestFor(...)`; signed ones in
  `SamlObjectSignatureValidatorTests` are signed with OpenSAML and the `metadata/idp-signing.*` key pair, whose
  certificate is written into a temporary SP metadata file. Do not reintroduce a pac4j `SAML2Client` as a test SP.
- OpenSAML 5 has no `BasicSignatureSigningParameters`; build `org.opensaml.xmlsec.SignatureSigningParameters` directly.

## SAML2 SOAP profiles (ECP, attribute query, artifact resolution)

- SOAP endpoints answer failures with a SOAP fault and HTTP 200, so a test that only checks the status passes on a fault;
  assert that `SamlIdPConstants.REQUEST_ATTRIBUTE_ERROR` and the `FaultString` request attribute are absent and that the
  body carries the `Response`. `ECPSamlIdPProfileHandlerControllerTests.verifyOK` once passed while every ECP login failed
  with a `ClassCastException`, because `samlProfileSamlSoap11ResponseBuilder` was given the NameID builder.
- The SAML2 response inside a SOAP envelope must be built with `PARAMETER_ENCODE_RESPONSE` set to false in the
  `ScratchContext` of its message context (`SamlProfileSamlSoap11ResponseBuilder.buildSaml2Response`), or the response
  builder also writes an HTML POST form to the HTTP response ahead of the envelope.
- SAML attribute release policies find the SP metadata through `SamlIdPSAttributeReleasePolicyUtils.getEntityIdFromRequest`,
  which reads the service attributes or the `entityId`/`service` request parameters. A SOAP request carries neither, so
  the attribute query ticket's service records the relying party as `entityId`. `SamlIdPTestUtils.getSamlRegisteredService()`
  puts `entityId` on the mock request, which hides a missing entity id; reset the request attributes when testing that path.
- In `cas-server-support-saml-idp` tests, `signSamlObject` (and helpers built on it, such as `getSignedAttributeQuery`)
  signs with the identity provider's key generated for the test run, which no service provider metadata lists, so a
  signature-required SOAP request signed that way is refused at signature validation. To reach the success path, sign
  with `metadata/idp-signing.crt`/`.key`, whose certificate the `https://cassp.example.org` metadata in
  `testshib-providers.xml` lists (`SamlIdPSaml2AttributeQueryProfileHandlerControllerTests.verifyOK`).
