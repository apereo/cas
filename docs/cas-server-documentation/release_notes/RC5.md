---
layout: default
title: CAS - Release Notes
category: Planning
---

# 7.1.0-RC5 Release Notes

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

### CAS Initializr SBOM Support

CAS Initializr is now modified to generate a Software Bill of Materials (SBOM) using the CycloneDX format. This SBOM can be used to
track and manage the open-source components used in your CAS deployment and may be examined via the `sbom` actuator endpoint.
  
### Encryption Algorithm

The default content encryption algorithm for crypto operations has now switched from `A128CBC-HS256` to `A256CBC-HS512`, which requires a larger key size for better security.
To continue using your existing keys, you would need to instruct CAS to use the previous algorithm by setting the following property:

```properties
cas.[path-to-configuration-key].crypto.alg=A128CBC-HS256
```
  
### Passwordless Authentication with reCAPTCHA

[Passwordless authentication](../authentication/Passwordless-Authentication.html) can now support reCAPTCHA to protect against automated abuse, 
such as credential stuffing attacks.

## Other Stuff

- [Simple Multifactor Authentication](../mfa/Simple-Multifactor-Authentication.html) may prompt the user to proceed to account registration, when no contact information is found.
- ID token `jti` claims in [OpenID Connect](../authentication/OIDC-Authentication.html) are no longer plain ticket (granting-ticket) identifiers but are instead digested using `SHA-512`.
- The `ticketRegistry` [actuator endpoint](../ticketing/Configuring-Ticketing-Components.html) now offers the ability to run the ticket registry cleaner task on-demand.
- Docker Swarm support for Hazelcast has been removed from the CAS codebase. 
- Claims that are collected in a JWT Access Token are now forced to pass through attribute release policies assigned to the application definition.
- WebAuthn registration endpoint is now able to detect and track an authenticated request using the CAS in-progress authentication attempt. 

## Library Upgrades

- Gradle
- AWS SDK
- Spring Shell
- Grouper Client
- Elastic APM
- Spring Boot
- Spring Cloud
- Google Cloud
- Spring Boot Admin
- Nimbus JOSE JWT
- Java Melody
- ErrorProne
- Twilio
- Sentry
- Jakarta Servlet API
- Netty
- Amazon SDK
- Micrometer
