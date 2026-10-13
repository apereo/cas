---
layout: default
title: CAS - 8.1.0-RC3 Release Notes
category: Planning
release:
  line: 8.1.0 development
  summary: >-
    The third release candidate reworks Heimdall around the AuthZEN 1.0 specification, brings WebAuthn Level 3 and
    passkeys to passwordless and multifactor login, and carries the stateless ticket-granting ticket in its cookie.
  facts:
    - label: Requires
      value: JDK 25
      url: ../planning/Installation-Requirements.html
    - label: Built on
      value: Spring Boot 4.2
    - value: "572"
      suffix: browser test scenarios
      url: ../../developer/Test-Process.html
    - value: "94%"
      suffix: test coverage
  upgrade:
    - type: action
      title: Certificate-bound access tokens
      text: issued before the upgrade are rejected; thumbprints now follow RFC 8705.
      section: Certificate-Bound Access Tokens
      area: oidc
    - type: action
      title: Heimdall AuthZEN resources
      text: must define `resourceType`, `actions` and optionally `resourceIdPattern`, or AuthZEN requests are denied.
      section: Heimdall AuthZEN
      area: authorization
    - type: changed
      title: Heimdall policies
      text: deny access when a resource has none, and one granting policy is enough unless `enforceAllPolicies` is set.
      section: Heimdall AuthZEN
      area: authorization
    - type: removed
      title: "`cas.ticket.registry.stateless.storage-type`"
      text: no longer applies; the ticket-granting ticket is carried by the ticket-granting cookie.
      section: Stateless Ticket Registry
      area: tickets
    - type: changed
      title: Stateless ticket-granting tickets
      text: no longer carry principal attributes; attribute repositories are consulted each time instead.
      section: Stateless Ticket Registry
      area: tickets
    - type: changed
      title: Authentication throttling
      text: counts a failed attempt only on a `401` response, once per request.
      section: Authentication Throttling
      area: authentication
    - type: changed
      title: WebAuthn
      text: requires browsers that support the WebAuthn Level 3 JSON serialization.
      section: WebAuthn Level 3
      area: mfa
    - type: changed
      title: Passwordless tokens
      text: arrive as `PasswordlessTokenCredential`, the only credential the handler accepts; the recorded credential type changes.
      section: Passwordless Authentication
      area: passwordless
    - type: action
      title: Google Authenticator on DynamoDB
      text: needs a new `useridIndex` global secondary index; create it yourself if tables are not created at startup.
      section: Multifactor & Passkeys
      area: mfa
  highlights:
    - section: Heimdall AuthZEN
      summary: Follows the AuthZEN Authorization API 1.0, with resource instances, the access evaluations API, decision point metadata and decision context.
    - section: WebAuthn Level 3
      summary: Related origins, the Signal API, passkey provider names and icons, and a passkey upgrade after password login.
    - section: Passwordless Authentication
      summary: Single-use tokens under load, one-time-code autofill, origin-bound SMS codes and passkeys from the autofill menu.
    - section: Stateless Ticket Registry
      summary: The ticket-granting ticket now rides in the ticket-granting cookie, with a versioned format and type-bound tickets.
    - section: OpenID Connect Verifiable Credentials
      summary: Proofs follow each credential configuration, and holder keys may be `x5c`, `did:jwk` or Ed25519.
    - section: Documentation
      summary: A refreshed theme, searchable settings, grouped actuator endpoints and feature toggles, and a Quick Start recipe.
---

{% include variables.html %}

{% include release-digest.html %}

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.

### OpenRewrite Recipes
{: data-area="operations"}

