---
name: cas-oauth-oidc-authentication
description: "How the OAuth2/OIDC/UMA server authenticates requests without pac4j: servlet-based extension points, client authentication providers (client_secret_*, private_key_jwt, tls_client_auth, attestation), endpoints protected by access tokens (dynamic registration, client configuration, UMA), the OAuth20UserProfile, and the authorization endpoint's CAS login handshake (callbackAuthorize, prompt, max_age). Use when touching interceptors, authenticators, profiles or the login callback in support/cas-server-support-oauth*, oidc* or uma*."
---

# OAuth/OIDC request authentication

Protocol behavior (token errors, resource indicators, discovery, DPoP, client ID metadata documents) is in the
`cas-oauth-oidc` skill; the shared Spring Security chain is in `cas-web-security`.

## Servlet plumbing

- The OAuth/OIDC extension points take `HttpServletRequest`/`HttpServletResponse`, not pac4j's `WebContext`, `CallContext`
  or `JEEContext` (parameter resolver, request validators, grant extractors, authorization response builders, consent and
  callback view resolvers, proof-of-possession validator, DPoP nonce service, authentication builder, issuer service,
  `OAuth20Utils`, `OidcRequestSupport`, Heimdall's `AuthorizationPrincipalParser`). Do not reintroduce pac4j types there.
- The OAuth, OIDC, UMA and Heimdall modules do not depend on pac4j, nor do token authentication and the shell (JWTs there are
  Nimbus); pac4j remains only in delegated authentication. Do not add pac4j back to these modules.
- Validators' `validate` and `supports` take request and response; extractors' `extract` takes both and `supports` only
  the request. Token validators' `validateInternal(request, response, grantType, userProfile)` and the token-exchange
  extractor's helpers take both, because reading the profile needs the response.

## Client authentication (Spring Security)

- `requiresAuthenticationAccessTokenInterceptor` is an `OAuth20ClientAuthenticationInterceptor` (token, revocation, PAR,
  CIBA and VC transaction/presentation requests). It tries the `OAuth20ClientAuthenticationProvider` beans in order: each
  pairs a Spring Security `AuthenticationConverter` (returns `null` when the request carries none of its credentials) with an
  `OAuth20ClientCredentialsAuthenticator`; `DefaultOAuth20ClientAuthenticationProvider` builds one. The first profile wins.
  An `AuthenticationException` (usually `BadCredentialsException`) or an empty result moves to the next method; an
  `OAuth20ClientAuthenticationResponseException` answers the request itself (client attestation challenge, DPoP nonce).
  The CAS methods are ordered from `CasOAuth20Configuration.CLIENT_AUTHENTICATION_ORDER` in pac4j's old client order.
- Responses: the security headers on every request, and on failure `401` through `sendError` with
  `WWW-Authenticate: Basic realm="authentication required"`, whatever the method tried (RFC 9110 requires a challenge on
  every `401`). `OAuth20HandlerInterceptorAdapterTests` pins these.
- The authenticated profile goes into the request attribute `OAuth20Constants.REQUEST_ATTRIBUTE_AUTHENTICATED_PROFILE`, with
  the request's client id as `oauthClientId` (`OAuth20Utils.resolveClientIdFromRequest`); `findAuthenticatedUserProfile`
  reads that attribute before the session store.
- Authenticators implement `OAuth20ClientCredentialsAuthenticator`, never Spring's `AuthenticationProvider`, as beans: Spring
  Boot's `UserDetailsServiceAutoConfiguration` backs off and `InitializeAuthenticationProviderBeanManagerConfigurer` picks up
  any `AuthenticationProvider` bean. They build `DefaultOAuth20UserProfile`s named
  `OAuth20UserProfile.COMMON_PROFILE_HANDLER_NAME` (`X509_PROFILE_HANDLER_NAME` for certificates). The token's `source` (`FORM`, `HEADER`, `CERTIFICATE`) tells `client_secret_post` from
  `client_secret_basic`.
- A bearer access token is never client authentication (RFC 9126 section 2, CIBA section 7.1): bearer clients stay out of
  the providers; `OidcHandlerInterceptorAdapterTests.verifyBearerAccessTokenIsNotClientAuthentication` guards it.
- A client authenticates only as the client the request names: a profile whose `client_id` differs from the request's
  client (`OAuth20ClientAuthenticationInterceptor.isClientOfRequest`) is dropped and the next method is tried, because the
  validators and extractors read the client from the request. Every authenticator puts `client_id` on its profile.
- The registered `tokenEndpointAuthenticationMethod` is enforced (`OAuth20Utils.isTokenAuthenticationMethodSupportedFor`)
  wherever client authentication runs, not only at the token endpoint: the interceptor marks the request with
  `REQUEST_ATTRIBUTE_CLIENT_AUTHENTICATION_REQUEST` (`OAuth20Utils.isClientAuthenticationRequest`).
- `tls_client_auth` (RFC 8705): a client must register `tlsClientAuthSubjectDn` or a `tlsClientAuthSan*` value, unless its
  client id is the certificate's SPIFFE ID. The subject value is first compared as a DN (`X500Principal.equals`, profile id
  = subject DN), so DNs with parentheses work; otherwise a value with a capturing group is a pac4j-style regex (one match in
  the RFC 2253 DN, the profile id is the group). Without a registered subject the subject is not matched: the profile id
  is the single CN, else the SPIFFE ID, the subject DN or the first SAN (SPIRE SVIDs have no CN). SAN values are exact (RFC 8705 section 2.1.2): DNS and email ignoring case,
  IP as `InetAddress`, URI as is; never regexes. Unit tests that use the test certificates must register a subject
  (`BaseOAuth20AuthenticatorTests.serviceTlsAuth`).
