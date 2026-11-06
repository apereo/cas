---
name: cas-oauth-oidc
description: "Review and change CAS OAuth2/OIDC protocol behavior: token endpoint error mapping, RFC 8707 resource indicators, discovery metadata, DPoP (RFC 9449) and certificate-bound tokens (RFC 8705), client ID metadata documents, device user code approval, OpenID Federation (support/cas-server-support-oidc-federation, delegated RP federation). Use when touching support/cas-server-support-oauth* or support/cas-server-support-oidc* outside verifiable credentials; for client authentication, access-token-protected endpoints, the authenticated profile and the login handshake, use cas-oauth-oidc-authentication."
---

# OAuth and OpenID Connect

## OAuth/OIDC token endpoint error mapping

- `OAuth20AccessTokenEndpointController` turns failures into OAuth errors in two places. A validator returning `false` is `invalid_grant`, but a validator that *throws* lands in the generic catch as `invalid_request`. So a validator must catch its own lookups: `TicketRegistry.getTicket(id, type)` throws `InvalidTicketException` for a missing ticket rather than returning `null`. Exceptions from extractors and generators go through `ACCESS_TOKEN_RESPONSE_EXCEPTIONS`, which is keyed by the exact class name (subclasses do not match) and defaults to `invalid_grant`.
- Grant-neutral exceptions in `org.apereo.cas.support.oauth.validator.token` map to the RFC 8628 / CIBA Core 1.0 polling errors: `OAuth20AuthorizationPendingException` → `authorization_pending`, `OAuth20ExpiredTokenException` → `expired_token`. Use these for any new polling grant rather than the device-code ones (`UnapprovedOAuth20DeviceUserCodeException`, `ThrottledOAuth20DeviceUserCodeApprovalException`, `InvalidOAuth20DeviceTokenException` → `invalid_grant`). The CIBA validator therefore lets expired and unapproved requests through; the extractor reports them.
- `TicketRegistry.getTicket(id)` and `getTicket(id, type)` delete an expired ticket and return nothing, so expired and unknown look the same. To report `expired_token`, look the ticket up with `getTicket(id, _ -> true)`, check `isExpired()`, and delete it yourself. That works only while the registry still holds the ticket: the registry cleaner, or a store with native TTL such as Redis, removes it, and from then on it is reported as unknown. Scenarios that assert `expired_token` set `cas.ticket.registry.cleaner.schedule.enabled=false`.
- `OidcConsentApprovalViewResolver` and `prompt=consent`: the consent view's Allow link is the original authorize URL plus `bypass_approval_prompt` and the approval key, so it still carries `prompt=consent`. Under that prompt, only `isConsentApprovalSubmitted` (a valid approval key) may let the request through; the service-level or global bypass may not. Returning a plain `false` there loops the consent screen forever.

## OAuth resource indicators (RFC 8707)