CAS continues to produce and publish [OpenRewrite](https://docs.openrewrite.org/) recipes that allow the project to upgrade installations
in place from one version to the next. [See this guide](../installation/OpenRewrite-Upgrade-Recipes.html) to learn more.

### Graal VM Native Images
{: data-area="operations"}

A CAS server installation and deployment process can be tuned to build and run
as a [Graal VM native image](../installation/GraalVM-NativeImage-Installation.html). We continue to polish native runtime hints.
The collection of end-to-end [browser tests based on Puppeteer](../../developer/Test-Process.html) have selectively switched
to build and verify Graal VM native images and we plan to extend the coverage to all such scenarios in the coming releases.

### Testing Strategy
{: data-area="project"}

The collection of end-to-end [browser tests based on Puppeteer](../../developer/Test-Process.html) continue to grow to cover more use cases
and scenarios. At the moment, total number of jobs stands at approximately `572` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`.

### JSpecify & NullAway
{: data-area="project"}

CAS codebase is now annotated with [JSpecify](https://jspecify.dev/) annotations to indicate nullness contracts on method parameters,
return types and fields. We will gradually extend the coverage of such annotations across the entire codebase in future releases
and will integrate the Gradle build tool with tools such as [NullAway](https://github.com/uber/NullAway) to prevent nullness contract violations
during compile time.

### Spring Boot 4.2
{: .changed data-area="project"}

CAS is now built on top of Spring Boot `4.2.x`. This is an in-progress ongoing minor platform upgrade that
affects almost all aspects of the codebase including many of the third-party core libraries used by CAS
as well as some CAS functionality.

Please refer to the [Spring Boot Wiki](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.2-Release-Notes)
for more information on the changes and updates in this release. 

### Documentation
{: .new data-area="docs"}

The CAS documentation site has received a visual and functional overhaul. Notable changes include:

- A refreshed theme with light and dark modes, a bold grotesque typeface for headings, a monospaced sidebar, and
  quiet icons for recurring sections such as configuration, actuator endpoints and troubleshooting.
- Configuration settings are presented as a searchable, filterable reference list. Each setting can be expanded
  to show its description, type, default value and deprecation status, and copied as `.properties`, YAML or
  environment variables. The chosen format is remembered across pages.
- [Actuator endpoints](../monitoring/Monitoring-Statistics.html) are grouped by endpoint, with each operation listing its parameters, response and a working `curl`
  example inline. Instructions to enable, expose and secure the endpoint along with related settings and
  troubleshooting notes are shown once per endpoint rather than once per operation, which makes pages considerably lighter.
- The [configuration properties](../configuration/Configuration-Properties.html) search is rewritten. It matches setting names regardless
  of how they are written (property names, environment variables including indexed ones such as `CAS_AUTHN_LDAP_0_LDAP_URL`{: .cas-setting}, or pasted assignments), supports exact name matches,
  searches names or descriptions, filters CAS or third-party and deprecated settings, and keeps the search in the page address so results can be shared.
- Pressing <kbd>Shift</kbd> twice on any documentation page opens a quick search for configuration settings.
- [Feature toggles](../configuration/Configuration-Feature-Toggles.html) are grouped by area and can be searched or filtered
  to those that are off by default. Each feature can be expanded to show its modules, their default state, the auto-configuration
  classes they control and a link to the relevant documentation, and every toggle can be copied as `.properties`, YAML or environment variables.
- The documentation build and validation time is significantly reduced and changes are now published up to `75%` faster. External links are checked on a weekly schedule
  rather than on every change, and broken external links no longer block publishing.
- Every page has its own title, overview pages carry a written description, and other pages describe themselves
  in search results with their first paragraph rather than a shared boilerplate sentence.
- The **Versions** menu opens the current page in the selected release, and lists development and the releases
  still under [maintenance](../../developer/Maintenance-Policy.html).
- Comparison tables help choose between [ticket registries](../ticketing/Configuring-Ticketing-Components.html),
  [service registries](../services/Service-Management.html), [multifactor providers](../mfa/Configuring-Multifactor-Authentication.html)
  and [protocols](../protocol/Protocol-Overview.html).
- Text in setting lists and feature toggles meets contrast requirements in both themes, the dependency tabs are
  announced correctly by screen readers, and third-party scripts and stylesheets are pinned and loaded with integrity checks.
- On phones, the site header scrolls away to leave only the navigation bar, and the development notice shrinks to a
  single line, so the page heading appears on the first screen.
- [Heimdall](../authorization/Heimdall-Authorization-Overview.html) and [multitenancy](../multitenancy/Multitenancy-Overview.html)
  documentation is split into focused pages with their own menu entries.
- Every page opens with a short summary, which also serves as its description in search results.
- [Docker installation](../installation/Docker-Installation.html) names an explicit image tag, and
  [Getting Started](../planning/Getting-Started.html) points to it for a quick local trial.
- A short [Quick Start](../planning/Quick-Start.html) recipe, featured on the home page and at the top of Getting Started, takes a new deployer from the Docker image to a configured
  overlay with a registered application and an optional LDAP connection. Every setting it mentions is a link: clicking one
  opens its description, default value, module and `.properties`, YAML or environment variable form in place, without
  leaving the page. Settings mentioned throughout the documentation and release notes are marked the same way, and the <kbd>Shift</kbd> <kbd>Shift</kbd> search shows a
  setting's details directly instead of moving to the configuration catalog.
- The site loads two web fonts instead of four; code and the sidebar use the system monospace font. Images hosted on
  third-party sites are removed, and the [logout](../installation/Logout-Single-Signout.html) session example is now a table.
- Release notes open with a digest of each release candidate: what to review before upgrading, the highlights and,
  where a release has screenshots, a spotlight. Every change below it is tagged by type and grouped by area, and can be
  searched and filtered; the [overview](Overview.html) lists every release candidate with its summary, and
  [the combined release candidate page](RC.html) gathers every change from every release candidate on one page, tagged with the release candidate it shipped in.
- {: .changed data-area="docs"} The documentation sidebar can be filtered by page name, groups its sections under six headings
  (start, sign-in, protocols and applications, identity data and access, experience and operations), keeps only the current
  path open with its parent sections pinned at the top while scrolling, and opens a section's overview from the section name
  itself instead of a separate *Overview* entry.
  
### Heimdall AuthZEN
{: .changed data-area="authorization"}

[Heimdall](../authorization/Heimdall-Authorization-Overview.html) AuthZEN support is reworked to follow the
[AuthZEN Authorization API 1.0](https://openid.net/specs/authorization-api-1_0.html) specification:

- The AuthZEN `resource.id` now identifies the resource instance rather than a policy namespace. AuthZEN requests are matched
  against [resources in all namespaces](../authorization/Heimdall-Authorization-Resources.html) by their new `resourceType`, `actions` and optional `resourceIdPattern` fields, and all matching resources must grant access.
  Existing AuthZEN resources must define these fields, or AuthZEN requests are denied; requests to `/heimdall/authorize` are unaffected.
- Evaluated denials return `200` with `"decision": false`, malformed requests `400`, and failed caller authentication `401`. The `X-Request-ID` header is echoed.
- Tokens must be issued to a registered OAuth or OpenID Connect application whose access strategy allows access, and
  a new [Heimdall access strategy](../authorization/Heimdall-Authorization-Principal.html) can prevent an application from calling Heimdall. [DPoP-bound](../authentication/OIDC-Authentication-DPoP.html) and certificate-bound tokens
  now require their proof. On the AuthZEN endpoint, `Basic` credentials are the `client_id:client_secret` of a registered application
  rather than CAS user credentials.
- [JWT bearer assertions](../authentication/OIDC-Authentication-JWT-Bearer.html) must carry `jti` and `iat` claims, are accepted once, and may not live longer than
  `cas.heimdall.jwt-assertion-max-lifetime`{: .cas-setting} (five minutes by default).
- Failed caller authentication on `/heimdall/authorize` now returns `401` instead of `403`. Failed caller authentication on both
  Heimdall endpoints is subject to [authentication throttling](../authentication/Configuring-Authentication-Throttling-Failure.html).
- A resource with no policies now denies access instead of granting it, and the REST policy no longer sends the resource's policies to its endpoint.
- [JDBC policies](../authorization/Heimdall-Authorization-Policies.html) use a shared connection pool, registered as an application context bean and optionally named via `dataSourceName`,
  instead of opening a new database connection for every decision. Queries time out after `queryTimeout` (five seconds by default).
  Policies are evaluated in order rather than on the shared thread pool.
- AuthZEN subjects are resolved from CAS [attribute repositories](../integration/Attribute-Resolution.html) only for the `user` subject type. Required and rejected attribute policies accept qualified names such as `subject.properties.department`,
  `resource.properties.owner`, `action.properties.method` and `context.channel`. HTTP request headers are no longer added to the
  AuthZEN request context; on `/heimdall/authorize` they no longer override body entries or include protocol headers.
- A resource that does not set `enforceAllPolicies` is now granted when any one of its policies grants access, as documented;
  previously every policy had to grant. Set `enforceAllPolicies` to `true` on resources that rely on the old behavior.
  In that mode, a policy that fails with an error no longer prevents a later policy from granting access.
- Heimdall supports the AuthZEN [access evaluations API](../authorization/Heimdall-Authorization-AuthZEN.html) at
  `/heimdall/authzen/evaluations`, with the `execute_all`, `deny_on_first_deny` and `permit_on_first_permit` semantics.
- Heimdall publishes AuthZEN [policy decision point metadata](../authorization/Heimdall-Authorization-AuthZEN.html) at
  `/heimdall/.well-known/authzen-configuration`; the well-known location defined by the specification needs a
  [rewrite rule](../installation/Servlet-Container-Embedded-Tomcat-RewriteValve.html).
- Denied AuthZEN decisions carry a [decision context](../authorization/Heimdall-Authorization-AuthZEN.html)
  with a `reason` code.
- [JDBC and OpenFGA policies](../authorization/Heimdall-Authorization-Policies.html) receive the AuthZEN subject, resource and action. [Palantir](../installation/Admin-Dashboard.html) can edit the AuthZEN fields
  of a resource and configure the Heimdall access strategy.

### Certificate-Bound Access Tokens
{: .action data-area="oidc"}

The certificate thumbprint that CAS records for [mutual TLS client authentication](../authentication/OIDC-Authentication-AccessToken-AuthMethods.html)
and emits as the `cnf` `x5t#S256` claim of access tokens and introspection responses is now computed as specified
by [RFC 8705](https://www.rfc-editor.org/rfc/rfc8705): the base64url-encoded SHA-256 hash of the DER-encoded certificate.
Previously, it was computed from the certificate's public key and was not encoded as specified. Certificate-bound tokens issued
before the upgrade carry the old value and are no longer accepted by resource servers that verify the binding.

### Authentication Throttling
{: .changed data-area="authentication"}

[Authentication throttling](../authentication/Configuring-Authentication-Throttling-Failure.html) now records a failed attempt only
when the response status is `401`, once per request. Previously, any response other than `200`, `201` or `302`, such as a malformed
request (`400`), an authorization denial (`403`), a missing resource (`404`) or a server error (`500`), was counted as a failed login,
and failures were recorded twice. Failed [SAML2 ECP authentication](../authentication/Configuring-SAML2-Authentication.html) attempts, which answer with a SOAP fault, are now counted as well.

[Redis authentication throttling](../authentication/Configuring-Authentication-Throttling-Redis.html) no longer reads the audit log.
It used to scan and load every audit record stored in Redis on each login attempt, which slowed every login as the audit log grew.
Failed attempts are now tracked in a small key per username and IP address that expires once outside the failure range.
[Redis audit records](../audits/Audits-Redis.html) now expire after `cas.audit.redis.max-age`{: .cas-setting}, 180 days by default, and
looking up audit records reads only the newest matching entries instead of loading every record.

### Passwordless Authentication
{: .new data-area="passwordless"}

- [Passwordless authentication](../authentication/Passwordless-Authentication.html) tokens are now single-use under
  concurrent submissions, and every submitted token, including a wrong one, goes through the authentication manager and
  is recorded in the [audit log](../audits/Audits.html).
- Tokens kept in [JPA](../authentication/Passwordless-Authentication-Tokens-JPA.html) or
  [MongoDb](../authentication/Passwordless-Authentication-Tokens-MongoDb.html) are now removed once used, and their
  cleaner removes expired tokens; it used to remove the valid ones.
- MongoDb and [REST](../authentication/Passwordless-Authentication-Tokens-Rest.html) stores no longer return expired
  tokens, and a REST endpoint must answer a single-token `DELETE` with a `2xx` status only when it removed the token.
- A token that could not be delivered by
  [email or SMS](../authentication/Passwordless-Authentication-Notifications.html) is no longer stored and the user is
  told so, while a failure in one channel no longer discards a token the other one delivered.
- Submitted tokens now arrive as a dedicated `PasswordlessTokenCredential`, which is the only credential the
  passwordless authentication handler accepts; other one-time password credentials, such as Duo Security passcodes, are
  no longer checked against the passwordless token store, and the recorded credential type changes accordingly.
- The token field is now a plain text field marked as `one-time-code`, so browsers and phones can fill in the code. A
  wrong token no longer causes a new token to be issued and sent.
- SMS messages now end with an
  [origin-bound one-time code](../authentication/Passwordless-Authentication-Notifications.html) line (`@host #token`),
  which the token page reads through the WebOTP API where available.
- When [WebAuthn primary authentication](../authentication/Passwordless-Authentication-Passkeys.html) is allowed, the
  passwordless username field offers discoverable passkeys from the browser's autofill menu (WebAuthn conditional
  mediation) where the browser supports it, and the
  [passwordless selection menu](../authentication/Passwordless-Authentication-UserSelectionMenu.html) offers a passkey
  option. Both hand the passkey assertion to the existing WebAuthn primary authentication flow.

### WebAuthn Level 3
{: .new data-area="mfa"}

- [FIDO2 WebAuthn](../mfa/FIDO2-WebAuthn-Authentication.html) publishes its origins at `/.well-known/webauthn` for
  related origin requests, so passkeys can be used from origins whose domain differs from the relying party identifier.
- After a successful authentication, CAS reports the user's accepted passkeys and current account details to the browser
  through the [Signal API](../mfa/FIDO2-WebAuthn-Authentication.html), and passkey autofill checks `getClientCapabilities()` where the browser offers it.
- Registration and authentication requests carry the user-agent hints set in `cas.authn.mfa.web-authn.core.hints`{: .cas-setting}, and
  each registration records whether the authenticator reported a discoverable credential (`credProps`).
- When an assertion fails because the owning account no longer holds the passkey, the response says so and the browser
  is told to stop offering it (`signalUnknownCredential`); the account profile does the same as soon as a passkey is
  deleted, which also covers the account's last passkey.
- WebAuthn pages now use the browser's JSON serialization (`parseCreationOptionsFromJSON`,
  `parseRequestOptionsFromJSON`, `toJSON()`) and no longer override the configured attestation conveyance preference
  with `direct`; browsers without this WebAuthn Level 3 support can no longer use WebAuthn in CAS.
