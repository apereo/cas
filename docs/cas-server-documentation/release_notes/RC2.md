---
layout: default
title: CAS - Release Notes
category: Planning
palantir_images:
  - src: img_14.png
    alt: Palantir history version restore view
    title: Palantir history version restore view
---

{% include variables.html %}

# 8.1.0-RC2 Release Notes

We strongly recommend that you take advantage of the release candidates as they come out. Waiting for a `GA` release is only going to set
you up for unpleasant surprises. A `GA` is [a tag and nothing more](https://apereo.github.io/2017/03/08/the-myth-of-ga-rel/). Note
that CAS releases are *strictly* time-based releases; they are not scheduled or based on specific benchmarks,
statistics or completion of features. To gain confidence in a particular
release, it is strongly recommended that you start early by experimenting with release candidates and/or follow-up snapshots.

## Apereo Membership

If you benefit from Apereo CAS as free and open-source software, we invite you
to [join the Apereo Foundation](https://www.apereo.org/content/apereo-membership)
and financially support the project at a capacity that best suits your deployment. Note that all development activity is performed
*almost exclusively* on a voluntary basis with no expectations, commitments or strings attached. Having the financial means to better
sustain engineering activities will allow the developer community to allocate *dedicated and committed* time for long-term support,
maintenance and release planning, especially when it comes to addressing critical and security issues in a timely manner.

## Get Involved

- Start your CAS deployment today. Try out features and [share feedback](/cas/Mailing-Lists.html).
- Better yet, [contribute patches](/cas/developer/Contributor-Guidelines.html).
- Suggest and apply documentation improvements.

## Resources

- [Release Schedule](https://github.com/apereo/cas/milestones)
- [Release Policy](/cas/developer/Release-Policy.html)

## System Requirements

The JDK baseline requirement for this CAS release is and **MUST** be JDK `25`. All compatible distributions
such as Amazon Corretto, Zulu, Eclipse Temurin, etc should work and are implicitly supported.

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.

### OpenRewrite Recipes

CAS continues to produce and publish [OpenRewrite](https://docs.openrewrite.org/) recipes that allow the project to upgrade installations
in place from one version to the next. [See this guide](../installation/OpenRewrite-Upgrade-Recipes.html) to learn more.

### Graal VM Native Images

A CAS server installation and deployment process can be tuned to build and run
as a [Graal VM native image](../installation/GraalVM-NativeImage-Installation.html). We continue to polish native runtime hints.
The collection of end-to-end [browser tests based on Puppeteer](../../developer/Test-Process.html) have selectively switched
to build and verify Graal VM native images and we plan to extend the coverage to all such scenarios in the coming releases.

### Testing Strategy

The collection of end-to-end [browser tests based on Puppeteer](../../developer/Test-Process.html) continue to grow to cover more use cases
and scenarios. At the moment, total number of jobs stands at approximately `556` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`.

### Java 27

CAS may be built and run using Java `27` and the build process has been updated to use 
the latest Java `27` features and capabilities. Please note that this is only a preparatory step for future 
releases and the baseline requirement will remain as it was.

### Gradle 9.8

CAS is now built with Gradle `9.8` and the build process has been updated to use the
latest Gradle features and capabilities.

### Spring Boot 4.2

CAS is now built on top of Spring Boot `4.2.x`. This is an in-progress ongoing minor platform upgrade that
affects almost all aspects of the codebase including many of the third-party core libraries used by CAS
as well as some CAS functionality.

Please refer to the [Spring Boot Wiki](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.2-Release-Notes)
for more information on the changes and updates in this release. The biggest change to CAS would be support for AMQP 1.0.

REST password management, trusted-device storage, Clickatell SMS and Spring Boot Admin registration now use Spring's
`RestClient` in place of deprecated `RestTemplate` APIs.

### JSpecify & NullAway

CAS codebase is now annotated with [JSpecify](https://jspecify.dev/) annotations to indicate nullness contracts on method parameters,
return types and fields. We will gradually extend the coverage of such annotations across the entire codebase in future releases
and will integrate the Gradle build tool with tools such as [NullAway](https://github.com/uber/NullAway) to prevent nullness contract violations
during compile time.

### OpenID Connect Verifiable Credentials
                   
Several improvements are now available for [OpenID Connect with Verifiable Credentials](../authentication/OIDC-Authentication-Verifiable-Credentials.html):

- CAS may require specific principal attributes to determine eligibility before a credential offer transaction can be created for a principal.
- Pre-authorization codes are now also properly updated and removed from the ticket registry after a credential offer transaction is completed once the ticket is expired. 
- Access tokens used for credential issuance are now bound to the credential configuration id when the token is minted; this is then enforced when the token is used to request credentials.
- Transaction-code requirements are enforced during token exchange.
- A pre-authorized code is now redeemed atomically, before the access token is minted, rather than being marked used afterwards. Previously two concurrent exchanges of the same code could each receive an access token, since the code was only updated once the token already existed. OpenID4VCI requires the code to be single use.
- Credential batch issuance is limited and capped at a predefined limit.
- OpenID Connect discovery now advertises `none` among `token_endpoint_auth_methods_supported`, and `none` is understood as a client authentication method. CAS already accepted unauthenticated token requests for grants that carry their own proof of authorization, such as the OpenID4VCI pre-authorized code, but never said so; a client with no credentials had no way to discover that and would choose a method it could not satisfy.
- OpenID Connect discovery now publishes `token_endpoint_auth_signing_alg_values_supported`. [RFC 8414](https://www.rfc-editor.org/rfc/rfc8414) requires this whenever `private_key_jwt` or `client_secret_jwt` appears in `token_endpoint_auth_methods_supported`, which CAS advertises by default, and clients that enforce the requirement previously refused to use the authorization server at all. The published list is configurable and, as the specification requires, excludes `none`.
- Credential issuance now speaks the finalized OpenID4VCI 1.0 wire format rather than draft 13. The credential request takes `proofs` (plural) and accepts `credential_identifier`; the credential response is a `credentials` array and no longer carries `format`. The separate batch credential endpoint is removed, since a batch is now one request carrying several proofs, and the issuer metadata advertises `batch_credential_issuance` instead of `batch_credential_endpoint`. The proof challenge is no longer returned from the token endpoint, and the nonce endpoint returns `c_nonce` alone.
- The outcome of a [verifiable presentation](../authentication/OIDC-Authentication-Verifiable-Credentials.html#verifiable-presentations) now reaches the relying party that asked for it. Previously the wallet was told whether its presentation verified and the transaction was deleted, leaving the relying party with no way to learn the result or the disclosed claims. A new `oidcVcPresentationResult` endpoint, protected by the same client authentication as the request creation endpoint, reports `pending` until the wallet answers and then delivers the outcome together with the disclosed claims, once.
- Verifiable presentation requests are now delivered the way OpenID4VP requires. A request made under the `redirect_uri` client identifier prefix cannot be signed, and a request URI must serve a signed request object, so CAS now carries the whole authorization request by value in the wallet deep link and no longer offers a request URI in that mode. Setting `cas.authn.oidc.vc.presentation.client-identifier-prefix` to `X509_SAN_DNS` signs the request object and serves it by reference as `application/oauth-authz-req+jwt`. Previously CAS advertised a request URI that returned a plain JSON document.
- The credential endpoint now behaves as an OAuth protected resource. A missing, unknown or expired access token is answered with `401` and a `WWW-Authenticate` challenge in the scheme the wallet used, rather than `400 invalid_request`. Other failures now carry the credential error codes OpenID4VCI defines, such as `invalid_credential_request`, `unsupported_credential_type`, `credential_request_denied`, `invalid_proof` and `invalid_nonce`, instead of a single opaque `invalid_request`. The split between `invalid_proof` and `invalid_nonce` is what lets a wallet tell a stale nonce, which it can recover from by fetching a new one, from a proof it cannot fix.
- The authorization-code flow for verifiable credentials no longer ignores confidential clients or requests without PKCE. The response builder previously claimed the request only when the client had no secret *and* sent a code challenge; every other client had its `authorization_details` quietly dropped, and the failure surfaced much later as a refusal at the credential endpoint. OpenID4VCI 1.0 only recommends PKCE and places no restriction on confidential clients. A credential the client cannot be granted is now reported as `invalid_authorization_details` per [RFC 9396](https://www.rfc-editor.org/rfc/rfc9396) rather than dropped.
- Credential types are now [authorized per service](../authentication/OIDC-Authentication-Verifiable-Credentials.html#authorized-credential-types). Previously the offer endpoint and the authorization-details filter only checked that a credential configuration id existed in `cas.authn.oidc.vc.issuer.credential-configurations`, so any registered client could obtain any credential the deployment defined. An OpenID Connect service can now carry a `verifiableCredentialsPolicy` naming the credential configuration ids it may obtain, enforced when an offer transaction is created, when authorization details become an authorization code, and again at the credential endpoint when the token is spent. A service with no policy, or a policy that names no credential types, may obtain everything the issuer publishes, so existing deployments are unaffected.
- A service's verifiable credentials policy may also narrow the algorithms its credentials are signed with, through `credentialSigningAlgValuesSupported`. Like the credential types on the same policy it can only narrow: the effective set is the intersection with what the credential configuration advertises.
- Verifiable credentials are no longer signed as if they were ID tokens. A credential was passed to the ID token signing service, so a client with `encryptIdToken` received a JWE rather than an SD-JWT, `signIdToken=false` produced an unsigned credential, and the algorithm came from the client's `idTokenSigningAlg` instead of the advertised `credential-signing-alg-values-supported` which CAS's own verifier then rejected. Credentials are now always signed, never encrypted, and use the first advertised algorithm the issuer key can perform, with the advertised list enforced as the permitted set.
- Issued credentials now honor a configurable lifetime per credential configuration, `cas.authn.oidc.vc.issuer.credential-configurations[].credential-validity`, which defaults to thirty days. Previously every credential expired five minutes after issuance, which left it unusable by the time a wallet had stored it.

### OAuth and OpenID Connect 

[OAuth](../protocol/OAuth-Protocol.html) and [OpenID Connect](../authentication/OIDC-Authentication.html)
security have been strengthened across several flows.

- Client assertions now require matching subject, issuer, and client identifiers.
- JWT IDs are tracked through the shared ticket registry to prevent replay across clustered deployments.
- Device and CIBA polling are bound to the authenticated client and completed poll-mode requests are consumed.
- Dynamic client key registrations are memory-bounded.
- [Token exchanges](../authentication/OAuth-ProtocolFlow-TokenExchange.html) are now authorized against the authenticated requesting client, and exchanged tokens cannot gain scopes beyond those granted to the subject token and allowed for the requesting client.
- Client secrets are now compared and enforced using a case sensitive strategy.
- Token introspection reports tokens from other clients as inactive, while revocation only affects the requesting client's tokens and no longer reveals whether other tokens exist.
- [DPoP-bound access tokens](../authentication/OIDC-Authentication-DPoP.html) can now be spent. CAS answers the token request with `token_type: DPoP` whenever a proof accompanies it, but protected resources recognized the `Bearer` authorization scheme alone, so a client following [RFC 9449](https://www.rfc-editor.org/rfc/rfc9449) section 7.1 and presenting the token as `Authorization: DPoP ...` was told the token was missing. The userinfo, JWKS registration and credential endpoints now accept either scheme.
- DPoP proofs are now verified at protected resources through a single `validateProtectedResourceRequest` operation on the proof-of-possession validator. The credential endpoint previously accepted a DPoP-bound access token on presentation alone, which left the sender constraint doing nothing; it now answers a missing or invalid proof with `401` and `WWW-Authenticate: DPoP error="invalid_dpop_proof"`. The userinfo endpoint keeps the same checks but no longer runs the token endpoint's verifier over a resource request in addition to the correct one, and the resource-side check now includes the single-use `jti` check.
- DPoP proof validation no longer takes the client identifier from the unauthenticated `client_id` request parameter. The identifier is resolved from the authenticated profile first, then from the access token, and only then from the request, which matters for grants that carry their own proof of authorization rather than client credentials, such as the OpenID4VCI pre-authorized code. Validation also no longer overwrites the profile identifier when that identifier belongs to the subject rather than the client.
- Single-use checking for DPOP proofs is now enforced using the CAS ticket registry. Furthermore, DPOP requests are no longer treated as a form of client authentication and now sit on top of existing client authentication approaches such as client ID and client secret for confidential clients or PKCE.
- [User-Managed Access](../protocol/OAuth-UMA-Protocol.html) resources and policies are now bound to both the authenticated client and resource owner. New resource registrations also receive non-sequential, server-assigned identifiers.

### WebAuthn Multifactor Authentication

[FIDO2 WebAuthn](../mfa/FIDO2-WebAuthn-Authentication.html) multifactor authentication receives the following improvements:

- Authentication and QR session proofs are now principal-bound and single-use, and configured user verification is enforced for assertions.
- Credential and user-handle resolution now avoids repeated repository-wide scans.

### Duo Multifactor Authentication

[Duo Security](../mfa/DuoSecurity-Authentication.html) multifactor authentication receives the following improvements:

- REST passcodes now require completed primary authentication, and Universal Prompt callbacks validate browser-session state before restoring the flow.
- Duo Auth and Admin API clients now reuse outbound connection pools across requests.

### SAML2 Identity Provider 

[SAML2 identity provider](../authentication/Configuring-SAML2-Authentication.html) support receives the following improvements:

- Presented SAML2 authentication request signatures are now validated independently of the metadata signing requirement. Only a successfully validated signature may authorize an assertion consumer service URL that is not registered in service provider metadata.
- IdP-initiated SSO now also requires a caller-provided `shire` to match a POST assertion consumer service registered in metadata.
- Inbound encrypted SAML2 identifiers now use CAS's matching IdP encryption certificate and private key for decryption.
- Signature algorithm inclusion and exclusion policies are now enforced consistently across all inbound SAML bindings.
- The `Address` field of the SAML2 `SubjectConfirmationData` is now populated with the requester's IP address when possible.
- SAML2 metadata cache entries are now isolated by registered service and resolved without scanning unrelated cached resolvers.
- SAML2 authentication request session entries are updated per session and consumed after callback correlation, avoiding controller-wide serialization and retained replicated-session state.
- Storage-backed SAML2 metadata resolution now uses the requested entity ID to limit document lookups and resolver rebuilds.
- SAML2 SOAP attribute queries and artifact resolutions now require independently validated signatures, message freshness, destinations, and replay protection; artifact tickets are relying-party bound and consumed atomically.
- [SAML2 single logout](../installation/Configuring-SAML2-Logout.html) now validates request freshness and replay, fully authenticates and correlates logout responses, and generates actuator logout requests with the correct IdP issuer and service-provider destination.
- [Identity provider metadata](../installation/Configuring-SAML2-DynamicMetadata.html#per-service) is now resolved atomically, so concurrent requests can no longer observe a metadata document, or the signing and encryption credentials that belong to it, that was installed on behalf of a different registered service.
- A [service provider metadata backup file](../installation/SAML2-ServiceProvider-Metadata.html#default) is no longer deleted ahead of a download attempt, and is used as a fallback when the remote metadata source or MDQ server cannot be reached, so a momentary outage there no longer takes the service offline.
- A service that defines a metadata signature location now fails to load its metadata when that certificate cannot be read, rather than loading the metadata unverified.
- Downloaded service provider metadata is written to its backup file in a single step, so services that share one metadata location can no longer read a partially written document.
- The JDBC and MongoDB identity provider metadata stores now scope the global document lookup to the global owner, so a service's own document and keys can no longer answer in its place.
- Metadata query requests now ask for `application/samlmetadata+xml` via the `Accept` header, as the SAML profile for the Metadata Query Protocol requires, and are issued through the CAS HTTP client so they use the deployment's TLS trust store and hostname verifier.
- The SAML2 metadata health indicator answers from the local metadata backup copy where one exists, instead of reaching out to every service's metadata host on every health check.
- SAML2 metadata user interface information is resolved once per login attempt rather than twice, and dynamically-resolved entities are cached, so rendering the login page no longer issues a metadata query per request.

### CAS Protocol

- The CAS `1.0` [validation response](../ux/User-Interface-Views-CASv1.html) is line-delimited and carries no escaping mechanism.
  Principal identifiers and rendered attribute lines are now stripped of line breaks, so a value that contains a newline
  can no longer forge additional lines in the response.
- A [proxy-granting ticket](../authentication/Configuring-Proxy-Authentication.html#use-case) is now issued only after the service ticket, the validation specification and the
  authentication context have all been accepted. The `pgtUrl` callback used to be contacted and the proxy-granting
  ticket minted before any of those checks ran, so presenting a leaked service ticket could drive an outbound request
  and leave an unused proxy-granting ticket behind even though validation went on to fail.
- Validation responses now use the protocol's own error codes for two cases that previously reported something else.
  A ticket that fails the validation specification without a `renew` request 
  is reported as `INVALID_TICKET_SPEC` rather than `INVALID_TICKET`, and an
  unexpected failure during validation is reported as `INTERNAL_ERROR` rather than `INVALID_REQUEST`, which stays
  reserved for a request that is missing required parameters. A ticket that did not come from an initial login while
  `renew` was requested continues to be reported as `INVALID_TICKET`, as the protocol specifies.
- Attribute names are now sanitized into valid XML names before they are rendered as elements in the CAS `2.0`/`3.0`
  validation response, and proxy URLs in `<cas:proxy>` are XML-escaped. Previously an attribute name was only stripped
  of spaces and a proxy URL was written out verbatim, so either could carry markup or quoting characters into the
  response. Note that a name containing characters that are not legal in an XML name — a colon, for instance — now
  renders with those characters replaced by an underscore; such a name previously produced a response that was not
  well-formed XML.
- The SAML `1.1` [validation response](../protocol/SAML-v1-Protocol.html) now sets `Recipient` to the service URL the
  response is intended for, and reserves `InResponseTo` for the `RequestID` of the request being answered, omitting it
  when the caller did not send one. `InResponseTo` previously carried the hostname of the `TARGET` service unless a
  `RequestID` happened to be present, and `Recipient` was never set at all, so a relying party had no way to bind the
  response to its own request or endpoint.
- The `renew` parameter presented to the [CAS protocol](../protocol/CAS-Protocol.html) validation endpoints is now
  evaluated per request. Validation specifications are shared components and the requested value used to be assigned
  onto them for the duration of a request, which allowed a concurrent validation request to reset it. A service ticket
  that was not issued from a new login can no longer satisfy a `renew=true` validation because of another request that
  happened to be in flight at the same time.

### Views and Themes

- The [REST-based view resolver](../ux/User-Interface-Views-External.html) no longer forwards credential-bearing request
  headers, such as `Cookie`, `Authorization` and `Proxy-Authorization`, to the external template endpoint.
- Every resolved theme name is now matched against a real
  [theme definition](../ux/User-Interface-Customization-Themes-Static.html) before it is honored, no matter how the
  name was chosen. A name without a matching `[theme].properties` file, either at the root of a configured template
  prefix or at the root of the classpath, is ignored and the next resolver in the chain applies. This closes a gap
  where names taken from a request header, cookie or session value were used without any check, and it makes the
  [Groovy](../ux/User-Interface-Customization-Themes-Groovy.html) and
  [REST](../ux/User-Interface-Customization-Themes-REST.html) theme sources hold to the same contract as every other
  theme: a script or endpoint that returns a name with no theme definition behind it now falls back to the default
  theme rather than selecting a theme that does not exist.
- A service theme is now resolved once per request rather than once per template lookup, so service resolution, access
  strategy evaluation and theme file lookups no longer repeat for every fragment rendered on a page.
- [Thymeleaf template caching](../ux/User-Interface-Thymeleaf.html) now takes effect. The chaining template resolver previously declared every resolution
  non-cacheable regardless of `spring.thymeleaf.cache`, so each page and each fragment was re-resolved and re-parsed on
  every request. Resolutions are now cached per the configured setting, except for those produced by a theme-aware
  resolver: those map one template name to different files depending on the request's theme, and Thymeleaf's cache key
  does not carry the theme, so they remain non-cacheable. Deployments with no themed template overrides cache every
  template; deployments with themed overrides cache everything except the overridden templates.

### LDAP Integrations

- [Surrogate authentication](../authentication/Surrogate-Authentication-Storage-LDAP.html), [delegated authentication profile selection](../integration/Delegate-Authentication-ProfileSelection.html), [acceptable usage policy](../webflow/Webflow-Customization-AUP-LDAP.html) and [password management](../password_management/Password-Management.html) now build their LDAP connection pools once and reuse them across requests.
- LDAP connection pools are now addressed by the directory and base DN they serve, so multiple configuration blocks that point at the same server no longer collapse onto a single set of connection settings.
- Surrogate search filters that do not reference the impersonated account are now rejected, as such a filter is unable to restrict the accounts a user may impersonate.

### MongoDB Integrations

- A [configuration refresh](../configuration/Configuration-Management-Reload.html) no longer drops MongoDB collections. `drop-collection` and `drop-indexes` describe what happens at startup, but a refresh of the [MongoDB ticket registry](../ticketing/MongoDb-Ticket-Registry.html) or [service registry](../services/MongoDb-Service-Management.html) re-ran them and destroyed live tickets or registered services. Collection and index creation still runs on refresh, as it is repeatable.
- The MongoDB ticket registry no longer reports a storage failure as a missing ticket. Adding, updating or fetching a ticket now surfaces the underlying failure instead of logging it and carrying on, so a database outage, an oversized document or a ticket definition missing from the catalog can no longer look like a successful login followed by an invalid ticket.
- Result limits are now applied by MongoDB rather than after the fact, so paging through tickets and querying the ticket registry no longer fetch and deserialize whole collections only to discard them. Cursors opened across several collections are now closed even when the caller stops reading early, and collections shared by more than one ticket definition are read and counted once.
- An `IDX_PRINCIPAL` index is now created on every ticket collection. Queries by principal, such as removing all tickets issued to a user, run against all collections while only the ticket-granting ticket collection carried the index.
- Querying single sign-on sessions by an attribute whose name contains a dot now matches. Such names are escaped when the document is written, and the query did not apply the same escaping.

### JMX Management

[JMX management](../integration/JMX-Integration.html) now includes service reload and lookup, ticket and session
counts with filtered listings, expired-ticket cleanup, authentication and MFA diagnostics, and principal attribute
cache invalidation. Service listings also release backend resources correctly.

### Palantir

The OpenID Connect service wizard now includes an advanced verifiable credentials policy section for allowed credential types and signing algorithms.

The [service history endpoint](../services/Configuring-Service-Version-History.html) can now restore a selected
registered-service revision to the service registry and live service cache, while retaining its history.
[Palantir](../installation/Admin-Dashboard.html) offers a **Restore Version** action in the revision table's context menu under **View Change History**.

{% include imagegallery.html gallery_id="palantir-dashboard" images=page.palantir_images %}

### Interrupt Notifications

- Following a link on a [blocking interrupt](../webflow/Webflow-Customization-Interrupt.html) no longer records the interrupt as acknowledged, and a blocking response is never skipped by [interrupt tracking](../webflow/Webflow-Customization-Interrupt-Tracking.html). Tracking cookies are now bound to the principal; cookies issued by earlier versions are ignored, so users may see an acknowledged interrupt once more.
- Interrupt notifications no longer render during passive requests (CAS `gateway`, OpenID Connect `prompt=none`, SAML2 `IsPassive`); CAS returns to the application without a ticket instead, as required by the respective specifications.
- [JSON interrupt notifications](../webflow/Webflow-Customization-Interrupt-JSON.html) now load their policies once and reload file changes atomically, so concurrent logins can no longer skip an interrupt while the file is being re-read.
- [REST interrupt notifications](../webflow/Webflow-Customization-Interrupt-REST.html) now read the response payload only for successful status codes; error responses no longer interrupt every login with a generic message.

### Groovy Scripting

- [Groovy scripts](../integration/Apache-Groovy-Scripting.html) no longer abandon an execution when the script is already busy on another thread. Previously a script that stayed busy for more than five seconds caused queued executions to return no result at all, which could silently skip a multifactor authentication trigger or an acceptable usage policy check. Executions now wait for their turn.
- Bindings assigned to an inline `groovy { ... }` script are now scoped to the assigning thread and to a single execution, so variables belonging to one request can no longer be observed by the next execution of that script or by an unrelated script.
- An inline `groovy { ... }` script that is executed without variables, such as a scripted multifactor authentication trigger, now keeps working past its first execution. The script's variables were previously reset to an immutable map once it had run, so every later execution failed internally and produced no result.
- [OpenID Connect claim mappings](../authentication/OIDC-Authentication-Claims-Mapping.html#mapping-claims-per-service) defined as scripts no longer discard the shared Groovy script cache after every claim. Compiled scripts, and the reloading of scripts as their files change, are now retained across token requests.
- Inline scripts used by [allowed attributes](../integration/Attribute-Release-Policy-InlineGroovy.html), pattern-matching attribute transformations and service access strategy required attributes are compiled once and served from the script cache. Previously each of these compiled a fresh script for every attribute on every request.
- The [surrogate access strategy](../authentication/Surrogate-Authentication.html) and [Groovy SAML2 metadata resolution](../installation/Configuring-SAML2-DynamicMetadata-Groovy.html) no longer compile their script, and register a new file watcher, on every request.
- The multifactor authentication [principal attribute predicate](../mfa/Configuring-Multifactor-Authentication-Triggers-PrincipalAttribute-PerApplication.html) compiles its Groovy class once and recompiles it only after the file changes, instead of on every authentication attempt.
- A Groovy script that fails to compile, or that throws while running, now reports that failure to the CAS component which asked for it rather than reporting no result at all. Components designed to carry on without a script result, such as mapped attribute release and the Groovy username provider, keep their existing behavior.
- A Groovy access strategy activation criteria that produces no result now fails the request with an explanation. Previously it raised an unexplained error, and treating the missing result as an inactive criteria would have granted access without evaluating the service's required attributes.

## Other Stuff
    
- CAS [actuator endpoints](../monitoring/Monitoring-Statistics.html#actuator-endpoints) that are built on Spring MVC request mappings now honor `management.endpoint.<id>.access`. A `READ_ONLY` declaration registers only the endpoint's read mappings, so its `POST`, `PUT`, `PATCH` and `DELETE` mappings are no longer reachable, and a `NONE` declaration registers no mappings at all.
- [CloudWatch logging](../logging/Logging-Cloudwatch.html) now avoids recursive logging initialization when reporting appender startup or delivery failures.
- Authentication history, theme caching and CloudWatch shutdown now use concurrent collections and explicit coordination in place of Java monitor locking.
- Several optimizations are in place to assist with faster startup time, allowing for more components to be lazily initialized.
- A large number of dependencies and libraries have been updated to their latest versions.
