---
layout: default
title: CAS - 8.1.0-RC4 Release Notes
category: Planning
interface_images:
  - src: img_24.png
    alt: Theme builder next to the login page, turning off the password reveal button
    title: The theme builder previews theme properties live, here turning off the password reveal button
  - src: img_25.png
    alt: Login page with external identity providers in a two-column grid before the login form
    title: External identity providers in a grid, placed before the login form
  - src: img_26.png
    alt: Multifactor authentication provider selection shown as cards
    title: Multifactor authentication provider selection as cards
  - src: img_27.png
    alt: Google Authenticator registration with a blue QR code and scratch codes as a numbered list
    title: Google Authenticator registration with themed QR code colors and scratch codes as a list
  - src: img_28.png
    alt: WebAuthn device registration recommending a security key
    title: WebAuthn registration recommending a security key instead of a passkey
  - src: img_29.png
    alt: Attribute consent listing released attributes as cards
    title: Attribute consent with the list layout
  - src: img_30.png
    alt: The logout page with its card placed in each of the nine main content positions
    title: The nine values of the main content position, from top left to bottom right
account_profile_images:
  - src: img_30.png
    alt: Account profile overview with a banner saying the password expires in five days
    title: The overview warns before the password expires, with a shortcut to change it
  - src: img_31.png
    alt: Multifactor devices with Google Authenticator, WebAuthn and YubiKey registration options and new scratch codes
    title: YubiKey registration from the profile, and new Google Authenticator scratch codes shown once
  - src: img_32.png
    alt: Recent sign-ins listing a failed attempt, a successful sign-in and a risky attempt with location and browser
    title: Recent sign-ins from the authentication events
  - src: img_33.png
    alt: Contact methods listing the email address and phone number for one-time codes and passwordless sign-in
    title: Contact methods for one-time codes and passwordless sign-in
  - src: img_34.png
    alt: Tokens listing an access token and a refresh token with the number of access tokens issued from it
    title: Refresh tokens next to access tokens