- CAS also publishes `/.well-known/passkey-endpoints` so password managers can link users to the pages where passkeys
  are created and managed; by default both point to the
  [account profile](../registration/Account-Management-Overview.html) when account management is enabled.
- When WebAuthn primary authentication is allowed, the default registration button now asks for a discoverable
  credential (`residentKey=preferred`), so passkeys registered with it can also log in on their own.
- Each registration now keeps the authenticator's backup eligibility and backup state, which are updated on every login;
  a passkey whose backup eligibility changes is rejected, as WebAuthn Level 3 requires. The transports recorded at
  registration are sent back with the credentials CAS lists to the browser, so it can reach each authenticator directly.
- Registrations also keep the authenticator's AAGUID, and the account profile names passkeys by provider (for example
  *Google Password Manager* or *1Password*) with the provider's icon when the attestation does not name the device; the
  registration and login pages show that name and icon for the passkey just registered or used.
- A new [passkey upgrade](../mfa/FIDO2-WebAuthn-Authentication.html), turned on with
  `cas.authn.mfa.web-authn.core.passkey-upgrade-enabled`{: .cas-setting} alongside primary authentication and untrusted attestation,
  shows a short page after a password login that lets the browser's password manager create a passkey for the account on
  its own (WebAuthn conditional create), then continues as usual. Such passkeys keep the typed username as their name
  when CAS later reports account details through the Signal API.

### OpenID Connect Verifiable Credentials
{: .new data-area="oidc"}

- Credential proofs now follow what each [credential configuration](../authentication/OIDC-Authentication-Verifiable-Credentials.html)
  advertises: a proof signed with an algorithm outside `proof-signing-alg-values-supported`{: .cas-setting}, or naming its key by a binding
  method outside `cryptographic-binding-methods-supported`{: .cas-setting}, is refused with `invalid_proof`. Holder keys may now also be given
  as an `x5c` certificate or a `did:jwk` key identifier, and Ed25519 (`EdDSA`) keys are accepted.
- Authorization server metadata advertises `pre-authorized_grant_anonymous_access_supported` whenever the pre-authorized code
  grant is supported, so wallets without a client registration know they may redeem a pre-authorized code.
- A wallet may request a credential in the [authorization code flow](../authentication/OIDC-Authentication-Verifiable-Credentials.html) by the `scope` its credential configuration publishes,
  as OpenID4VCI 1.0 allows, instead of authorization details. Such scopes were previously dropped unless listed among the
  discovery scopes, and the resulting token was refused at the credential endpoint.
- A wallet that declines a [verifiable presentation](../authentication/OIDC-Authentication-Verifiable-Credentials.html)
  request can now say so: its error response is accepted, answered as OpenID4VP requires, and reported to the relying party
  as an `error` outcome once the request expires. Since nothing authenticates an error response, it does not end the request,
  and a valid presentation that arrives before the request expires takes precedence. Previously error responses were rejected.
- Issuer metadata describes each credential format as OpenID4VCI 1.0 requires: `jwt_vc_json` and `jwt_vc_json-ld` configurations
  publish `credential_definition` instead of `vct`. JSON-LD credentials no longer reference a context document CAS never served,
  and a configuration without a scope no longer issues a `null` credential type.
- CAS publishes JWT VC Issuer Metadata at `/.well-known/jwt-vc-issuer`, so verifiers other than CAS can find the keys that sign
  the credentials it issues. Deployments under a context path should add `jwt-vc-issuer` to the [well-known rewrite rule](../installation/Servlet-Container-Embedded-Tomcat-RewriteValve.html).
- The verifier accepts RSA and Ed25519 holder keys in addition to EC keys, advertises those key binding algorithms,
  and advertises the signing algorithms of its `dc+sd-jwt` credential configurations instead of a fixed list.
- A presentation request may carry a registered `redirect_uri` for a [same-device flow](../authentication/OIDC-Authentication-Verifiable-Credentials.html): the wallet is sent back to it
  with a `response_code`, which the relying party must present to collect the outcome. Outcomes are now released
  only to the client that created the request.
- Claims marked `"required": false` in a presentation request are now optional: they are requested through DCQL
  `claim_sets` and may be withheld. Previously the flag was ignored and every claim was required.
- Issuer metadata can describe the issuer for wallets via `cas.authn.oidc.vc.issuer.display`{: .cas-setting} (name, language, logo).
- Credential offer transactions also return the `openid-credential-offer://` deep link for the offer.
- Issued credentials no longer carry `client_id` (claim or header) or `credential_configuration_id`, which revealed to
  every verifier which relying party requested the credential; CAS verifies its own credentials by the key's `kid`.
- A wallet's error response is stored bounded: `error` to 128 characters and `error_description` to 1024.
- Attribute values with a leading zero, such as postal codes, are issued as text. Previously they were read as octal numbers,
  so `0123` was issued as `83` and `08` failed issuance.
- Issued credentials carry the issuer signing key's certificate chain as the `x5c` header, without the trust anchor,
  as HAIP 1.0 requires, when the key in the keystore has one.
- Signed presentation request objects (`X509_SAN_DNS`) no longer include the trust anchor in their `x5c` header,
  as HAIP 1.0 requires.
- A self-signed credential or request object signing certificate, which HAIP 1.0 forbids, is still used but now
  logs a warning.
- Refresh tokens keep the authorization details of the authorization code they were issued for, so access tokens obtained
  by refreshing may still request the credentials those details granted. Previously only scope-based grants survived a refresh.
- [Verifiable presentation responses](../authentication/OIDC-Authentication-Verifiable-Credentials.html) can be
  encrypted (`direct_post.jwt`), as the High Assurance Interoperability Profile requires, with
  `cas.authn.oidc.vc.presentation.response-mode=DIRECT_POST_JWT`{: .cas-setting} or per request with
  `"response_mode": "direct_post.jwt"`: each request carries its own ephemeral `ECDH-ES` key, and presentations sent in
  the clear are refused.
- Verifiable presentation requests may identify CAS with the `x509_hash` client identifier prefix, which the High Assurance
  Interoperability Profile requires of verifiers that sign requests: `cas.authn.oidc.vc.presentation.client-identifier-prefix=X509_HASH`{: .cas-setting}.
- Verifiable presentations may be requested through the [W3C Digital Credentials API](../authentication/OIDC-Authentication-Verifiable-Credentials.html)
  with the `dc_api` and `dc_api.jwt` response modes: CAS builds the request the relying party page passes to the browser, and
  verifies what the page posts back, bound to the page's origin.
