---
layout: default
title: CAS - Release Notes
category: Planning
---

# RC5 Release Notes

We strongly recommend that you take advantage of the release candidates as they come out. Waiting for a `GA` release is only going to set 
you up for unpleasant surprises. A `GA` is [a tag and nothing more](https://apereo.github.io/2017/03/08/the-myth-of-ga-rel/). Note that CAS 
releases are *strictly* time-based releases; they are not scheduled or based on specific benchmarks, statistics or completion of features. To gain 
confidence in a particular release, it is strongly recommended that you start early by experimenting with 
release candidates and/or follow-up snapshots.

## Apereo Membership

If you benefit from Apereo CAS as free and open-source software, we 
invite you to [join the Apereo Foundation](https://www.apereo.org/content/apereo-membership) 
and financially support the project at a capacity that best suits your deployment. Note that all development activity 
is performed *almost exclusively* on a voluntary basis with no expectations, commitments or strings attached. Having the financial means to better 
sustain engineering activities will allow the developer community to allocate *dedicated and committed* time for long-term support, 
maintenance and release planning, especially when it comes to addressing critical and security issues in a timely manner. Funding will 
ensure support for the software you rely on and you gain an advantage and say in the way Apereo, and the CAS project at that, runs 
and operates. If you consider your CAS deployment to be a critical part of the identity and access 
management ecosystem, this is a viable option to consider.

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
cas.version=6.3.0-RC5
```

<div class="alert alert-info">
  <strong>System Requirements</strong><br/>There are no changes to the minimum system/platform requirements for this release.
</div>

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.

## Puppeteer Tests

Documentation is now available to highlight [test processes](../developer/Test-Process.html) used by the 
CAS project and developers/contributors. The newest addition is the availability of functional/browser testing 
mechanisms that are backed by the Puppeteer framework. The test scenarios that are designed are 
executed by the CAS continuous integration system and will be improved over time
to account for advanced use cases such as ensuring protocol compatibility and other variations of the authentication webflow.  

## Apple Signin

[Delegated authentication](../integration/Delegate-Authentication.html) can now hand off 
authentication requests to [sign in with Apple](https://developer.apple.com/sign-in-with-apple/).

## Other Stuff

- [Maven repositories](https://spring.io/blog/2020/10/29/notice-of-permissions-changes-to-repo-spring-io-fall-and-winter-2020) managed by the Spring project are removed from the CAS gradle build. 
- Improvements to [password management](../password_management/Password-Management.html) flows to handle invalid tokens more gracefully, and allow password reset with or without single signon sessions.
- All external links found in the CAS documentation are corrected to point to valid resources. Validation processes are also adjusted to prevent bad links. 

## Library Upgrades

- JRadius
- Hazelcast
- MySQL Driver
- MongoDb Driver
- HSQL Driver
- MariaDb Driver
- Groovy
- Caffeine
- Pac4j
- Hibernate
- Infinispan
- Thymeleaf Dialect
- Micrometer
