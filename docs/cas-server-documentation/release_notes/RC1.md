---
layout: default
title: CAS - Release Notes
category: Planning
---

# 7.1.0-RC1 Release Notes

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

### OpenRewrite Upgrade Recipes

A carry-over item from the previous release and while not exactly new, CAS has started to produce 
and publish [OpenRewrite](https://docs.openrewrite.org/) recipes that allow the project to upgrade installations 
in place from one version to the next. [See this guide](../installation/OpenRewrite-Upgrade-Recipes.html) to learn more.

### Graal VM Native Images

A CAS server installation and deployment process can be tuned to build and run
as a [Graal VM native image](../installation/GraalVM-NativeImage-Installation.html).
The collection of end-to-end [browser tests based on Puppeteer](../../developer/Test-Process.html) have selectively switched
to build and verify Graal VM native images and we plan to extend the coverage to all such scenarios in the coming releases.

### Testing Strategy

The collection of end-to-end [browser tests based on Puppeteer](../../developer/Test-Process.html) continue to grow to cover more use cases
and scenarios. At the moment, total number of jobs stands at approximately `463` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`. Furthermore, a large number of test categories that group internal unit tests
are now configured to run with parallelism enabled.

### Attribute Release Activation Criteria

Attribute release policies can now be [activated conditionally](../integration/Attribute-Release-Policy-Activation.html). 
This feature allows one to define conditions that must be met before the policy is chosen and activated to start processing attribute release rules.

### Deprecations & Removals

- Dedicated GoogleApps support, deprecated since CAS `6.2.x`, is now removed from the CAS codebase.
- The `RegexRegisteredService` service type, deprecated since CAS `6.6.x`, is now removed from the CAS codebase.

## Other Stuff

- Internal cleanup and refactoring efforts to remove duplicate code, particularly when it comes to grouping `@AutoConfiguration` components.
- Changes to the CAS Gradle build to allow more efficient caching of configuration and build artifacts and reduce overall build times.

## Library Upgrades

- Spring Framework
- Gradle
- ErrorProne
- Spring Security
- Spring Boot
- Twilio
- Pac4j