- Credential requests may carry [key attestations](../authentication/OIDC-Authentication-Verifiable-Credentials.html), as
  OpenID4VCI 1.0 and the High Assurance Interoperability Profile define: in the `key_attestation` header of a `jwt` proof, or
  as an `attestation` proof that yields one credential per attested key. Attestations must chain to trust anchors set with
  `cas.authn.oidc.vc.issuer.key-attestation.trust-anchors`{: .cas-setting}, and a credential configuration may require them, along with the
  key storage and user authentication levels it accepts, which the issuer metadata advertises as `key_attestations_required`.
- Clients, wallets above all, may authenticate at the token and pushed authorization request endpoints with a client attestation
  and its proof of possession, per [OAuth 2.0 Attestation-Based Client Authentication](../authentication/OIDC-Authentication-AccessToken-AuthMethods.html)
  (`attest_jwt_client_auth`), as the High Assurance Interoperability Profile requires of wallet attestations. Attestations must chain to
  trust anchors set with `cas.authn.oidc.client-attestation.trust-anchors`{: .cas-setting}, which also advertises the method in the discovery document.
- Wallets may report what became of issued credentials at the new [notification endpoint](../authentication/OIDC-Authentication-Verifiable-Credentials.html):
  credential responses carry a `notification_id`, and `credential_accepted`, `credential_failure` and `credential_deleted`
  notifications are recorded in the audit log.
- Issued SD-JWT VC credentials may carry a `status` claim and be revoked or suspended, following the
  [Token Status List](../authentication/OIDC-Authentication-Verifiable-Credentials.html) specification: CAS publishes signed
  status list tokens, keeps entries in the ticket registry, offers an `oidcVcStatus` actuator endpoint to change a credential's
  status, and as a verifier checks the status of its own credentials instead of refusing them. Turn it on with
  `cas.authn.oidc.vc.issuer.status-list.enabled=true`{: .cas-setting}.
- Credential requests and responses may be [encrypted](../authentication/OIDC-Authentication-Verifiable-Credentials.html) on top
  of TLS, per OpenID4VCI 1.0: wallets encrypt requests to the encryption keys of the OpenID Connect keystore, published as
  `credential_request_encryption`, and receive the response encrypted to the key they send in `credential_response_encryption`.
  Turn it on with `cas.authn.oidc.vc.issuer.encryption.enabled=true`{: .cas-setting}; either direction may also be required.
- The credential endpoint answers with the error codes of OpenID4VCI 1.0: an unpublished credential configuration is
  `unknown_credential_configuration` instead of the draft-era `unsupported_credential_type`, and a `credential_identifier` the
  token response did not return is `unknown_credential_identifier` instead of `credential_request_denied`.
- Once the token response returns `credential_identifiers`, credential requests must use `credential_identifier`, as OpenID4VCI 1.0
  requires; a `credential_configuration_id` is then refused with `invalid_credential_request`. Otherwise a request must name a
  `credential_configuration_id`, and no longer falls back to the configuration recorded on the access token.
- Credential configurations may defer issuance (`deferred-issuance`{: .cas-setting}): credential requests are answered with a `transaction_id`,
  an `oidcVcDeferred` actuator endpoint approves or denies the pending transactions, and wallets collect the credentials from the
  new [deferred credential endpoint](../authentication/OIDC-Authentication-Verifiable-Credentials.html), per OpenID4VCI 1.0.
- Client attestations may be bound to [short-lived challenges](../authentication/OIDC-Authentication-AccessToken-AuthMethods.html)
  from the new challenge endpoint, advertised as `challenge_endpoint`, once turned on with
  `cas.authn.oidc.client-attestation.challenge.enabled=true`{: .cas-setting}. With trust anchors set, clients may also use a DPoP proof
  in place of the attestation proof of possession (`attest_jwt_client_auth_dpop`).
- Status list tokens carry an `aggregation_uri`, and the new [status list aggregation endpoint](../authentication/OIDC-Authentication-Verifiable-Credentials.html), advertised as
  `status_list_aggregation_endpoint`, lists all published status lists.
- Wallets that ask for `application/jwt` receive the credential issuer metadata as
  [signed metadata](../authentication/OIDC-Authentication-Verifiable-Credentials.html), per OpenID4VCI 1.0.
- CAS can require [DPoP proofs to carry a nonce](../authentication/OIDC-Authentication-DPoP.html) it handed out, per RFC 9449:
  turned on with `cas.authn.oidc.dpop.nonce.enabled=true`{: .cas-setting}, the token endpoint and the DPoP combined mode answer
  with `400`, and protected resources with `401`, `use_dpop_nonce` and a fresh nonce in the `DPoP-Nonce` header. The OpenID4VCI
  nonce endpoint and the client attestation challenge endpoint hand out nonces as well.
- Authorization codes can be bound to the client's [DPoP key](../authentication/OIDC-Authentication-DPoP.html),
  per RFC 9449: with `dpop_jkt` at the authorization endpoint or in a pushed authorization request, or with a `DPoP` proof sent with
  the pushed authorization request. The token request must then carry a DPoP proof made with that key, or it is refused with `invalid_dpop_proof`.
- Clients can be required to use [DPoP for every token request](../authentication/OIDC-Authentication-DPoP.html),
  per RFC 9449, with `dpopBoundAccessTokens` in the service definition or `dpop_bound_access_tokens` in a dynamic registration request.
  Polling with a device code and the JWT bearer grant now verify DPoP proofs too, and bind the tokens they issue.
- A [DPoP-bound access token](../authentication/OIDC-Authentication-DPoP.html) is now accepted only in the `Authorization` header
  with the `DPoP` scheme; sent as a bearer token or as a request parameter, it is refused with `invalid_token`. Refresh tokens
  issued to public clients are bound to the [DPoP key](../authentication/OIDC-Authentication-DPoP.html), per
  RFC 9449, and the stateless ticket registry keeps the DPoP binding of access and refresh tokens.
- [DPoP proofs](../authentication/OIDC-Authentication-DPoP.html) are checked as RFC 9449 requires at every endpoint: a request
  with more than one `DPoP` header, or with one that is not a JWT, is refused with `invalid_dpop_proof`, which the token endpoint
  used to answer with `invalid_request`. The profile and userinfo endpoints now answer a missing or invalid proof with `401` and a
  `WWW-Authenticate: DPoP error="invalid_dpop_proof"` challenge, as the credential endpoint did, and every `DPoP` challenge,
  Heimdall's included, lists the algorithms accepted for DPoP proofs in its `algs` parameter.
- CAS issues and verifies [ISO/IEC 18013-5 mdocs](../authentication/OIDC-Authentication-Verifiable-Credentials.html),
  such as a mobile driving licence, in the `mso_mdoc` format of OpenID4VCI 1.0 and OpenID4VP 1.0, which HAIP also covers. A credential
  configuration with the `MSO_MDOC` format names its document type with `cas.authn.oidc.vc.issuer.credential-configurations[].doctype`{: .cas-setting},
  and each claim may name its name space with `cas.authn.oidc.vc.issuer.credential-configurations[].claims[].namespace`{: .cas-setting}. The
  issuer signs the claims' digests and the wallet's device key in a mobile security object with an elliptic curve key that carries an
  X.509 chain. A presented mdoc must be signed by one of CAS's keys and its device signature must cover the session transcript of the
  request. CBOR and COSE are handled by the Authlete `cbor` library, from the same vendor as the SD-JWT library CAS already uses.

### Stateless Ticket Registry
{: .action data-area="tickets"}

With the [stateless ticket registry](../ticketing/Stateless-Ticket-Registry.html), the ticket-granting ticket is now carried by the
ticket-granting cookie, like with any other ticket registry, instead of being kept in browser storage. The single sign-on session
therefore follows the [ticket-granting cookie settings](../authentication/Configuring-SSO-Cookie.html), and login pages no longer render a browser storage page before the login form.
The `cas.ticket.registry.stateless.storage-type` setting no longer applies and is removed.

Stateless tickets now use a versioned format in which every field is length-prefixed, so values that contain separator characters,
like distinguished names, round-trip correctly. Other changes:

- Tickets are compressed only when that makes them smaller.
- Each ticket is bound to the ticket type it was issued as, so changing its prefix (for example, presenting an access token
  as a refresh token, or a proxy ticket as a proxy-granting ticket) no longer yields a valid ticket.
