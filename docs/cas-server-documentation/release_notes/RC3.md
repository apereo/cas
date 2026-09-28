---
layout: default
title: CAS - Release Notes
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

### Heimdall AuthZEN

[Heimdall](../authorization/Heimdall-Authorization-Overview.html) AuthZEN support is reworked to follow the
[AuthZEN Authorization API 1.0](https://openid.net/specs/authorization-api-1_0.html) specification:

- The AuthZEN `resource.id` now identifies the resource instance rather than a policy namespace. AuthZEN requests are matched
  against [resources in all namespaces](../authorization/Heimdall-Authorization-Overview.html) by their new `resourceType`, `actions` and optional `resourceIdPattern` fields, and all matching resources must grant access.
  Existing AuthZEN resources must define these fields, or AuthZEN requests are denied; requests to `/heimdall/authorize` are unaffected.
- Evaluated denials return `200` with `"decision": false`, malformed requests `400`, and failed caller authentication `401`. The `X-Request-ID` header is echoed.
- Tokens must be issued to a registered OAuth or OpenID Connect application whose access strategy allows access, and
  a new [Heimdall access strategy](../authorization/Heimdall-Authorization-Overview.html) can prevent an application from calling Heimdall. [DPoP-bound](../authentication/OIDC-Authentication-DPoP.html) and certificate-bound tokens
  now require their proof. On the AuthZEN endpoint, `Basic` credentials are the `client_id:client_secret` of a registered application
  rather than CAS user credentials.
- [JWT bearer assertions](../authentication/OIDC-Authentication-JWT-Bearer.html) must carry `jti` and `iat` claims, are accepted once, and may not live longer than
  `cas.heimdall.jwt-assertion-max-lifetime` (five minutes by default).
- Failed caller authentication on `/heimdall/authorize` now returns `401` instead of `403`. Failed caller authentication on both
  Heimdall endpoints is subject to [authentication throttling](../authentication/Configuring-Authentication-Throttling-Failure.html).
- A resource with no policies now denies access instead of granting it, and the REST policy no longer sends the resource's policies to its endpoint.
- [JDBC policies](../authorization/Heimdall-Authorization-Overview.html) use a shared connection pool, registered as an application context bean and optionally named via `dataSourceName`,
  instead of opening a new database connection for every decision. Queries time out after `queryTimeout` (five seconds by default).
  Policies are evaluated in order rather than on the shared thread pool.
- AuthZEN subjects are resolved from CAS attribute repositories only for the `user` subject type. Required and rejected attribute policies accept qualified names such as `subject.properties.department`,
  `resource.properties.owner`, `action.properties.method` and `context.channel`. HTTP request headers are no longer added to the
  AuthZEN request context; on `/heimdall/authorize` they no longer override body entries or include protocol headers.
- A resource that does not set `enforceAllPolicies` is now granted when any one of its policies grants access, as documented;
  previously every policy had to grant. Set `enforceAllPolicies` to `true` on resources that rely on the old behavior.
  In that mode, a policy that fails with an error no longer prevents a later policy from granting access.
- Heimdall supports the AuthZEN [access evaluations API](../authorization/Heimdall-Authorization-Overview.html) at
  `/heimdall/authzen/evaluations`, with the `execute_all`, `deny_on_first_deny` and `permit_on_first_permit` semantics.
- Heimdall publishes AuthZEN [policy decision point metadata](../authorization/Heimdall-Authorization-Overview.html) at
  `/heimdall/.well-known/authzen-configuration`; the well-known location defined by the specification needs a rewrite rule.
- [JDBC and OpenFGA policies](../authorization/Heimdall-Authorization-Overview.html) receive the AuthZEN subject, resource and action. [Palantir](../installation/Admin-Dashboard.html) can edit the AuthZEN fields
  of a resource and configure the Heimdall access strategy.

### Certificate-Bound Access Tokens

The certificate thumbprint that CAS records for [mutual TLS client authentication](../authentication/OIDC-Authentication-AccessToken-AuthMethods.html)
and emits as the `cnf` `x5t#S256` claim of access tokens and introspection responses is now computed as specified
by [RFC 8705](https://www.rfc-editor.org/rfc/rfc8705#section-3.1): the base64url-encoded SHA-256 hash of the DER-encoded certificate.
Previously, it was computed from the certificate's public key and was not encoded as specified. Certificate-bound tokens issued
before the upgrade carry the old value and are no longer accepted by resource servers that verify the binding.

### Authentication Throttling

[Authentication throttling](../authentication/Configuring-Authentication-Throttling-Failure.html) now records a failed attempt only
when the response status is `401`, once per request. Previously, any response other than `200`, `201` or `302`, such as a malformed
request (`400`), an authorization denial (`403`), a missing resource (`404`) or a server error (`500`), was counted as a failed login,
and failures were recorded twice. Failed SAML2 ECP authentication attempts, which answer with a SOAP fault, are now counted as well.

## Other Stuff

- A large number of dependencies and libraries have been updated to their latest versions.
- Almost all CAS unit tests are internally reworked to allow maximum parallelization and speed up the overall test execution time.
- [Delegated authentication](../integration/Delegate-Authentication.html) no longer fails intermittently when concurrent requests reach an identity provider that is still being initialized, typically right after startup. Such requests now wait for the initialization in progress instead of failing, which also affects [SAML2 identity providers](../integration/Delegate-Authentication-SAML2.html) when building SAML2 responses, metadata and logout requests.

