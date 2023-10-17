---
layout: default
title: CAS - Release Notes
category: Planning
---

# 7.0.0-RC9 Release Notes

We strongly recommend that you take advantage of the release candidates as they come out. Waiting for a `GA` release is only going to set
you up for unpleasant surprises. A `GA` is [a tag and nothing more](https://apereo.github.io/2017/03/08/the-myth-of-ga-rel/). Note
that CAS releases are *strictly* time-based releases; they are not scheduled or based on specific benchmarks,
statistics or completion of features or bug fixes. To gain confidence in a particular
release, we recommended that you start early by experimenting with release candidates and/or follow-up snapshots.

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
- [Support](https://apereo.github.io/cas/Support.html)

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

### CAS Initializr & OpenRewrite

[CAS Initializr](../installation/WAR-Overlay-Initializr.html) is now able to produce upgrade recipes based on [OpenRewrite](https://docs.openrewrite.org/).
The goal is to allow for in-place automatic upgrades using recipes that understand the differences and nuances from one CAS version to the next.

<div class="alert alert-info">:information_source: <strong>Rough Waters Ahead</strong><p>
Like a cake still baking in the oven, this feature is a masterpiece in the making. It might be still a little doughy and 
half-baked, so approach with care and a pinch of curiosity. </p></div>

### Testing Strategy

The collection of end-to-end [browser tests based on Puppeteer](../../developer/Test-Process.html) continue to grow to cover more use cases
and scenarios. At the moment, total number of jobs stands at approximately `441` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`. Furthermore, a large number of test categories that group internal unit tests
are now configured to run with parallelism enabled.

### Account Profile & Multifactor Authentication

[Account profile management](../registration/Account-Management-Overview.html) is now able to allow users to register their own multifactor authentication devices.
Registration flows are supported for [Google Authenticator](../mfa/GoogleAuthenticator-Authentication.html) 
and [FIDO2 WebAuthn](../mfa/FIDO2-WebAuthn-Authentication-Registration.html).

### Refactoring & Cleanup

There has been lots of internal cleanup, refactoring and build system improvements to remove assumptions and hard dependencies between CAS modules.
As a result, about **33 MB worth of cruft** has been removed from the CAS web application and the final packaged artifact is now leaner. While the changes
should generally prove invisible to the deployer, functionality that deals with fetching attributes from LDAP and Active Directory
systems should be reviewed carefully to make sure [the correct module listed here](../integration/Attribute-Resolution-LDAP.html) is included in the build
where and when necessary.

### OpenID Connect Access Token Authentication

Requests to the OpenID Connect access token endpoint have started to enforce the required authentication method
assigned to the relying party and registered service definition. The default authentication method has always been `client_secret_basic`
and the endpoint will begin to enforce this for all requests. 

## Other Stuff

- When using [OpenID Connect](../protocol/OIDC-Protocol.html), requests that carry a `client_secret` as a querystring parameter are now rejected and/or ignored.
- Changes to Spring Security configuration to allow basic authentication requests to pass through correctly to CAS endpoints where appropriate.
- Apache Shiro, only used to assist with database password encoding and hashing functions, is now fully removed from CAS codebase.
- A new and somewhat humble [actuator endpoint](../ticketing/Configuring-Ticketing-Components.html) to interact with and query the CAS ticket registry.
- X.509 client authentication is now available as an authentication for the access token endpoint in [OpenID Connect](../protocol/OIDC-Protocol.html). 

## Library Upgrades
  
- Jacoco 
- Jackson
- Spring Boot
- Spring
- Spring Data
- Spring Security
- Apache Log4j
- Spring Integration
- Hjson
- Apache Tomcat
- Pac4j
- Spring Shell