- A method whose client is not the request's (`OAuth20ClientAuthenticationProvider.isRequestedClientIgnored`, set for the
  VC pre-authorized code, whose client is the offer's) skips the client-of-request check.
- In tests, build credentials with `OAuth20TestUtils.newClientAuthenticationToken` and reach the authenticator of a provider
  bean with `((DefaultOAuth20ClientAuthenticationProvider) provider).getAuthenticator()`.

## Endpoints protected by access tokens

- OIDC dynamic registration, client configuration and the UMA protection/authorization APIs run an
  `OAuth20AccessTokenAuthenticationInterceptor` (beans `requiresAuthenticationDynamicRegistrationInterceptor`,
  `requiresAuthenticationClientConfigurationInterceptor`, `umaRequestingPartyTokenSecurityInterceptor`,
  `umaAuthorizationApiTokenSecurityInterceptor`). It reads the token from the `Authorization` header only (`Bearer` or
  `DPoP`, any letter case), decodes JWT access tokens, looks the token up, then checks the certificate binding and DPoP proof
  (`OAuth20Utils.verifyProofOfPossession`), then the authenticator's required scopes, then the authenticator itself.
- Responses follow RFC 6750 section 3.1 through `OAuth20Utils.protectedResourceErrorResponse`, the same helper behind
  `BaseOAuth20Controller.unauthorized`: no token -> `401` with a bare challenge; unknown/expired token, an unbound token
  under the `DPoP` scheme (RFC 9449 section 7.1), or an authenticator that refuses it or throws -> `401 invalid_token`; missing scope -> `403 insufficient_scope` with `scope="..."`. pac4j's security
  headers come from `OAuth20Utils.addSecurityHeaders`, shared with client authentication.
- What an endpoint accepts is an `OAuth20AccessTokenAuthenticator` (oauth-api): `getRequiredScopes()` plus
  `authenticate(request, token)`, empty meaning `invalid_token`. `DefaultOAuth20AccessTokenAuthenticator` builds the old pac4j
  profile (principal + authentication attributes + `client_id`, `COMMON_PROFILE_HANDLER_NAME`); OIDC adds the ID token's
  subject and claims, client configuration requires the token's client to be the `client_id` parameter (RFC 7592), UMA adds
  the scopes as roles and the token as an attribute. The profile goes into the request attribute like client authentication's.
- The registration access token carries only `client_configuration_scope`; the initial access token (`/oidc/initToken`,
  HTTP Basic against `cas.authn.oidc.registration.initial-access-token-user`/`-password`, compared in constant time) carries
  `client_registration_scope`.

