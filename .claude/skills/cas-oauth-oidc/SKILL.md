---
name: cas-oauth-oidc
description: "Review and change CAS OAuth2/OIDC protocol behavior: token endpoint error mapping, RFC 8707 resource indicators, discovery metadata, DPoP (RFC 9449) and certificate-bound tokens (RFC 8705), client ID metadata documents, device user code approval. Use when touching support/cas-server-support-oauth* or support/cas-server-support-oidc* outside verifiable credentials; for client authentication, access-token-protected endpoints, the authenticated profile and the login handshake, use cas-oauth-oidc-authentication."
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
