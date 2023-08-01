---
layout: default
title: CAS - Release Notes
category: Planning
---

# 7.0.0-RC8 Release Notes

We strongly recommend that you take advantage of the release candidates as they come out. Waiting for a `GA` release is only going to set
you up for unpleasant surprises. A `GA` is [a tag and nothing more](https://apereo.github.io/2017/03/08/the-myth-of-ga-rel/). Note
that CAS releases are *strictly* time-based releases; they are not scheduled or based on specific benchmarks,
statistics or completion of features or bug fixes. To gain confidence in a particular
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
- [Support](https://apereo.github.io/cas/Support.html)

## System Requirements

The JDK baseline requirement for this CAS release is and **MUST** be JDK `21`. All compatible distributions
such as Amazon Corretto, Zulu, Eclipse Temurin, etc should work and are implicitly supported.

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.

### Spring Framework Upgrades

Starting with this release candidate and going forward, CAS will switch to building against Spring Framework `6.1.x`
and Spring Boot `3.2.x` milestone builds.

The Spring framework `6.1.x` generation presents with the following feature themes:

- Embracing JDK `21` LTS
- Virtual Threads (Project Loom)
- JVM Checkpoint Restore (Project CRaC)
- Data Binding and Validation, revisited

Note that Spring Framework `6.1` provides a first-class experience on JDK `21` and Jakarta EE 10 at
runtime while retaining a JDK `17` and Jakarta EE `9` baseline. We also embrace the latest edition of
Graal VM for JDK 17 and its upcoming JDK `21` version while retaining compatibility with GraalVM `22.3`.

As stated above, it is likely that CAS `7` would switch to using JDK `21` as its baseline
in the next few release candidates.

## Other Stuff


## Library Upgrades