## Authenticated profile (`OAuth20UserProfile`)

- The user or client of a request is a CAS `OAuth20UserProfile` (oauth-api) everywhere:
  `AccessTokenRequestContext`, `OAuth20AccessTokenResponseResult`, `IdTokenGenerationContext`, the authentication builder,
  token validators, `OAuth20CallbackAuthorizeViewResolver`, UMA resource sets and the `OAuth20Utils`/`OidcRequestSupport`
  helpers. Read it with `OAuth20Utils.findAuthenticatedUserProfile(request, response, sessionStore)` (or
  `getAuthenticatedUserProfile`, which throws when there is none).
- The profile of a request is one live object: the instance in the request attribute, which client authentication, the
  access-token interceptor and the login interceptor set before the endpoint runs. Writes such as
  `DefaultOAuth20ProofOfPossessionValidator.adjustUserProfile` (DPoP thumbprint, client id) and the token-exchange and JWT
  bearer extractors' `setId`/`addAttribute` reach it, and later reads in the same request see them. Do not replace it with a copy:
  `BaseAccessTokenGrantRequestExtractor.extract` reads the profile again after the subclass changed it.
- A profile read from the session store is a copy when sessions are replicated (`TicketRegistryCasSessionStore` serializes
  it): writes to it are lost unless it is stored again, and tests cannot `assertSame` it. The login interceptor puts the
  session profile in the request attribute, so the authorize and device approval endpoints work on that one instance.
- `DefaultOAuth20UserProfile` (oauth-api) builds every profile. Tests put one in the request attribute, or in the session
  with `storeProfileIntoSession(request, profile)` (`AbstractOAuth20Tests`, `AbstractOidcTests`), which goes through
  `oauthDistributedSessionStore`; the OAuth tests replicate sessions, so the value lives in the ticket registry and is found
  through a request attribute on the same request or the `DISSESSIONOauthOidcServerSupport` cookie on the next one.
- `getAuthenticationHandlerName()` is the authentication handler name of the CAS authentication built from the profile
  (also audited as `authn_methods`), one of the `OAuth20UserProfile.*_HANDLER_NAME` constants: `OAuth20BasicUserProfile` for
  the user who signed in, `OAuth20CommonProfile` for clients and access tokens, `OAuth20X509Profile` for certificates,
  `OAuth20AnonymousProfile` for device code polling (they were pac4j class names before 8.1). Renaming them changes audit
  records and `authn_methods`; do it with a release note.
- Attribute semantics follow pac4j: `null` values are ignored and collection values merge into one distinct list;
  `getAttribute` is `@Nullable`, so guard `.toString()` calls.
- `AccessTokenRequestContext` is Java-serialized into pushed authorization request tickets; changing the profile type breaks
  PARs pending at upgrade (release note).
- HTTP Basic is read by `OAuth20Utils.readBasicAuthorization`, and client credentials by `readClientBasicAuthorization`,
  which also form-url-decodes the id and the secret (RFC 6749 section 2.3.1): the scheme is matched in any case (RFC 9110); an `Authorization`
  header that is not `Basic ` or does not decode to `id:secret` throws Spring's `BadCredentialsException`, which the Basic
  converter, `OAuth20HandlerInterceptorAdapter`, `OAuth20Utils.resolveClientIdFromRequest` and
  `DefaultOAuth20RequestParameterResolver.resolveClientIdAndClientSecret` callers treat as "no credentials".

## Login handshake of the authorization endpoint

