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
    - value: "614"
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
and scenarios. At the moment, total number of jobs stands at approximately `614` distinct scenarios. The overall
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

### Theme Properties
{: .new data-area="ui"}

Several screens can now be adjusted through [theme properties](../ux/User-Interface-Customization-Themes-Static.html),
so they can differ per theme, per application and per tenant, and most of them preview live in the theme builder:

| Screen                      | Theme properties                                                                                     |
|-----------------------------|------------------------------------------------------------------------------------------------------|
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

Defaults keep the screens as they were, with two exceptions: external identity providers keep their configured order in
the dropdown that replaces the buttons once there are more than ten, and the error page offers its details button only
when `cas.error.show-details` is turned on, in which case it now shows the stack trace the server includes rather than an
empty box. QR codes for Google Authenticator, WebAuthn and QR code login use the QR code properties of the theme that
applies to the request; their size stays between `100` and `1000` pixels, and a foreground that is not darker than the
background falls back to black on white so the code remains scannable. Device and account names suggested when
registering trusted devices, Google Authenticator, YubiKey or WebAuthn devices can start empty, random or with the
username.

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

## Other Stuff
- {: .changed data-area="project"} A large number of dependencies and libraries have been updated to their latest versions.
- {: .changed data-area="services"} The [JPA service registry](../services/JPA-Service-Management.html) reads only the
  identifier and body of each definition, keeps no entities for reads, and no longer deserializes a definition on reload
  when its stored body is unchanged. Deleting a service is a single statement, new services are no longer looked up before
  they are stored, bulk saves flush every `batch-size`{: .cas-setting} definitions, and the service id and name columns
  are indexed.
- {: .changed data-area="project"} MariaDB integration tests and functional test scenarios use port `3307`, so they can run
  alongside the MySQL tests on `3306`.
- {: .changed data-area="tickets"} The [JPA ticket registry](../ticketing/JPA-Ticket-Registry.html) updates a ticket with a
  single statement instead of reading the row back first, no longer deletes service tickets twice when a session ends,
  pages ticket listings in the query instead of reading and skipping the rows before the page, and reads tickets without
  keeping copies of them for change tracking. Session queries by attribute bind the attribute values as parameters.
- {: .action data-area="tickets"} With MySQL, the [JPA ticket registry](../ticketing/JPA-Ticket-Registry.html) declares shorter
  `type`, `principal_id` and `parent_id` columns so that its indexes fit MySQL's key length limit; until now MySQL refused
  every one of them. Existing tables need the statements listed on that page. The index on the ticket type alone is gone
  on all databases.
- {: .fixed data-area="ui"} The account profile only ends single sign-on sessions, revokes OpenID Connect tokens and removes
  trusted devices that belong to the signed-in user; it used to act on any session, token or device whose identifier was submitted.
- {: .fixed data-area="ui"} [Themes](../ux/User-Interface-Customization-Themes.html) other than the default one, including themes chosen per
  application or per tenant, now inherit `cas-theme-custom.properties` before falling back to `cas-theme-default.properties`.
  Theme files found under the template prefixes now apply regardless of the browser language; they used to be ignored for
  languages other than English, and the aggregate theme source only honored them for an exact `en` request.
- {: .fixed data-area="ui"} The account profile and account management pages follow the shared card, text and border colors of the
  theme, and the light Palantir theme follows the theme's primary and secondary colors.
- {: .fixed data-area="ui"} When password management is turned off, the login page no longer links to `https://pwd.example.org`; the
  address of an external password recovery page is set with the `cas.pm-links.forgot-password.url` theme property, and
  nothing is shown when it is blank.
- {: .fixed data-area="ui"} Generated passwords on the password update form and client secrets generated in Palantir use the
  browser's cryptographic random number generator. WebAuthn no longer shows the session token after authentication, and the
  account unlock page only blocks the context menu on the captcha.
- {: .fixed data-area="ui"} Browser storage pages show their error panel when progress display is turned off, instead of leaving a
  blank page.
- {: .fixed data-area="ui"} Labels and messages that were written in English inside templates and scripts (generic success page,
  logout confirmation, error pages, QR code login, WebAuthn progress, attribute consent, interrupts, delegated profile
  selection, impersonation, device code approval, account profile and sign-up dialogs) now come from message bundles.
