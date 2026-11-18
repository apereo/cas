---
name: cas-oidc-federation
description: "Review and change OpenID Federation 1.0 in CAS (support/cas-server-support-oidc-federation): entity configurations and roles (OP, intermediate, trust anchor), automatic and explicit client registration, trust chain resolution, constraints and metadata policies, federation keys, historical keys and trust anchors, trust marks, the fetch/list/resolve endpoints, federation HTTP limits and the oidc-federation-* scenarios (go-oidfed). Use when touching federation code, its docs or scenarios."
---

# OpenID Federation

Core OIDC behavior that federation builds on (request objects, keys, client registration, pairwise subjects, the
address policy) is in the `cas-oauth-oidc` skill; unsigned JWTs are in `cas-jwt`.

## Scope, module map and scenarios

- Review against OpenID Federation 1.0 Final (17 February 2026); automatic and explicit client registration are in its
  Section 12, not in a separate spec. The wallet profile is "OpenID Federation for Wallet Architectures" (draft, entity
  types `openid_wallet_provider`, `openid_credential_issuer`, `openid_credential_verifier`). Findings of the November
  2026 review are in the project doc `claude/oidc-federation-review.md`; read it before another round.
- Module map: `OidcWellKnownFederationEndpointController` (Entity Configuration for every role),
  `OidcFetchFederationEndpointController` and `OidcListFederationEndpointController` (trust anchor/intermediate, fed by the
  JSON files in `cas.authn.oidc.federation.subordinate-directory`), `OidcFederationDefaultEntityStatementService` (builds
  and signs every statement), `OidcFederationDefaultTrustChainResolver` (trust chain to a temporary `OidcRegisteredService`;
  refuses any client authentication but `private_key_jwt`, the default, and `tls_client_auth`),
  `OidcFederationAutomaticClientRegistration` (registers and refreshes those services; `https` entity ids only; a
  refresh saves under the existing service id, or the registry keeps two services with one client id; first contacts
  are serialized per client with striped `CasReentrantLock`s, never a lock per client id, which made-up ids would grow) and
  `OidcFederationAutomaticRegistrationInterceptor`. CAS as a federated RP is pac4j's `DefaultEntityConfigurationGenerator`
  behind `DelegatedOidcFederationEntityStatementController`.
- The federation module runs without `cas-server-support-oidc`: trust anchors and intermediates (`oidc-federation-ta-role`,
  `-int-role`, the trust anchor of `oidc-federation-end2end`) only depend on `oidc-federation`, and refresh-scoped beans are
  created eagerly at startup whatever the role. Beans in `OidcFederationConfiguration` may only require beans of the
  federation module and CAS core: take `oidcFederationIssuerService`, never `OidcIssuerService.BEAN_NAME`, and anything
  from the OIDC module through an `ObjectProvider`. The module's unit tests always load the OIDC auto-configuration, so
  they do not catch this; the role scenarios do.
- `client_registration_types_supported` is published only when `CasFeatureModule.FeatureCatalog.OpenIDConnect`
  has `federation` registered (the auto-configuration condition) and the role is `OPENID_PROVIDER`.
- Scenarios: `oidc-federation-op-role`, `-int-role`, `-ta-role` (endpoint shape) and `oidc-federation-end2end` (CAS trust
  anchor plus CAS OP, the go-oidfed OFFA relying party in docker and the scenario's Node RP with negative cases; see the
  `cas-puppeteer-scenarios` skill); `delegated-oidc-rp-federation-endpoint` for the RP side (`404` for an unknown client).
- go-oidfed (OFFA) sends the request object by value (`request_uri` only when it has a request URI generator, which OFFA
  does not), signs it with the first of its `signing.oidc.algs` the OP lists, and addresses client assertions to the token
  endpoint. It signs ID token and userinfo algorithms into its RP metadata from the same setting, so keep OFFA on `RS256`
  against CAS's RSA keystore.

## Automatic registration and request handling

