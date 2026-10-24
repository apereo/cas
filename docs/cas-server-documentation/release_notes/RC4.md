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
    - value: "611"
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
and scenarios. At the moment, total number of jobs stands at approximately `611` distinct scenarios. The overall
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
| WebAuthn                    | `cas.webauthn.authentication.show-device-details`, `cas.webauthn.registration.recommended`          |
| Google Authenticator        | `cas.gauth.registration.show-secret-key`, `.scratch-codes` (`chips`, `list`), `.show-print`          |
| QR codes                    | `cas.qr-code.size`, `cas.qr-code.foreground`, `cas.qr-code.background`, `cas.qr-code.margin`        |
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

## Other Stuff
- {: .changed data-area="project"} A large number of dependencies and libraries have been updated to their latest versions.
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

{% include release-footer.html %}