- {: .fixed data-area="attributes"} The [pattern matching attribute release policy](../integration/Attribute-Release-Policy-PatternMatching.html)
  no longer releases the transformation template itself for values that do not match the pattern; such values are dropped, and
  the attribute is not released when none of its values match, as the inline Groovy transformation already did.
- {: .changed data-area="project"} Browser tests now cover service matching strategies (literal, starts with, full and partial
  regular expressions), throttled ticket-granting ticket timeouts, per-service service ticket, proxy ticket and proxy-granting
  ticket expiration policies, single sign-on participation policies (never, last used time, authentication date), per-service
  security headers, encrypted, pattern matching, linked, environment and Groovy script attribute release policies, the
  anonymous username provider, multifactor bypass by remote address, request header and credential type, and the REST
  multifactor trigger (`cas.authn.mfa.triggers.rest.url`{: .cas-setting}).

- {: .fixed data-area="oidc"} [Front-channel logout](../authentication/OIDC-Authentication-Logout.html) requests now carry the same
  `sid` as the ID token and the back-channel logout token; the session identifier used to be computed differently, so
  applications could not match it to the session they had recorded.
- {: .fixed data-area="oidc"} Access tokens issued as JWTs can be revoked through the revocation endpoint, and refresh tokens issued as
  JWTs can be redeemed with the `refresh_token` grant; both used to be looked up in the ticket registry without being decoded
  first, so revocation silently did nothing and the refresh request was refused.
- {: .fixed data-area="mfa"} The Google Authenticator registration QR code includes `digits` and `period` when
  `cas.authn.mfa.gauth.core.code-digits`{: .cas-setting} or `cas.authn.mfa.gauth.core.time-step-size`{: .cas-setting} differ
  from the defaults, so authenticator apps generate codes CAS accepts.
- {: .changed data-area="project"} Browser tests now cover OpenID Connect front-channel logout, refused hybrid and unauthorized
  response types, encrypted user info and introspection responses, JWT refresh tokens, optional ID token encryption, token
  revocation with its cascade from refresh tokens to access tokens, WebAuthn signature counters, user verification and
  attestation, and Google Authenticator replay protection with custom code length, time step and window.

- {: .changed data-area="project"} Browser tests now sign in with [SPNEGO](../authentication/SPNEGO-Authentication.html) against a
  real Kerberos KDC: the browser presents a Kerberos ticket and is signed in without a password, with and without
  `cas.authn.spnego.principal-with-domain-name`{: .cas-setting}, and falls back to the login form when it has no ticket.
- {: .changed data-area="docs"} The [SPNEGO](../authentication/SPNEGO-Authentication.html) settings explain that
  `cas.authn.spnego.properties[0].jcifs-service-password`{: .cas-setting} is only used when
  `cas.authn.spnego.system.use-subject-creds-only`{: .cas-setting} is `true` (otherwise the service keys come from a keytab),
  and that setting both the Kerberos KDC and realm makes the JDK ignore the Kerberos configuration file.
- {: .fixed data-area="services"} A service whose single sign-on participation policy does not set
  `createCookieOnRenewedAuthentication` (for example the last-used-time and authentication-date policies) no longer
  fails the login flow; such a policy behaves like one that leaves the setting undefined, and the single sign-on
  session is kept when the service does not take part in it.
- {: .fixed data-area="oidc"} JWT access tokens are no longer signed with keys from the application's own `jwks` that
  are marked for encryption (`"use": "enc"`); when the application publishes only encryption keys, CAS signs with its own
  keys. `jwtAccessTokenSigningAlg` is honored as documented: CAS picks a signing key whose type fits the algorithm,
  and the key's own algorithm only applies when none is configured or the configured one does not fit the key.
  Signatures made with an EC (`ES*`) or RSA-PSS (`PS*`) key are randomized, so such a token is a different string each
  time it is encoded; the ID token's `at_hash` is computed over the access token the response carries.
- {: .fixed data-area="attributes"} The [return linked attribute release policy](../integration/Attribute-Release-Policy-ReturnLinked.html)
  releases the values of the first linked attribute that has a value, as documented; it used to release the last one.
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
- {: .fixed data-area="services"} With the services cache turned off (`cas.service-registry.cache.cache-size`{: .cas-setting}
  set to `0`), listing services no longer re-indexes every service it reads, which also made the next service lookup
  read the whole service registry again.

{% include release-footer.html %}
