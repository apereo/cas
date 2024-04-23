---
layout: default
title: CAS - Release Notes
category: Planning
---

# 7.1.0-RC3 Release Notes

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

### Java 22

CAS is able to successfully build and run with Java `22`, should you decide to switch. All compatible distributions
such as Amazon Corretto, Zulu, Eclipse Temurin, etc should work and are implicitly supported.

### Graal VM Native Images

A CAS server installation and deployment process can be tuned to build and run
as a [Graal VM native image](../installation/GraalVM-NativeImage-Installation.html).
The collection of end-to-end [browser tests based on Puppeteer](../../developer/Test-Process.html) have selectively switched
to build and verify Graal VM native images and we plan to extend the coverage to all such scenarios in the coming releases.

### Testing Strategy

The collection of end-to-end [browser tests based via Puppeteer](../../developer/Test-Process.html) continue to grow to cover more use cases
and scenarios. At the moment, total number of jobs stands at approximately `476` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`. Furthermore, a large number of test categories that group internal unit tests
are now configured to run with parallelism enabled.

### AWS Cloudwatch Metrics

CAS metrics can now be exported to [AWS Cloudwatch](../monitoring/Configuring-Metrics-Storage-Cloudwatch.html).

### Person Directory

The [Person Directory](https://github.com/apereo/person-directory) project is now cleaned up and merged into the CAS codebase.
The separate project and repository will no longer be maintained or managed separately, and all relevant components, resolvers
and features are refactored, cleaned up and hand-picked to be part of the CAS codebase. While changes should largely be invisible
to the typical user, the refactoring and cleanup should make it significantly easier to maintain and manage the codebase moving forward.

## Library Upgrades

- Sentry
- JAXB
- Yubico WebAuthN
- Google Cloud Monitoring
- Amazon SDK
- Gradle
- OpenSAML
- Twilio
- Jackson
- ErrorProne
- SpringDoc
- Apache ZooKeeper
- Apache CXF
- Micrometer
- Spring
- Spring Security
- Spring Integration
- Spring Boot
- Spring Boot Admin
- Spring Data
- Apache Tomcat
- Slack
- Pac4j
- Logback
- Azure Identity