- `requiresAuthenticationAuthorizeInterceptor` is an `OAuth20CasAuthenticationInterceptor` (`OidcCasAuthenticationInterceptor`
  for OIDC, which also refuses the session profile for `prompt=login` or an authentication older than `max_age`). It uses the
  profile under `SESSION_ATTRIBUTE_AUTHENTICATED_PROFILE` only when its `oauthClientId` matches the client of the request
  (case-sensitive, as client lookup is; a request without a client takes any) and a live TGT cookie comes with the request,
  or the authentication was stateless. Otherwise it saves the request under `SESSION_ATTRIBUTE_REQUESTED_URL` (a `POST` as
  the `GET` URL with its parameters) and redirects to the login with `oauthCasClientRedirectActionBuilder`; an AJAX request
  (`X-Requested-With: XMLHttpRequest`, or pac4j's `is_ajax_request=true` header or parameter) gets `401` with
  `Bearer realm="CAS"` and is not saved.
- The service is `OAuth20CasCallbackUrlResolver.compute(request)`: `callbackAuthorize` plus the request parameters plus
  `client_name=CasOAuthClient`, byte for byte what pac4j sent, so tickets, service registry patterns and puppeteer
  assertions keep matching. The callback validates the ticket for `computeServiceForTicket(request)` (the raw query minus
  `ticket`), renews the session (the container session; `TicketRegistryCasSessionStore`, used when sessions are replicated,
  keeps its id), stores the profile with `oauthClientId`, and resumes the saved URL (else a registered
  `redirect_uri`, else `/`). A ticket that does not validate is the service error view with `401`.
- `OidcCasClientRedirectActionBuilder` adds `gateway=true` for `prompt=none` and `renew=true` for `prompt=login` or an
  expired `max_age` (`max_age=0` included); `OidcCallbackAuthorizeViewResolver` turns a missing profile under `prompt=none`
  into `login_required` at the bare redirect URI when the client registered it (only a profile the callback has just
  validated counts: a gateway that returns no ticket means no SSO session, whatever the session store still holds), as it
  does a profile that does not
  satisfy `prompt=login`, `max_age=0` or `max_age` (the gateway login only reuses the SSO session, so resuming would loop) (the response mode adds `error` and
  `state`; otherwise a JSON error), and strips the `login` token of `prompt` and `max_age=0` from the resumed URL only when
  the callback validated a ticket from a new login (`CASOAuth20TicketValidator.validate` returns `fromNewLogin`, kept in
  `REQUEST_ATTRIBUTE_NEW_LOGIN`), so neither opening the callback without a ticket nor dropping `renew=true` from the login
  URL skips them. Saved URLs are parsed with `URIBuilder.setPlusAsBlank(true)`: without it a form-encoded `+` comes back as
  `%2B` when the URL is rebuilt, which merges scopes and changes the state. The consent view's approval link is built the
  same way, and for a `POST` from the request's parameters, as the login interceptor saves a `POST`.
- `BaseOidcJwtCipherExecutor` is a shared bean: `encode` loads the keystore's keys into the executor, and `decode` passes its
  own keys to the protected `decode(value, parameters, encryptionKey, signingKey)` instead; setting the public key on the
  executor while decoding let a concurrent encode sign with it.
- Prompt values of a request (`resolveRequestedPromptValues`, `resolveSupportedPromptValues(request)`) come from its query
  and its servlet parameters (form body of a `POST` authorization request, which the interceptor saves as a `GET`); MockMvc
  `.param()` sets no query string, so a URL-only reading misses them. They must come from where the callback can read and
  remove them: a `prompt` inside a request object or a pushed authorization request is not read, because the callback
  cannot drop it from the saved URL after a fresh login and the browser would go back to the login without end.
- `CASOAuth20TicketValidator` builds the profile from the validation: a principal id that delegated authentication typed
  with a pac4j profile class (`org.pac4j...GitHubProfile#12345`) loses the type, as pac4j did, so subjects stay stable;
  attribute values pass through unchanged (pac4j's converters, which kept the first `email` and so on, are gone).
- MockMvc tests of the handshake: carry cookies between requests yourself (the session cookie and the TGC), send a
  `User-Agent`, and set `ClientInfoHolder` right before `ticketGrantingTicketCookieGenerator.addCookie`: MockMvc clears the
  holder after each request, and a TGC built without it lacks the pinned address, so it never validates and the request
  quietly goes to the login. Read redirect parameters with `URI.getRawQuery()`; `getQuery()` decodes the `service` value
  and splitting it on `&` cuts it short. `OAuth20AuthorizeEndpointControllerTests.verifyLoginHandshake` is the end-to-end
  example, and the consent view renders from `src/test/resources/templates/oauthConfirmView.html` in that module.
