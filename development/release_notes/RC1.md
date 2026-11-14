# 8.1.0-RC1 Release Notes

Source: https://apereo.github.io/cas/development/release_notes/RC1.html

Release notes · 8.1.0 development

# 8.1.0-RC1

The first release candidate of the 8.1.0 line turns Palantir into a place to manage authorization policies, attribute repositories and cluster topology, and lets OAuth and OpenID Connect applications rotate multiple client secrets.

- [Requires **JDK 25**](https://apereo.github.io/cas/development/planning/Installation-Requirements.html)
- Built on **Spring Boot 4.2**
- Built with **Gradle 9.7**
- [**556** browser test scenarios](https://apereo.github.io/cas/developer/Test-Process.html)

- **15** New & noteworthy topics
- **5** Changes to review before upgrading
- **13** Other changes
- **0** Fixes

## Before You Upgrade

These changes alter existing behavior. Each one links to its full description below.

- Action

  [**Palantir admin functions**](https://apereo.github.io/cas/development/release_notes/RC1.html#palantir-user-vs-admin-roles) now require a released `role` attribute of `ADMIN` or `ROLE_ADMIN`; other users only manage registered applications.Platform & Operations
- Changed

  [**Transient session tickets**](https://apereo.github.io/cas/development/release_notes/RC1.html#tickets--sessions) now live for `5` minutes by default instead of `15`.Tickets & Sessions
- Changed

  [**Date formatting**](https://apereo.github.io/cas/development/release_notes/RC1.html#general) in a number of operations now uses `UTC` instead of the system default time zone.
- Changed

  [**CAS REST APIs**](https://apereo.github.io/cas/development/release_notes/RC1.html#cas--other-protocols) answer unauthorized application requests with `403` instead of a `500` error.CAS & Other Protocols
- Removed

  [**LettuceMod**](https://apereo.github.io/cas/development/release_notes/RC1.html#tickets--sessions) is removed; Lettuce itself now provides its functionality.Tickets & Sessions

[![Palantir scripting view](https://apereo.github.io/cas/development/release_notes/img_1.png)](https://apereo.github.io/cas/development/release_notes/img_1.png) [![Palantir applications view](https://apereo.github.io/cas/development/release_notes/img_2.png)](https://apereo.github.io/cas/development/release_notes/img_2.png) [![Palantir Heimdall authorization view](https://apereo.github.io/cas/development/release_notes/img_3.png)+11 more](https://apereo.github.io/cas/development/release_notes/img_3.png)

Spotlight · Palantir admin dashboard

## Manage Authorization from the Dashboard

- Create, edit and remove Heimdall authorization policies, and simulate authorization requests.
- Right-click context menus replace inline table buttons.
- View attribute repositories in detail and register LDAP, JDBC and stub repositories.
- Inspect, remove or recompute cached scripted resources, and edit inline Groovy in a dedicated editor.

[Read the full section](https://apereo.github.io/cas/development/release_notes/RC1.html#palantir-admin-dashboard)

## Highlights

- OAuth & OpenID Connect New

  ### [Multiple Client Secrets](https://apereo.github.io/cas/development/release_notes/RC1.html#oauth--openid-connect-client-secrets)

  Applications can hold several expiring client secrets for smooth rotation, managed through the `oauthClientSecrets` endpoint.
- Platform & Operations New

  ### [Cluster Topology](https://apereo.github.io/cas/development/release_notes/RC1.html#cluster-topology)

  A `clusterTopology` actuator endpoint reports every node and its status across eight ticket registries.
- Attributes & Consent New

  ### [Attribute Definition Dependencies](https://apereo.github.io/cas/development/release_notes/RC1.html#attribute-definition-dependencies)

  Attribute definitions can depend on other definitions, which are resolved first and available during resolution.
- OAuth & OpenID Connect New

  ### [OpenID Connect Verifiable Credentials](https://apereo.github.io/cas/development/release_notes/RC1.html#openid-connect-verifiable-credentials)

  Credential issuance supports the authorization code flow, more formats and initial OpenID4VP support.
- OAuth & OpenID Connect Changed

  ### [OpenID Connect Federation](https://apereo.github.io/cas/development/release_notes/RC1.html#openid-connect-federation)

  Federation support is roughly finalized, with remaining edge cases tracked for the next releases.
- Interface & Accounts New

  ### [Acceptable Usage Policy & Multitenancy](https://apereo.github.io/cas/development/release_notes/RC1.html#acceptable-usage-policy--multitenancy)

  Acceptable usage policies work per tenant, starting with MongoDb storage.

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.

### OpenRewrite Recipes

CAS continues to produce and publish [OpenRewrite](https://docs.openrewrite.org/) recipes that allow the project to upgrade installations in place from one version to the next. [See this guide](https://apereo.github.io/cas/development/installation/OpenRewrite-Upgrade-Recipes.html) to learn more.

### Graal VM Native Images

A CAS server installation and deployment process can be tuned to build and run as a [Graal VM native image](https://apereo.github.io/cas/development/installation/GraalVM-NativeImage-Installation.html). We continue to polish native runtime hints. The collection of end-to-end [browser tests based on Puppeteer](https://apereo.github.io/cas/developer/Test-Process.html) have selectively switched to build and verify Graal VM native images and we plan to extend the coverage to all such scenarios in the coming releases.

### Testing Strategy

The collection of end-to-end [browser tests based on Puppeteer](https://apereo.github.io/cas/developer/Test-Process.html) continue to grow to cover more use cases and scenarios. At the moment, total number of jobs stands at approximately `556` distinct scenarios. The overall test coverage of the CAS codebase is approximately `94%`.

### Gradle 9.7

CAS is now built with Gradle 9.7 and the build process has been updated to use the latest Gradle features and capabilities.

### Spring Boot 4.2

CAS is now built on top of Spring Boot `4.2.x`. This is an in-progress ongoing minor platform upgrade that affects almost all aspects of the codebase including many of the third-party core libraries used by CAS as well as some CAS functionality.

Please refer to the [Spring Boot Wiki](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.2-Release-Notes) for more information on the changes and updates in this release. The biggest change to CAS would be support for AMQP 1.0.

### JSpecify & NullAway

CAS codebase is now annotated with [JSpecify](https://jspecify.dev/) annotations to indicate nullness contracts on method parameters, return types and fields. We will gradually extend the coverage of such annotations across the entire codebase in future releases and will integrate the Gradle build tool with tools such as [NullAway](https://github.com/uber/NullAway) to prevent nullness contract violations during compile time.

### OAuth & OpenID Connect Client Secrets

OAuth and OIDC client applications may now define [multiple client secrets](https://apereo.github.io/cas/development/authentication/OAuth-ClientSecret-Management.html), allowing deployments to support secret expiration and smoother secret rotation. Existing single-secret configurations remain compatible, while new configurations can include additional secrets with expiration metadata so clients can transition to new secrets without immediate disruption. Client secret rotation may be carried out using a dedicated `oauthClientSecrets` actuator endpoint.

### Attribute Definition Dependencies

Attribute definitions may now [declare dependencies](https://apereo.github.io/cas/development/integration/Attribute-Definitions.html) on other attribute definitions. When an attribute is resolved, its declared dependencies are resolved first and their results are made available during resolution, allowing definitions to build on values produced by other definitions in a predictable, reusable way.

### Palantir Admin Dashboard

Inlined table buttons in [Palantir Admin Dashboard](https://apereo.github.io/cas/development/installation/Admin-Dashboard.html) are replaced with proper context menus triggered by right clicks. The configuration tab is also extended to display cached scripted resources with the ability to either remove or recompute the cache entry.

Furthermore, [Heimdall authorization policies](https://apereo.github.io/cas/development/authorization/Heimdall-Authorization-Policies.html) can now be created, edited and removed from the [Palantir Admin Dashboard](https://apereo.github.io/cas/development/installation/Admin-Dashboard.html). There is also dedicated simulation support to experiment with authorization requests.

Configuration for almost all attribute repositories can also be viewed in better detail. The dashboard is also able to register new attribute repositories for LDAP, JDBC and Stub repositories.

Finally, fields that do support [inline Groovy scripts](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) are also allowed to better receive their value from a dedicated editor.

### Palantir User vs. Admin Roles

The [Palantir Admin Dashboard](https://apereo.github.io/cas/development/installation/Admin-Dashboard.html) now supports basic user roles and permissions. By default, all authenticated users are assigned a `ROLE_USER` role/authority. To access critical functionality as an admin, you will need to resolve and release a `role` attribute to Palantir with a value of `ADMIN` or `ROLE_ADMIN` if you are accessing Palantir via external CAS authentication, or your configuration needs to assign the authenticated user role via `spring.security.user.roles=ADMIN`.

At this moment, all Palantir functionality is disabled and hidden for non-admin users, except for the ability to manage the list of registered applications.

### Cluster Topology

A new `clusterTopology` actuator endpoint is available to report on the current cluster topology and the status of each node in the cluster, particularly relevant when CAS is running in high-availability mode. Cluster topology support is available for the following features:

- [MongoDb Ticket Registry](https://apereo.github.io/cas/development/ticketing/MongoDb-Ticket-Registry.html)
- [Redis Ticket Registry](https://apereo.github.io/cas/development/ticketing/Redis-Ticket-Registry.html)
- [Hazelcast Ticket Registry](https://apereo.github.io/cas/development/ticketing/Hazelcast-Ticket-Registry.html)
- [Apache Ignite Ticket Registry](https://apereo.github.io/cas/development/ticketing/Ignite-Ticket-Registry.html)
- [Apache Kafka Ticket Registry](https://apereo.github.io/cas/development/ticketing/Kafka-Ticket-Registry.html)
- [Apache Pulsar Ticket Registry](https://apereo.github.io/cas/development/ticketing/Pulsar-Ticket-Registry.html)
- [Apache Geode Ticket Registry](https://apereo.github.io/cas/development/ticketing/Geode-Ticket-Registry.html)
- [AMQP Ticket Registry](https://apereo.github.io/cas/development/ticketing/Messaging-AMQP-Ticket-Registry.html)

This capability is also supported and available for the [Palantir Admin Dashboard](https://apereo.github.io/cas/development/installation/Admin-Dashboard.html).

### Passwordless Authentication

A dedicated actuator endpoint, `passwordless`, is available to allows one to query a username and retrieve the associated [passwordless account](https://apereo.github.io/cas/development/authentication/Passwordless-Authentication-Account-Storage.html) information. This capability is also supported and available for the [Palantir Admin Dashboard](https://apereo.github.io/cas/development/installation/Admin-Dashboard.html).

### OpenID Connect Verifiable Credentials

[OpenID Connect with Verifiable Credentials](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-Verifiable-Credentials.html) now supports the authorization code flow. There are also significant changes in place to support more formats with additional bug fixes and enhancements, and an improved test suite to verify credential issuance. There is also initial support for OpenID Connect with Verifiable Presentations (OpenID4VP).

### OpenID Connect Federation

Work on [OpenID Connect Federation](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-Federation.html) is now roughly finalized. A number of test scenarios and minor edge cases are still being worked on and should be resolved in the next few releases.

### Acceptable Usage Policy & Multitenancy

[Acceptable Usage Policy (AUP) support](https://apereo.github.io/cas/development/webflow/Webflow-Customization-AUP.html) has been extended to support multitenancy. A number of storage mechanisms are now available to support multitenancy, specifically [MongoDb](https://apereo.github.io/cas/development/webflow/Webflow-Customization-AUP-MongoDb.html) and more will be added in the future.

## Other Stuff

### Authentication

- When removing cookies, particularly during logout, the existing cookie value is no longer echoed back for remove operations.

### Multifactor & Passkeys

- Multifactor authentication may also be activated using [SAML2 metadata entity attributes](https://apereo.github.io/cas/development/mfa/Configuring-Multifactor-Authentication-Triggers-EntityId.html).

### OAuth & OpenID Connect

- Custom ID token claims can also be constructed using [Apache Groovy](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-Claims-Custom.html).

### CAS & Other Protocols

- [CAS REST APIs](https://apereo.github.io/cas/development/protocol/REST-Protocol.html) now return a `403` status code instead of a `500` type of error when unauthorized application requests are identified.

### Attributes & Consent

- Releasing attributes via [pattern matching](https://apereo.github.io/cas/development/integration/Attribute-Release-Policy-PatternMatching.html) accepts Groovy transformation rules.
- Attributes requested for [Consent](https://apereo.github.io/cas/development/integration/Attribute-Release-Consent.html) may now be localized using language bundles and a prefixed language key that is `screen.consent.attributes.attribute.[attribute-name]`.

### Tickets & Sessions

- [RediSearch](https://apereo.github.io/cas/development/ticketing/Redis-Ticket-Registry-RediSearch.html) functionality now supports Redis clustering.
- The maximum lifetime of a transient session ticket (i.e. `TST`) is by default reduced from `15` minutes to `5` minutes.
- [LettuceMod](https://apereo.github.io/cas/development/ticketing/Redis-Ticket-Registry-RediSearch.html) is removed from CAS and its functionality is directly provided by Lettuce itself.

### Build & Project

- A large number of dependencies and libraries have been updated to their latest versions.
- Groovy integration tests have now switched to use Groovy `5.1.x`.
- Redis integration tests have now switched to use Redis `8.10.x`.

### General

- A number of date-formatting operations have switched their base timezone from system default to `UTC`.

## Test Release Candidates Early

CAS releases are strictly time-based, and a `GA` release is [a tag and nothing more](https://apereo.github.io/2017/03/08/the-myth-of-ga-rel/). Trying each release candidate and the snapshots that follow it is the best way to gain confidence in an upgrade.

### Support the Project

Development is almost entirely voluntary. If CAS serves you well, [join the Apereo Foundation](https://www.apereo.org/content/apereo-membership) to fund dedicated time for long-term support, maintenance and timely security fixes.

### Get Involved

- [Share feedback](https://apereo.github.io/cas/Mailing-Lists.html) on the features you try
- [Contribute patches](https://apereo.github.io/cas/developer/Contributor-Guidelines.html) and documentation improvements

### Resources

- [Release schedule](https://github.com/apereo/cas/milestones)
- [Release policy](https://apereo.github.io/cas/developer/Release-Policy.html)
- [Maintenance policy](https://apereo.github.io/cas/developer/Maintenance-Policy.html)