release:
  line: 8.1.0 development
  summary: >-
    The fourth release candidate is in progress. This page fills in as changes land on the development branch.
  facts:
    - label: Requires
      value: JDK 25
      url: ../planning/Installation-Requirements.html
    - label: Built on
      value: Spring Boot 4.2
    - value: "618"
      suffix: browser test scenarios
      url: ../../developer/Test-Process.html
    - value: "94%"
      suffix: test coverage
  upgrade:
    - type: action
      title: Theme builder
      text: is turned on by default; turn it off for production with `cas.theme-builder.enabled=false` in theme properties.
      section: Theme Builder
      area: ui
    - type: changed
      title: External identity providers
      text: keep their configured order in the dropdown as well; set `cas.login-providers.sort=true` in theme properties to sort them by name.
      section: Theme Properties
      area: ui
    - type: changed
      title: Error pages
      text: no longer offer the details button unless `cas.error.show-details=true` is set in theme properties.
      section: Theme Properties
      area: ui
    - type: action
      title: OpenID Federation relying parties
      text: registered automatically must send a signed request object or a pushed authorization request with every authorization request.
      section: OpenID Federation
      area: oidc
    - type: removed
      title: Azure CosmosDb
      text: service and ticket registries are removed; move service definitions and tickets to another registry before upgrading.
      section: Tickets & Sessions
      area: tickets
    - type: action
      title: Trusted devices
      text: include the client IP address in the device fingerprint by default, as documented; devices trusted before the upgrade are asked for multifactor authentication once more. Set `cas.authn.mfa.trusted.device-fingerprint.client-ip.enabled=false` to keep the previous fingerprint.
      section: Multifactor Authentication
      area: mfa
    - type: action
      title: Interrupt inquirers
      text: no longer receive the Spring Webflow request context; custom inquirers drop that parameter, and Groovy interrupt scripts receive the logger as their fifth argument.
      section: Authentication Interrupts
      area: ui
  spotlight:
    section: Theme Properties
    kicker: User interface
    title: Shape the CAS Screens From Theme Properties
    images: interface_images
    summary: >-
      Login, error, multifactor and consent screens gain theme properties, each previewed live in the new theme builder.
    points:
      - Identity providers move before or after the login form, in a stack, a grid or a row, with their own sort and dropdown threshold.
      - Multifactor selection, Google Authenticator, WebAuthn and consent screens choose their layout, what they show and what they recommend.
      - QR codes take their size, colors and margin from the theme, and device names can start empty, random or with the username.
      - The main content of every page can sit anywhere on a 3x3 grid, from the top left to the bottom right of the screen.
  highlights:
    - section: Theme Builder
      summary: Edit theme properties and custom.css from the CAS header, preview live and download both files for an overlay.
    - section: Theme Properties
      summary: Over twenty new theme properties for the login form, identity providers, errors, multifactor, QR codes and consent.
    - section: Account Profile
      summary: Sign-in activity, contact methods, refresh tokens, usage policy, impersonation, QR devices and password expiry in the profile.
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
and scenarios. At the moment, total number of jobs stands at approximately `628` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`.

- {: .changed data-area="project"} MariaDB integration tests and functional test scenarios use port `3307`, so they can run
  alongside the MySQL tests on `3306`.
- {: .changed data-area="project"} Browser tests now cover service matching strategies (literal, starts with, full and partial
  regular expressions), throttled ticket-granting ticket timeouts, per-service service ticket, proxy ticket and proxy-granting
  ticket expiration policies, single sign-on participation policies (never, last used time, authentication date), per-service
  security headers, encrypted, pattern matching, linked, environment and Groovy script attribute release policies, the
  anonymous username provider, multifactor bypass by remote address, request header and credential type, and the REST
  multifactor trigger (`cas.authn.mfa.triggers.rest.url`{: .cas-setting}).
- {: .changed data-area="project"} Browser tests now cover OpenID Connect front-channel logout, refused hybrid and unauthorized
  response types, encrypted user info and introspection responses, JWT refresh tokens, optional ID token encryption, token
  revocation with its cascade from refresh tokens to access tokens, WebAuthn signature counters, user verification and
  attestation, and Google Authenticator replay protection with custom code length, time step and window.
- {: .changed data-area="project"} Browser tests now sign in with [SPNEGO](../authentication/SPNEGO-Authentication.html) against a
  real Kerberos KDC: the browser presents a Kerberos ticket and is signed in without a password, with and without
  `cas.authn.spnego.principal-with-domain-name`{: .cas-setting}, and falls back to the login form when it has no ticket.
- {: .changed data-area="project"} Browser tests now cover refusal paths of the CAS protocol and its security controls:
  validation error codes and per-service supported protocols, refused and mismatched proxy callbacks and multi-hop proxy
  chains, plain gateway requests, time-based access strategies, global authentication policies (any with try-all, all
  handlers, required handler, not prevented, Groovy and REST), request parameter filtering
  (`cas.http-web-request.pattern-to-block`{: .cas-setting}), actuator endpoint `DENY`, `PERMIT` and `AUTHORITY` access,
  audit action filters with `cas.audit.engine.ignore-audit-failures`{: .cas-setting}, and CAPTCHA verification on the login form.
- {: .changed data-area="project"} Browser tests now cover OAuth refresh token rotation and expiration, rejected client
  assertions and enforced client authentication methods, PKCE on the OAuth endpoints, expired client secrets, tampered request
  objects and logout redirect validation; trusted device fingerprints, expiration and per-service bypass, multifactor bypass by
  request attributes, simple multifactor token expiration and rate limiting, YubiKey verification against a mock server, REST
  authentication account states, password history (`cas.authn.pm.history.core.enabled`{: .cas-setting}), passwordless token
  expiration, and SAML2 signed logout requests, AuthnRequest rejection, artifact resolution, attribute queries and ECP.
- {: .changed data-area="project"} Browser tests now cover OpenID Connect client attestation and JWKS rotation and revocation
  across issued tokens, SAML2 back-channel logout, offline metadata signature trust, persistent NameIDs, signing and encryption
  algorithms, Active Directory and OpenLDAP password policy warnings, X.509 revocation through CRLs, the ticket registry
  cleaner, concurrent ticket validation across nodes, ticket encryption at rest in MongoDb, masking of tickets in logs, and
  strict audit failure handling (`cas.audit.engine.ignore-audit-failures`{: .cas-setting}).

### JSpecify & NullAway
{: data-area="project"}

CAS codebase is now annotated with [JSpecify](https://jspecify.dev/) annotations to indicate nullness contracts on method parameters,
return types and fields. We will gradually extend the coverage of such annotations across the entire codebase in future releases
and will integrate the Gradle build tool with tools such as [NullAway](https://github.com/uber/NullAway) to prevent nullness contract violations
during compile time.

### Gradle 9.9
{: .changed data-area="project"}

CAS is now built with Gradle `9.9` and the build process has been updated to use the
latest Gradle features and capabilities.

### Spring Boot 4.2
{: .changed data-area="project"}

CAS is now built on top of Spring Boot `4.2.x`. This is an in-progress ongoing minor platform upgrade that
affects almost all aspects of the codebase including many of the third-party core libraries used by CAS
as well as some CAS functionality.

Please refer to the [Spring Boot Wiki](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.2-Release-Notes)
for more information on the changes and updates in this release.


### Theme Builder
{: .new data-area="ui"}

The CAS header has a new palette button, next to the notifications button, that opens a
[theme builder](../ux/User-Interface-Customization-CSSJS.html#theme-builder). It lists every theme property with its
description and current value, and edits the colors, shapes, font, header, footer and background that `custom.css` usually
controls. Changes are previewed live on the page without a reload, including elements that a theme property would turn off,
and the result downloads as a `cas-theme-custom.properties` file and a `custom.css` file ready for an overlay. When the builder
opens on a server that already serves such a `custom.css`, it reads its own block back so the file can keep evolving.
Nothing is saved on the server; the builder is turned on by default and is controlled by the `cas.theme-builder.enabled`
theme property.

### Palantir Search, User Lookup, Service History, Interrupts & Drafts
{: .new data-area="ui"}

[Palantir](../installation/Admin-Dashboard.html) has a search box, opened from the navigation bar or with `Ctrl+K`
(`⌘K` on macOS) or `/`, that goes to any page or section, finds a registered application by name, identifier or service id,
and runs common actions without leaving the keyboard. A new *Users* page looks up one user and gathers in one place their
single sign-on sessions, multifactor and trusted devices, consent decisions, resolved attributes, impersonation accounts,
passwordless account, password history, throttled login attempts, the interrupt they would see at login and recent audit events, each with its usual actions
such as ending a session, removing a device or releasing a throttled attempt. Only sources with something recorded for
the user are shown, and sources whose actuator endpoints are not enabled are left out. The change history of an application
compares any recorded version side by side with the current definition or the version before it, and restores the
selected version from the same dialog. Under *Interrupts*, the authentication section shows which
[interrupt](../webflow/Webflow-Customization-Interrupt.html) a user would see at login and what each interrupt
source answered.

Applications can be duplicated into the wizard as new applications, and the wizard and the definition editor ask before
closing with unsaved changes, keeping a draft in the browser that can be resumed later. A keyboard shortcut overlay opens
with `?`, and tables and dialogs are easier to use with a keyboard and a screen reader: tables are named, rows with actions
take focus and open their actions with `Shift+F10`, modal dialogs announce themselves, and icon-only buttons are named.

### Theme Properties
{: .new data-area="ui"}

Several screens can now be adjusted through [theme properties](../ux/User-Interface-Customization-Themes-Static.html),
so they can differ per theme, per application and per tenant, and most of them preview live in the theme builder:

| Screen                      | Theme properties                                                                                     |
|-----------------------------|------------------------------------------------------------------------------------------------------|
| Every page                  | `cas.main-content.position` (`topleft` … `bottomright`, default `topcenter`)                         |
| Login form                  | `cas.login-form.password-reveal.enabled`, `cas.login-form.caps-lock-warning.enabled`                 |
| Login card                  | `cas.login-side-panel.position` (`start`, `end`), `cas.login-providers.position` (`after`, `before`) |
| External identity providers | `cas.login-providers.layout` (`stacked`, `grid`, `inline`), `.select-threshold`, `.sort`             |
| Password management links   | `cas.pm-links.forgot-password.url`                                                                   |
| Error pages                 | `cas.error.show-message`, `cas.error.show-details`                                                   |
| WebAuthn                    | `cas.webauthn.authentication.show-device-details`, `cas.webauthn.registration.recommended`           |
| Google Authenticator        | `cas.gauth.registration.show-secret-key`, `.scratch-codes` (`chips`, `list`), `.show-print`          |
| QR codes                    | `cas.qr-code.size`, `cas.qr-code.foreground`, `cas.qr-code.background`, `cas.qr-code.margin`         |
| Multifactor authentication  | `cas.mfa.selection.layout` (`list`, `grid`, `cards`), `cas.mfa.device-name.prefill`                  |
| Attribute consent           | `cas.consent.show-attribute-values`, `cas.consent.layout` (`table`, `list`)                          |

The main content position places the content of every page in a 3x3 grid on the screen: top, middle or bottom, and
left, center or right, as in `middleleft` or `bottomright`. Pages taller than the screen still start at the top and scroll,
and Palantir keeps its own layout.

Defaults keep the screens as they were, with two exceptions: external identity providers keep their configured order in
the dropdown that replaces the buttons once there are more than ten, and the error page offers its details button only
when `cas.error.show-details` is turned on, in which case it now shows the stack trace the server includes rather than an
empty box. QR codes for Google Authenticator, WebAuthn and QR code login use the QR code properties of the theme that
applies to the request; their size stays between `100` and `1000` pixels, and a foreground that is not darker than the
background falls back to black on white so the code remains scannable. Device and account names suggested when
registering trusted devices, Google Authenticator, YubiKey or WebAuthn devices can start empty, random or with the
username.

- {: .fixed data-area="ui"} [Themes](../ux/User-Interface-Customization-Themes.html) other than the default one, including themes chosen per
  application or per tenant, now inherit `cas-theme-custom.properties` before falling back to `cas-theme-default.properties`.
  Theme files found under the template prefixes now apply regardless of the browser language; they used to be ignored for
  languages other than English, and the aggregate theme source only honored them for an exact `en` request.

### Account Profile
{: .new data-area="ui"}

The [account profile](../registration/Account-Management-Overview.html) shows more of what CAS knows about the user:

| Section                    | What it shows                                                                                          |
|----------------------------|--------------------------------------------------------------------------------------------------------|
| Overview                   | Days left before the password expires, from the password policy warning, with a shortcut to change it. |
| Sign-ins                   | Successful, failed and risky sign-in attempts of the last 30 days from the authentication events.      |
| Contact                    | Email addresses and phone numbers for one-time codes and for passwordless sign-in.                     |
| Tokens                     | Refresh tokens (offline access) next to access tokens; revoking one revokes its access tokens.         |
| Usage policy               | Whether the acceptable usage policy is accepted, and the policy terms.                                 |
| Impersonation              | Accounts the user may impersonate, and on whose behalf an impersonation session runs.                  |
| QR devices                 | Devices authorized for QR code login, which the user can remove.                                       |
| MFA devices                | YubiKey registration, new Google Authenticator scratch codes, renaming WebAuthn passkeys.              |

YubiKey registration is offered when the user has no device yet or when
`cas.authn.mfa.yubikey.multiple-device-registration-enabled`{: .cas-setting} is turned on, and an email address for
one-time codes can be added when `cas.authn.mfa.simple.mail.accepted-email-pattern`{: .cas-setting} is set.

{% include imagegallery.html images=page.account_profile_images gallery_id="account-profile" %}

- {: .fixed data-area="ui"} The account profile only ends single sign-on sessions, revokes OpenID Connect tokens and removes
  trusted devices that belong to the signed-in user; it used to act on any session, token or device whose identifier was submitted.
- {: .fixed data-area="ui"} The account profile and account management pages follow the shared card, text and border colors of the
  theme, and the light Palantir theme follows the theme's primary and secondary colors.

### Service Registries and Access Strategies
{: .changed data-area="services"}

- {: .changed data-area="services"} The [JPA service registry](../services/JPA-Service-Management.html) reads only the
  identifier and body of each definition, keeps no entities for reads, and no longer deserializes a definition on reload
  when its stored body is unchanged. Deleting a service is a single statement, new services are no longer looked up before
  they are stored, bulk saves flush every `batch-size`{: .cas-setting} definitions, and the service id and name columns
  are indexed.
- {: .fixed data-area="services"} The [JPA service registry](../services/JPA-Service-Management.html) keeps the identifier
  of a service that carries one, such as an imported definition; a database sequence used to replace it. New services
  get a random identifier, as with other service registries.
- {: .fixed data-area="services"} A service whose single sign-on participation policy does not set
  `createCookieOnRenewedAuthentication` (for example the last-used-time and authentication-date policies) no longer
  fails the login flow; such a policy behaves like one that leaves the setting undefined, and the single sign-on
  session is kept when the service does not take part in it.
- {: .changed data-area="services"} Attribute-based activation criteria for [access strategies](../services/Service-Access-Strategy-ABAC-Activation.html)
  and [attribute release policies](../integration/Attribute-Release-Policy-Activation.html) match required values against
  the entire attribute value, like required and rejected attributes; `admin` no longer activates a policy for `sysadmin`.
  Use `.*admin.*` to match part of a value.
- {: .fixed data-area="services"} [Service replication](../services/Configuring-Service-Replication.html) no longer broadcasts
  every service definition from every node on each reload: a loaded definition is sent only when it differs from the
  replicated copy, and the replicated copies are kept across reloads instead of being cleared, so nodes started later still
  receive every definition. A replicated definition that differs from the local one replaces it instead of being listed twice.
- {: .fixed data-area="services"} When several service registries are chained, a service URL that is not cached is matched
  against the best candidate from every registry by evaluation order, instead of the first registry's match.
- {: .fixed data-area="services"} The [REST service registry](../services/REST-Service-Management.html) no longer publishes a
  loaded event for every service when services are counted or looked up, and counting services while the registry is
  unreachable logs the failure and reports zero instead of failing (for example at startup).
- {: .changed data-area="services"} The [REST access strategy](../services/Service-Access-Strategy-REST.html) calls its endpoint
  once per access decision, without retries, and waits at most 5 seconds for the response; an endpoint that does not answer
  in time denies access. It used to retry up to three times on errors and timeouts, holding the login for the duration.
- {: .fixed data-area="services"} Groovy single sign-on participation policies and Groovy activation criteria for access
  strategies and attribute release resolve their script once, and again only when the script changes or expires from the
  script cache, instead of resolving its location and expressions on every evaluation.
- {: .fixed data-area="services"} [Service replication](../services/Configuring-Service-Replication.html) through Apache Kafka
  keeps one entry per service definition; an updated definition used to be stored next to its earlier versions.
- {: .fixed data-area="services"} A service definition deleted on one node stays deleted across the cluster with
  [service replication](../services/Configuring-Service-Replication.html): the deletion marker is kept instead of being
  removed by the first node that applies it, so nodes that still have the definition remove it rather than sending it
  back. Deletions now also replicate through Apache Kafka, whose nodes used to drop the marker on arrival.
- {: .fixed data-area="services"} With [service replication](../services/Configuring-Service-Replication.html) through Apache
  Kafka, a node reads the replication topic from the beginning when it starts, so it receives the service definitions that
  other nodes loaded before it joined; it used to rely on every node sending all definitions again on every reload.
- {: .fixed data-area="services"} With the services cache turned off (`cas.service-registry.cache.cache-size`{: .cas-setting}
  set to `0`), listing services no longer re-indexes every service it reads, which also made the next service lookup
  read the whole service registry again.
- {: .removed data-area="services"} The deprecated Azure CosmosDb service registry is removed, along with its settings.
  Definitions stored there should be exported and imported into another [service registry](../services/Service-Management.html).

### Tickets & Sessions
{: .changed data-area="tickets"}

- {: .fixed data-area="protocols"} [CAS 2.0 proxy validation](../protocol/CAS-Protocol-V2-Specification.html) responses list
  each proxy of a chain once; a ticket obtained through several proxies listed each of them as many times as there were proxies.
- {: .action data-area="tickets"} New deployments of CAS, and upgrades to a new CAS version, should always start with a
  clean [JPA ticket registry](../ticketing/JPA-Ticket-Registry.html) table: drop the existing table and let CAS create it.
  Schema updates do not change the type or length of an existing column, and this version changes several of them.
- {: .changed data-area="tickets"} With PostgreSQL, the [JPA ticket registry](../ticketing/JPA-Ticket-Registry.html)
  stores ticket attributes as `jsonb`, and session lookups by attribute, as in delegated OpenID Connect and SAML2 logout,
  can be answered by a GIN index instead of reading every session. The index is not created by CAS: the administrator
  must create it once after the ticket table is created, with the statement on that page. A table created by an earlier
  version keeps a `json` column, on which those lookups fail; start with a clean table or convert the column.
- {: .changed data-area="tickets"} The [JPA ticket registry](../ticketing/JPA-Ticket-Registry.html) stores when each
  ticket expires, including its idle timeout, and indexes it, and the [ticket registry cleaner](../ticketing/Ticket-Registry-Cleaner.html)
  asks only for tickets whose time has passed, with a full scan at startup and hourly; it used to read and decode
  every ticket on every run. With MySQL, the registry streams scans through a server-side cursor (`useCursorFetch`)
  and leaves single-ticket lookups as they were; the defaults can be overridden with
  `cas.ticket.registry.jpa.properties`{: .cas-setting}.
- {: .fixed data-area="tickets"} Looking up sessions by attribute in the [JPA ticket registry](../ticketing/JPA-Ticket-Registry.html)
  no longer fails on MySQL when the caller runs outside a transaction, as delegated OpenID Connect and SAML2 logout do.
  Reading sessions and tickets without a transaction no longer fails on PostgreSQL or MySQL either, as the
  [single sign-on sessions](../authentication/Configuring-SSO.html#actuator-endpoints) and
  [ticket registry](../ticketing/Configuring-Ticketing-Components.html#actuator-endpoints) endpoints, the MCP server
  tools, JMX, OAuth token management and deferred verifiable credential issuance do; outside a transaction the registry
  reads the matching tickets in one of its own. Listing every ticket over JMX or every OAuth token through the
  management endpoint reads the registry in batches instead of loading all of its tickets first, and the JMX ticket
  count is a single count query rather than a read of every ticket.
  Updating a ticket encrypts it once instead of twice when ticket encryption is turned on.
- {: .changed data-area="tickets"} The [JPA ticket registry](../ticketing/JPA-Ticket-Registry.html) updates a ticket with a
  single statement instead of reading the row back first, no longer deletes service tickets twice when a session ends,
  pages ticket listings in the query instead of reading and skipping the rows before the page, and reads tickets without
  keeping copies of them for change tracking. Session queries by attribute bind the attribute values as parameters.
- {: .action data-area="tickets"} With MySQL, the [JPA ticket registry](../ticketing/JPA-Ticket-Registry.html) declares shorter
  `type`, `principal_id` and `parent_id` columns so that its indexes fit MySQL's key length limit; until now MySQL refused
  every one of them. Existing tables need the statements listed on that page. The index on the ticket type alone is gone
  on all databases.
- {: .fixed data-area="tickets"} With the [stateless ticket registry](../ticketing/Stateless-Ticket-Registry.html),
  device codes keep their requested scopes, so tokens issued for an approved device code carry them; the scopes used
  to be dropped.
- {: .action data-area="tickets"} The compact form of tickets kept by the
  [stateless ticket registry](../ticketing/Stateless-Ticket-Registry.html) now carries the resources of OAuth codes,
  access tokens, refresh tokens, device codes and pushed authorization requests, and the audience of exchanged access
  tokens; its format version changes: tickets and tokens issued before the upgrade can no longer be read, so users sign in
  again and clients obtain new tokens.
- {: .removed data-area="tickets"} The deprecated Azure CosmosDb ticket registry is removed, along with its settings.
  Deployments that still use it should switch to another [ticket registry](../ticketing/Configuring-Ticketing-Components.html).
- {: .changed data-area="tickets"} A [logout](../installation/Logout-Single-Signout.html) request now looks up its
  `service` parameter once for all the steps of the logout flow, where every step, and every single logout protocol in
  use, used to look it up again.
  [Redirecting logout to the service](../installation/Logout-Single-Signout.html#redirecting-logout-to-service) looks it
  up once more, and only when `cas.logout.follow-service-redirects`{: .cas-setting} is turned on. Logout requests that
  name unknown services, such as those sent by crawlers, cost far less.

### OAuth & OpenID Connect
{: .changed data-area="oidc"}

- {: .fixed data-area="oidc"} [Front-channel logout](../authentication/OIDC-Authentication-Logout.html) requests now carry the same
  `sid` as the ID token and the back-channel logout token; the session identifier used to be computed differently, so
  applications could not match it to the session they had recorded.
- {: .fixed data-area="oidc"} Access tokens issued as JWTs can be revoked through the revocation endpoint, and refresh tokens issued as
  JWTs can be redeemed with the `refresh_token` grant; both used to be looked up in the ticket registry without being decoded
  first, so revocation silently did nothing and the refresh request was refused.
- {: .fixed data-area="oidc"} JWT access tokens are no longer signed with keys from the application's own `jwks` that
  are marked for encryption (`"use": "enc"`); when the application publishes only encryption keys, CAS signs with its own
  keys. `jwtAccessTokenSigningAlg` is honored as documented: CAS picks a signing key whose type fits the algorithm,
  and the key's own algorithm only applies when none is configured or the configured one does not fit the key.
  Signatures made with an EC (`ES*`) or RSA-PSS (`PS*`) key are randomized, so such a token is a different string each
  time it is encoded; the ID token's `at_hash` is computed over the access token the response carries.
- {: .new data-area="oidc"} [OAuth relying parties](../authentication/OAuth-Authentication-Clients.html#resource-indicators)
  may request access tokens for specific resources with the RFC 8707 `resource` parameter, limited to the resources listed
  in the service definition's `allowedResources`, with authorization and pushed authorization requests, device
  authorization and CIBA requests, and the password, client credentials, JWT bearer and pre-authorized code grants.
  Other resources are refused with `invalid_target`. Requested resources are added to the access token audience. Redeeming a
  code or refresh token may narrow the access token to some of the granted resources, while the refresh token keeps them all.
  A [token exchange](../authentication/OAuth-ProtocolFlow-TokenExchange.html) issues tokens for its own audience or resource
  only, without the resources of the exchanged token. In request objects, `resource` may be a single value or a list.
  The consent screen lists the requested resources, and a remembered consent decision covers only the resources it was
  given for. Resources can be defined in the Palantir service wizard.
- {: .changed data-area="oidc"} The `allowedResources` of a service definition and of its
  [token exchange policy](../authentication/OAuth-ProtocolFlow-TokenExchange.html) are regular expressions with the same
  rule: every requested resource must match one of them in full, case-sensitively. The token exchange policy used to accept
  a request when any one of its resources was found by any pattern. A token exchange is also checked against the service's
  own `allowedResources` when the service defines them.
- {: .new data-area="oidc"} [Resources every client may request](../authentication/OAuth-Authentication-Clients.html#resources-for-every-client)
  can be listed with `cas.authn.oauth.core.allowed-resources`{: .cas-setting}, in addition to the `allowedResources` of each
  service. CAS allows its own [MCP server](../installation/Admin-MCP-Server.html) endpoint by default, so that clients
  registered dynamically, which cannot list resources themselves, may request access tokens for it. `checkOidcClientResource`
  reports whether the client or this setting allowed a resource.
- {: .new data-area="oidc"} OpenID Connect clients may identify themselves with
  [client ID metadata documents](../authentication/OIDC-Authentication-Client-ID-Metadata-Documents.html), as MCP clients do:
  the client id is an HTTPS URL whose JSON document CAS fetches, checks and registers, refreshes when its cache lifetime ends
  and removes once unused. Documents are only fetched from public addresses, without redirects and up to 5 KB, and these
  clients always see a consent screen that names the host of the client id. Turned off by default; enable it with
  `cas.authn.oidc.registration.client-id-metadata-document.enabled=true`{: .cas-setting}.
- {: .action data-area="oidc"} The keys of an OpenID Connect application defined inline as JSON, as registered through
  [OpenID Federation](../authentication/OIDC-Authentication-Federation.html#automatic-registration) or dynamic client
  registration, are taken as they are; only keystore locations support
  [Spring expressions](../configuration/Configuration-Spring-Expressions.html). OpenID Federation relying parties
  that publish their keys at a URL other than `https` are not registered.
- {: .fixed data-area="oidc"} OpenID Connect applications whose keys change no longer keep being checked against the keys
  cached for their earlier definition until that cache expires.
- {: .action data-area="oidc"} Unsigned JWTs, whose algorithm is `none`, are no longer supported: CAS neither issues nor
  accepts them. Request objects, including those encrypted without being signed, are refused with `invalid_request_object`;
  subject and actor tokens of a token exchange, risk verification tokens and QR authentication tokens must be signed.
  JWT [access and refresh tokens](../authentication/OAuth-Authentication-JWT-AccessTokens.html),
  [JWT service and ticket-granting tickets](../installation/Configure-ServiceTicket-JWT.html) and [JWT authorization
  responses](../authentication/OIDC-Authentication-JARM.html) that would be neither signed nor encrypted are refused
  rather than issued, so deployments that turn off both must turn on signing.
  [ID tokens](../authentication/OIDC-Authentication-Clients.html) are always signed: the `signIdToken` field of OpenID
  Connect applications is removed, from the Palantir service editor and MCP client summaries too, and ignored in existing
  definitions, so applications that turned it off now get signed ID tokens; applications that ask for `none` are
  refused, and dynamic registration refuses `none` with `invalid_client_metadata`.
  `none` as the user info signing algorithm means a plain JSON response, and user info that is only encrypted is signed
  first; [JWT introspection responses](../authentication/OAuth-Authentication-TokenIntrospection.html) are always signed,
  with the default algorithm when none or `none` is set; `none` as an encryption algorithm means no encryption;
  [UMA requesting party tokens](../protocol/OAuth-UMA-Protocol.html#requesting-party-tokens) are signed with their
  keystore. Discovery no longer advertises `none` for ID tokens, user info, introspection, request objects or
  backchannel authentication requests, even when configured, for example with
  `cas.authn.oidc.discovery.id-token-signing-alg-values-supported`{: .cas-setting}. The
  [CAS command-line shell](../installation/Configuring-Commandline-Shell.html) only generates signed JWTs: its JWT
  commands refuse to run without a signing algorithm and secret size, or without a keystore.
- {: .action data-area="oidc"} A request object whose `client_id` differs from the `client_id` parameter of the request is
  refused with `invalid_request_object`, as [OpenID Connect](https://openid.net/specs/openid-connect-core-1_0.html#RequestObject)
  requires, instead of being used for the client it names.
- {: .fixed data-area="oidc"} [DPoP](../authentication/OIDC-Authentication-DPoP.html) proofs are remembered as used for as
  long as their `iat` keeps them acceptable, not only for the lifetime of transient session tickets.
- {: .changed data-area="oidc"} [Encrypted request objects](../authentication/OIDC-Authentication-JWKS.html#encrypted-request-objects)
  now work: CAS decrypts them with the encryption keys it publishes in its JWKS (any rotation state, chosen by `kid`) and
  verifies the nested request object with the client's keys; before, it tried the client's own keys and every encrypted
  request object failed, OpenID Federation relying parties included. Discovery no longer advertises algorithms CAS cannot
  decrypt with: `cas.authn.oidc.discovery.request-object-encryption-alg-values-supported`{: .cas-setting} now defaults to
  `RSA-OAEP`, `RSA-OAEP-256` and the `ECDH-ES` family, without `RSA1_5` and symmetric key wrapping. `RSA1_5` is never
  accepted nor advertised, even when listed, and the encryption keys are read from the keystore once per JWKS cache period
  instead of for every request object, and again as soon as the node sees the keystore change or rotates or revokes its
  keys;
  signing keys are never used to decrypt.
- {: .fixed data-area="oidc"} Elliptic curve keys that CAS generates for its
  [OpenID Connect keystore](../authentication/OIDC-Authentication-JWKS.html#key-types) carry the signing algorithm of their
  curve. P-256 keys used to be tagged `ES512`, which the JWKS published and which made signing with them fail, and a key
  size of `521` produced P-256 keys instead of P-521. Existing keystores are corrected as they are loaded, without new
  keys, encryption keys no longer carry a signing algorithm, and a configured signing algorithm only selects an elliptic
  curve key of the matching curve.
- {: .action data-area="oidc"} An authorization request whose request object cannot be decrypted or its signature verified
  is refused with `invalid_request_object`, as OpenID Connect requires. CAS used to ignore such a request object and carry
  on with the query parameters alone. Request objects signed with `HS256`, `HS384` or `HS512` are
  [verified with the client secret](../authentication/OIDC-Authentication-JWKS-Clients.html#request-objects) of the
  application, which is checked for expiry and decoded as when the application authenticates with it; one that names
  no registered client or verifies with none of its secrets is refused the same way instead of failing with a server
  error. [Backchannel authentication](../authentication/OIDC-Authentication-CIBA.html) requests whose request object
  cannot be verified, or is for another client, are refused with `invalid_request` instead of being answered from their
  other parameters. [Signed backchannel authentication requests](../authentication/OIDC-Authentication-CIBA.html#signed-authentication-requests)
  must be signed with an asymmetric algorithm, name the client as their issuer, be addressed to the CAS issuer and carry
  `exp`, `iat`, `nbf` and `jti`, as CIBA requires; they may not be issued in the future, may be valid for 60 minutes at
  most and are used once. They are verified with the keys of the client that authenticated the request, also when the
  request names no `client_id`, and refused when CAS does not support the request parameter.
- {: .action data-area="oidc"} [Dynamically registered](../authentication/OIDC-Authentication-Dynamic-Registration.html)
  OpenID Connect clients, and [OpenID Federation](../authentication/OIDC-Authentication-Federation.html#automatic-registration)
  relying parties registered automatically, have their redirect URIs matched exactly, case included. Dynamic registration used to join
  them into a pattern where `.` matched any character, so look-alike hosts matched, and `?` was not literal, so a redirect
  URI with a query never matched; automatic registration joined them so that relying parties with two or more redirect
  URIs never matched. Clients registered dynamically before keep their pattern until they update their registration.
- {: .action data-area="oidc"} [Pairwise subject identifiers](../authentication/OIDC-Authentication-Claims-Pairwise.html) of
  clients that CAS registered, dynamically or through OpenID Federation, without a sector identifier URI use the host of
  their first redirect URI, as before. When their redirect URIs name no host, as with native applications, each client is
  now its own sector; such clients used to share a single sector, so their users receive new identifiers. OpenID
  Federation relying parties that ask for pairwise identifiers now receive them; they used to receive the plain user id.
  Clients whose definition carries no salt of their own, which includes clients that CAS registers, calculate them with
  `cas.authn.oidc.core.pairwise-subject-salt`{: .cas-setting}, read when they are used and never written into client
  definitions; without it, federation relying parties that ask for them are not registered, and clients registered
  dynamically are each given a random salt kept with their registration, where they used to get one that changed with
  every restart. Clients registered dynamically before keep such a changing salt until the setting is defined or they
  update their registration. Clients that ask for pairwise identifiers with redirect URIs on several hosts must register
  a sector identifier URI, which must be an `https` URL that can be fetched and lists all of their redirect URIs; the
  sector identifier URI of a federation relying party is fetched and checked the same way. For clients that CAS
  registers, only `http` and `https` redirect URIs on hosts other than loopback ones give a sector host; any other
  redirect URI, such as the private-use scheme of a native application or a loopback redirect URI, puts the client in a
  sector of its own, its client id, whatever it names, so clients registered by CAS with such redirect URIs receive new
  identifiers, and mixing both kinds needs a sector identifier URI. Clients registered dynamically before keep the sector
  they had until they next update their registration, which then gives them the sector described here, and so new
  identifiers, even when their redirect URIs stay the same. Clients defined by hand keep the host read from their
  service id; those whose service id names no host, such as `^https?://app.example.org/.*`, in which the `?` ends what
  is read as the host, or whose sector identifier URI names no host, used to share one sector, and are now each a sector
  of their own and receive new identifiers. A service id whose host is a pattern, such as `^https://.*`, gives that
  pattern as its sector; without a salt of their own, such clients used to get a salt that changed with every restart,
  and with the setting above they now share identifiers. Sectors
  are read the same way when clients register and when identifiers are calculated, and an `http` or `https` redirect URI
  whose host cannot be read is refused. Sector identifier documents of dynamically registered clients are fetched from
  public addresses only, without redirects or retries, within a limited time and size, and must be valid JSON, whatever
  type they are served as. A client configuration update that is refused now leaves the client as it was and is
  answered with `invalid_client_metadata`, and an update keeps the generator of pairwise identifiers the client has.