- OAuth response and grant types are stored by name, so reordering them in a later release does not change existing tokens.
- Expanding a ticket no longer goes through the ticket factories, which skips id generation and service registry lookups.
- An expanded ticket keeps the id it was looked up by, and its authentication keeps the original authentication date.
- The ticket-granting ticket no longer carries its own id or the tickets it has granted, which the stateless registry does not track.
- The ticket-granting ticket no longer carries principal attributes. They are fetched from [attribute repositories](../integration/Attribute-Resolution.html) again each time
  the ticket-granting ticket is read, the same way they already were during ticket validation, which keeps the ticket-granting cookie small.
  Attributes that only authentication handlers produce, such as claims from Duo Security or delegated authentication, are no longer
  available to single sign-on decisions unless an attribute repository produces them as well.
- The ticket-granting ticket keeps only its authentication and is created through the ticket-granting ticket factory when read.
  It expires at the end of its maximum lifetime; an [idle timeout](../ticketing/Configuring-Ticket-Expiration-Policy-TGT.html) configured for it is not enforced.
- [Verifiable credentials](../authentication/OIDC-Authentication-Verifiable-Credentials.html) can be issued: OAuth authorization codes
  keep their authorization details, access tokens keep their credential configurations and authorization details, and the
  credential offer and nonce endpoints hand out the stored ids. The pre-authorized code and nonces are not single use there.
  Verifiable presentations are not supported.
- The OAuth [client credentials](../authentication/OAuth-ProtocolFlow-ClientCredentials.html) and [resource owner password](../authentication/OAuth-ProtocolFlow-ResourceOwner.html) grants issue access tokens; they failed while recording the use of
  the ticket-granting ticket they create, which the stateless registry hands back in encoded form.

The ticket-granting cookie can now be encrypted without being signed, using `cas.tgc.crypto.signing-enabled=false`{: .cas-setting} (signing stays
on while a signing key is defined). The cookie encryption is authenticated, so this keeps tamper detection and makes the cookie
about a quarter smaller. This is recommended with the stateless ticket registry, where the cookie carries the
ticket-granting ticket and can otherwise exceed the `4096` bytes browsers accept, for example after Duo Security multifactor
authentication. See the [stateless ticket registry documentation](../ticketing/Stateless-Ticket-Registry.html) for details.

## Other Stuff

- {: .changed data-area="project"} A large number of dependencies and libraries have been updated to their latest versions.
- {: .changed} [Groovy scripts](../integration/Apache-Groovy-Scripting.html), whether inline or in a file, that keep no state between
  executions no longer run one request at a time: each execution gets its own instance of the compiled script, so concurrent
  requests through the same script run in parallel. Scripts that declare fields other than `final` ones holding plain values,
  such as counters or caches, still run one execution at a time so that their state stays consistent.
- {: .changed} When the background reload is enabled, a request for an application that matches no cached service definition no longer
  reads the whole service registry; definitions added outside CAS, for example by another node, are recognized at the
  next scheduled reload. Changes picked up by the [JSON](../services/JSON-Service-Management.html) and
  [YAML](../services/YAML-Service-Management.html) registry watchers now update the cache right away, fetching services
  by type through the [`registeredServices` actuator endpoint](../services/Service-Management.html) no longer empties the cache, and CAS warns when the cache
  expires definitions before the scheduler reloads them. See [this guide](../services/Service-Management-Caching-Reloading.html).
- {: .fixed} Exporting a single service definition through the [`registeredServices` actuator endpoint](../services/Service-Management.html)
  now answers `404` for an unknown identifier instead of failing with an error.
- {: .fixed} Saving or deleting a service definition while the service registry is being reloaded now waits for the reload
  to finish. Previously, a save or delete that waited more than five seconds was dropped without an error; the
  `registeredServices` actuator endpoint reported success and a dynamically registered client could receive credentials
  that were never stored.
- {: .fixed} A service registry that fails to load, such as a [REST service registry](../services/REST-Service-Management.html)
  whose endpoint is unavailable or answers with an error, no longer empties the cache of service definitions; CAS logs the
  failure and keeps the definitions it already loaded until a later reload succeeds.
- {: .fixed} Groovy scripts referenced by service definitions as files, for [access strategies](../services/Service-Access-Strategy-Groovy.html),
  their activation criteria, attribute release activation criteria, single sign-on participation policies, attribute
  filters and surrogate access strategies, are now compiled and watched once per script location instead of once per
  service definition on every reload or evaluation. Previously each reload leaked a file watcher, which could exhaust the
  operating system's limit on file watchers.
- {: .fixed data-area="authorization"} [Attribute-based access strategies](../services/Service-Access-Strategy-ABAC.html) no longer deny a
  principal only because it carries fewer attributes than the strategy lists as rejected, or, when `requireAllAttributes`
  is `false`, fewer than it lists as required. The documentation now spells out that `requireAllAttributes` also governs
  rejected attributes: by default access is refused only when every rejected attribute is present with a matching value,
  so a strategy that should refuse access on any one rejected attribute must set `requireAllAttributes` to `false`.
- {: .changed data-area="authorization"} The [HTTP request access strategy](../services/Service-Access-Strategy-Http.html) now denies
  access when a configured header is missing from the request, matches header names case-insensitively, and requires the
  IP address, user agent and header patterns to match the entire value. Previously a missing header satisfied its rule and
  patterns matched any part of a value, so `10.0.0.1` also matched `110.0.0.15`. Patterns written to match only part of a
  value, such as `Chrome.+` for a user agent, must now be written as `.*Chrome.+`.
- {: .changed data-area="authorization"} [Attribute-based access strategies](../services/Service-Access-Strategy-ABAC.html), including
  those used by [Grouper](../services/Service-Access-Strategy-Grouper.html) and SCIM access strategies, now require a required or
  rejected attribute value pattern to match the entire attribute value. Previously a pattern matched any part of a value, so a
  required `admin` also accepted `sysadmin-disabled`. Patterns written to match only part of a value must now be written as,
  for example, `.*admin.*`.
- {: .changed data-area="authorization"} The [REST access strategy](../services/Service-Access-Strategy-REST.html) no longer sends
  the application definition to the remote endpoint, since it may carry client secrets, keys and other credentials. The
  request body now carries the principal id and attributes, the requested service id and the id, name, service id and type of
  the application definition.
- {: .changed} CAS no longer tries to match a service identifier, such as the `service` parameter, that is longer than
  4096 characters against application definitions, which guards against expensive regular expression matching on oversized
  input. The limit is controlled by `cas.service-registry.core.maximum-service-id-length`{: .cas-setting}; zero or a negative value disables it.
- {: .changed} [Expired application definitions](../services/Configuring-Service-Expiration-Policy.html) are processed once each time service definitions are loaded instead of on every
  request. Contacts of an expired application are notified once per expiration date rather than on every request and every
  reload, a request for an expired application no longer sends notifications, and definitions set to be deleted when they
  expire are no longer found by requests and are deleted at the next load instead of on the request thread.
- {: .fixed} Lookups in the [JSON](../services/JSON-Service-Management.html) and [YAML](../services/YAML-Service-Management.html)
  service registries no longer read a map that a reload, a save or the file watcher is modifying at the same time.
- {: .changed} New application definitions without an identifier receive a random positive identifier instead of one derived
  from the system clock, so definitions created at the same time, on the same or different nodes, no longer collide.
- {: .changed data-area="oidc"} OAuth and OpenID Connect requests find the application definition for a `client_id` through
  the service index instead of examining every definition, falling back to examining them when the index has no match.
- {: .changed} Checking access to an application now looks up the additional access strategy enforcers once instead of on every
  request.
- {: .fixed} Service registry listeners that replace a loaded application definition now take effect; the
  [REST service registry](../services/REST-Service-Management.html), among others, used to return the definitions as they
  were before the listeners ran. Single sign-on checks no longer fail with an error when no service is given.
- {: .changed data-area="saml"} The [SAML2 identity provider](../authentication/Configuring-SAML2-Authentication.html) no longer
  matches an entity id longer than `cas.service-registry.core.maximum-service-id-length`{: .cas-setting} against application definitions.
- {: .changed} Inline Groovy scripts used by access strategy and attribute release activation criteria and by single sign-on
  participation policies are compiled once and shared, instead of being compiled again on every evaluation. Groovy script
  files used by service definitions resume picking up changes after the script cache expires them, and compiling one script
  for the first time no longer holds up compiling a different one.
