---
layout: default
title: CAS - Release Notes
category: Planning
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
- Credential batch issuance is limited and capped at a predefined limit.

### OAuth and OpenID Connect 

[OAuth](../protocol/OAuth-Protocol.html) and [OpenID Connect](../authentication/OIDC-Authentication.html)
security have been strengthened across several flows.

- Client assertions now require matching subject, issuer, and client identifiers.
- JWT IDs are tracked through the shared ticket registry to prevent replay across clustered deployments.
- Device and CIBA polling are bound to the authenticated client and completed poll-mode requests are consumed.
- Dynamic client key registrations are memory-bounded.
- Token exchanges are now authorized against the authenticated requesting client, and exchanged tokens cannot gain scopes beyond those granted to the subject token and allowed for the requesting client.
- Client secrets are now compared and enforced using a case sensitive strategy.
- Token introspection reports tokens from other clients as inactive, while revocation only affects the requesting client's tokens and no longer reveals whether other tokens exist.
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
- SAML2 single logout now validates request freshness and replay, fully authenticates and correlates logout responses, and generates actuator logout requests with the correct IdP issuer and service-provider destination.

### CAS Protocol

- The CAS `1.0` [validation response](../ux/User-Interface-Views-CASv1.html) is line-delimited and carries no escaping mechanism.
  Principal identifiers and rendered attribute lines are now stripped of line breaks, so a value that contains a newline
  can no longer forge additional lines in the response.
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
- Thymeleaf template caching now takes effect. The chaining template resolver previously declared every resolution
  non-cacheable regardless of `spring.thymeleaf.cache`, so each page and each fragment was re-resolved and re-parsed on
  every request. Resolutions are now cached per the configured setting, except for those produced by a theme-aware
  resolver: those map one template name to different files depending on the request's theme, and Thymeleaf's cache key
  does not carry the theme, so they remain non-cacheable. Deployments with no themed template overrides cache every
  template; deployments with themed overrides cache everything except the overridden templates.

### LDAP Integrations

- [Surrogate authentication](../authentication/Surrogate-Authentication-Storage-LDAP.html), [delegated authentication profile selection](../integration/Delegate-Authentication-ProfileSelection.html), [acceptable usage policy](../webflow/Webflow-Customization-AUP-LDAP.html) and [password management](../password_management/Password-Management.html) now build their LDAP connection pools once and reuse them across requests.
- LDAP connection pools are now addressed by the directory and base DN they serve, so multiple configuration blocks that point at the same server no longer collapse onto a single set of connection settings.
- Surrogate search filters that do not reference the impersonated account are now rejected, as such a filter is unable to restrict the accounts a user may impersonate.

## Other Stuff
    
- CloudWatch logging now avoids recursive logging initialization when reporting appender startup or delivery failures.
- Authentication history, theme caching and CloudWatch shutdown now use concurrent collections and explicit coordination in place of Java monitor locking.
- Several optimizations are in place to assist with faster startup time, allowing for more components to be lazily initialized.
- A large number of dependencies and libraries have been updated to their latest versions.