- {: .fixed data-area="oidc"} [Pushed authorization requests](../authentication/OIDC-Authentication-PAR.html) are
  accepted again when [verifiable credentials](../authentication/OIDC-Authentication-Verifiable-Credentials.html) are
  turned on; CAS answered them with `401` as if they needed a single sign-on session. A wallet that pushes its
  authorization request now receives the credentials named by the `authorization_details` it pushed; CAS issued the
  authorization code without them. Authorization details that cannot be granted are refused with
  `invalid_authorization_details` when they are pushed, instead of after the redirect to the authorization endpoint.
- {: .changed data-area="oidc"} [Pushed authorization requests](../authentication/OIDC-Authentication-PAR.html) that
  fail validation are answered with `400` and the actual error, such as `invalid_request_object` or
  `unsupported_response_type`, as RFC 9126 requires, instead of `403` with a generic `invalid_request`. When pushed authorization
  requests are required, an authorization request without one is refused at every path of the authorization endpoint,
  including encoded ones and the OAuth path under the OpenID Connect base path. Endpoints that require client or token
  authentication now recognize their paths with path parameters, such as `;x=y`, too.
- {: .changed data-area="oidc"} The `aud` of a token introspection response is the audience of the JWT access token: the
  service `audience` (or the client ID) and the token's resources, or the audience requested by a token exchange. It used
  to be the service URL the token was issued for. Custom introspection response generators now receive the registered
  service of the client.