- {: .fixed} Listing application definitions, for example through the `registeredServices` actuator endpoint or for the
  [OpenID Connect JWKS endpoint](../authentication/OIDC-Authentication-JWKS.html), no longer includes expired definitions that
  are set to be deleted when they expire. A definition that is renewed and expires again notifies its contacts again.
- {: .fixed} A [REST service registry](../services/REST-Service-Management.html) that fails to load no longer reports
  credentials or query parameters of its endpoint URL in the error.
- {: .fixed} Queries against the service index return their results in full instead of reading from a result set that is
  already closed.
- {: .fixed} Reloading application definitions no longer empties the cache and the service index before filling them again.
  New and updated definitions are added first and removed ones are dropped last, so requests that arrive during a reload no
  longer miss an application, match a broader definition by mistake, or fail to find an OAuth or OpenID Connect client.
- {: .fixed} Application definitions that CAS reads straight from the service registry, because they are not cached yet,
  now get their [templates](../services/Configuring-Service-Template-Definitions.html) applied, like definitions that are
  loaded in bulk. Previously settings that a template supplies, such as access or multifactor authentication rules, were
  missing from such definitions until the next reload.
- {: .changed} Looking up an application definition by name or by identifier and type no longer examines every cached
  definition. A lookup by identifier for a different type of definition now finds nothing instead of returning the
  definition of the other type.
- {: .fixed} Values that only resemble a whole number, such as `1.5`, `1e3`, `0x1F` or one too large to store, no longer fail
  the request with a server error. A `service` parameter, including the one used to apply an application's
  [HTTP security headers](../services/Configuring-Service-Http-Security-Headers.html) and to fetch
  [SAML2 identity provider metadata](../authentication/Configuring-SAML2-Authentication.html), is matched as a service URL
  instead of an identifier; the issue instant of an IdP-initiated SAML2 request, the WS-Federation `wfresh` parameter, the
  single sign-on revocation attribute, numeric durations, cookie lifetimes and the actuator endpoints that accept a service
  identifier handle such values the same way. Cookie lifetimes too large for a cookie are capped instead of overflowing.
- {: .fixed} When the service registry is consulted for an application that matches no cached definition, the
  [JPA](../services/JPA-Service-Management.html) and the deprecated [CosmosDb](../services/CosmosDb-Service-Management.html)
  service registries now evaluate every definition the way other registries do. Both used to search for definitions whose
  pattern contained the requested URL, which never found definitions written as regular expressions; CosmosDb also placed
  the requested URL in its query text unescaped and no longer sends it to the database at all.
- {: .fixed} Service lookups against the [Git service registry](../services/Git-Service-Management.html) use the definitions
  from the last load instead of pulling from the remote repository on each lookup; definitions are pulled when service
  definitions are reloaded.
- {: .fixed} Application definitions that are saved through CAS, picked up by the JSON and YAML registry watchers, or
  announced by [Google Cloud Storage](../services/GCP-Storage-Service-Management.html) change notifications now have their
  [templates](../services/Configuring-Service-Template-Definitions.html) applied before they are cached, as they are when
  definitions are loaded, instead of being matched without them until the next reload.
- {: .changed} Matching a request to an application definition no longer sorts every cached definition on each lookup, and listing
  definitions, as the [OpenID Connect JWKS endpoint](../authentication/OIDC-Authentication-JWKS.html) and the `registeredServices` actuator endpoint do, no longer
  re-indexes every cached definition.
- {: .changed} The [SAML2 identity provider](../authentication/Configuring-SAML2-Authentication.html) parses its signing key once
  instead of for every signed response or assertion; a rotated or replaced key is picked up the first time it is used.
- {: .changed data-area="saml"} Back-channel [SAML2 single logout](../installation/Logout-Single-Signout.html) notifications sent with the HTTP-POST or
  HTTP-Redirect bindings now honor `cas.slo.asynchronous`{: .cas-setting} the way SOAP and CAS protocol notifications already did: by default
  they are sent in the background, so one slow service provider no longer holds up the logout of the others, and the
  logout reports them as sent without waiting for a response. The incoming logout request is also parsed once per logout
  instead of once per service provider.
- {: .changed data-area="oidc"} An OAuth/OpenID Connect token request now saves the user's single sign-on session once instead of once per token
  it issues; exchanging an authorization code for an access and a refresh token used to write it three times.
- {: .fixed} The `at_hash` claim of an [OpenID Connect](../authentication/OIDC-Authentication.html) ID token is now computed over the
  access token returned in the same response. CAS used to build and sign the access token a second time for the hash, which
  doubled the work for JWT access tokens and produced an `at_hash` that did not match when the access token was signed with
  ES* or PS* algorithms, encrypted, or requested as a JWT through token exchange.
- {: .changed} Reading the [OpenID Connect keystore](../authentication/OIDC-Authentication-JWKS.html) from a file, as
  every request to the JWKS endpoint does, no longer fails an attempt to parse the file path as an inline keystore first,
  and the keystore generated event is published only when CAS actually generates a new keystore instead of on every read.
- {: .changed} Cached [SAML2 service provider metadata](../installation/Configuring-SAML2-DynamicMetadata.html) is reloaded
  in the background once three quarters of its cache lifetime has passed, so the request that finds it nearly expired no longer
  waits for the metadata to be fetched, parsed and validated. The current metadata stays in use until the reload completes.
- {: .changed} The [SAML2 identity provider](../authentication/Configuring-SAML2-Authentication.html) builds the resolver that
  locates its signing credentials once instead of for every signed response or assertion.
- {: .changed data-area="saml"} The [SAML2 identity provider](../authentication/Configuring-SAML2-Authentication.html) decodes and
  parses the incoming `SAMLRequest` or `SAMLResponse` once when matching it to a service provider, instead of once for every
  SAML2 application definition it compares against.
- {: .changed data-area="oidc"} JWT access tokens, JWT response modes and other tokens signed or encrypted with keys defined
  per application now reuse the prepared signing and encryption keys instead of parsing them again for every token issued
  or read; a changed application definition or a rotated key is picked up the first time it is used.
- {: .fixed data-area="tickets"} Ticket registries that replicate tickets through a message queue, such as
  [Kafka](../ticketing/Kafka-Ticket-Registry.html), [AMQP](../ticketing/Messaging-AMQP-Ticket-Registry.html),
  [Pulsar](../ticketing/Pulsar-Ticket-Registry.html) and [Google Cloud Pub/Sub](../ticketing/GCP-PubSub-Ticket-Registry.html),
  now send one message per ticket update instead of two, and a node that applies an update received from another node no
  longer sends it back out. Those echoed copies cost one extra message per node for every update and could overwrite a
  newer version of the ticket on the node that made the change.
- {: .changed data-area="tickets"} When a single sign-on session ends, the [JPA ticket registry](../ticketing/JPA-Ticket-Registry.html)
  removes the session's service tickets with one statement instead of reading and deleting them one at a time. Service and
  OAuth tickets that were updated after they were issued, which lost the link to their session when ticket encryption was
  enabled, now keep it and are removed with the session.
- {: .changed data-area="tickets"} When a single sign-on session ends, the remaining ticket registries, such as
  [Redis](../ticketing/Redis-Ticket-Registry.html), [Hazelcast](../ticketing/Hazelcast-Ticket-Registry.html),
  [DynamoDb](../ticketing/DynamoDb-Ticket-Registry.html), [Cassandra](../ticketing/Cassandra-Ticket-Registry.html),
  [Memcached](../ticketing/Memcached-Ticket-Registry.html) and the default in-memory registry, remove the session's service and
  proxy tickets by their identifier instead of reading and decoding each one first. With registries that cannot tell whether
  a ticket was still present, the number of removed tickets reported for the session may include service tickets that were
  already validated. Removing a ticket that is already gone from the Apache Geode and the deprecated CosmosDb registries
  no longer fails.
- {: .changed data-area="tickets"} The [ticket registry cleaner](../ticketing/Ticket-Registry-Cleaner.html) now looks for
  expired tickets first, in batches of up to ten thousand so a large backlog is never held in memory at once, and then removes
  up to eight of them at a time, where it used to remove them one by one while the scan was still running. With the [JPA ticket registry](../ticketing/JPA-Ticket-Registry.html), the database cursor,
  connection and transaction used for the scan are released before any ticket is removed, and rows read by the scan are no
  longer all kept in memory until it ends.