- Resource indicators live on the token (`OAuth20Token#getResources`/`setResources`; `BaseOAuth20Token`, the device code
  and the CIBA request keep them). The audience of a token has one rule, `OAuth20Utils.determineAudience`, used by the JWT
  access token and by introspection (whose generators receive the client's registered service): the token's fixed
  audience (`OAuth20Token#getAudience`, set by a token exchange to its audience or resource) or else the service
  `audience` (client id when undefined) plus the resources. The exchanged token never inherits the subject token's
  resources; `oidc-token-exchange` asserts this with a subject token issued for a resource.
- Both `allowedResources` (service and token exchange policy) are regular expressions matched by
  `RegisteredServiceOAuthResourceMatcher`: every requested resource must match one in full, case-sensitively. A token
  exchange checks the policy (which also requires a resource when it defines patterns) and, when defined, the service's
  own `allowedResources`. Rejected resources are logged at DEBUG only.
- `oauth2-login-devicecode` and `oidc-login-ciba` (poll) cover resources, `invalid_target` and the introspected audience
  for the device and CIBA flows.
- A rejected resource is `invalid_target` everywhere (RFC 8707): validators and extractors throw
  `OAuth20InvalidTargetException` (use `OAuth20Utils.verifyRequestedResources`), which the token endpoint maps in both its
  validation and extraction phases; the authorization endpoint and CIBA write the error themselves.
- Resources are given to the code, access token, refresh token and device code factories when the ticket is created
  (the `create` overloads that take `resources`; the older signatures delegate with none). Do not set them afterwards.
- Consent: the approval key covers the requested resources (appended only when there are some, so earlier remembered
  decisions stay valid), and the OAuth and OIDC consent views list them (`screen.oauth.confirm.resources*`, every bundle).
  `oauth2-login-authzcode` covers consent, token endpoint narrowing, refresh and `invalid_target` end to end.
- At the token endpoint, a code or refresh token grant may narrow the access token to some of its resources
  (`OAuth20Utils.narrowRequestedResources`; the service's `allowedResources` when the grant carries none); the refresh
  token keeps all the resources of its grant (`determineRefreshTokenResources`).
- Every compactor of an OAuth ticket that carries resources (code, access token, refresh token, device code, pushed
  authorization request) must write and restore them, and the access token compactor its fixed audience; a new token field is a compact layout change (see the stateless registry section).
- Request objects may carry list-valued claims such as `resource` as a single string; the parameter resolver reads
  `String[]` and collection claims through `getJwtRequestClaimValues`, so do not call `getStringArrayClaim` directly.
- The login round-trip does not lose request parameters: after `callbackAuthorize`, the callback restores the saved original
  `/authorize` URL with every value; the callback URL's own parameters only feed the login flow.

## OpenID Connect discovery metadata

- Discovery metadata is a contract, not a description: RFC 8414 makes some entries mandatory *because* of what others advertise. `token_endpoint_auth_signing_alg_values_supported` MUST be present when `private_key_jwt` or `client_secret_jwt` is in `token_endpoint_auth_methods_supported` (and MUST NOT contain `none`). When adding an entry to one list, check whether the specification makes a second entry mandatory as a result.
- Nothing in CAS's own test suite reads this metadata as a client would, so missing entries stay invisible until a real relying party parses it. Treat a third-party client's complaint about metadata as a CAS defect until the specification says otherwise.
- Do not assert that encoding a JWT twice gives the same string. The OIDC test `keystore.jwks` holds an RSA key marked
  `"use": "enc"` and an EC key; only the EC key signs, and ECDSA (like RSA-PSS and any JWE) is randomized. Compare decoded
  values or headers, and hash the token actually issued (`IdTokenGenerationContext.encodedAccessToken`) when checking
  `at_hash`.

## Sender-constrained tokens (DPoP)

- CAS mints DPoP-bound tokens but the protected-resource half of RFC 9449 is thin. `BaseOAuth20Controller.getAccessTokenFromRequest` is the single place every protected resource resolves a token from, and it parses the authorization scheme by name; a token type CAS is willing to issue has to be listed there or the token is silently unusable, reported as `[access_token]: [null]` and then `INVALID_TICKET`.
- The two Nimbus verifiers are not interchangeable. `DPoPTokenRequestVerifier` belongs to the token endpoint and checks `htm`, `htu` and `jti`; `DPoPProtectedResourceRequestVerifier` belongs to every other endpoint and additionally binds the proof to the token through `ath` and to the confirmation recorded at issuance. `OAuth20ProofOfPossessionValidator` now has one method for each: `validate` and `validateProtectedResourceRequest`. A resource endpoint that calls the wrong one appears to work, because `htu` still matches, while checking nothing that matters.
- `ath` hashes the access token *as the client presented it*, not its decoded identifier. Pass `getAccessTokenFromRequest(...).getKey()`, never `getValue()`.
- Before reporting a gap here, read the endpoint. The userinfo endpoint was already doing the resource verification correctly in an overridden `validateAccessToken`, and the defect worth reporting was the redundant second call beside it -- the opposite shape from the one first assumed. Grepping for the validator bean found the wrong call and missed the right one.
- Identifiers that arrive on an unauthenticated request parameter are not identities. Under grants that carry their own proof of authorization -- the OpenID4VCI pre-authorized code above all -- `client_id` is whatever the wallet felt like sending. Resolve from the authenticated profile, then the token, then the parameter.
- DPoP nonces (RFC 9449 sections 8 and 9) are transient session tickets, like attestation challenges, through `BaseTransientSessionNonceService` (one marker property per kind, so a challenge is never accepted as a nonce). Nimbus treats an empty set of accepted nonces as "a `nonce` claim is prohibited", so a verifier must be handed the proof's own nonce (`OAuth20DPoPNonceService.getAcceptedNonces`) and CAS checks it afterwards (`isAccepted`); the protected resource verifier's single-`Nonce` overload with `null` prohibits the claim too. On failure the fresh nonce goes on the servlet response before `InvalidDPoPNonceException` is thrown, and each endpoint maps it: `400 use_dpop_nonce` at the token endpoint and in combined mode, `OAuth20Utils.useDPoPNonceResponse(algs)` (`401` + `WWW-Authenticate: DPoP error="use_dpop_nonce"`) at resources and Heimdall. The validator runs before the authorization code is consumed, so the client retries with the same code.
- `dpop_jkt` (RFC 9449 section 10) is an authentication attribute of the authorization request, like `state` and `nonce`: `OAuth20AuthorizeEndpointController.bindProofOfPossessionKey` adds it, the pushed authorization request keeps all authentication attributes and merges them into the authorization at `/authorize`, and the stateless code compactor retains it explicitly. A `DPoP` header at PAR goes through `OAuth20ProofOfPossessionValidator.validateKeyBinding`, which verifies without touching the profile (`validate` copies the proof's claims onto it), and its thumbprint is passed on as a request attribute. The token endpoint compares the code's `dpop_jkt` with the `DPoPConfirmation` the validator put on the profile.
- `dpop_bound_access_tokens` (RFC 9449 section 5.2) is `dpopBoundAccessTokens` on the service. With no `DPoP` header, `validate` resolves the client as it would for a proof (profile, then token, then `client_id`) and throws `InvalidDPoPProofException`, which the token endpoint answers with `400 invalid_dpop_proof` before any grant is redeemed. Grants with no authenticated client profile (device code polling, JWT bearer) call `validateTokenRequest(request, response, service)` instead, which hands the proof and its thumbprint to the extractor as request attributes; `BaseAccessTokenGrantRequestExtractor` lets them win over the profile's, which may be a stale one kept in the session. `DPoPTokenRequestVerifier` requires `htm` to be `POST`, so a device poll by `GET` cannot be bound. The device token generator rebuilds its request context, so a new context field must be copied there too. Do not wrap a call that throws `InvalidDPoP*Exception` in jOOλ `Unchecked`: it comes out as `UncheckedException`, the endpoint's catch misses it, and the client gets `invalid_request`.
- `OidcJwtAuthenticator` verifies a `client_secret_jwt` assertion with the application's unexpired client secrets (SpEL, then
  `oauthRegisteredServiceCipherExecutor`, UTF-8 bytes as the HMAC key per OIDC Core 10.1) when `tokenEndpointAuthenticationMethod`
  is `client_secret_jwt` or blank, and still adds the `jwks` keys (a raw secret there is the deprecated fallback). An HMAC key shorter
  than the algorithm's hash output is skipped in `isVerificationKeyUsable` with a warning naming the application; jose4j would reject it
  anyway, only with a generic warning. Puppeteer: `oidc-login-jwt-hmac-metadata-cors` covers `clientSecret` with `client_secret_jwt`,
  `oidc-login-jwt-rsa` covers `private_key_jwt`; keep each service's method matching how its script signs.
- A DPoP-bound access token is accepted at a protected resource only as `Authorization: DPoP <token>` (RFC 9449 section 7.2). `validateProtectedResourceRequest` checks the scheme itself, before the proof, and throws `DPoPBoundAccessTokenDowngradeException`; the profile and VC endpoints answer `401 invalid_token` through `BaseOAuth20Controller.unauthorized`, whose challenge uses the scheme the client sent. `getAccessTokenFromRequest` prefers the `access_token`/`token` parameters over the header, so the check compares the presented token with the header's. Tests and puppeteer scenarios that present a bound token at a resource must send it in that header.
- Refresh tokens of public clients (`OAuth20Utils.isPublicClient`: no secret, auth method unset or `none`) carry `DPoPConfirmation` in their authentication (`OAuth20DefaultTokenGenerator.bindRefreshTokenToProofOfPossessionKey`). The refresh grant's authentication is the refresh token's, so its access tokens inherit the confirmation; `BaseOAuth20TokenRequestValidator.verifyBoundProofOfPossessionKey(token, attribute, manager)` makes the proof match first, for codes (`dpop_jkt`) and refresh tokens alike. A refresh token built from a ticket-granting ticket alone has no authentication, so read it null-safely. A public client redeems a code only with PKCE.
- Stateless compactors keep only the authentication attributes they are told to retain. `DPoPConfirmation` is retained for access and refresh tokens (`dpop_jkt` for codes); without it a DPoP-bound token expands as a plain bearer token. A new binding attribute must be added there too.
- Certificate-bound tokens (RFC 8705): `OAuth20X509Authenticator` leaves the certificate thumbprint (`x509_digest`) on the client's profile; the extractor copies it to `AccessTokenRequestContext.x509CertificateDigest` and the token generator puts it on the access token's authentication, so every grant is bound, not only `client_credentials` (whose principal is the client profile). `OAuth20Utils.getBoundCertificateThumbprint` and `isBoundCertificatePresented` (the `jakarta.servlet.request.X509Certificate` request attribute, as pac4j uses at the token endpoint) are shared by `OAuth20Utils.verifyProofOfPossession` and Heimdall. A test or puppeteer call that presents a certificate-bound token at a resource must also present the client certificate (`requestAttr` in MockMvc, the `httpsAgent` argument of `cas.doPost`).
- `tls_client_certificate_bound_access_tokens` (RFC 8705 section 3.4) is `tlsClientCertificateBoundAccessTokens` on the service, enforced next to `dpopBoundAccessTokens` in `DefaultOAuth20ProofOfPossessionValidator` (`validate` for profile-authenticated grants, `validateTokenRequest` for device code polling and the JWT bearer grant): the request's authenticated profile must be this client's and carry `x509_digest`, or `CertificateBoundAccessTokenRequiredException` becomes `400 invalid_request` before any grant is redeemed. A new service flag also belongs in dynamic registration (request, translator, response utils), the Palantir switch list in `servicestab.html` and the MCP `CasOidcTools.OidcClient` summary.
- PAR runs the authorization request validators in `OidcHandlerInterceptorAdapter.preHandle`, before client
  authentication. Failures are answered `400` JSON (RFC 9126 section 2.3) by `writePushedAuthorizationRequestError`
  with the error the validator recorded through `setErrorDetails`; never redirect from PAR. Refusals that only the
  response builder can make go through `OAuth20AuthorizationResponseBuilder.verifyPushedAuthorizationRequest`.
- The validator owns the header checks of RFC 9449 section 4.3. `getProofOfPossessionHeader` refuses a second `DPoP` header (`HttpServletRequest.getHeader`, like pac4j's `getRequestHeader` before it, silently returns the first), and `getSignedProofOfPosessionJwt` turns a parse failure into `InvalidDPoPProofException`, so a value that is not a JWS is `invalid_dpop_proof` rather than `invalid_request`. Read the header through the former, never with `getHeader(DPOP)`; PAR has no check of its own any more.
- Protected resources map proof failures in one place, `OAuth20Utils.verifyProofOfPossession` (called by `BaseOAuth20Controller.verifyProofOfPossession` for profile/userinfo and VC, and by `OAuth20AccessTokenAuthenticationInterceptor`): `use_dpop_nonce`, `invalid_token` for a downgrade, `invalid_dpop_proof` for anything else, each a `401` with a challenge. A `DPoP` challenge carries `algs` (RFC 9449 section 7.1) from `OAuth20ProofOfPossessionValidator.getAcceptedSigningAlgorithms()`, which is the discovery `dpop_signing_alg_values_supported` in order: `unauthorized` adds it whenever the scheme is `DPoP`, and `useDPoPNonceResponse` and Heimdall's `unauthenticated` take it as an argument.

## Client ID metadata documents

- `OidcClientIdMetadataDocumentResolver` (`DefaultOidcClientIdMetadataDocumentResolver`) runs at the top of
  `OidcHandlerInterceptorAdapter.preHandle` for every OIDC endpoint with a `client_id` parameter: an `https://` client id is
  fetched, checked and saved as an `OidcRegisteredService` (property `oidcClientIdMetadataDocument`, refresh instant in
  `oidcClientIdMetadataDocumentRefresh`, expiration = last fetch + retention with delete-when-expired, evaluation order
  `Integer.MAX_VALUE`) so every other client lookup works unchanged. Off by default
  (`cas.authn.oidc.registration.client-id-metadata-document.enabled`).
- It reuses the DCR translator and then undoes what does not fit: no client secret, exact `\Q...\E` redirect URIs instead
  of the translator's `|`-joined regex, empty post-logout URIs (the translator maps them to `logoutUrl`, which CAS calls
  for back-channel logout). Metadata CAS would fetch server-side (`jwks_uri`, `sector_identifier_uri`, back-channel
  endpoints) is refused, because only the document fetch is SSRF-checked.
- `DefaultOidcClientIdMetadataDocumentFetcher` resolves the host, requires every address to be public
  (`isPublicAddress`) and pins them with `HttpExecutionRequest.resolvedAddresses`, with redirects off, one attempt,
  `maximumResponseSize` 5 KB and the CAS `httpClient` for TLS. Tests inject a permissive address policy and host resolver
  and serve the document over plain HTTP from `MockWebServer(false, ...)`: with pinned addresses the request gets a
  dedicated client, and the self-signed `MockWebServer(true, ...)` certificate fails the handshake there. Never add a
  setting that relaxes the address check. Expiration dates on services are written as a `ZonedDateTime` truncated to
  seconds; a `LocalDateTime` with fractions makes `DateTimeUtils.zonedDateTimeOf` warn on every expiration check.
- Consent for these clients is never bypassed (`OidcConsentApprovalViewResolver`); `confirm.html` shows `clientIdHost`,
  `redirectHost` and a loopback warning (`screen.oidc.confirm.cimd.*`, every bundle).

## Device user code approval

- The approval form at `/oauth2.0/device` carries `approvaltoken`, an HMAC of the ticket-granting ticket id keyed by the
  TGC signing and encryption keys (the TGT id itself when they are blank, so it holds on every node), and
  `OAuth20DeviceUserCodeApprovalEndpointController` checks it before it looks the user code up. Spring Security CSRF covers
  only the endpoints that configurers declare, and the authorization endpoint sets no CSRF cookie, so nothing else protects
  that POST: keep the check, and keep the hidden input in `deviceCodeApproval.html`. Error keys live under
  `cas.oauth.device.error.*`.
- CSRF protection for `CasWebSecurityConfigurer`s (how to declare protected paths): see the `cas-web-security` skill.

## OpenID Federation

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
- Federation key revocation (section 8.7): `OidcFederationJsonWebKeystoreService.revokeKey(kid, reason)` and the
  actuator `POST oidcFederationJwks/revoke/{kid}?reason=` (404 for an unknown kid). Revoked keys keep a jose4j other
  parameter `revoked` = {`revoked_at`, `reason`}, which nimbus `JWK.parse` maps to `KeyRevocation` on the historical keys
  endpoint (the lifecycle `state` is dropped there). Revoking the current key promotes the future key (or generates a
  current one) and adds a future key when none is left; retiring with `revoke()` never sets `revoked`.
- Receiving side (Misagh chose: trust anchor only, fail-open): `OidcFederationDefaultTrustChainResolver
  .isSignedWithRevokedTrustAnchorKey` runs in `toRelyingPartyMetadata` (resolved and sent chains). It takes the TA EC
  from the chain or the retriever (verified with the out-of-band keys), GETs its `federation_historical_keys_endpoint`
  (5 s), verifies the `jwk-set+jwt` with the out-of-band keys, and refuses chains whose TA-issued statement (or TA EC)
  `kid` is revoked by now. Failures only log a warning. Intermediates' and leaves' revocations are not checked (would need
  their entity configurations, which chains do not carry).
- Trust mark issuer: `OidcFederationDefaultTrustMarkIssuer` behind `OidcTrustMarkFederationEndpointController`
  (`/federation_trust_mark` GET, `/federation_trust_mark_status` POST, `/federation_trust_mark_list` GET; all roles,
  unauthenticated, in the federation `CasWebSecurityConfigurer` ignored endpoints). Misagh chose properties as the
  source (`issued-trust-marks[]`: type, subjects, expiration, logo/ref/delegation), stateless: marks are signed on
  request, status is derived (`invalid` = wrong `typ` or no published key by `kid` verifies, so marks signed by a
  retired key become invalid; `expired`; `revoked` = subject removed; unknown issuer or type → 404). The federation
  entity metadata advertises the three endpoints only when marks are issued. `trust_mark_owners` (TA only) publishes the
  public JWKS of each owner. The OP's trust mark validator then reaches the TA status endpoint in `oidc-federation-end2end`.
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
- The federation module runs without `cas-server-support-oidc`: trust anchors and intermediates (`oidc-federation-ta-role`,
  `-int-role`, the trust anchor of `oidc-federation-end2end`) only depend on `oidc-federation`, and refresh-scoped beans are
  created eagerly at startup whatever the role. Beans in `OidcFederationConfiguration` may only require beans of the
  federation module and CAS core: take `oidcFederationIssuerService`, never `OidcIssuerService.BEAN_NAME`, and anything
  from the OIDC module through an `ObjectProvider`. The module's unit tests always load the OIDC auto-configuration, so
  they do not catch this; the role scenarios do.
- Trust anchors (`cas.authn.oidc.federation.trust-anchors`, entity id plus out-of-band JWKS as a resource or inline JSON,
  private members dropped) feed `OidcFederationDefaultTrustChainResolver.buildTrustChainResolvers`, one nimbus
  `TrustChainResolver(entityId, jwks)` each, or `TrustChainResolver(entityId)` (keys from the anchor's own entity
  configuration) when no keys are given. Authority hints are only the immediate superiors published in the entity
  configuration; they serve as trust anchors only when none are configured (backward compatible, with a warning).
- `createAndSign` builds entity configurations (no `federationKeys`: own keys, no `constraints`) and subordinate
  statements (`federationKeys`: only the subject's keys, `constraints` when the subject has a fetch endpoint), valid from
  now to the second. Every test that counts `jwks` keys of a fetched statement expects the subordinate's keys alone.
- Automatic registration is bounded (section 18.1): `OidcFederationBoundedEntityStatementRetriever` refuses any fetched
  entity configuration with more than `max-authority-hints` authority hints, resolvers carry
  `TrustChainConstraints(max-path-length)`, and `OidcFederationAutomaticClientRegistration` keeps unresolvable entity ids
  in a Caffeine cache for `unresolved-entity-retention` (no resolution meanwhile; a registration whose refresh failed is
  returned as is). Every nimbus resolver CAS builds goes through `buildTrustChainResolvers`; `TrustChainResolver` beans
  from the context (tests) are used as they are.
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
- Trust marks (`cas.authn.oidc.federation.trust-marks`: JWT or resource, read on every request) go only into the entity
  configuration as `trust_marks` `{trust_mark_type, trust_mark}`, the type taken from the JWT; marks about another `sub`,
  untyped or expired are skipped with a warning; signatures are not checked. Do not use nimbus 11.38 `TrustMarkEntry` /
  `TrustMarkClaimsSet`: they still use the pre-Final `id` claim, not `trust_mark_type`.
- `client_registration_types_supported` is published only when `CasFeatureModule.FeatureCatalog.OpenIDConnect`
  has `federation` registered (the auto-configuration condition) and the role is `OPENID_PROVIDER`.
- Encrypted request objects (OIDC Core 6.3.1): `OidcRequestObjectDecrypter` (oidc-core-api, bean `oidcRequestObjectDecrypter`)
  decrypts with every encryption key of the OIDC keystore with a private key, any rotation state (read through
  `OidcJsonWebKeystoreGeneratorService`, like the JWKS endpoint; the keystore cache holds only current keys, but clients
  encrypt to whatever the JWKS lists; the decrypter takes it as a `Supplier` because it is an `@EventListener` for
  keystore events that the generator publishes while it is created, so a direct dependency is a circular reference
  whenever the keystore is generated at startup), by `kid` or each key in turn; algorithms limited to the discovery lists, `zip`
  refused; an encrypted unsigned payload becomes a `PlainJWT`. It runs through the
  `DefaultOAuth20RequestParameterResolver.decryptRequestObject` hook (OIDC resolver overrides it) before
  `JwtBuilder.unpack`, which verifies with the client's keys; never let `unpack` see a JWE: its service cipher decrypts
  with the private keys of the client's own JWKS. The federation interceptor decrypts through an `ObjectProvider` (no OIDC
  bean on TA/intermediate instances) and keeps replay digests on the encrypted object. Discovery defaults advertise only
  `RSA-OAEP`, `RSA-OAEP-256`, `ECDH-ES*` (no `RSA1_5`, no symmetric key wrap).
- Request objects that cannot be decrypted or verified: `OAuth20RequestParameterResolver.isRequestObjectVerified`, checked
  in `BaseOAuth20AuthorizationRequestValidator.preValidate` after the redirect URI, refuses with `invalid_request_object`
  (callback). The resolver itself still answers such a request from the query parameters (the claims are dropped), so
  anything reading parameters before the validators must not trust them; tests that pass a `request` must sign it with
  a key the client's JWKS publishes for signing.
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
- OIDC keystore key algorithms: `OidcJsonWebKeyStoreUtils.getSigningAlgorithm(JsonWebKey)` is the one curve-to-`alg`
  map (P-256/384/521, secp256k1, RSA → `RS256`, else null); reuse it (OID4VC issuer and verifier do) instead of writing
  another. Generated EC signing keys carry it, EC encryption keys carry no `alg` (like RSA ones). Key size 384 → P-384,
  512 or 521 → P-521, anything else → P-256.
  `OidcJsonWebKeystoreGeneratorService.toJsonWebKeyStore` corrects stored EC keys whose `ES*` `alg` does not match
  the curve (and drops it from `enc` keys), so every reader of the keystore (cache, JWKS endpoint, rotation, decrypter)
  sees fixed keys; rotation persists them. `isSigningAlgorithmCompatible` checks the curve for EC keys. jose4j refuses
  to sign with an `ES*` algorithm of another curve ("expects a key using P-521 but was P-256").
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
- Tests: methods of one test class run in parallel and share the Spring context. Never rotate or revoke keys in a context
  other tests use (give the test its own nested class and keystore file), and anything a `@BeforeEach` writes into
  shared beans (e.g. `getSubordinates().put(...)`) must be identical for every instance, so keys must be `static`.
  `OAuthRegisteredService.setClientSecret` appends; clear with `setClientSecrets(new ArrayList<>())` first.
- Replay records in the ticket registry must outlive acceptance: set `ExpirationPolicy.class.getName()` to a
  `HardTimeoutExpirationPolicy` of `max(transient lifetime, until no longer acceptable)` (transient lifetime from
  `factory.getExpirationPolicyBuilder().buildTicketExpirationPolicy()`). Federation request objects: at most 10 minutes
  (`exp - now`, `exp - iat`); DPoP: `2 x skew` (nimbus accepts `iat` within ±skew). Never rely on the default TST lifetime.
- Redirect URIs to service ids: always `OidcClientRegistrationUtils.toServiceId` (delegates to
  `OidcRedirectUriUtils` in oidc-services: `(?-i)^(Pattern.quote|...)$`, full-regex match; `(?-i)` because
  `RegexUtils.createPattern` is case-insensitive by default) and back with `toRedirectUris` for registration responses
  (older `^(\Q..\E)$` ids still parse); never join raw URIs (`.` matched look-alike hosts, `?` broke URIs with a query,
  `,` never matched). Used by dynamic registration, client ID metadata documents, federation. Anything that reads a
  host from a service id must go through `toRedirectUris` (`UriComponentsBuilder` finds no host after `(?-i)`); the
  pairwise sector identifier does: first redirect URI host, else the client id for built ids, else null.
- Unsigned JWTs (`alg: none`): `JwtBuilder.unpack` returns null for them (`JwtBuilder.isUnsigned`), so request objects
  (encrypted-but-unsigned too), token exchange subject/actor JWTs, risk/QR tokens and JWT access/refresh tokens are
  refused; `OAuth20JwtAccessTokenEncodableCipher.encode` refuses to issue an unsigned JWT token (IllegalStateException);
  discovery never advertises `none` for request objects. Tests sign request objects with the client's keys or
  `accessTokenJwtBuilder.getDefaultTokenCipherExecutor().encode(claims.toString())`, never `PlainJWT`.
- Request objects: `DefaultOAuth20RequestParameterResolver.unpackRequestObject` is the verification hook; the unpacked
  request object is cached as a request attribute, so tests need a fresh request for each verification they assert;
  `OidcRequestParameterResolver` verifies `HS*` with the client's secrets that `OAuth20ClientSecretValidator` (expiry,
  cipher executor) keeps, decoded (min length per alg via
  `MACSigner.getMinRequiredSecretLength`), the rest through `JwtBuilder.unpack` with the client's keys. Clients without
  keys otherwise fall back to CAS's own token keys, which never verify a client's object.
- `OidcRequestObjectDecrypter.getDecryptionKeys()` is the cached (JWKS cache period) source of CAS encryption keys in
  any rotation state; the OID4VP verifier decrypts `direct_post.jwt` with it after its current keys. `RSA1_5` is filtered
  from the permit list and from discovery. `oidcDefaultJsonWebKeystoreCache` holds CURRENT keys only.
- Use `OidcIssuerService.determineIssuer(Optional.empty())` (strips the trailing slash) for identifiers others compare
  to the entity id (OID4VP `openid_federation:` client id); the raw `cas.authn.oidc.core.issuer` may end with `/`.
- Subordinate statements: `federation_fetch/list/trust_mark_status_endpoint` are stripped from `federation_entity`
  (entity configuration only, spec 8.1/8.2/8.4); `constraints` come from the subordinate file only.
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
- Client of an authorization request: validators read `client_id` from the request object first, so the federation
  interceptor uses the query `client_id`, else the request object's (also on `/oauth2.0/authorize`, registered there at
  order 5: the OAuth interceptor at order 10 redirects to the login first, so order 50 only ran after the login),
  and `DefaultOAuth20RequestParameterResolver` refuses a request object whose `client_id` differs from the query one
  (unpack returns null, so `isRequestObjectVerified` fails). Plain (`alg=none`) request objects are otherwise accepted by
  `JwtBuilder.unpack`; only the federation interceptor demands a signature.