- Automatic registration runs in a `WebMvcConfigurer` interceptor at order 50 on the authorize and PAR endpoints, ahead of
  the OIDC interceptor (order 100): the OIDC interceptor runs the authorization request validators and client
  authentication, and the request parameter resolver verifies a request object with the keys of the client it looks up, so a
  client that is not registered yet fails there before any federation code runs. The interceptor also enforces section
  12.1.1 and answers `400 invalid_request` itself (never a redirect): authorize requests of temporary services need a
  signed request object (jose4j `JwksVerificationKeyResolver` over the service JWKS, `aud` exactly the issuer, `jti`, `exp`,
  no `sub`); PAR requests and `request_uri` authorize requests are left to client authentication. The `jti` goes into a
  transient session ticket holding the request object digest: CAS replays the authorize URL after login and consent, so
  the identical object must be accepted again within the ticket's lifetime while a different object with that `jti` is
  refused. Request objects from relying parties live about a minute (go-oidfed: 60 s), so re-checking `exp` on the replay
  would break real logins.
- An authorization request passed by value goes through the CAS login inside the `callbackAuthorize` service URL, and
  `AbstractServicesManager.findServiceBy` refuses to match service ids longer than
  `cas.service-registry.core.maximum-service-id-length` (4096): the login then shows the unauthorized-service page and
  stays on `/cas/login?service=...callbackAuthorize...`. A request object with a `trust_chain` header (about 9 KB) hits
  that. Misagh chose not to exempt CAS's own callback services in code: `oidc-federation-end2end` turns the check off
  with the property (`0`) on its OP, and the federation docs recommend PAR or raising the limit.
- `OidcJwtAuthenticator` accepts the issuer as client assertion audience as well as the token endpoint URLs (federation
  PAR requires the OP entity id).
- Key rollover of a federated RP: the interceptor is mapped to all of `/oidc/**` and filters itself. A signed request
  object (decrypted first) on authorize/PAR, or a `client_assertion` on any endpoint, that verifies with none of the
  service's signing keys (signature only, jose4j `JsonWebSignature` per key) has `OidcFederationAutomaticClientRegistration.reevaluate`
  resolve the RP again before the request object check and client authentication. The client is `client_id`, else the
  assertion's `sub`; token requests often carry no `client_id`. Reevaluation is limited per entity by a second Caffeine
  cache on `unresolved-entity-retention`, set when it starts (not when it succeeds), so random keys cannot make CAS
  refetch; any test or scenario that sends a request signed with an unknown key therefore blocks a key rollover right
  after it (`oidc-federation-end2end` rolls over before its negative checks and runs its OP at `PT5S`). The resolver does
  not cache entity statements, so a re-resolution sees the RP's current metadata.
- The client keys are also read through `oidcServiceJsonWebKeystoreCache` (request objects via `JwtBuilder.unpack`,
  token signing/encryption), keyed by `OidcJsonWebKeyCacheKey`. It used to hash only serviceId|clientId|usage, so a
  re-resolved RP passed the interceptor and then failed with `invalid_request_object` against the cached old key; the
  service key now also hashes `jwks` and `jwksKeyId`. Unit tests of the interceptor alone do not see this; the
  end2end rollover does.
- Automatic registration is bounded (section 18.1): `OidcFederationBoundedEntityStatementRetriever` refuses any fetched
  entity configuration with more than `max-authority-hints` authority hints, resolvers carry
  `TrustChainConstraints(max-path-length)`, and `OidcFederationAutomaticClientRegistration` keeps unresolvable entity ids
  in a Caffeine cache for `unresolved-entity-retention` (no resolution meanwhile; a registration whose refresh failed is
  returned as is). Every nimbus resolver CAS builds goes through `buildTrustChainResolvers`; `TrustChainResolver` beans
  from the context (tests) are used as they are.
