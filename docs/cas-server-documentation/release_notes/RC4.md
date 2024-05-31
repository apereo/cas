---
layout: default
title: CAS - Release Notes
category: Planning
---

# 7.1.0-RC4 Release Notes

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
- [Maintenance Policy](/cas/developer/Maintenance-Policy.html)

## System Requirements

The JDK baseline requirement for this CAS release is and **MUST** be JDK `21`. All compatible distributions
such as Amazon Corretto, Zulu, Eclipse Temurin, etc should work and are implicitly supported.

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.

### Spring Boot 3.3.x

The migration of the entire codebase to Spring Boot `3.3.x` is now complete, and we'll continue to watch for the wider 
ecosystem of supporting frameworks and libraries to catch up to changes. 

### Graal VM Native Images

A CAS server installation and deployment process can be tuned to build and run
as a [Graal VM native image](../installation/GraalVM-NativeImage-Installation.html).
The collection of end-to-end [browser tests based on Puppeteer](../../developer/Test-Process.html) have selectively switched
to build and verify Graal VM native images and we plan to extend the coverage to all such scenarios in the coming releases.

### Testing Strategy

The collection of end-to-end [browser tests based via Puppeteer](../../developer/Test-Process.html) continue to grow to cover more use cases
and scenarios. At the moment, total number of jobs stands at approximately `482` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`. Furthermore, a large number of test categories that group internal unit tests
are now configured to run with parallelism enabled.

### OpenID Connect CIBA Flow

[Client-Initiated Backchannel Authentication (CIBA)](../authentication/OIDC-Authentication-CIBA.html) is 
an OpenId Connect authentication flow in which RPs, that can obtain a valid 
identifier for the user they want to authenticate, will be able to initiate an interaction flow to authenticate their 
users without having end-user interaction from the consumption device. The flow involves direct communication from the 
Client to CAS without redirect through the user's browser (consumption device).

## Other Stuff
          
- CAS offers options to control SNI Host Checking for [Jetty](../installation/Configuring-Servlet-Container-Embedded-Jetty.html) when used as an embedded container.
- Selecting an [authentication source](../authentication/Configuring-Authentication-Policy-SourceSelection.html) during login attempts will force CAS to use that source, disregarding other sources allowed via the application's authentication policy.
- The dependency graph for CAS libraries and dependencies is now published to GitHub.
- [Redis ticket registry](../ticketing/Redis-Ticket-Registry.html) can now support [idle or moving](../ticketing/Configuring-Ticket-Expiration-Policy-TGT.html) ticket expiration policies.
- Device registration and management for multifactor authentication is disabled for YubiKey, Google Authenticator and WebAuthN providers during [password reset](../password_management/Password-Management-Reset.html) operations.
- Trusted devices for multifactor authentication are now disabled and ignored during password reset operations.
- The password management flow is heavily reworked to better support multifactor authentication flows.
- [Account management profile](../registration/Account-Management-Overview.html) will no longer list duplicate multifactor authentication devices when multiple providers are in effect. 
- Registering multiple WebAuthN devices in account management profile is now corrected to use the correct configuration property for activation.
- Calculation a [device fingerprint](../mfa/Multifactor-TrustedDevice-Authentication-DeviceFingerprint.html) for multifactor authentication trusted devices can use client-side technology to build a browser fingerprint.
- Impersonation conditions can now be controlled via [principal attributes](../authentication/Surrogate-Authentication-AccountSelection.html) to allow for more fine-grained control.
- CAS will forcefully not load services that might have been assigned a blank `id`, `name` or `serviceId` during service registration and loading.

## Library Upgrades

- Gradle
- Spring Boot
- Spring
- Spring Retry
- Spring Data
- Spring WS
- Spring Security
- Spring Rabbit
- Spring Session
- Spring Integration
- Spring Kafka
- Apache Cassandra
- CosmosDb
- InfluxDb
- Undertow
- Amazon SDK
- Ldaptive
- NodeJS
- Nimbus JOSE
- Nimbus OIDC
- Node.js
- Jackson
- Swagger
- Hibernate
- ACME
- Oracle JDBC Driver
- Google Cloud Monitoring
- Google Firebase
- Jakarta Validation