- Client authentication at federation endpoints (spec 8.8): `OidcFederationSubordinateClientAuthenticator` verifies
  `private_key_jwt` against the subordinate file's `federationKeys` (nimbus `DefaultJWTProcessor`, exact iss/sub, only
  aud = CAS entity id, jti + exp) and records the jti in the ticket registry with a `HardTimeoutExpirationPolicy` until
  exp + skew (the pattern of `OidcJwtAuthenticator.registerJwtIdentifier`; never the default transient lifetime).
  Trust mark endpoint: `trust-mark-endpoint-authentication` NONE/OPTIONAL/REQUIRED; POST form, requester must be the
  `sub`; metadata `federation_trust_mark_endpoint_auth_methods` + `endpoint_auth_signing_alg_values_supported`.
  Errors: `invalid_client` 401. Tests can add a subordinate through `getSubordinates()` (a `ConcurrentHashMap`;
  definitions without `entityId` are skipped). The jti check-and-record runs under a striped lock per node.
- Sent trust chains (`trust_chain` header): statements are public, so a verified chain proves nothing about the sender.
  `OidcFederationTrustChainResolver.resolveSentTrustChain` only verifies (no fallback); the registration uses it only
  when not older (`>=` on the `OPENIDFEDERATION_ENTITY_CONFIGURATION_ISSUED_AT` property) than the registration and
  when the signed objects verify with the keys it resolves to (`reevaluate` passes `stale.negate()`), else resolves.
  The reevaluation slot is consumed before verification by design; it must always end in a fresh or proven chain.
  Federation `jwks_uri` must be `https` (`extractRegisteredService`), else `file:`/`jar:` reach the keystore loader.