- Client of an authorization request: validators read `client_id` from the request object first, so the federation
  interceptor uses the query `client_id`, else the request object's (also on `/oauth2.0/authorize`, registered there at
  order 5: the OAuth interceptor at order 10 redirects to the login first, so order 50 only ran after the login),
  and `DefaultOAuth20RequestParameterResolver` refuses a request object whose `client_id` differs from the query one
  (unpack returns null, so `isRequestObjectVerified` fails). Plain (`alg=none`) request objects are otherwise accepted by
  `JwtBuilder.unpack`; only the federation interceptor demands a signature.
- Federation "cannot tell now" (Misagh: time and slots only, per step): the HTTP client counts `OutOfTimeException` and
  `TooManyRequestsException` in `RequestBudget.markIndeterminate()`; a step takes `indeterminateMark()` when it starts
  and fails as `Unavailable` (503, not remembered) only if `isIndeterminateSince(mark)` after it found nothing. A used-up
  request budget is a failure (remembered), since fabricated `authority_hints` would otherwise bypass the negative
  cache. Trust-mark loops throw `Unavailable` only when no candidate of the type is valid; unreachable status or
  historical keys endpoints (IO, 5xx) are `Unavailable`, not invalid. A sent chain whose leaf declares inline RP
  `jwks` the request is not signed with is skipped before verification; `Unavailable` from a sent chain falls back to
  resolution.
- Federation registration: `register` throws `OidcFederationResolutionUnavailableException` for a client that is not
  registered yet when the resolution is busy/unavailable or the stripe lock is not had within 5 s (`tryLock`); the
  interceptor answers `503 temporarily_unavailable`, the validator returns null. `resolveAndSave` runs the sent chain and
  the full resolution in one `resolutionLimiter.execute`. Trust-mark issuer historical keys are not cached when the
  budget or time ran out. The federation keystore is written to a temp file (0600 for a new keystore; an existing one's
  POSIX permissions/owner/group copied; symlinks followed with `readSymbolicLink`, dangling ones included) and moved over
  the keystore atomically; a refused move fails the save (never an in-place write). The federation HTTP client keeps
  fixed connect/read timeouts and enforces the deadline only by abandoning the exchange. CIBA `jti` check-and-record runs under striped `CasReentrantLock`s; request objects are refused when
  the discovery request parameter is off.
- Federation interceptor paths: `baseEndpoint/**` (order 50) plus, at order 5, `/oauth2.0/authorize`,
  `/**/oidcAuthorize` and `/**/oidcPushAuthorize` outside the base endpoint (the controllers answer there and the
  issuer check accepts them); the OAuth interceptor is at order 10, after the OAuth throttle (0).
- Federated PAR: the interceptor refuses (`401 invalid_client`) a client assertion of an automatically registered RP
  whose `aud` is not exactly the OP entity id (12.1.1.2); the generic authenticator still accepts token endpoint URLs.

## Trust chains, constraints and metadata policies

