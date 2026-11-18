---
name: cas-oauth-oidc
description: "Review and change CAS OAuth2/OIDC protocol behavior: token endpoint error mapping, RFC 8707 resource indicators, discovery metadata, DPoP (RFC 9449) and certificate-bound tokens (RFC 8705), client ID metadata documents, device user code approval, request objects and their keys, client registration, authorization response modes (fragment encoding), OIDC logout. Use when touching support/cas-server-support-oauth* or support/cas-server-support-oidc* outside verifiable credentials and federation; for client authentication, access-token-protected endpoints, the authenticated profile and the login handshake, use cas-oauth-oidc-authentication; for OpenID Federation, cas-oidc-federation; for unsigned JWTs, cas-jwt."
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

## Request objects and keys

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
- Request objects: `DefaultOAuth20RequestParameterResolver.unpackRequestObject` is the verification hook; the unpacked
  request object is cached as a request attribute, so tests need a fresh request for each verification they assert;
  `OidcRequestParameterResolver` verifies `HS*` with the client's secrets that `OAuth20ClientSecretValidator` (expiry,
  cipher executor) keeps, decoded (min length per alg via
  `MACSigner.getMinRequiredSecretLength`), the rest through `JwtBuilder.unpack` with the client's keys. Clients without
  keys otherwise fall back to CAS's own token keys, which never verify a client's object.
- `OidcRequestObjectDecrypter.getDecryptionKeys()` is the cached (JWKS cache period) source of CAS encryption keys in
  any rotation state; the OID4VP verifier decrypts `direct_post.jwt` with it after its current keys. `RSA1_5` is filtered
  from the permit list and from discovery. `oidcDefaultJsonWebKeystoreCache` holds CURRENT keys only.
- OIDC keystore key algorithms: `OidcJsonWebKeyStoreUtils.getSigningAlgorithm(JsonWebKey)` is the one curve-to-`alg`
  map (P-256/384/521, secp256k1, RSA → `RS256`, else null); reuse it (OID4VC issuer and verifier do) instead of writing
  another. Generated EC signing keys carry it, EC encryption keys carry no `alg` (like RSA ones). Key size 384 → P-384,
  512 or 521 → P-521, anything else → P-256.
  `OidcJsonWebKeystoreGeneratorService.toJsonWebKeyStore` corrects stored EC keys whose `ES*` `alg` does not match
  the curve (and drops it from `enc` keys), so every reader of the keystore (cache, JWKS endpoint, rotation, decrypter)
  sees fixed keys; rotation persists them. `isSigningAlgorithmCompatible` checks the curve for EC keys. jose4j refuses
  to sign with an `ES*` algorithm of another curve ("expects a key using P-521 but was P-256").
- Use `OidcIssuerService.determineIssuer(Optional.empty())` (strips the trailing slash) for identifiers others compare
  to the entity id (OID4VP `openid_federation:` client id); the raw `cas.authn.oidc.core.issuer` may end with `/`.

## Client registration

- Redirect URIs to service ids: always `OidcClientRegistrationUtils.toServiceId` (delegates to
  `OidcRedirectUriUtils` in oidc-services: `(?-i)^(Pattern.quote|...)$`, full-regex match; `(?-i)` because
  `RegexUtils.createPattern` is case-insensitive by default) and back with `toRedirectUris` for registration responses
  (older `^(\Q..\E)$` ids still parse); never join raw URIs (`.` matched look-alike hosts, `?` broke URIs with a query,
  `,` never matched). Used by dynamic registration, client ID metadata documents, federation. Anything that reads a
  host from a service id must go through `toRedirectUris` (`UriComponentsBuilder` finds no host after `(?-i)`); the
  pairwise sector identifier does: first redirect URI host, else the client id for built ids, else null.
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

## Authorization response modes

- `OAuth20ResponseModeFragmentBuilder` percent-encodes every parameter name and value (`UriUtils.encode`, so a space is
  `%20`) and keeps the redirect URI's query as it was encoded. Spring's `RedirectView` expands `{...}` as URI template
  variables by default: a decoded `state` carrying JSON (1Password's "Unlock with SSO") failed the redirect with
  `Model has no value for key`, and `&`, `=`, `#` or spaces broke the fragment. A builder that assembles the whole URL
  itself encodes the values and calls `setExpandUriTemplateVariables(false)`; the query mode leaves its parameters in the
  model, which `RedirectView` encodes when it appends them.
- Tests that read only `RedirectView.getUrl()` never reach template expansion: render the view with
  `MockHttpServletRequest`/`MockHttpServletResponse` and decode the `Location` fragment (`DefaultOAuth20AuthorizationModelAndViewBuilderTests`).
  `oidc-debugger-code-login-fragment` sends a JSON `state` end to end.

## Replay records, expressions, service lookups and logout

- Replay records in the ticket registry must outlive acceptance: set `ExpirationPolicy.class.getName()` to a
  `HardTimeoutExpirationPolicy` of `max(transient lifetime, until no longer acceptable)` (transient lifetime from
  `factory.getExpirationPolicyBuilder().buildTicketExpirationPolicy()`). Federation request objects: at most 10 minutes
  (`exp - now`, `exp - iat`); DPoP: `2 x skew` (nimbus accepts `iat` within ±skew). Never rely on the default TST lifetime.
- Never run client-supplied values through `SpringExpressionLanguageValueResolver`: its context exposes `#env`,
  `#environmentVars` and `#systemProperties` as maps, and map indexing needs no method or property access, so the
  allow-listed context still reads them. Service `jwks` goes through `OidcJsonWebKeyStoreUtils.resolveJsonWebKeySet`
  (expressions for locations only, inline JSON literal); federation and dynamic registration store inline JSON, and the
  `kid` of a client's keys is echoed in JWE headers. Dynamic registration also rejects expressions at intake
  (`OidcJsonWebKeyStoreJacksonDeserializer`), federation metadata does not pass through it.
- `WebApplicationServiceFactory.createService(url)` copies the current request's parameters into the service attributes,
  and the OAuth/OIDC service locator finds a service by its `client_id` attribute alone. Looking up a URL taken from the
  request (a redirect or logout URI) that way finds the requesting client whatever the URL is. Remove `client_id` from
  the service first, or check that the located service matches the URL (`OidcLogoutEndpointController`).
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
- OIDC logout follows a `post_logout_redirect_uri` that matches any registered CAS service, not only the client's own
  URIs. That is intended: deployments are expected to tighten the service registry and remove the catch-all
  `^(https|imaps)://.*` service, so do not report it as an open redirect
  (`OidcLogoutEndpointControllerTests.verifyPostLogoutRedirectToAnotherRegisteredApplication`).
- OpenID Federation: see the `cas-oidc-federation` skill. Unsigned JWTs and token signing rules shared with other modules: see the `cas-jwt` skill.