- Never run client-supplied values through `SpringExpressionLanguageValueResolver`: its context exposes `#env`,
  `#environmentVars` and `#systemProperties` as maps, and map indexing needs no method or property access, so the
  allow-listed context still reads them. Service `jwks` goes through `OidcJsonWebKeyStoreUtils.resolveJsonWebKeySet`
  (expressions for locations only, inline JSON literal); federation and dynamic registration store inline JSON, and the
  `kid` of a client's keys is echoed in JWE headers. Dynamic registration also rejects expressions at intake
  (`OidcJsonWebKeyStoreJacksonDeserializer`), federation metadata does not pass through it.
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
- Pairwise subjects for clients CAS registers: `cas.authn.oidc.core.pairwise-subject-salt`, read at generation time from
  the `Environment` by `PairwiseOidcRegisteredServiceUsernameAttributeProvider` for generators without a salt (never
  stored in service definitions). Federation refuses an RP that asks for `pairwise` without it; dynamic registration
  registers a salt-less provider when it has none, else `withSalt(random)`; an update keeps the provider and its
  generator (`assignSaltIfMissing` only salts a salt-less Shibboleth generator when CAS has no salt); read the salt with
  `getPairwiseSubjectSalt(Environment)` (federation too, through a `Supplier`); a salt-less generator is cloned
  (`SerializationUtils.clone`, keeps subclasses; falls back to a default generator with the same attribute when it
  cannot be serialized) and salted at runtime. Sectors must be verified: for CAS-built service ids only
  `OidcRedirectUriUtils.toSectorHost` (web URIs, `http(s)://` with a readable non-loopback host, Spring
  `UriComponentsBuilder`) and `toSectors` (that host, or "" for the client's own sector for any other or loopback URI;
  `https:/x` or hostless web URIs refused); never `new URI(uri).getHost()` (drops IDN/underscore hosts, throws on `{`)
  nor hosts of private-use schemes. Hand-defined service ids (`toRedirectUris(id) == [id]`) keep the lenient
  `UriComponentsBuilder.fromUriString(serviceId).getHost()` (`^https://host/...` gives `host`, `^https?://host/...` none,
  `^https://.*` gives `.*`); no host at all gives the client id as sector, never null. Legacy dynamic ids (`a|b` joined
  before 8.1) are hand-defined for this rule until a client configuration PATCH rebuilds them with `toServiceId`, which
  changes their sector and `sub`; hand-written `^(\Q…\E)$` ids count as CAS-built. The generator copy WARNs once per
  generator class. Sector URIs need a non-blank host equal to `toHost`. DCR fetches `sector_identifier_uri` with a `DefaultOidcClientIdMetadataDocumentFetcher` (public
  addresses, no redirects, one attempt, 64 KB, any content type, strict JSON); federation fetches it through the federation client inside the
  resolution (counted in the budget), refuses http/refused targets/4xx/bad lists, and throws
  `OidcFederationResolutionUnavailableException` for IO/5xx/budget so the RP is not negative-cached.
