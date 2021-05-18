---
layout: default 
title: CAS - Release Notes
category: Planning
---

# RC4 Release Notes

We strongly recommend that you take advantage of the release candidates as they come out. Waiting for a `GA` release is only going to set
you up for unpleasant surprises. A `GA` is [a tag and nothing more](https://apereo.github.io/2017/03/08/the-myth-of-ga-rel/). Note that CAS
releases are *strictly* time-based releases; they are not scheduled or based on specific benchmarks, statistics, or completion of features.
To gain confidence in a particular release, it is strongly recommended that you start early by experimenting with release candidates and/or
follow-up snapshots.

## Apereo Membership

If you benefit from Apereo CAS as free and open-source software, we invite you
to [join the Apereo Foundation](https://www.apereo.org/content/apereo-membership)
and financially support the project at a capacity that best suits your deployment. Note that all development activity is performed *almost
exclusively* on a voluntary basis with no expectations, commitments or strings attached. Having the financial means to better sustain
engineering activities will allow the developer community to allocate *dedicated and committed* time for long-term support, maintenance and
release planning, especially when it comes to addressing critical and security issues in a timely manner. Funding will ensure support for
the software you rely on and you gain an advantage and say in the way Apereo, and the CAS project at that, runs and operates. If you
consider your CAS deployment to be a critical part of the identity and access management ecosystem, this is a viable option to consider.

## Get Involved

- Start your CAS deployment today. Try out features and [share feedback](/cas/Mailing-Lists.html).
- Better yet, [contribute patches](/cas/developer/Contributor-Guidelines.html).
- Suggest and apply documentation improvements.

## Resources

- [Release Schedule](https://github.com/apereo/cas/milestones)
- [Release Policy](/cas/developer/Release-Policy.html)

## Overlay

In the `gradle.properties` of the [CAS WAR Overlay](../installation/WAR-Overlay-Installation.html), adjust the following setting:

```properties
cas.version=6.4.0-RC4
```

Alternatively and for new deployments, [CAS Initializr](../installation/WAR-Overlay-Initializr.html) has been updated and can also be used
to generate an overlay project template for this release.

<div class="alert alert-info">
  <strong>System Requirements</strong><br/>There are no changes to the minimum system/platform requirements for this release.
</div>

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.

### FIDO2 Discoverable Credentials

[FIDO2 WebAuthn Multifactor Authentication](../mfa/FIDO2-WebAuthn-Authentication.html) is now able to support Discoverable Credentials /
Resident Keys.

### Authentication Interrupts

[Authentication Interrupts](../webflow/Webflow-Customization-Interrupt.html) are now able to execute and trigger after the single sign-on
session.

### CodeCov Test Coverage

CAS test coverage across all modules in the codebase has now reached `91%` and continues to climb.

### OpenID Connect Conformance

Initial groundwork to integrate the OpenID Connect conformance test suite with the CAS CI to then ensure compatibility and compliance of the
CAS implementation with the OpenID Connect test suite.

### AWS Integration

A dedicated [integration module](../integration/AWS-Integration.html) and endpoint is now available to obtain temporary AWS access
credentials from AWS STS.

### Duo Security Passcode Authentication

Multifactor authentication with [Duo Security](../mfa/DuoSecurity-Authentication.html) is now given the ability to support passcodes as
credentials, mainly for CLI or REST-based authentication requests.

### Authentication Handler States

Most (but not all) authentication handlers are given the ability to remain in `STANDBY` mode, which puts 
them in a semi-enabled state. A handler in `STANDBY` is ready to be invoked with specifically called upon, but 
will not be auto-included in the list of eligible authentication handlers globally and by default.

### Multifactor Authentication via REST Protocol

<div class="alert alert-info">
  <strong>Workers Ahead</strong><br/>This is a work-in-progress and will be improved and refined in future releases. 
</div>

Operations provided by the [CAS REST Protocol](../protocol/REST-Protocol.html) that allow one to
[authenticate credentials](../protocol/REST-Protocol-CredentialAuthentication.html)
or [generate ticket-granting tickets](../protocol/REST-Protocol-Request-TicketGrantingTicket.html) are now put through multifactor
authentication, if the authentication request is deemed eligible via the
configured [multifactor authentication triggers](../mfa/Configuring-Multifactor-Authentication-Triggers.html).

### JPA Service Registry Performance

JPA data models and mapping relationships for registered services have changed to improve performance of
the [JPA service registry](../services/JPA-Service-Management.html). Previous releases struggled this area to load
`1000` registered service definitions in under `3` minutes. Changes to the data models have significantly improved
performance and allow CAS to load and/or store `10,000` registered service definitions in `5` seconds or less.

<div class="alert alert-warning">
  <strong>WATCH OUT!</strong><br />This is a breaking change. The underlying data models and repository implementations that manage 
registered service records are now changed to allow for more performant load operations and as a result, database schemas have 
been altered. If you are managing application records in a database, you should take advantage of import/export operations provided by
CAS to export services from the existing database and import them back into the table structures.
</div>

### JPA Ticket Registry Performance

Similar to the above entry, JPA data models and mapping relationships for CAS tickets have changed to improve performance of
the [JPA ticket registry](../ticketing/JPA-Ticket-Registry.html). The main change in the underlying data model is the removal
of binary objects and blobs in favor of JSON serialization.

## Other Stuff

- CAS CI builds are updated to ensure all web application types can be deployed successfully via external servlet containers.
- System properties used during the build now use the proper Gradle API to respect the configuration cache.
- Puppeteer tests to ensure authentication interrupts do function correctly with or without authentication warnings.
- The `authenticationHandlers` actuator endpoint is corrected to respond with the collection of registered authentication handlers.
- The `xml-apis` module dependency is now removed from the CAS dependency graph.
- CAS themes are now able to turn off core Javascript/CSS libraries with a dedicated setting.
- Minor improvements to SAML2 SLO responses to handle and recognize `Asynchronous` logout requests.
- Puppeteer tests to ensure actuator endpoints can produce the expected output for `GET/READ` operations.
- Puppeteer tests to password reset flows with security questions.
- Git-based integrations are given the option to specify the HTTP client used for remote operations.
- Multiple Javascript and/or CSS files can be specified in theme configurations, separated via comma.
- JDBC/JPA integrations are given the option to specify the database `fetch-size`. Furthermore, a number of JPA collections and
  relationships associated with registered services are now marked as lazy to improve query performance for load operations.
- A new `casModules` [actuator endpoint](../configuration/Configuration-Metadata-Repository.html) to output the collection of CAS modules
  activated and included at runtime.
- Various actuator endpoints such as those that manage YubiKey, Consent or Google Authenticator accounts are 
  given export/import options to facilitate data management and upgrades. Also existing import/export actuator endpoints for
  registered services are now merged back into the `registeredServices` endpoint with dedicated paths.
- Registered classes with [Kryo](../ticketing/Memcached-Ticket-Registry.html) are now sorted by name to force a registration order explicitly.
- Authentication handlers for [Duo Security](../mfa/DuoSecurity-Authentication.html) are now registered 
  using their name and not the multifactor provider id. Application registration records may need slight adjustments if 
  Duo Security is specified as a required authentication handler in a service definition body.

## Library Upgrades

- JUnit
- Apache Tomcat
- Infinispan
- JSON Smart
- Twilio
- Couchbase
- Kryo
- Mockito
- Pac4j
- Material Web Components
- Spring Boot Admin
- Spring Session
- Spring Data
- Infinispan
- Amazon SDK
- Spring
- Spring Boot
- DropWizard
- Gradle