- {: .fixed data-area="tickets"} [Validating a single-use service ticket](../authentication/CAS-Protocol-TicketValidation.html) now removes it in one step, and only the request that
  actually removes it receives a successful response. Two requests validating the same ticket at the same moment, on the
  same node or on different nodes, could previously both succeed with ticket registries that store copies of tickets, such as
  JPA, MongoDB, Redis or Hazelcast, or with ticket encryption enabled. Each validation also costs fewer trips to the ticket
  registry, and a validation that cannot obtain the ticket's lock is now rejected instead of being allowed without
  consuming the ticket.
- {: .fixed data-area="oidc"} [OpenID Connect](../authentication/OIDC-Authentication.html) ID tokens issued for
  `response_type=id_token` no longer carry an `at_hash` claim, since no access token is issued with them. For
  `response_type=id_token token`, the `at_hash` claim is now computed over the access token returned in the same response;
  for JWT access tokens it used to be computed over a different token and did not match.
- {: .fixed data-area="oidc"} An OAuth/OpenID Connect [authorization code](../authentication/OAuth-ProtocolFlow-AuthorizationCode.html) is now redeemed by removing it from the ticket
  registry before any token is issued, and only the request that removes it receives tokens. Two token requests presenting
  the same code at the same moment could previously both receive tokens. The code is also no longer written back to the
  ticket registry right before it is redeemed.
- {: .fixed data-area="tickets"} Removing a proxy-granting ticket no longer writes back the copy of the single sign-on
  session it was issued from, which undid any change made to the session since, such as applications the user logged into
  afterwards. When a single sign-on session ends, it is no longer rewritten once for each of its proxy-granting tickets and
  once more right before it is removed.
- {: .changed data-area="saml"} The [SAML2 identity provider](../authentication/Configuring-SAML2-Authentication.html) locates and
  verifies the service provider behind an authentication request, and resolves its metadata, once per request instead of up to
  four times while validating the request, choosing the binding and building the response.
- {: .changed data-area="saml"} Templates the SAML2 identity provider loads from the classpath, such as the page that posts a
  SAML2 response or logout request with the HTTP-POST binding, are now parsed once and cached instead of being read and parsed
  for every response.
- {: .changed data-area="oidc"} The [OpenID Connect JWKS endpoint](../authentication/OIDC-Authentication-JWKS.html) now caches the
  published key set instead of loading and parsing the keystore for every request, whichever keystore is used. The cached set
  expires along with the keys CAS signs with (`cas.authn.oidc.jwks.core.jwks-cache-expiration`{: .cas-setting}), and never later
  than a day. Rotating or revoking keys, or changing a keystore file, publishes the new key set right away on that CAS node;
  other nodes publish it once their cached set expires.
- {: .changed data-area="oidc"} OAuth and OpenID Connect client authentication at the token endpoint no longer builds a
  throw-away access token to decide which claims to release, and the authorization code or refresh token in the request is
  read from the ticket registry once per request instead of once by every authenticator and validator that inspects it.
- {: .fixed data-area="oidc"} Authorization responses that return their parameters in the URL fragment, such as those of the
  implicit flow, no longer repeat every query parameter of the client's redirect URI. When CAS includes the issuer in
  authorization responses (RFC 9207), it now sends it once, with the other response parameters: it used to be sent twice with
  the `query` response mode, and also added to the query of `fragment` and `form_post` responses.
- {: .fixed data-area="oidc"} JWT access tokens returned from the OpenID Connect authorization endpoint, with the `token` and
  `id_token token` response types, now name the OpenID Connect issuer, as the ID token and the token endpoint do. They used
  to name the CAS server, so resource servers that check the issuer of a JWT access token rejected them.
- {: .changed data-area="oidc"} When a request to an [OAuth](../authentication/OAuth-Authentication.html) endpoint carries
  no `client_id` parameter, as when a client authenticates at the token endpoint with HTTP Basic, CAS now takes the client
  from those credentials instead of matching the redirect URI against every OAuth application each time the
  authenticated profile is read. Where the redirect URI still has to be matched, that happens once per request. Clients
  whose redirect URI patterns overlap are no longer mistaken for one another in this case.
- {: .changed data-area="oidc"} A request object passed with the `request` parameter is now unpacked and verified once per
  request instead of once for every authorization parameter read from it.
- {: .changed data-area="oidc"} A client that authenticates at the token endpoint with `private_key_jwt` or `client_secret_jwt`
  and sends no `client_id` parameter is now identified from its client assertion, whose issuer and subject are the client
  identifier, instead of by matching the redirect URI against every OAuth application.
- {: .changed data-area="oidc"} When CAS works out which application a login is for, a request object carried by the
  OAuth or OpenID Connect authorization request is now unpacked and verified once, instead of once for each of the client
  identifier, redirect URI and grant type read from it.
- {: .changed data-area="oidc"} Rotating or revoking keys in the [OpenID Connect keystore](../authentication/OIDC-Authentication-JWKS.html)
  now also clears the keys CAS signs with on that node, so the new current key is used and a revoked key stops being used right
  away instead of once the cached keys expire.
- {: .fixed data-area="oidc"} Changes to an [OpenID Connect keystore](../authentication/OIDC-Authentication-JWKS.html) file
  are announced again once CAS notices them. The file watcher has never broadcast them, so CAS kept signing with the keys it had
  loaded until its key cache expired.
- {: .changed data-area="project"} Almost all CAS unit tests are internally reworked to allow maximum parallelization and speed up the overall test execution time.
- {: .fixed} [Delegated authentication](../integration/Delegate-Authentication.html) no longer fails intermittently when concurrent requests reach an identity provider that is still being initialized, typically right after startup. Such requests now wait for the initialization in progress instead of failing, which also affects [SAML2 identity providers](../integration/Delegate-Authentication-SAML2.html) when building SAML2 responses, metadata and logout requests.
- {: .fixed} [WebAuthn](../mfa/FIDO2-WebAuthn-Authentication.html) registration and login pages no longer show a broken image when the device has no icon in its attestation metadata, and fall back to the credential nickname for the device name.
- {: .fixed} [WebAuthn](../mfa/FIDO2-WebAuthn-Authentication.html) pages now report browsers that cannot run them; the support check was never applied, and it no longer requires a platform authenticator.
- {: .fixed} [WebAuthn](../mfa/FIDO2-WebAuthn-Authentication.html) authentication pages now send the CSRF token rendered by CAS instead of reading it from the `XSRF-TOKEN` cookie, which failed with `403` whenever the page could not read that cookie.
- {: .fixed} Browser storage used by [Duo Security](../mfa/DuoSecurity-Authentication.html) and the [SAML2 identity provider](../authentication/Configuring-SAML2-Authentication.html) now falls back to cookies when the browser cannot use local or session storage.
- {: .fixed} Ed25519 keys presented to the OpenID Connect [client JWKS registration endpoint](../authentication/OIDC-Authentication-JWKS-Clients.html) are now
  verified with the JDK's own EdDSA support. Verification previously relied on Google Tink, which CAS does not ship, so such registrations failed at runtime.
- {: .new} CAS now logs a warning when a cookie it writes, such as the [ticket-granting cookie](../authentication/Configuring-SSO-Cookie.html), is larger than the 4 KB that browsers are guaranteed to accept.
- {: .changed data-area="project"} MongoDb integration tests have now switched to using MongoDb `9.x`.
- {: .fixed} [Google Authenticator](../mfa/GoogleAuthenticator-Authentication.html) devices can only be removed or confirmed by the user that owns them;
  a device id that belongs to another user is now refused.
- {: .fixed} Google Authenticator device repositories: removing a device or a user from
  [DynamoDB](../mfa/GoogleAuthenticator-Authentication-Registration-DynamoDb.html) no longer removes other records with
  it; registering or updating a device in [LDAP](../mfa/GoogleAuthenticator-Authentication-Registration-LDAP.html) no
  longer replaces the user's other devices; and removing or confirming a device with
  [JPA](../mfa/GoogleAuthenticator-Authentication-Registration-JPA.html) works again and no longer re-encrypts the
  device secret when encryption is enabled.
- {: .changed} Google Authenticator device removal and confirmation now expire if not completed within a few minutes of the code being verified.
  The [REST device repository](../mfa/GoogleAuthenticator-Authentication-Registration-Rest.html) now sends deletions as `DELETE`
  and includes the device `id` and `properties` when saving. A REST or JSON repository that cannot be read now fails the login
  instead of offering device registration, and LDAP removes the right device when several users' device ids share leading digits.
