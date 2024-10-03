---
layout: default
title: CAS - Release Notes
category: Planning
---

# 7.2.0-RC1 Release Notes

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

The JDK baseline requirement for this CAS release is and **MUST** be JDK `21`. All compatible distributions
such as Amazon Corretto, Zulu, Eclipse Temurin, etc should work and are implicitly supported.

## New & Noteworthy

The following items are new improvements and enhancements presented in this release. 

### Spring Boot 3.4

The migration of the entire codebase to Spring Boot `3.4` is ongoing, and at the moment is waiting for the wider ecosystem 
of supporting frameworks and libraries to catch up to changes. We anticipate the work to finalize in the next few 
release candidates and certainly prior to the final release.
   
The following integrations and extensions remain dysfunctional for now until the underlying library adds
support for the new version of Spring Boot:

1. [Swagger](../integration/Swagger-Integration.html)
2. [Spring Boot Admin](../monitoring/Configuring-SpringBootAdmin.html)

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
and scenarios. At the moment, total number of jobs stands at approximately `490` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`. Furthermore, a large number of test categories that group internal unit tests
are now configured to run with parallelism enabled.
  
### Java 23

As described, the JDK baseline requirement for this CAS release is and **MUST** be JDK `21`. We are still waiting for the
wider ecosystem of supporting frameworks and libraries to catch up to Java `23`. We anticipate the work to finalize in the next few
release candidates and certainly prior to the final release. Remember that the baseline requirement will remain unchanged
and this is just a preparatory step to ensure CAS is ready for the next version of Java.

### Jaeger Distributed Tracing

Jaeger is an open-source distributed tracing platform, that is now 
[supported by CAS](../monitoring/Configuring-Tracing-Jaeger.html) for metrics and monitoring.

### Redis Ticket Registry

The performance of the [Redis Ticket Registry](../ticketing/Redis-Ticket-Registry.html) is improved to remove unnecessary 
classloading, locks and JSON serialization overhead.
         
### Apache Kafka Ticket Registry

Apache Kafka can now be used as a [ticket registry](../ticketing/Kafka-Ticket-Registry.html) to broadcast ticket operations across the cluster.

## Other Stuff
   
- When processing `refresh_token` and `authorization_code` grant types in OAuth2 and OpenID Connect, CAS only requires the requested scopes to be a subset of the granted scopes.
- Support for [reCAPTCHA](../integration/Configuring-Google-reCAPTCHA.html) is now extended to support [Friendly CAPTCHA](https://friendlycaptcha.com/). 
- Content security policy header configuration can now support a dynamic nonce, which is a random value different for every HTTP request.
- Static resources can now be externalized by default via the following additional directories: `file:/etc/cas/static` and `file:/etc/cas/public`.
- The `max-age` cookie setting can now be configured as a duration.
- An optional principal attribute can now be configured to be used as the username sent to [Duo Security](../mfa/DuoSecurity-Authentication.html).
- Response mode handling of OpenID Connect `token` or `id_token` response types is adjusted to build the redirect URL and parameters/fragments correctly.  
- [Palantir Admin Console](../installation/Admin-Dashboard.html) now offers the ability to remove (trusted) registered multifactor authentication devices.
- OAuth access tokens with an expiration policy value of zero now prevent CAS from issuing access tokens altogether. 
- [SAML2 responses](../authentication/Configuring-SAML2-Authentication.html) may now include an additional attribute that carries the satisfied authentication context in addition to the `AuthnContext` element.
- In [delegated authentication](../integration/Delegate-Authentication.html), disabling SSO for a registered service definition will allow CAS to instruct the external identity provider to ask for user credentials.
- Removing a number of duplicate dependencies in the final CAS web application artifact.
- A series of small bug fixes related to the management of SAML2 identity provider metadata via Amazon S3 buckets.

## Library Upgrades

- Spring Boot
- Spring 
- Spring Session
- Spring Integration
- Spring Kafka
- Spring Cloud
- Spring Data
- Spring AMQP
- Spring Security
- HikariCP
- Micrometer
- ErrorProne
- Apache Log4j
- Logback
- Jackson
- Mockito
- JUnit
- Amazon SDK
- Gradle
- JGit
- Twilio
- Apache Tomcat

