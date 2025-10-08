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

The JDK baseline requirement for this CAS release is and **MUST** be JDK `25`. All compatible distributions
such as Amazon Corretto, Zulu, Eclipse Temurin, etc should work and are implicitly supported.

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.

### Java 25

As described, the JDK baseline requirement for this CAS release is and **MUST** be JDK `25`. 

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

### Gradle 9.2

CAS is now built with Gradle `9.1` and the build process has been updated to use the latest Gradle 
features and capabilities. 

### Spring Boot 4

CAS is now built with Spring Boot `4`. This is a major platform upgrade that affects almost all aspects of the codebase
including many of the third-party core libraries used by CAS as well as some CAS functionality. The following
notable changes are worth mentioning:

#### Retry Functionality

The `spring-retry` library has been removed and replaced with the native retry capabilities
provided by the Spring Framework. The CAS retry configuration has been updated to reflect this change, which 
brings about the following minor change: retry attempts are set to **ONLY** affect retry operations, and do
not count the initial execution attempt. This means that if a CAS operation is set to retry `3` times, the
operation will be attempted a total of `4` times (`1` initial + `3` retries).

#### Undertow Support

[Support for Undertow]((../installation/Configuring-Servlet-Container-Embedded-Undertow.html)) 
as an embedded servlet container has been dropped. Please consider using Apache Tomcat or Jetty
as an alternative embedded server until Undertow adds support for Servlet `6.1`.
We will consider re-adding support for Undertow once it is compatible with Spring Boot.

#### JSON Processing w/ Jackson

The Jackson library, responsible for JSON processing and parsing in CAS, is upgraded to `3.x` version. 
This is a major upgrade that brings in many significant changes to the way JSON is processed in CAS. Almost all 
such changes are internal and **SHOULD NOT** affect CAS configuration, application 
records, etc are processed and loaded.

## Other Stuff