- {: .fixed} More Google Authenticator repository fixes:
  [MongoDB](../mfa/GoogleAuthenticator-Authentication-Registration-MongoDb.html) no longer treats usernames that differ
  only by accents (such as `jose` and `josé`) as the same user; the tenant of a device is kept when it is registered and
  stored, including in DynamoDB and JPA; DynamoDB stores encrypted scratch codes and an emptied list of scratch codes;
  saving a device no longer alters the object passed in; and every repository now counts devices rather than users and
  replaces a device saved twice instead of storing a duplicate.
- {: .new} Google Authenticator devices now record when they were last used, in every repository. Updating a device that is not
  stored now adds it in every repository. A code used to verify a device removal or confirmation is no longer restored
  afterwards. A device id that is not a number is refused instead of failing with a server error. The
  [JSON repository](../mfa/GoogleAuthenticator-Authentication-Registration-JSON.html) replaces its file in one atomic step
  and is documented as single-node.
- {: .action} Faster Google Authenticator lookups: [DynamoDB](../mfa/GoogleAuthenticator-Authentication-Registration-DynamoDb.html)
  reads a device by key and finds a user's devices through a new `useridIndex` global secondary index instead of scanning
  the table (CAS adds the index at startup; create it yourself if table creation on startup is turned off), and counts
  without reading records back. [MongoDB](../mfa/GoogleAuthenticator-Authentication-Registration-MongoDb.html) creates
  an index on usernames with the collation its lookups use.
- {: .changed} Google Authenticator [LDAP](../mfa/GoogleAuthenticator-Authentication-Registration-LDAP.html) device lookups by id no
  longer read every entry, and saving or removing a device no longer decrypts and re-encrypts the user's other devices.
  [Redis](../mfa/GoogleAuthenticator-Authentication-Registration-Redis.html) loads all devices with batched `MGET`
  instead of one request per device, and saving a device no longer decrypts the user's other devices.
- {: .fixed} The Google Authenticator [JPA repository](../mfa/GoogleAuthenticator-Authentication-Registration-JPA.html) can now save
  a device whose id is not already in the database, such as one imported through the actuator endpoint; the database
  assigns it a new id. Removing a user's devices is now a single bulk delete.
- {: .changed} The [MongoDB service registry](../services/MongoDb-Service-Management.html) deletes a service definition in one
  request instead of reading it back first, counts definitions from collection metadata instead of scanning the
  collection, and, when several definitions share a service id or name, returns the one that comes first in evaluation
  order instead of an arbitrary match. Definitions it looks up by id or name, or reads while the cache is empty, now get
  the same post-processing as those it loads in bulk, so OpenID Connect applications keep their scope-based attribute
  release policies.
- {: .changed} The [MongoDB ticket registry](../ticketing/MongoDb-Ticket-Registry.html) reads only the stored ticket when fetching
  tickets, counts tickets from collection metadata instead of scanning collections, and removes the service tickets of a
  ticket-granting ticket with one request per collection instead of reading each one back first. Ticket identifiers are
  now stored as the document `_id`, which removes the separate `IDX_ID` index and makes duplicate identifiers impossible;
  tickets stored by earlier versions are converted at startup, so all nodes sharing the registry should be upgraded
  together. Only ticket-granting tickets store
  principal attributes and carry the attribute index, and only tickets linked to a service carry the service index, so
  issuing and validating a service ticket no longer writes an index entry per attribute value; indexes left on other
  collections by earlier versions are removed at startup.
- {: .new} When MongoDB runs as a replica set, the [MongoDB service registry](../services/MongoDb-Service-Management.html)
  watches its collection through a change stream and reloads service definitions on every CAS node shortly after they
  change, including changes made by another node or directly in MongoDB. Deployments that are not replica sets keep
  relying on the scheduled reload.
- {: .changed} MongoDB connection pools now default to 100 connections, keep connections for 30 minutes and idle connections for
  5 minutes, and wait at most 10 seconds for a free connection instead of a minute. The server heartbeat uses the
  driver's default instead of the connection `timeout`{: .cas-setting}. Pool settings do not apply when `client-uri`{: .cas-setting} is used; set the
  connection string's own options instead. If an index CAS replaces cannot be rebuilt, the previous index is restored
  instead of the collection being left without it.
- {: .fixed} Redis-backed counts and key lookups, such as the [ticket](../ticketing/Redis-Ticket-Registry.html) and
  [service registry](../services/Redis-Service-Management.html) counts, now return their connection when done. With
  connection pooling enabled, every call used to keep a pooled connection until the pool ran out.
- {: .fixed} The [Redis ticket registry](../ticketing/Redis-Ticket-Registry.html) query for ticket ids and loading all
  [YubiKey](../mfa/YubiKey-Authentication.html) devices from Redis also return their connection when done.
- {: .fixed data-area="tickets"} The in-memory cache of the [Redis ticket registry](../ticketing/Redis-Ticket-Registry.html)
  no longer keeps tickets that have changed or been removed on another CAS node. A cached ticket used to stay cached for as
  long as it was read, so a session ended on one node could stay valid on another and could even be written back to Redis.
  Tickets are now kept for at most 30 seconds, a ticket read while another node announces a change to it is not kept, and
  changes are announced to other nodes without the ticket itself, which used to be sent unencrypted to every node on every
  change even with ticket encryption enabled. Nodes still on an earlier version handle the new notifications correctly.
- {: .fixed} WebAuthn falls back to the cached [FIDO metadata BLOB](../mfa/FIDO2-WebAuthn-Authentication-Attestation.html) when
  downloading a fresh one fails at startup, for example when the FIDO metadata service rate-limits the request, instead of failing to start.
- {: .changed data-area="project"} The project [README](https://github.com/apereo/cas) is reworked as a landing page, with a one-minute Docker try-out,
  a link to the new [Quick Start](../planning/Quick-Start.html), features grouped by area and the maintained documentation versions.
- {: .changed data-area="ui"} The [account profile](../registration/Account-Management-Overview.html) and the account management screens (sign-up,
  password reset, forgot username, expired or must-change password) are redesigned: the profile gets a section rail,
  an overview with summary cards and lists with inline details, filtering and keyboard support, and every account
  management flow shows its steps beside a single form card. Features are unchanged; colors follow the CAS theme.
- {: .changed data-area="ui"} Buttons and text fields on CAS pages share one [look](../ux/User-Interface-Customization-CSSJS.html):
  labels sit above rounded inputs, buttons are pill-shaped and password fields get round reveal and generate buttons, matching
  the account screens and following the CAS theme colors. Alert boxes, tables and progress bars follow the same style, and
  page layouts stay as they were.
- {: .changed data-area="ui"} [WebAuthn](../mfa/FIDO2-WebAuthn-Authentication.html) and consent screens are redesigned. WebAuthn login, registration and QR code pages sit in a
  centered card with a status icon, and registration offers passkeys and security keys side by side. OpenID Connect, OAuth and
  CIBA consent show the application up front, list each scope and claim with its own icon, and show the CIBA binding message
  as a large code. Consent no longer shows two *Deny* buttons, CIBA lists scope names again, OAuth scopes without a
  description no longer show a raw message key, and the WebAuthn status heading now updates during login and registration.
- {: .changed data-area="ui"} [Interrupt](../webflow/Webflow-Customization-Interrupt.html) and [attribute consent](../integration/Attribute-Release-Consent.html) screens follow the same style: a centered card with a status icon,
  the interrupt message as a callout with its links, attribute consent tabs as a segmented control and consent options as
  selectable cards. Tabs no longer show a focus ring when a page loads.
- {: .changed data-area="ui"} The rest of the CAS screens follow the same style: error, account status, logout and confirmation
  pages show a status icon in a centered card, multifactor, passwordless, delegated, surrogate and other sign-in steps show the
  provider's icon above their form, provider and option choices appear as cards, and the login card picks up the same surface.
  Palantir keeps its layout and colors with a rounded navigation rail, pill-style tabs and softer cards.
- {: .changed data-area="ui"} [Palantir](../installation/Admin-Dashboard.html) dialogs, including the service wizard and editor, use the same look as the rest of CAS:
  rounded dialogs with a plain title bar, matching buttons (the main action highlighted), select menus and multi-value fields
  that look like the other inputs, round row buttons, and restyled menus, tooltips and confirmation prompts. Dialogs no
  longer fail to position themselves when opened before any right-click menu was set up.

{% include release-footer.html %}