- The DCR translator validates the request before touching the given service, and the client configuration endpoint
  translates a `SerializationUtils.clone` of the registered service (the services manager hands out its cached
  instance); any clone or translation failure is `400 invalid_client_metadata`. GET renews an expired secret on a clone
  too, falling back to the cached instance only when it cannot be cloned.
- Address policy shared by core and federation: `DefaultOidcClientIdMetadataDocumentFetcher.isPublicAddress` (client
  documents) and `isAllowedWithPrivateNetworks(InetAddress|URL)` (federation, client `jwks` URLs: only
  link-local/any-local/multicast refused; IP literals always checked, host names skipped through a JVM proxy). IPv4-mapped
  `::ffff:0:0/96` is a 16-byte `AddressBlock` built from bytes: `InetAddress.getByName("::ffff:…")` returns an
  `Inet4Address`, so tests of mapped DNS answers need `Inet6Address.getByAddress(null, bytes, null)`. Client keys at an http(s) `jwks` URL are read through a `UrlResource` without
  redirects (5 s timeouts), checked before any request.
- Endpoint detection from requests: `OidcRequestSupport.isRequestForEndpoint` (decoded path within the application, no
  path parameters or trailing slash, as handler mappings match); never `getRequestURI()`/`getRequestURL().endsWith`,
  which `/oidc/oidcAuthoriz%65` or `;x=y` bypasses. `OAuth20HandlerInterceptorAdapter.doesUriMatchPattern` strips
  `;params` before decoding, so the OAuth/OIDC interceptors see `/oidc/oidcPushAuthorize;x=y` as PAR. MockMvc tests of encoded paths need `get(URI.create(...))`
  (the String template re-encodes `%`).
