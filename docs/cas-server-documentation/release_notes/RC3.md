---
layout: default
title: CAS - 8.1.0-RC3 Release Notes
category: Planning
---

{% include variables.html %}

# 8.1.0-RC3 Release Notes

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

The [JDK baseline requirement](../planning/Installation-Requirements.html) for this CAS release is and **MUST** be JDK `25`. All compatible distributions
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

### JSpecify & NullAway

CAS codebase is now annotated with [JSpecify](https://jspecify.dev/) annotations to indicate nullness contracts on method parameters,
return types and fields. We will gradually extend the coverage of such annotations across the entire codebase in future releases
and will integrate the Gradle build tool with tools such as [NullAway](https://github.com/uber/NullAway) to prevent nullness contract violations
during compile time.

### Spring Boot 4.2

CAS is now built on top of Spring Boot `4.2.x`. This is an in-progress ongoing minor platform upgrade that
affects almost all aspects of the codebase including many of the third-party core libraries used by CAS
as well as some CAS functionality.

Please refer to the [Spring Boot Wiki](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.2-Release-Notes)
for more information on the changes and updates in this release. 

### Documentation

The CAS documentation site has received a visual and functional overhaul. Notable changes include:

- A refreshed theme with light and dark modes, a serif typeface for headings, a monospaced sidebar, and
  quiet icons for recurring sections such as configuration, actuator endpoints and troubleshooting.
- Configuration settings are presented as a searchable, filterable reference list. Each setting can be expanded
  to show its description, type, default value and deprecation status, and copied as `.properties`, YAML or
  environment variables. The chosen format is remembered across pages.
- [Actuator endpoints](../monitoring/Monitoring-Statistics.html) are grouped by endpoint, with each operation listing its parameters, response and a working `curl`
  example inline. Instructions to enable, expose and secure the endpoint along with related settings and
  troubleshooting notes are shown once per endpoint rather than once per operation, which makes pages considerably lighter.
- The [configuration properties](../configuration/Configuration-Properties.html) search is rewritten. It matches setting names regardless
  of how they are written (property names, environment variables or pasted assignments), supports exact name matches,
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
  leaving the page. Any page can mark settings the same way, and the <kbd>Shift</kbd> <kbd>Shift</kbd> search shows a
  setting's details directly instead of moving to the configuration catalog.
- The site loads two web fonts instead of four; code and the sidebar use the system monospace font. Images hosted on
  third-party sites are removed, and the [logout](../installation/Logout-Single-Signout.html) session example is now a table.

### Heimdall AuthZEN

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
- Heimdall supports the AuthZEN [access evaluations API](../authorization/Heimdall-Authorization-AuthZEN.html#access-evaluations) at
  `/heimdall/authzen/evaluations`, with the `execute_all`, `deny_on_first_deny` and `permit_on_first_permit` semantics.
- Heimdall publishes AuthZEN [policy decision point metadata](../authorization/Heimdall-Authorization-AuthZEN.html#policy-decision-point-metadata) at
  `/heimdall/.well-known/authzen-configuration`; the well-known location defined by the specification needs a
  [rewrite rule](../installation/Servlet-Container-Embedded-Tomcat-RewriteValve.html).
- Denied AuthZEN decisions carry a [decision context](../authorization/Heimdall-Authorization-AuthZEN.html#decision-context)
  with a `reason` code.
- [JDBC and OpenFGA policies](../authorization/Heimdall-Authorization-Policies.html) receive the AuthZEN subject, resource and action. [Palantir](../installation/Admin-Dashboard.html) can edit the AuthZEN fields
  of a resource and configure the Heimdall access strategy.

### Certificate-Bound Access Tokens

The certificate thumbprint that CAS records for [mutual TLS client authentication](../authentication/OIDC-Authentication-AccessToken-AuthMethods.html)
and emits as the `cnf` `x5t#S256` claim of access tokens and introspection responses is now computed as specified
by [RFC 8705](https://www.rfc-editor.org/rfc/rfc8705): the base64url-encoded SHA-256 hash of the DER-encoded certificate.
Previously, it was computed from the certificate's public key and was not encoded as specified. Certificate-bound tokens issued
before the upgrade carry the old value and are no longer accepted by resource servers that verify the binding.

### Authentication Throttling

[Authentication throttling](../authentication/Configuring-Authentication-Throttling-Failure.html) now records a failed attempt only
when the response status is `401`, once per request. Previously, any response other than `200`, `201` or `302`, such as a malformed
request (`400`), an authorization denial (`403`), a missing resource (`404`) or a server error (`500`), was counted as a failed login,
and failures were recorded twice. Failed SAML2 ECP authentication attempts, which answer with a SOAP fault, are now counted as well.

### Passwordless Authentication

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

- [FIDO2 WebAuthn](../mfa/FIDO2-WebAuthn-Authentication.html) publishes its origins at `/.well-known/webauthn` for
  related origin requests, so passkeys can be used from origins whose domain differs from the relying party identifier.
- After a successful authentication, CAS reports the user's accepted passkeys and current account details to the browser
  through the Signal API, and passkey autofill checks `getClientCapabilities()` where the browser offers it.
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

- Credential proofs now follow what each [credential configuration](../authentication/OIDC-Authentication-Verifiable-Credentials.html)
  advertises: a proof signed with an algorithm outside `proof-signing-alg-values-supported`, or naming its key by a binding
  method outside `cryptographic-binding-methods-supported`, is refused with `invalid_proof`. Holder keys may now also be given
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
- A presentation request may carry a registered `redirect_uri` for a same-device flow: the wallet is sent back to it
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

### Stateless Ticket Registry

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

The ticket-granting cookie can now be encrypted without being signed, using `cas.tgc.crypto.signing-enabled=false`{: .cas-setting} (signing stays
on while a signing key is defined). The cookie encryption is authenticated, so this keeps tamper detection and makes the cookie
about a quarter smaller. This is recommended with the stateless ticket registry, where the cookie carries the
ticket-granting ticket and can otherwise exceed the `4096` bytes browsers accept, for example after Duo Security multifactor
authentication. See the [stateless ticket registry documentation](../ticketing/Stateless-Ticket-Registry.html) for details.

## Other Stuff

- A large number of dependencies and libraries have been updated to their latest versions.
- Almost all CAS unit tests are internally reworked to allow maximum parallelization and speed up the overall test execution time.
- [Delegated authentication](../integration/Delegate-Authentication.html) no longer fails intermittently when concurrent requests reach an identity provider that is still being initialized, typically right after startup. Such requests now wait for the initialization in progress instead of failing, which also affects [SAML2 identity providers](../integration/Delegate-Authentication-SAML2.html) when building SAML2 responses, metadata and logout requests.
- [WebAuthn](../mfa/FIDO2-WebAuthn-Authentication.html) registration and login pages no longer show a broken image when the device has no icon in its attestation metadata, and fall back to the credential nickname for the device name.
- [WebAuthn](../mfa/FIDO2-WebAuthn-Authentication.html) pages now report browsers that cannot run them; the support check was never applied, and it no longer requires a platform authenticator.
- [WebAuthn](../mfa/FIDO2-WebAuthn-Authentication.html) authentication pages now send the CSRF token rendered by CAS instead of reading it from the `XSRF-TOKEN` cookie, which failed with `403` whenever the page could not read that cookie.
- Browser storage used by [Duo Security](../mfa/DuoSecurity-Authentication.html) and the [SAML2 identity provider](../authentication/Configuring-SAML2-Authentication.html) now falls back to cookies when the browser cannot use local or session storage.
- Ed25519 keys presented to the OpenID Connect [client JWKS registration endpoint](../authentication/OIDC-Authentication-JWKS-Clients.html) are now
  verified with the JDK's own EdDSA support. Verification previously relied on Google Tink, which CAS does not ship, so such registrations failed at runtime.
- CAS now logs a warning when a cookie it writes, such as the ticket-granting cookie, is larger than the 4 KB that browsers are guaranteed to accept.
- MongoDb integration tests have now switched to using MongoDb `9.x`.
- [Google Authenticator](../mfa/GoogleAuthenticator-Authentication.html) devices can only be removed or confirmed by the user that owns them;
  a device id that belongs to another user is now refused.
- Google Authenticator device repositories: removing a device or a user from
  [DynamoDB](../mfa/GoogleAuthenticator-Authentication-Registration-DynamoDb.html) no longer removes other records with
  it; registering or updating a device in [LDAP](../mfa/GoogleAuthenticator-Authentication-Registration-LDAP.html) no
  longer replaces the user's other devices; and removing or confirming a device with
  [JPA](../mfa/GoogleAuthenticator-Authentication-Registration-JPA.html) works again and no longer re-encrypts the
  device secret when encryption is enabled.
- Google Authenticator device removal and confirmation now expire if not completed within a few minutes of the code being verified.
  The [REST device repository](../mfa/GoogleAuthenticator-Authentication-Registration-Rest.html) now sends deletions as `DELETE`
  and includes the device `id` and `properties` when saving. A REST or JSON repository that cannot be read now fails the login
  instead of offering device registration, and LDAP removes the right device when several users' device ids share leading digits.
- More Google Authenticator repository fixes:
  [MongoDB](../mfa/GoogleAuthenticator-Authentication-Registration-MongoDb.html) no longer treats usernames that differ
  only by accents (such as `jose` and `josé`) as the same user; the tenant of a device is kept when it is registered and
  stored, including in DynamoDB and JPA; DynamoDB stores encrypted scratch codes and an emptied list of scratch codes;
  saving a device no longer alters the object passed in; and every repository now counts devices rather than users and
  replaces a device saved twice instead of storing a duplicate.
- Google Authenticator devices now record when they were last used, in every repository. Updating a device that is not
  stored now adds it in every repository. A code used to verify a device removal or confirmation is no longer restored
  afterwards. A device id that is not a number is refused instead of failing with a server error. The
  [JSON repository](../mfa/GoogleAuthenticator-Authentication-Registration-JSON.html) replaces its file in one atomic step
  and is documented as single-node.
- Faster Google Authenticator lookups: [DynamoDB](../mfa/GoogleAuthenticator-Authentication-Registration-DynamoDb.html)
  reads a device by key and finds a user's devices through a new `useridIndex` global secondary index instead of scanning
  the table (CAS adds the index at startup; create it yourself if table creation on startup is turned off), and counts
  without reading records back. [MongoDB](../mfa/GoogleAuthenticator-Authentication-Registration-MongoDb.html) creates
  an index on usernames with the collation its lookups use.
- Google Authenticator [LDAP](../mfa/GoogleAuthenticator-Authentication-Registration-LDAP.html) device lookups by id no
  longer read every entry, and saving or removing a device no longer decrypts and re-encrypts the user's other devices.
  [Redis](../mfa/GoogleAuthenticator-Authentication-Registration-Redis.html) loads all devices with batched `MGET`
  instead of one request per device, and saving a device no longer decrypts the user's other devices.
- The Google Authenticator [JPA repository](../mfa/GoogleAuthenticator-Authentication-Registration-JPA.html) can now save
  a device whose id is not already in the database, such as one imported through the actuator endpoint; the database
  assigns it a new id. Removing a user's devices is now a single bulk delete.
- Redis-backed counts and key lookups, such as the [ticket](../ticketing/Redis-Ticket-Registry.html) and
  [service registry](../services/Redis-Service-Management.html) counts, now return their connection when done. With
  connection pooling enabled, every call used to keep a pooled connection until the pool ran out.
- The [Redis ticket registry](../ticketing/Redis-Ticket-Registry.html) query for ticket ids and loading all
  [YubiKey](../mfa/YubiKey-Authentication.html) devices from Redis also return their connection when done.
- WebAuthn falls back to the cached [FIDO metadata BLOB](../mfa/FIDO2-WebAuthn-Authentication-Attestation.html) when
  downloading a fresh one fails at startup, for example when the FIDO metadata service rate-limits the request, instead of failing to start.