- Sent trust chains (`trust_chain` header of the request object, else the client assertion): Misagh chose "always, as
  the spec allows" (first contact, refresh, key-rollover reevaluation). `OidcFederationDefaultTrustChainResolver
  .resolveTrustChains(clientId, chain)` verifies with nimbus `TrustChain.parseSerialized` + `verifySignatures(TA keys)`
  (the TA EC needs a `metadata` claim; `exp` is checked) + `TrustChainSet.filter(resolver.getConstraints())` for path
  length, only for anchors with out-of-band keys, then the same trust marks/metadata policy as resolved chains; anything
  else falls back to resolving. Registrations carry `OPENIDFEDERATION_ENTITY_CONFIGURATION_ISSUED_AT`; a sent chain with
  an older leaf `iat` than the registration it would replace is ignored (rollback guard).
- Statement `constraints` (section 6.2): nimbus only applies the configured `TrustChainConstraints`, so
  `OidcFederationTrustChainConstraints.isSatisfiedBy(chain, leafEntityType)` (used by
  `OidcFederationDefaultTrustChainResolver.satisfiesConstraints` and by the trust mark validator for issuer chains,
  with a null type since `federation_entity` is always allowed) applies each subordinate statement's `max_path_length`
  (k - 1 intermediates below the k-th superior), `naming_constraints` (RFC 5280 host matching: `.example.org` =
  subdomains only, other names = exact host; excluded wins) and `allowed_entity_types` (must keep
  `openid_relying_party` for RPs) to resolved chains (filtered before `getShortest`), sent chains and trust mark issuer
  chains. Tests that mock `TrustChainSet` break this filtering (`stream()` of a mock is empty): use a real set holding a
  mocked `TrustChain`, whose `getSuperiorStatements()` defaults to an empty list.
- Receiving side (Misagh chose: trust anchor only, fail-open): `OidcFederationDefaultTrustChainResolver
  .isSignedWithRevokedTrustAnchorKey` runs in `toRelyingPartyMetadata` (resolved and sent chains). It takes the TA EC
  from the chain or the retriever (verified with the out-of-band keys), GETs its `federation_historical_keys_endpoint`
  (5 s), verifies the `jwk-set+jwt` with the out-of-band keys, and refuses chains whose TA-issued statement (or TA EC)
  `kid` is revoked by now. Failures only log a warning. Intermediates' and leaves' revocations are not checked (would need
  their entity configurations, which chains do not carry).
- Sent trust chains (`trust_chain` header): statements are public, so a verified chain proves nothing about the sender.
  `OidcFederationTrustChainResolver.resolveSentTrustChain` only verifies (no fallback); the registration uses it only
  when not older (`>=` on the `OPENIDFEDERATION_ENTITY_CONFIGURATION_ISSUED_AT` property) than the registration and
  when the signed objects verify with the keys it resolves to (`reevaluate` passes `stale.negate()`), else resolves.
  The reevaluation slot is consumed before verification by design; it must always end in a fresh or proven chain.
  Federation `jwks_uri` must be `https` (`extractRegisteredService`), else `file:`/`jar:` reach the keystore loader.
- Metadata policies (`chain/OidcFederationMetadataPolicies`): do not use nimbus `TrustChain.resolveCombinedMetadataPolicy`
  (11.38). Its `MetadataPolicy.parse` fails on any unknown operator instead of ignoring non-critical ones, `scope`
  operators fail on the space-separated string, the superior's `metadata` is never applied, and
  `EntityStatementClaimsSet` still reads the draft name `policy_language_crit` (read `metadata_policy_crit` from the
  raw claims). `resolveMetadata(chain, type)` overlays the immediate superior's `metadata`, sanitizes each policy (drop
  unknown ops, `PolicyViolationException` when critical, split a `scope` string operand), combines from the TA down and
  applies with `scope` as a list. Subordinate files carry optional `metadata`, `metadataPolicy`, `metadataPolicyCrit`
  (only `federationKeys` is required); the fetch endpoint validates the policy (500 when unreadable) and `/list`
  matches `entity_type` on the definition's `entityTypes`, else its metadata keys (never policy keys).
- Resolution limits (M4, `chain/OidcFederationResolutionLimiter`): `max-requests-per-resolution` (50) and
  `max-concurrent-resolutions` (10). The budget is a `ScopedValue` bound for the resolution (nested calls share it and
  the slot); `OidcFederationBudgetedEntityStatementRetriever` counts remote fetches and memoizes statements per
  resolution (wrap the remote retriever, under the local one for the resolve endpoint), and issuer historical keys and
  trust mark status requests call `consumeRequest` (TA historical keys do not). Entry points: `resolveTrustChains`,
  `resolveSentTrustChain`, the entity resolver. `OidcFederationResolutionUnavailableException` (busy, unreachable TA,
  unreadable TA historical keys) must not mark the entity unresolved (registration) and is `503 temporarily_unavailable`
  at the resolve endpoint. Trust mark status endpoints must be https. The budget also carries a deadline
  (`resolution-timeout`, PT10S) and the entities that could not be reached for reasons other than the deadline; slots
  wait 1 s.
- Federation HTTP: every outbound federation request goes through `chain/OidcFederationHttpClient` (bean
  `oidcFederationHttpClient`, TLS from `CasSSLContext`, no redirects, per-resolution deadline, 256 KB response cap; the
  exchange runs on its own virtual thread and the caller waits with `Future.get(remaining)`, so DNS, TLS and headers are
  inside the deadline; https only, link-local/any-local/multicast refused, embedded IPv4 included, loopback/private
  allowed, no check when a JVM HTTP proxy is selected (SOCKS resolves locally, so it is checked unless the JVM cannot
  resolve the name; `isAllowedWithPrivateNetworks(URL, ProxySelector)` is the testable form); at most 64 exchanges in flight, abandoned ones included, released by
  whichever of start/abandon claims the exchange first; `RefusedRequestException` for a refused target,
  `OutOfTimeException` for a request that started with less than the read timeout left, which the retriever does not
  mark unreachable, while a full-time timeout, a connect timeout (fixed 2 s, always given in full) and a 5xx answer
  do; `markUnreachable` matters only for trust anchors, whose 5xx thus gives 503, and issuer historical keys 5xx is an
  `IOException`, unavailable and not cached) and
  `OidcFederationHttpEntityStatementRetriever`; never nimbus `DefaultEntityStatementRetriever` (it records every URL in
  an unbounded list and uses JVM-wide TLS defaults) or `HTTPRequest.send()`. Tests reach `MockWebServer(true, ...)`
  with a client built on `CasSSLContext.disabled()` passed to the resolver/validator, never
  `HTTPRequest.setDefaultSSLSocketFactory`.

## Federation keys and trust anchors

- Federation key revocation (section 8.7): `OidcFederationJsonWebKeystoreService.revokeKey(kid, reason)` and the
  actuator `POST oidcFederationJwks/revoke/{kid}?reason=` (404 for an unknown kid). Revoked keys keep a jose4j other
  parameter `revoked` = {`revoked_at`, `reason`}, which nimbus `JWK.parse` maps to `KeyRevocation` on the historical keys
  endpoint (the lifecycle `state` is dropped there). Revoking the current key promotes the future key (or generates a
  current one) and adds a future key when none is left; retiring with `revoke()` never sets `revoked`.
- Trust anchors (`cas.authn.oidc.federation.trust-anchors`, entity id plus out-of-band JWKS as a resource or inline JSON,
  private members dropped) feed `OidcFederationDefaultTrustChainResolver.buildTrustChainResolvers`, one nimbus
  `TrustChainResolver(entityId, jwks)` each, or `TrustChainResolver(entityId)` (keys from the anchor's own entity
  configuration) when no keys are given. Authority hints are only the immediate superiors published in the entity
  configuration; they serve as trust anchors only when none are configured (backward compatible, with a warning).
- `createAndSign` builds entity configurations (no `federationKeys`: own keys, no `constraints`) and subordinate
  statements (`federationKeys`: only the subject's keys, `constraints` when the subject has a fetch endpoint), valid from
  now to the second. Every test that counts `jwks` keys of a fetched statement expects the subordinate's keys alone.
- Federation keys (`OidcFederationDefaultJsonWebKeystoreService`): one JWKS file with the OIDC lifecycle `state`
  (current/future/previous, missing = current) plus retired keys, public only, marked by an `exp` member (and `iat`
  from generation). EC publishes non-retired keys with `alg` derived from key type/curve (never trust the stored
  `alg`: files written before the OIDC keystore fix below hold P-256 keys tagged `ES512`); statements are signed with the
  current key through `EntityStatement.sign(claims, jwk, alg)`. `rotate()` without a future key only adds one;
  `revoke()` retires previous keys. Historical keys at `/oidc/federation_historical_keys` (all roles, ignored by
  security, `application/jwk-set+jwt`, `typ` `jwk-set+jwt`). Schedules `cas.authn.oidc.federation.rotation|revocation`
  and actuator `oidcFederationJwks` (GET, POST rotate/revoke). Startup writes the keystore only when it is missing,
  empty or holds no keys; one that exists but cannot be read or parsed fails the bean (never made-up per-node keys).
  Test existence with `resource.exists()` and the file length, never `ResourceUtils.doesResourceExist`, which answers
  false for an unreadable file or an I/O error and so would let CAS replace it. On POSIX
  the replacement keeps mode/owner/group, set separately, and drops group bits when the group cannot be kept. Schedules
  call `rotateWhenDue`/`revokeWhenDue` with `getScheduledKeyAge` (half the repeat interval, or of the gap between two
  cron fires): rotate only when every future key's `iat` is that old, retire only keys whose `previous_since` (set by
  `rotate()`; stamped on first sight for older files) is that old, so `start-delay` runs after restarts and other nodes
  are no-ops. The actuator stays forced. The service re-reads the file when its `lastModified` changes (shared keystore
  across nodes); custom key members (`iat`, `previous_since`) are dropped by nimbus `JWK.parse`, so never published.
- Historical keys (`chain/OidcFederationHistoricalKeys`): fetch (https only, every key must carry `exp`, else the whole
  set is refused) + verify `jwk-set+jwt`, `mayHaveSigned` (also used by the TA check, so a key retired before the
  statement's `iat` refuses it; tests serve historical keys from `MockWebServer(true, ...)` with nimbus
  `HTTPRequest.setDefaultSSLSocketFactory`/`HostnameVerifier` trust-all in `@BeforeAll`, from `CasSSLContext.disabled()`,
  which lives in `core-authentication-api`: a test-only dependency of the federation module, since `implementation`
  dependencies of other modules are not on its test classpath). The trust mark validator keeps
  them a minute per issuer/metadata/keys (failures as empty), so vary the endpoint path per test phase.
  (never revoked, statement `iat` <= key `exp`). Retired, unrevoked keys keep statements and trust marks valid (spec
  8.7): the issuer's status endpoint looks the `kid` up in `getHistoricalKeys()`, the validator falls back to the
  issuer's historical keys endpoint (`resolveVerificationKeys`). Revoked-key checks match by `kid`, so every chain
  statement must carry one (`OidcFederationTrustChainConstraints.isSatisfiedBy`): nimbus `verifySignatures` tries every
  key when the header has no `kid`, which let a statement signed with a revoked but still trusted key pass.
  `isSignedWithRevokedTrustAnchorKey` (now static in `OidcFederationHistoricalKeys`) always fetches the TA entity
  configuration through the resolver's retriever: a sent chain's TA configuration may be forged with the revoked key and
  advertise no (or someone else's) historical keys endpoint. The trust mark validator's `resolveTrustAnchorConfiguration`
  also always fetches (never the chain's TA configuration), so neither check depends on the other running first. It fails closed: TA configuration or advertised historical
  keys unobtainable/unverifiable makes the resolution unavailable (`OidcFederationResolutionUnavailableException`, the
  parent of the busy exception: chain not used, entity not remembered as unresolved, `503` at resolve); no endpoint
  advertised means none revoked. The check lives in `chain/OidcFederationTrustAnchorKeys` (bean
  `oidcFederationTrustAnchorKeys`), which keeps historical keys a minute per TA and keys and fetches them outside the
  request budget; tests that change the served keys need a new resolver instance. Resolver tests stub the TA
  configuration (`trustAnchorPublishesNoHistoricalKeys`). `OidcRequestObjectDecrypter` drops its decryption keys on the
  keystore generated/modified/rotated events (`@EventListener`) and caches a keystore without encryption keys for at
  most a minute.
- Tests: methods of one test class run in parallel and share the Spring context. Never rotate or revoke keys in a context
  other tests use (give the test its own nested class and keystore file), and anything a `@BeforeEach` writes into
  shared beans (e.g. `getSubordinates().put(...)`) must be identical for every instance, so keys must be `static`.
  `OAuthRegisteredService.setClientSecret` appends; clear with `setClientSecrets(new ArrayList<>())` first.

## Trust marks

- Trust mark issuer: `OidcFederationDefaultTrustMarkIssuer` behind `OidcTrustMarkFederationEndpointController`
  (`/federation_trust_mark` GET, `/federation_trust_mark_status` POST, `/federation_trust_mark_list` GET; all roles,
  unauthenticated, in the federation `CasWebSecurityConfigurer` ignored endpoints). Misagh chose properties as the
  source (`issued-trust-marks[]`: type, subjects, expiration, logo/ref/delegation), stateless: marks are signed on
  request, status is derived (`invalid` = wrong `typ` or no published key by `kid` verifies, so marks signed by a
  retired key become invalid; `expired`; `revoked` = subject removed; unknown issuer or type → 404). The federation
  entity metadata advertises the three endpoints only when marks are issued. `trust_mark_owners` (TA only) publishes the
  public JWKS of each owner. The OP's trust mark validator then reaches the TA status endpoint in `oidc-federation-end2end`.
- Trust marks (`cas.authn.oidc.federation.trust-marks`: JWT or resource, read on every request) go only into the entity
  configuration as `trust_marks` `{trust_mark_type, trust_mark}`, the type taken from the JWT; marks about another `sub`,
  untyped or expired are skipped with a warning; signatures are not checked. Do not use nimbus 11.38 `TrustMarkEntry` /
  `TrustMarkClaimsSet`: they still use the pre-Final `id` claim, not `trust_mark_type`.
- Required trust marks (`OidcFederationDefaultTrustMarkValidator`, called from
  `OidcFederationDefaultTrustChainResolver.fetchRelyingPartyMetadata`, so registration and refresh): leaf `trust_marks`
  entries of each required type; TA config from `TrustChain.getTrustAnchorConfiguration()` or the resolver's retriever,
  verified with the resolver's trust anchor keys; issuer accepted by `trust_mark_issuers`; issuer keys = TA config or the
  leaf of the issuer's own chain to the same TA (`TrustChainResolver.resolveTrustChains`); verification with nimbus
  `DefaultJWTProcessor` (typ verifier, `JWSAlgorithm.Family.SIGNATURE`, exact-match claims, 60s skew; `iat` in the
  future checked by hand); `delegation` against `trust_mark_owners`; status endpoint POST `trust_mark=`, response typed
  `trust-mark-status-response+jwt` must echo the mark with `status` `active`. Read federation claims as raw `Map`s, not
  `getJSONObjectClaim` (fails on statements built in code). `trust_mark_issuers` is published only by the TA role.
  Every required type is checked: one definitely missing fails the RP, and only otherwise does a type whose marks
  cannot be told now (status/historical keys 5xx or IO, out of time/slots, the TA found unreachable during the step,
  e.g. while an issuer's chain is resolved) make it unavailable. Every "cannot tell" check is per step: take
  `indeterminateMark()` and `unreachableMark(trustAnchor)` when the step starts and compare with
  `isIndeterminateSince`/`isUnreachableSince`; the budget only counts (`getUnreachableCount`), never a
  resolution-wide flag. Misagh keeps issuer 5xx as "cannot tell" though a member's issuer can keep its RPs from being
  negative-cached (bounded by the budget and slots).
  `TrustChain` is final but constructible (leaf + superior statements), and `TrustChainResolver` can be subclassed,
  for smoke tests without Mockito.

## Federation endpoints

- The list endpoint filters by `entity_type` (repeatable, any of) and `intermediate=true` (subordinate metadata has a
  `federation_fetch_endpoint`), from the subordinate files; trust-mark filters are `unsupported_parameter`. go-oidfed's
  OP discovery does not send `entity_type`: it lists everything and fetches each entity configuration.
- The fetch endpoint answers in JSON (section 8.9): unknown `sub` `404 not_found`, missing `sub` or its own entity id
  `400 invalid_request`, a role without subordinates `404 not_found`, a subordinate file without metadata or keys
  `500 server_error`.
- Other entity types join the OP entity configuration through `OidcFederationEntityMetadataContributor` beans
  (oidc-core-api; the federation module must not depend on oidc-vc). The VC module contributes `openid_credential_issuer`
  (only when `credential_issuer` equals the entity id, Wallet Architectures 6.2.1) and `oauth_authorization_server`
  (its `authorization_servers` names CAS), plus `openid_credential_verifier` when the OID4VP client id prefix is
  `OPENID_FEDERATION` (see `cas-oid4vc`). Contributions never replace `openid_provider` or `federation_entity`.
- Subordinate statements: `federation_fetch/list/trust_mark_status_endpoint` are stripped from `federation_entity`
  (entity configuration only, spec 8.1/8.2/8.4); `constraints` come from the subordinate file only.
- Resolve endpoint (spec 8.3, `resolve/OidcFederationDefaultEntityResolver`, `/oidc/resolve`, TA + intermediate, GET,
  and POST when authenticated): TA resolves to itself, intermediate to `trust-anchors` (else authority hints). Its own EC and subordinate
  statements come from `chain/OidcFederationLocalEntityStatementRetriever` (wraps the `oidcFederationEntityStatementRetriever`
  bean, swap it in tests) via the well-known/fetch controllers' `buildEntityConfiguration()`/`buildSubordinateStatement()`,
  no self-HTTP. nimbus `TrustChainResolver` chains carry no TA configuration and `TrustChain` needs at least one superior
  (a TA cannot resolve itself): fetch + verify the TA EC once and rebuild with the 3-arg constructor so `trust_chain` ends
  with it. Resolutions/failures cached per (sub, trusted TAs) for min(retention, exp), resolved outside the Caffeine compute
  (`getIfPresent`/`put`, never `get(key, loader)` around network calls); unavailable resolutions are not cached and
  answer `503`; signed per request. Trust marks via
  `OidcFederationTrustMarkValidator.resolveValidTrustMarks`, which returns `ValidTrustMarks(trustMarks, complete)`: a
  mark that cannot be told now is left out, `complete` false, and the resolution is kept for zero time. Authenticated POST (8.8, `resolve-endpoint-authentication`,
  subordinates only through `OidcFederationSubordinateClientAuthenticator`) adds `aud` = requester at signing; the
  cache holds the resolution, never a signed response. `resolveMetadata` refuses entity types the leaf EC lacks
  (superior `metadata`/`value`/`default` must not create one), so resolve types come from the leaf only.
- Client authentication at federation endpoints (spec 8.8): `OidcFederationSubordinateClientAuthenticator` verifies
  `private_key_jwt` against the subordinate file's `federationKeys` (nimbus `DefaultJWTProcessor`, exact iss/sub, only
  aud = CAS entity id, jti + exp) and records the jti in the ticket registry with a `HardTimeoutExpirationPolicy` until
  exp + skew (the pattern of `OidcJwtAuthenticator.registerJwtIdentifier`; never the default transient lifetime).
  Trust mark endpoint: `trust-mark-endpoint-authentication` NONE/OPTIONAL/REQUIRED; POST form, requester must be the
  `sub`; metadata `federation_trust_mark_endpoint_auth_methods` + `endpoint_auth_signing_alg_values_supported`.
  Errors: `invalid_client` 401. Tests can add a subordinate through `getSubordinates()` (a `ConcurrentHashMap`;
  definitions without `entityId` are skipped). The jti check-and-record runs under a striped lock per node.
- Federation endpoint JSON errors go through `AbstractOidcFederationEndpointController.errorResponse`, which presets
  `application/json`: without it, a request accepting only the endpoint's JWT type gets `406` instead of the error.