- CIBA signed authentication requests (`OidcCibaController.validateRequestObject`): asymmetric only, `iss` = client,
  `aud` contains the issuer, `exp`/`iat`/`nbf`/`jti` required, within the OIDC core skew, `iat` not in the future,
  exp − iat and exp − nbf at most 60 minutes (FAPI CIBA), `jti` kept as a transient session ticket until exp + skew;
  refused with `invalid_request`. The controller wraps a request without `client_id` with the authenticated client id
  (`withAuthenticatedClientId`), since the request parameter resolver verifies request objects with the client of the
  `client_id` parameter.
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
- Federation endpoint JSON errors go through `AbstractOidcFederationEndpointController.errorResponse`, which presets
  `application/json`: without it, a request accepting only the endpoint's JWT type gets `406` instead of the error.
- Scenarios: `oidc-federation-op-role`, `-int-role`, `-ta-role` (endpoint shape) and `oidc-federation-end2end` (CAS trust
  anchor plus CAS OP, the go-oidfed OFFA relying party in docker and the scenario's Node RP with negative cases; see the
  `cas-puppeteer-scenarios` skill); `delegated-oidc-rp-federation-endpoint` for the RP side (`404` for an unknown client).
- go-oidfed (OFFA) sends the request object by value (`request_uri` only when it has a request URI generator, which OFFA
  does not), signs it with the first of its `signing.oidc.algs` the OP lists, and addresses client assertions to the token
  endpoint. It signs ID token and userinfo algorithms into its RP metadata from the same setting, so keep OFFA on `RS256`
  against CAS's RSA keystore.
