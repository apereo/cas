---
layout: default
title: CAS - Release Notes
category: Planning
---

{% include variables.html %}

# 8.0.0-RC1 Release Notes

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

The JDK baseline requirement for this CAS release is and **MUST be JDK `25`**. All compatible distributions
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
and scenarios. At the moment, total number of jobs stands at approximately `526` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`. Furthermore, a large number of test categories that group internal unit tests
are now configured to run with parallelism enabled.

### Gradle 9.3

CAS is now built with Gradle `9.3` and the build process has been updated to use the latest Gradle 
features and capabilities. 
 
### Configuration Properties

CAS configuration properties, specifically those that belong to the `cas` namespace and begin with `cas.`
are now strictly and forcefully validated at startup to reject any unknown properties.
This is done to prevent misconfigurations and typos in property names that would otherwise go unnoticed.

<div class="alert alert-warning">:warning: <strong>Pay Attention</strong><p>
Unknown configuration properties that are rejected will prevent CAS from starting up. Take time to
go through the list of unknown settings and make adjustments. If you are using the <code>cas</code>
configuration namespaces for custom extensions, those most likely will need to be moved and reworked.</p></div>

### Project Leyden & AOT Caching

Functional tests are updated to use Project Leyden, AOT compilation and caching techniques that are offered
by JDK `25`. This allows tests to run faster by pre-compiling and caching classes and resources ahead of time.
A comparable CAS deployment now roughly takes `5` seconds to startup and be ready to serve requests
as opposed to the previous `7~9` seconds in earlier runs and with previous JDK versions.
     
Support for this functionality will ultimately supersede CDS that is now available to 
[CAS overlay installations](../installation/WAR-Overlay-Initializr.html).

### Google Authenticator Scratch Codes

When storing Google Authenticator accounts inside a relational database, the database column that holds
the scratch codes is now changed to use a `VARCHAR` type to accommodate longer values of scratch codes,
particularly if the codes are set to be encrypted.

<div class="alert alert-warning">:warning: <strong>Breaking Change</strong><p>
This may be a breaking change. You will need to adjust your database schema based on the notes above.</p></div>

### Palantir Admin Dashboard

[Palantir Admin Console](../installation/Admin-Dashboard.html) now supports a wizard-based web editor
that allows **registering** registered services.

<div class="alert alert-warning">:warning: <strong>Usage</strong><p>
This functionality is brand new, and may still have many rough edges. Not every configuration property and setting
that can presently be assigned to an application record is available via this wizard. We expect this functionality
to improve and mature over time in the coming releases and based on user demand and sponsorship.
</p></div>

### Apache Pulsar Ticket Registry

[Apache Pulsar Ticket Registry](../ticketing/Pulsar-Ticket-Registry.html) is now available as a new ticket registry option
that allows CAS nodes to broadcast ticket operations to other nodes in the cluster using Apache Pulsar topics.

### OpenID Connect JWT Authorization Grant

CAS now supports the [JWT Authorization Grant](../authentication/OIDC-Authentication-JWT-Bearer.html)
(also known as JWT Bearer Token grant identified as `urn:ietf:params:oauth:client-assertion-type:jwt-bearer`) as a new grant type
that allows a client application to obtain an access token by presenting a JWT assertion to the CAS server,
instead of using a username/password or client credentials. This functionality is also supported by the
[Heimdall authorization engine](../authorization/Heimdall-Authorization-Overview.html).
 
### Simple Multifactor Authentication & Rate Limiting

Rate limiting for [Simple Multifactor Authentication](../mfa/Simple-Multifactor-Authentication-RateLimiting.html) 
is now extended to support token validation attempts in addition to token generation requests.
 
### Password Management

[Password management operations](../password_management/Password-Management.html) are internally 
modified to support multiple backend account management systems. 
This means that you may enable multiple password management modules backed by different account stores such as 
LDAP and JDBC at the same time and CAS would force a chain to validate password policies across all such systems.
 
### Apache Ignite Ticket Registry

[Apache Ignite Ticket Registry](../ticketing/Ignite-Ticket-Registry.html) is now upgraded to support
the latest version of Apache Ignite, `3.1.x`.

### Single SignOn Sessions Per User
                
Many of the ticket registry implementations (i.e. MongoDb, Redis, JPA, etc) are 
extended to allow for removal of all tickets that were issued for a given
principal based on the ticket's attached authentication attempt. The `ssoSessions` endpoint is also modified
to support removing all such tickets when a single sign-on session is terminated for a user.

This allows for child/descendant tickets of a `ticket-granting-ticket` to be cleaned up 
when an SSO session is terminated for a user forcefully, specially when such tickets are not explicitly tracked by
the parent `ticket-granting-ticket` and are configured to outlive the parent ticket's lifetime. A practical example
of this, relevant configuration options permitting and activated, is OAuth2 refresh tokens that may 
continue to perform even after the user logs out and terminates their SSO session.
 
### JSpecify & NullAway

CAS codebase is now annotated with [JSpecify](https://jspecify.dev/) annotations to indicate nullness contracts on method parameters,
return types and fields. We will gradually extend the coverage of such annotations across the entire codebase in future releases
and will integrate the Gradle build tool with tools such as [NullAway](https://github.com/uber/NullAway) to prevent nullness contract violations
during compile time.

### Spring Boot 4

CAS is now built with Spring Boot `4.0.x`. This is a major platform upgrade that affects almost all aspects of the codebase
including many of the third-party core libraries used by CAS as well as some CAS functionality. The following
notable changes are worth mentioning.

#### Retry Functionality

The `spring-retry` library has been removed and replaced with the native retry capabilities
provided by the Spring Framework. The CAS retry configuration has been updated to reflect this change, which 
brings about the following minor change: retry attempts are set to **ONLY** affect retry operations, and do
not count the initial execution attempt. This means that if a CAS operation is set to retry `3` times, the
operation will be attempted a total of `4` times (`1` initial + `3` retries).

#### Undertow

[Support for Undertow](../installation/Configuring-Servlet-Container-Embedded-Undertow.html) 
as an embedded servlet container has been dropped. Please consider using Apache Tomcat or Jetty
as an alternative embedded server until Undertow adds support for Servlet `6.1`.
We will consider re-adding support for Undertow once it is compatible with our version of Spring Boot.

#### Google Cloud Platform

Almost all functionality that builds on top of Google Cloud platform is not yet **fully** compatible with Spring Boot `4`.
YMMV. We plan to re-add support for Google Cloud features in the future once the `spring-cloud-gcp` library is compatible
with Spring Boot `4`.

#### SpringBoot Admin

Support for [SpringBoot Admin](../monitoring/Configuring-SpringBootAdmin.html) is not yet compatible with Spring Boot `4`.
We plan to re-add support for Spring Boot Admin in the future once compatibility is restored.
      
#### Spring Session 

[Spring Session with MongoDb](../webflow/Webflow-Customization-Sessions-ServerSide-MongoDb.html) 
is not yet compatible with Spring Boot `4`. We plan to re-add support for these features 
in the future once compatibility is restored.

#### Jackson & JSON Processing

The Jackson library, responsible for JSON processing and parsing in CAS, is upgraded to its `3.x` version. 
This is a major upgrade that brings in many significant changes to the way JSON is processed in CAS. Almost all 
such changes are internal and **SHOULD NOT** affect how CAS configuration, application 
records, etc are processed and loaded.

## Other Stuff

- [JPA Ticket Registry](../ticketing/JPA-Ticket-Registry.html) will lowercase all table names to avoid issues with
  case sensitivity in certain database engines, namely MariaDb.
- PostgreSQL `18` is now the default PostgreSQL version for integration tests.
- Redis `8.4` is now the default Redis version for integration tests.
- Bitnami legacy docker images are now replaced with official docker images used for integration tests.
- A large number of deprecated classes, methods and configuration properties have been removed.
- Attribute values that are presented as valid JSON documents will be formatted as nested claims when collected into an [OpenID Connect ID token](../authentication/OIDC-Authentication-Claims.html).
- The ability to prepend a *launch script* to the CAS WAR overlay distribution and have it run in a fully standalone mode is removed from Spring Boot and thus has been removed from CAS as well.
- Most Redis operations that rely on the `KEYS` command have been replaced with `SCAN` operations to avoid performance issues on large datasets.
- A new theme option is now available to control whether client-side device fingerprinting is activated during authentication.
- [CosmosDb Ticket Registry](../ticketing/CosmosDb-Ticket-Registry.html) is deprecated and will be removed in a future release.
- [CosmosDb Service Registry](../services/CosmosDb-Service-Management.html) is deprecated and will be removed in a future release.
- The order of authentication handlers is now correctly preserved when authentication handlers are registered with the authentication plan.
- Logging out of [Account Profile](../registration/Account-Management-Overview.html) will now initiate SLO if configured.
- Memory leak fixes when managing virtual threads responsible for file monitoring features and watch services.
- Spring Webflow actions may optionally run in the context of a transaction if the underlying action is configured to do so.
- [OpenID Connect Claims Mapping rules](../authentication/OIDC-Authentication-Claims-Mapping.html) now support external Groovy scripts.
- [Releasing a principal id based on attributes](../integration/Attribute-Release-PrincipalId-Attribute.html) now supports multiple comma-separated attributes as fallback options.