- {: .action data-area="oidc"} OAuth/OpenID Connect clients are held to what they registered. They authenticate with their
  registered method, and only as the client the request names, at the token endpoint and also for
  [pushed authorization](../authentication/OIDC-Authentication-PAR.html),
  [backchannel authentication](../authentication/OIDC-Authentication-CIBA.html), revocation and
  [verifiable credentials](../authentication/OIDC-Authentication-Verifiable-Credentials.html); an access token no longer
  authenticates a client. Clients that use [mutual TLS](../authentication/OIDC-Authentication-AccessToken-AuthMethods.html#mutual-tls-client-authentication)
  must register the subject DN or a subject alternative name of their certificate, unless their client id is exactly the
  certificate's [SPIFFE](../authentication/OIDC-Authentication-AccessToken-AuthMethods.html#spiffe) ID; a subject DN written
  as a regular expression must now match the whole subject, as `CN=([^,]+),O=Example,C=US` does. User profiles keep
  every value of a multi-valued attribute, and the `cas.authn.oauth.csrf-cookie` settings are removed since CAS no
  longer issues that cookie.
- {: .action data-area="oidc"} The page where users approve a [device user code](../authentication/OAuth-ProtocolFlow-DeviceAuthorization.html)
  now carries an approval token tied to the single sign-on session, and an approval without it is refused, so another site
  can no longer make a signed-in browser approve a code. Customized `protocol/oauth/deviceCodeApproval.html` templates must
  add the hidden `approvaltoken` input of the default template to their form. The error messages of the page are now
  defined, under `cas.oauth.device.error.*`.
- {: .fixed data-area="oidc"} OAuth/OpenID Connect follow the specifications more closely.
  [Sender-constrained](../authentication/OIDC-Authentication-DPoP.html) access tokens always require their proof,
  `prompt=login` and `max_age=0` request parameters always lead to a fresh login and `prompt=none` to `login_required`,
  authorization requests
  keep all their parameters through login and [consent](../authentication/OIDC-Authentication-Clients-ScopeApproval.html),
  and refused requests receive the error and challenge the specifications call for instead of a server error. OpenID Connect
  JWT access tokens no longer fail to sign now and then under load.
- {: .fixed data-area="oidc"} [Access strategies](../services/Configuring-Service-Access-Strategy.html) of OAuth/OpenID Connect
  applications see the attributes of the authentication, such as `clientIpAddress`, at the authorization endpoint and when a
  code issued from a single sign-on session is exchanged for tokens, as they do for CAS applications. They used to see only the attributes of the principal there,
  so a strategy that relied on an authentication attribute refused access.
- {: .changed data-area="oidc"} OAuth/OpenID Connect endpoints no longer send the `X-XSS-Protection` header, which browsers
  ignore. Only the `X-Requested-With: XMLHttpRequest` header now marks a script request that is answered with `401` instead
  of a redirect to the login; the `is_ajax_request` header and parameter are no longer recognized.
- {: .fixed data-area="oidc"} [OpenID Connect logout](../authentication/OIDC-Authentication-Logout.html) no longer
  follows any `post_logout_redirect_uri` sent along with a `client_id`. The URI must match the client of the ID token
  hint, one of its logout URLs, or a registered service; otherwise the session ends without a redirect. An ID token
  hint that is not a JWT, names no registered client or whose signature cannot be verified is answered with `400`
  instead of a server error.

### OpenID Federation
{: .changed data-area="oidc"}

- {: .action data-area="oidc"} [OpenID Federation](../authentication/OIDC-Authentication-Federation.html#automatic-registration)
  relying parties registered automatically must prove they control their keys. Every authorization request, on the
  OpenID Connect or the OAuth authorization endpoint, wherever they answer and however their path is encoded, carries a signed request object addressed to the CAS issuer, valid
  for 10 minutes at most and used once, or refers to a pushed authorization request for which the relying party
  authenticated itself; the proof is for the client the request is actually for, taken from the request object when the
  request has no `client_id`; at the pushed authorization endpoint, a client assertion must be addressed to the CAS
  issuer only. Requests that fail are refused without a redirect. Entity identifiers must be `https`
  URLs, relying parties authenticate with `private_key_jwt` or `tls_client_auth`, client assertions may be addressed to
  the issuer, and relying parties are registered before client authentication, so a pushed authorization request can be
  the first contact. The end-to-end federation scenario runs against the go-oidfed OFFA relying party.
- {: .changed data-area="oidc"} An [OpenID Federation](../authentication/OIDC-Authentication-Federation.html#trust-anchors)
  OpenID provider trusts the trust anchors defined with `cas.authn.oidc.federation.trust-anchors`{: .cas-setting}, with
  their keys obtained out of band, instead of treating its authority hints as trust anchors and taking their keys from
  whoever answers; without trust anchors, the authority hints are still used, with a warning. Trust chains are resolved
  within bounds, `cas.authn.oidc.federation.max-authority-hints`{: .cas-setting} authority hints per entity and
  `cas.authn.oidc.federation.max-path-length`{: .cas-setting} intermediates, and an entity that could not be resolved is
  not resolved again for `cas.authn.oidc.federation.unresolved-entity-retention`{: .cas-setting}, during which a
  registration that failed to refresh is kept. Each resolution sends at most
  `cas.authn.oidc.federation.max-requests-per-resolution`{: .cas-setting} requests (50), reusing statements it already
  fetched, within `cas.authn.oidc.federation.resolution-timeout`{: .cas-setting} (10 seconds) all requests together, with
  responses limited in size, the TLS settings of CAS and no redirects; at most
  `cas.authn.oidc.federation.max-concurrent-resolutions`{: .cas-setting} resolutions (10) run at once. The time limit
  covers each whole request, host name lookup and TLS handshake included, and requests are only sent over `https` and
  never to link-local, unspecified or multicast addresses, IPv6 forms included, unless a proxy of the JVM resolves
  them: an HTTP proxy, or a SOCKS proxy for host names CAS cannot resolve. A trust anchor that cannot be reached, answer in time or answers with a server error, or whose historical keys
  cannot be read, a sector identifier
  document that cannot be fetched for now, and a resolution that runs out of time or requests or finds no room among the
  running requests make the resolution unavailable rather than failed, so the relying party is not remembered as
  unresolved; a relying party that is not registered yet is then answered with
  `temporarily_unavailable` instead of as an unknown client. Using a trust chain the relying party sent and resolving its
  trust chains share one time limit and request budget. The constraints of subordinate statements (maximum path length, naming
  constraints, allowed entity types) now apply to the trust chains of relying parties and of their trust mark issuers;
  they used to be ignored. A maximum path length must be a whole number that is not negative.
- {: .action data-area="oidc"} [Application keys](../authentication/OIDC-Authentication-Clients.html) published at a URL
  are no longer requested at link-local, unspecified or multicast addresses, and redirects are not followed, so keys
  published behind a redirect must be published at their final location. The
  [OpenID Federation](../authentication/OIDC-Authentication-Federation.html#key-rollover) keystore is replaced at once
  when it changes, so it is never read half written, keeping its permissions and any symbolic link to it. A keystore
  that holds no keys is given new ones, and one that cannot be read stops CAS from starting, where CAS used to sign with
  keys of its own on each node and restart, kept nowhere.
- {: .changed data-area="oidc"} A CAS [OpenID Federation](../authentication/OIDC-Authentication-Federation.html#subordinates)
  trust anchor or intermediate issues subordinate statements that carry only the subordinate's keys, its metadata
  without the fetch, list and trust mark status endpoints, and the `constraints` of its definition rather than a fixed
  `max_path_length`; statements are valid to the second, so lifetimes under a day work. The fetch endpoint answers
  unknown subordinates with `404 not_found` and other failures, such as malformed metadata policies in a subordinate
  definition, with JSON errors, the listing filters by `entity_type`,
  matching the entity types a subordinate definition lists or else those of its metadata, and by `intermediate`, and an issuer defined with a trailing slash is used without it as the entity identifier.
- {: .new data-area="oidc"} The [OpenID Federation](../authentication/OIDC-Authentication-Federation.html#entity-configuration)
  entity configuration of a CAS OpenID provider that issues verifiable credentials also describes it as
  `openid_credential_issuer` and `oauth_authorization_server`, so federation-based wallets can trust the credential
  issuer through its trust chain. Trust marks issued to CAS, defined with `cas.authn.oidc.federation.trust-marks`{: .cas-setting},
  are published as `trust_marks`. Discovery advertises `client_registration_types_supported` only when CAS is a federation
  OpenID provider, and the delegated relying party entity statement endpoint answers `404` for an unknown client instead
  of an empty page.
- {: .new data-area="oidc"} [OpenID Federation metadata policies](../authentication/OIDC-Authentication-Federation.html#metadata-policies):
  a CAS trust anchor or intermediate publishes the metadata policies of its subordinates, along with the operators they
  mark critical, and no longer requires metadata in subordinate definitions. A CAS OpenID provider resolves the metadata
  of relying parties as the specification describes: the metadata their superior states about them comes first,
  operators beyond the standard ones are ignored unless marked critical, `scope` is handled as a list, and superiors can
  only shape entity types the relying party publishes itself. Relying parties whose metadata names no
  `application_type` are registered as `web` applications; their registration used to fail. Relying parties without
  redirect URIs are not registered, and post-logout redirect URIs that contain a comma are left out.
- {: .new data-area="oidc"} An [OpenID Federation](../authentication/OIDC-Authentication-Federation.html#automatic-registration)
  relying party can send its trust chain in its request object or client assertion, which CAS verifies and uses instead
  of resolving the relying party, provided it is not older than the current registration and the request is signed with
  the keys it carries. A relying party that rolls its keys over is no longer refused until its registration expires: a
  request object or client assertion signed with a key its registration does not hold has CAS resolve it again, at
  most once per `cas.authn.oidc.federation.unresolved-entity-retention`{: .cas-setting}.
- {: .new data-area="oidc"} An [OpenID Federation](../authentication/OIDC-Authentication-Federation.html#required-trust-marks)
  OpenID provider can require trust marks of the relying parties it registers automatically, listed with
  `cas.authn.oidc.federation.required-trust-marks`{: .cas-setting}. Each trust mark is checked against the issuers that
  the trust anchor's own entity configuration accepts in `trust_mark_issuers`, verified with its issuer's keys through
  the issuer's own trust chain, its delegation checked when the trust anchor names an owner of the type, and its status
  asked of the issuer when it publishes a status endpoint, which must be an `https` URL. A trust mark that cannot be
  checked for now leaves the relying party unregistered for now, unless another trust mark of its type is valid and
  no required type is missing. A CAS trust anchor publishes the issuers it accepts, defined
  with `cas.authn.oidc.federation.trust-mark-issuers`{: .cas-setting}.
- {: .new data-area="oidc"} CAS can act as an [OpenID Federation trust mark issuer](../authentication/OIDC-Authentication-Federation.html#trust-mark-issuer)
  in any role: the trust mark types it issues and the entities each is issued to are defined with
  `cas.authn.oidc.federation.issued-trust-marks`{: .cas-setting}, and CAS publishes the trust mark, trust mark status and
  trust marked entities listing endpoints. Removing an entity from a type revokes its trust marks of that type. A trust
  anchor also publishes the owners of delegated trust mark types, defined with
  `cas.authn.oidc.federation.trust-mark-owners`{: .cas-setting}. Trust mark requests can be authenticated with
  `private_key_jwt` by subordinates, which then only obtain their own trust marks, optionally or always, using
  `cas.authn.oidc.federation.trust-mark-endpoint-authentication`{: .cas-setting}; only signing algorithms CAS can verify
  are advertised for them.
- {: .new data-area="oidc"} A CAS [OpenID Federation](../authentication/OIDC-Authentication-Federation.html#resolve-endpoint)
  trust anchor or intermediate publishes a resolve endpoint, which returns the resolved metadata, trust chain and valid
  trust marks of an entity for a trust anchor it trusts, as a signed resolve response. Resolutions and entities that
  could not be resolved are remembered for at most `cas.authn.oidc.federation.unresolved-entity-retention`{: .cas-setting};
  a trust mark that cannot be checked for now is left out of the response, which is then not remembered. Resolve requests can be authenticated with `private_key_jwt` by subordinates, optionally or always, using
  `cas.authn.oidc.federation.resolve-endpoint-authentication`{: .cas-setting}; the response is then addressed to the requester.
- {: .new data-area="oidc"} [OpenID Federation keys roll over](../authentication/OIDC-Authentication-Federation.html#key-rollover):
  the federation keystore holds current, future and previous keys, all published in the entity configuration, rotated
  and retired on a schedule (`cas.authn.oidc.federation.rotation.schedule.enabled`{: .cas-setting},
  `cas.authn.oidc.federation.revocation.schedule.enabled`{: .cas-setting}) or through the new `oidcFederationJwks`
  actuator endpoint, which can also revoke a key, for instance when it is compromised. Retired and revoked keys are
  published by the new federation historical keys endpoint. A scheduled rotation or revocation only changes keys that are
  old enough, at half the time between two runs of its schedule, so it does not run again after a restart, and nodes
  that share the keystore file read it again when it changes; in a cluster, the schedules are best enabled on one node.
  Keys are changed on copies and used only once saved. New keystores start with a future key; existing single-key
  keystores keep working, and published keys carry the algorithm they sign with, matching the key's curve. A CAS
  OpenID provider does not use trust chains whose statements do not name their signing key, or that the trust anchor
  signed with a key it has revoked or had retired before, as the historical keys it publishes over `https` show; they
  are read through the entity configuration fetched from the trust anchor, never one a chain carries, and a trust anchor
  whose historical keys cannot be checked is not trusted. Trust marks signed with a key that is retired but not revoked
  remain valid until they expire.
- {: .new data-area="oidc"} CAS as a [verifiable presentation verifier](../authentication/OIDC-Authentication-Verifiable-Credentials.html#openid-federation-verifier)
  can identify itself to wallets with the OpenID4VP `openid_federation` client identifier prefix, chosen with
  `cas.authn.oidc.vc.presentation.client-identifier-prefix=OPENID_FEDERATION`{: .cas-setting}: the client identifier is
  the entity identifier of CAS, request objects are signed and named by `kid` and carry no client metadata, and wallets
  read the `openid_credential_verifier` metadata (keys, response URI, formats) from the CAS entity configuration through
  its trust chain. Encrypted responses then use the encryption keys of the OpenID Connect keystore (`ECDH-ES` or
  `RSA-OAEP-256`), in any rotation state, instead of a key per request. Request objects under any prefix are now signed
  with the algorithm of the signing key's curve (`ES384`, `ES512`), no longer always `ES256`.

### Model Context Protocol (MCP)
{: .new data-area="operations"}

- {: .new data-area="oidc"} The [MCP server](../installation/Admin-MCP-Server.html) reports the audience and allowed
  resources of OAuth and OpenID Connect clients, and the new `checkOidcClientResource` tool tells whether a client may
  request a resource, which allowed expression it matches or why it would be refused, and the audience of the token.
- {: .new data-area="oidc"} The [MCP server](../installation/Admin-MCP-Server.html#oauth-access-tokens) accepts OAuth access
  tokens issued by CAS, alongside Basic authentication. It publishes RFC 9728 protected resource metadata that names CAS as the
  authorization server, challenges unauthenticated clients with both `Bearer` and `Basic`, and accepts a token only when it was
  issued for the MCP endpoint with the `mcp:read` scope to an application that is still allowed. The `MCP_ADMIN` role comes from
  the principal attributes listed in `cas.mcp.oauth.required-attributes`{: .cas-setting}, resolved for the person when the token
  is used rather than taken from the attributes released to the client; they match nobody by default, and the
  audit record of a call names the OAuth client. The `AI.mcp-oauth` feature toggle turns it off.
- {: .new data-area="operations"} The [MCP server](../installation/Admin-MCP-Server.html#tools-that-change-cas) can offer
  tools that change CAS: end the single sign-on sessions of a person with single logout, release throttled logins, turn an
  application on or off, change a log level, revoke OAuth tokens and consent decisions, and remove trusted devices,
  one-time password devices and WebAuthn devices. They are turned off by default, are turned on with the `AI.mcp-write`
  feature toggle, and require the new `MCP_OPERATOR` role. With an OAuth access token, that role also requires the
  `mcp:write` scope and the principal attributes listed in `cas.mcp.oauth.operator-required-attributes`{: .cas-setting},
  which match nobody by default.

### Attribute Release
{: .fixed data-area="attributes"}

- {: .fixed data-area="attributes"} The [pattern matching attribute release policy](../integration/Attribute-Release-Policy-PatternMatching.html)
  no longer releases the transformation template itself for values that do not match the pattern; such values are dropped, and
  the attribute is not released when none of its values match, as the inline Groovy transformation already did.
- {: .fixed data-area="attributes"} The [return linked attribute release policy](../integration/Attribute-Release-Policy-ReturnLinked.html)
  releases the values of the first linked attribute that has a value, as documented; it used to release the last one.
- {: .new data-area="saml"} [SAML2 attribute release policies](../installation/Configuring-SAML2-Attribute-Release.html) that
  match metadata entity attributes, entity categories included (InCommon and REFEDS Research and Scholarship, anonymous,
  pseudonymous and personalized access), accept entity id patterns: a service provider whose metadata does not carry the
  entity attribute still receives the attributes when its entity id matches one of them. The Palantir service wizard
  can edit these patterns.

### Authentication and Session Security
{: .changed data-area="authentication"}

- {: .action data-area="general"} The [SAML2 identity provider](../authentication/Configuring-SAML2-Authentication.html),
  [OAuth/OpenID Connect](../authentication/OAuth-Authentication.html), [UMA](../protocol/OAuth-UMA-Protocol.html),
  [Heimdall](../authorization/Heimdall-Authorization-Overview.html), [JWT authentication](../authentication/JWT-Authentication.html)
  and [Duo Security](../mfa/DuoSecurity-Authentication.html) no longer depend on pac4j, which remains only in
  [delegated authentication](../integration/Delegate-Authentication.html) and the features that hand off to it, such as
  [passwordless authentication](../authentication/Passwordless-Authentication.html) and
  [ADFS integration](../integration/ADFS-Integration.html). They are built on Spring Security and CAS
  components and keep their session state in a CAS session store, in the http session, in browser storage or in the
  ticket registry as configured. Customizations, Groovy scripts and session store beans that use pac4j types must be
  updated, and authentication handler names recorded in audit logs no longer carry pac4j class names. OAuth/OpenID Connect
  sign-ins and pushed authorization requests that are under way during the upgrade must start again. SAML2 requests passed
  on to [SAML2 identity providers](../integration/Delegate-Authentication-SAML2.html) keep their forced and passive
  authentication, authentication contexts and scoping.
- {: .action data-area="authentication"} [JWT authentication](../authentication/JWT-Authentication.html) takes the subject
  of a token as the principal as it is, and every other claim as an attribute. Tokens built with pac4j, whose subject
  carries a profile type such as `org.pac4j.core.profile.CommonProfile#casuser`, now sign in as that whole subject and
  must be issued again with a plain one. A service without a signing secret no longer accepts any token. The `tokenAuth`
  actuator endpoint no longer issues tokens: it validates one and answers the principal and the identifier, name and
  service id of the service, never its properties or secrets.
- {: .changed data-area="general"} When the [SAML2 identity provider](../authentication/Configuring-SAML2-Authentication.html)
  or [Duo Security](../mfa/DuoSecurity-Authentication.html#session-storage) keep their session state in browser storage,
  the browser receives only the state of that flow rather than every attribute of the http session, and the state
  expires with the lifetime of transient session tickets (`cas.ticket.tst.time-to-kill-in-seconds`{: .cas-setting}).
  A state that has expired or belongs to another flow restores nothing; sign-ins under way during the upgrade must
  start again.
- {: .fixed data-area="general"} Signing and encryption of values such as tokens and cookies now report a failure to
  sign, verify or encrypt a value instead of continuing without that step. A JSON web key that carries only a public
  key can now be used as a signing key to verify signatures produced elsewhere.
- {: .fixed data-area="general"} CSRF protection is configured once for all modules, so OpenID Connect and
  [WebAuthn](../mfa/FIDO2-WebAuthn-Authentication.html) no longer override each other's settings, and the
  [backchannel authentication](../authentication/OIDC-Authentication-CIBA.html) verification page is now protected.
  A custom CSRF token repository is defined once, as the `casCsrfTokenRepository` bean.
- {: .fixed data-area="mfa"} [Google Authenticator](../mfa/GoogleAuthenticator-Authentication.html) scratch codes are
  compared by their exact value, and a stored scratch code that cannot be decrypted is ignored instead of being used as
  it was stored. With scratch codes encrypted (`cas.authn.mfa.gauth.core.scratch-codes.encryption.key`{: .cas-setting}),
  the [REST registration store](../mfa/GoogleAuthenticator-Authentication-Registration-Rest.html) no longer loses
  precision when reading them back. Scratch codes that can no longer be decrypted, such as codes stored under another
  key, stop working; users can generate new ones from the account profile.
- {: .fixed data-area="authentication"} A login request that carries both `renew` and `gateway` now shows the login form,
  as the [CAS protocol](../protocol/CAS-Protocol-Specification.html) recommends, instead of returning to the application
  without a ticket. When renewed authentication is turned off with `cas.sso.renew-authn-enabled`{: .cas-setting}, the
  `gateway` parameter is honored as before.
- {: .new data-area="authentication"} The `throttles` actuator endpoint of
  [authentication throttling](../authentication/Configuring-Authentication-Throttling-Failure.html) can return only the
  throttled records of one username, matched regardless of case, instead of every record.

### SAML2
{: .changed data-area="saml"}

- {: .fixed data-area="saml"} The [ECP profile](../authentication/Configuring-SAML2-Authentication.html) answers a
  successful login with the SAML2 response in the SOAP envelope again, instead of a SOAP fault.
- {: .fixed data-area="saml"} [Back-channel SAML2 logout](../installation/Configuring-SAML2-Logout.html) with the SOAP binding
  sends the SOAP envelope as the body of the request, instead of a URL-encoded copy of it.
- {: .fixed data-area="saml"} A `nameIdQualifier` or `serviceProviderNameIdQualifier` set to `none` on a
  [SAML2 service](../services/SAML2-Service-Management.html) leaves that qualifier out of the NameID, as documented, instead of
  sending the value `none`.
- {: .fixed data-area="saml"} The [attribute query profile](../installation/Configuring-SAML2-AttributeQuery.html) releases
  the attributes its release policy allows; the policy could not find the metadata of the service provider for a query
  and released none of them.
- {: .action data-area="authentication"} For [unsolicited SSO](../authentication/Configuring-SAML2-Authentication-Unsolicited.html),
  the SAML2 identity provider no longer signs the authentication request it builds itself, nor checks it for a signature,
  and the `signUnsolicitedAuthnRequest` setting of SAML2 service definitions is removed. Unsolicited SSO also works when
  the session state is kept in browser storage. [Delegated authentication](../integration/Delegate-Authentication-SAML2.html)
  no longer reloads the metadata of a SAML2 identity provider for each request.
- {: .fixed data-area="saml"} [SAML2 service provider metadata](../installation/SAML2-ServiceProvider-Metadata.html#metadata-caching--resolution)
  that cannot be resolved is no longer retried with one-second pauses during the request, and a failure to load it is
  remembered for `cas.authn.saml-idp.metadata.core.failure-cache-expiration`{: .cas-setting} instead of contacting the
  metadata source on every request. Metadata query servers keep one backup per entity, so an entity no longer falls back
  onto the metadata of another, and metadata stores such as JPA or MongoDB are cached per entity, so providers that share
  a service definition no longer evict each other's metadata. Changes to the metadata settings of a service definition take
  effect on the next request on every node, and checking whether the JPA metadata store is available no longer opens a
  new connection pool each time.
- {: .fixed data-area="saml"} [SAML2 identity provider metadata](../installation/Configuring-SAML2-DynamicMetadata.html),
  keys and certificates are fetched again from their storage once `cas.authn.saml-idp.metadata.core.cache-expiration`{: .cas-setting}
  has passed, even while in constant use, so a key rotation in a shared metadata store reaches every node.
- {: .changed data-area="saml"} [SAML2 service provider metadata](../installation/SAML2-ServiceProvider-Metadata.html#default)
  downloaded over HTTP or from a [metadata query server](../installation/Configuring-SAML2-DynamicMetadata-MDQ.html) is
  fetched with conditional requests, and an unchanged document is no longer downloaded again. Comma-separated locations
  are all tried before the metadata backup file is used, and a failed load names every location and the reason it failed,
  in the logs and in the answer of the cached metadata endpoint. Validators are kept in a file next to the backup instead
  of an extended file attribute, which many file systems do not support.
- {: .changed data-area="saml"} Service provider metadata kept in [Amazon S3](../installation/Configuring-SAML2-DynamicMetadata-AmazonS3.html)
  is no longer downloaded in full to find one provider: only matching documents are fetched, and buckets with more than
  1000 documents are read completely. Metadata kept in [Git](../installation/Configuring-SAML2-DynamicMetadata-Git.html)
  pulls the repository at most once per `cas.authn.saml-idp.metadata.git.schedule.repeat-interval`{: .cas-setting} instead
  of on every load. The metadata health indicator checks each metadata source once per run rather than once per service.
- {: .changed data-area="saml"} [SAML2 service provider metadata](../installation/SAML2-ServiceProvider-Metadata.html#metadata-caching--resolution)
  read from files, directories or the JSON metadata file is loaded again once a file changes, checked at most once per
  `cas.authn.saml-idp.metadata.file-system.change-detection-interval`{: .cas-setting}. The cached metadata endpoint lists
  statistics and entries of the metadata cache, including the failures it remembers, and reports whether each entity is
  still valid; the cache statistics are also published as metrics. A metadata backup file that its source has not confirmed
  for longer than `cas.authn.saml-idp.metadata.http.metadata-backup-max-age`{: .cas-setting} is logged as an error when
  used and flagged by the metadata health indicator. Metadata criteria patterns are compiled once per load, metadata
  signature certificates are downloaded once per load, and downloaded metadata is no longer copied as text.
- {: .changed data-area="saml"} [Cached SAML2 service provider metadata](../installation/SAML2-ServiceProvider-Metadata.html#metadata-caching--resolution)
  is loaded again once its `validUntil` passes, and a `cacheDuration` declared by the metadata is kept between
  `cas.authn.saml-idp.metadata.core.minimum-cache-duration`{: .cas-setting} and
  `cas.authn.saml-idp.metadata.core.maximum-cache-duration`{: .cas-setting}. A request for an entity that an aggregate
  does not hold no longer walks every entity of the aggregate. Metadata resolvers returned by
  [Groovy scripts](../installation/Configuring-SAML2-DynamicMetadata-Groovy.html) that run background tasks are destroyed
  once no longer used. Metadata kept in [Redis](../installation/Configuring-SAML2-DynamicMetadata-Redis.html) is indexed by
  entity, so a service provider is found without scanning every key. A [metadata query](../installation/Configuring-SAML2-DynamicMetadata-MDQ.html)
  location given as an expression, or as one of several comma-separated locations, is cached per entity like any other.
- {: .fixed data-area="saml"} [SAML2 identity provider metadata](../installation/Configuring-SAML2-DynamicMetadata.html)
  is cached once for all services that have no metadata of their own, instead of once per service, so deployments with
  many SAML2 services no longer read identity provider metadata from its storage on every response.
- {: .changed data-area="saml"} [SAML2 identity provider metadata](../installation/Configuring-SAML2-DynamicMetadata.html)
  is served as `application/samlmetadata+xml` when asked for, carries an `ETag`, and answers `304 Not Modified` to a
  client that already holds it.
- {: .fixed data-area="saml"} A signed [SAML2 attribute query](../installation/Configuring-SAML2-AttributeQuery.html) is
  answered with the SOAP response alone. The identity provider also wrote the POST-binding form of the response before
  it, so the service provider received an HTML page and the SOAP response could not be sent.

### Authentication Interrupts
{: .changed data-area="ui"}

- {: .action data-area="ui"} [Interrupt inquirers](../webflow/Webflow-Customization-Interrupt-Custom.html) no longer receive
  the Spring Webflow request context, so they can be asked outside the login flow. Custom inquirers must drop that
  parameter, and [Groovy interrupt scripts](../webflow/Webflow-Customization-Interrupt-Groovy.html) now receive the logger
  as their fifth argument: scripts that read the sixth argument or destructure six arguments must be updated. The REST
  strategy still passes the language preferences of the current request along to the interrupt endpoint.
- {: .new data-area="ui"} The new `interrupts` [actuator endpoint](../webflow/Webflow-Customization-Interrupt.html#actuator-endpoints)
  determines the interrupt payload produced for a user, optionally signing in to an application: it asks every
  interrupt strategy in order and reports what each one produced, or why it failed, along with the interrupt the login
  flow would present.

### User Interface
{: .fixed data-area="ui"}

- {: .fixed data-area="ui"} When password management is turned off, the login page no longer links to `https://pwd.example.org`; the
  address of an external password recovery page is set with the `cas.pm-links.forgot-password.url` theme property, and
  nothing is shown when it is blank.
- {: .fixed data-area="ui"} Date fields in the [Palantir](../installation/Admin-Dashboard.html) service wizard open a calendar to
  pick from: the expiration date, client secret expiration dates, and the start and end of a time-based access strategy, which
  take the start and the end of the picked day. Values of the access strategy types (remote, HTTP request, time-based and
  others) and of the proxy and token exchange policies now reach the generated definition; they used to be left out.
- {: .fixed data-area="ui"} Generated passwords on the password update form and client secrets generated in Palantir use the
  browser's cryptographic random number generator. WebAuthn no longer shows the session token after authentication, and the
  account unlock page only blocks the context menu on the captcha.
- {: .fixed data-area="ui"} Browser storage pages show their error panel when progress display is turned off, instead of leaving a
  blank page.
- {: .fixed data-area="ui"} Labels and messages that were written in English inside templates and scripts (generic success page,
  logout confirmation, error pages, QR code login, WebAuthn progress, attribute consent, interrupts, delegated profile
  selection, impersonation, device code approval, account profile and sign-up dialogs) now come from message bundles.
- {: .fixed data-area="ui"} Turning on CAS login for [Palantir](../installation/Admin-Dashboard.html) no longer sends other
  protected endpoints, such as actuator endpoints and the [MCP server](../installation/Admin-MCP-Server.html), to the CAS
  login page; they challenge for their own credentials again, as API clients expect, and only Palantir redirects to CAS.

### Multifactor Authentication
{: .changed data-area="mfa"}

- {: .action data-area="mfa"} The [device fingerprint](../mfa/Multifactor-TrustedDevice-Authentication-DeviceFingerprint.html)
  of trusted devices includes the client IP address by default, as documented; it was only used when
  `cas.authn.mfa.trusted.device-fingerprint.client-ip.enabled`{: .cas-setting} was set explicitly. Devices trusted before the
  upgrade are asked for multifactor authentication once more; set that setting to `false` to keep the previous fingerprint.
- {: .fixed data-area="mfa"} The [browser fingerprint](../mfa/Multifactor-TrustedDevice-Authentication-DeviceFingerprint.html)
  that the login form collects is kept for the rest of the multifactor flow, so a device trusted while that fingerprint is
  part of the device fingerprint is recognized at the next login.
- {: .fixed data-area="mfa"} The Google Authenticator registration QR code includes `digits` and `period` when
  `cas.authn.mfa.gauth.core.code-digits`{: .cas-setting} or `cas.authn.mfa.gauth.core.time-step-size`{: .cas-setting} differ
  from the defaults, so authenticator apps generate codes CAS accepts.
- {: .new data-area="mfa"} [Principal attribute triggers per application](../mfa/Configuring-Multifactor-Authentication-Triggers-PrincipalAttribute-PerApplication.html#provider-selection-mappings)
  can map each multifactor provider to a pattern for the attribute values, so the service definition alone decides which
  provider a user is sent to; [Palantir](../installation/Admin-Dashboard.html) can edit these mappings.
- {: .changed data-area="mfa"} [Custom multifactor authentication triggers](../mfa/Configuring-Multifactor-Authentication-Triggers-Custom.html#register-triggers)
  register their event resolvers by tagging the resolver bean with an annotation; CAS collects tagged resolvers on its own, and
  resolvers no longer add themselves to the initial authentication event resolver. Custom resolvers, and beans that override a
  built-in trigger resolver, must carry the annotation.
- {: .fixed data-area="mfa"} [YubiKey accounts kept in a JSON file](../mfa/YubiKey-Authentication-Registration-JSON.html)
  are written to a temporary file that then replaces the JSON file in one step, so a failed or concurrent write no longer
  leaves a truncated file behind that stops CAS from starting; removing a single device no longer fails a change made
  at the same time.

### Documentation
{: .changed data-area="docs"}

- {: .changed data-area="docs"} The [SPNEGO](../authentication/SPNEGO-Authentication.html) settings explain that
  `cas.authn.spnego.properties[0].jcifs-service-password`{: .cas-setting} is only used when
  `cas.authn.spnego.system.use-subject-creds-only`{: .cas-setting} is `true` (otherwise the service keys come from a keytab),
  and that setting both the Kerberos KDC and realm makes the JDK ignore the Kerberos configuration file.
- {: .fixed data-area="docs"} Setting lists show default values in full instead of cutting them off; where a list is narrow, the
  value moves to a second row under the setting name. Each setting in the configuration catalog also has a *Copy link* button.
- {: .new data-area="docs"} Every page of the documentation has a Markdown copy at the same address ending in `.md`, which
  the page toolbar copies to the clipboard, and each version publishes an `llms.txt` index of its pages for AI assistants
  and other tools.
- {: .new data-area="docs"} The new [Configuration Properties Changes](../configuration/Configuration-Properties-Changes.html) page lists the
  CAS settings added, removed, replaced or given a new default since the previous version, generated from the configuration
  catalogs and also published as JSON.

## Other Stuff

- {: .fixed data-area="project"} The vulnerability report of the [dependencies endpoint](../installation/Dependency-Management-Versions.html#actuator-endpoints)
  follows OSV pagination for each dependency, so vulnerabilities past the first page of a result are no longer left out,
  and vulnerability details that cannot be read are reported as errors instead of being dropped silently.
- {: .changed data-area="project"} A large number of dependencies and libraries have been updated to their latest versions.

{% include release-footer.html %}
