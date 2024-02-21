---
layout: default
title: CAS - Release Notes
category: Planning
---

# 7.1.0-RC2 Release Notes

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

### Spring Boot 3.3

The migration of the entire codebase to Spring Boot `3.3` is ongoing, and at the
moment is waiting for the wider ecosystem of supporting frameworks and libraries to catch up to
changes. We anticipate the work to finalize in the next few release candidates and certainly prior to the final release.

### Graal VM Native Images

A CAS server installation and deployment process can be tuned to build and run
as a [Graal VM native image](../installation/GraalVM-NativeImage-Installation.html).
The collection of end-to-end [browser tests based on Puppeteer](../../developer/Test-Process.html) have selectively switched
to build and verify Graal VM native images and we plan to extend the coverage to all such scenarios in the coming releases.

### Testing Strategy

The collection of end-to-end [browser tests based via Puppeteer](../../developer/Test-Process.html) continue to grow to cover more use cases
and scenarios. At the moment, total number of jobs stands at approximately `475` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`. Furthermore, a large number of test categories that group internal unit tests
are now configured to run with parallelism enabled.
  
Finally, the overall execution of the [browser tests based via Puppeteer](../../developer/Test-Process.html) is reduced
by approximately `90` seconds for every rune of the test suite by removing unnecessary *wait* times and delays in test scenarios.

### OAuth/OpenID Connect Token Exchange

[OAuth Token Exchange](../authentication/OAuth-ProtocolFlow-TokenExchange.html) protocol can now support ID token exchanges
when CAS is running as an OpenID Connect provider..
  
### Devlocity Predictive Test Selection

[Develocity Predictive Test Selection](https://develocity.apereo.org/scans/test-selection) is now turned on for certain number
of CAS unit test categories. This feature saves testing time by identifying, prioritizing, and running only 
tests that are likely to provide useful feedback during test runs and it accomplishes 
this by applying a machine learning model that uniquely incorporates fine-grained code snapshots, 
comprehensive test analytics, and flaky test data.

To accommodate the machine learning model, CAS will also run its suite of unit tests using a fixed schedule.
The scheduled workflow runs disable the predictive test selection feature and instead run all tests in the suite to
avoid accidents and mistakes made when a test run is incorrectly skipped. As the number of runs increase, 
we expect the model to improve and learn more efficiently.

This capability is provided by [Devlocity](https://gradle.com/develocity/) to the Apereo CAS project for free and is proving
to be extremely valuable in cutting down test execution time and therefor resulting in a quicker feedback loop. 

### Startup Time Improvements

CAS web application startup time has been improved by approximately `3` seconds by removing unnecessary initializations.
Notable changes in this area include:

- Removing unnecessary I/O operations during startup to verify existence of embedded application resources on the classpath.
- Removing the `org.webjars:webjars-locator-core` library which does classpath scanning at startup to locate assets.
- ...and as a result, the *Hal Browser* interface that listed the CAS actuator endpoints has been removed.
- Delaying the reconstruction of the CAS webflow execution plan until the application container is fully ready.

## Other Stuff

- Internal enhancements to allow a few more ticket registries to support more advanced querying operations and session management features.
- [Redis Ticket Registry](../ticketing/Redis-Ticket-Registry.html) correctly sets the expiration time for principal records tied to ticket objects.           
- [LDAP Passwordless Authentication](../authentication/Passwordless-Authentication-Storage-LDAP.html) can be configured to require specific user attributes and values before triggering the flow.
- [Account Profile Management](../registration/Account-Management-Overview.html) can now display the list of access tokens that are issued for an authenticated user.
- [MDC logging](../logging/Logging-MDC.html) gains options to control what parameters or headers should be excluded from the logging output.

## Library Upgrades
           
- AWS SDK
- Slack
- Twilio
- Commons Codec
- Spring Cloud CosmosDb
- Spring Data CosmosDb
- Spring Boot
- Puppeteer
- Spring Data
- Micrometer
- Jose4j
- Elastic APM
- SendGrid
- JavaMelody
- Apache Tomcat
- Logback
- Apache Log4j
- MariaDb
- Gradle
- PostgreSQL
