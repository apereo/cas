---
layout: default
title: CAS - Release Notes
category: Planning
---

# 7.3.0-RC2 Release Notes

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

### Spring Boot 3.5

The migration of the entire codebase to Spring Boot `3.5` is now complete and CAS is now running
on Spring Boot `3.5.x`. 

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
and scenarios. At the moment, total number of jobs stands at approximately `518` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`. Furthermore, a large number of test categories that group internal unit tests
are now configured to run with parallelism enabled.

### Java 24

As described, the JDK baseline requirement for this CAS release is and **MUST** be JDK `21`. CAS is now able to
build and run using Java `24`. Once more, remember that the baseline requirement will remain unchanged
and this is just a preparatory step to ensure CAS is ready for the next version of Java.
 
### Multitenancy

Multitenancy support is improved to support attribute resolution per each tenant. Supported modules include:

- [REST](../integration/Attribute-Resolution-REST.html)
- [Stub](../integration/Attribute-Resolution-Stub.html)
- [LDAP](../integration/Attribute-Resolution-LDAP.html)
- [Apache Syncope](../integration/Attribute-Resolution-Syncope.html)
      
Furthermore, tenant properties now support [CAS configuration security](../configuration/Configuration-Properties-Security-CAS.html) 
and [Spring expression language](../configuration/Configuration-Spring-Expressions.html).
      
There is dedicated routing support to allow CAS to route requests to the appropriate tenant
internally based on the `Host` http header, in scenarios where CAS is deployed behind a reverse proxy.

## Other Stuff
        
- A new [Heimdall authorization policy](../authorization/Heimdall-Authorization-Overview.html) for SQL databases.
- We have laid the groundwork to begin supporting OpenID Connect federations. Support for this topic will gradually mature as federations begin to operate and remain functional. 
- Apache Tomcat's `RewriteValve` can now be added as an engine valve.
- CAS is now publishing events internally when webflow actions are executed. Such events are recorded into the [CAS event repository](../authentication/Configuring-Authentication-Events.html) and are also available in the [Palantir admin console](../installation/Admin-Dashboard.html).
- Redis integration tests are upgraded to use the latest Redis `8.0` server.
- Support for [ACME Integration](../integration/ACME-Integration.html) is now deprecated.
- The CAS server host name can now be accessed via the user interface and is displayed in the footer.
- The usage criteria of a ticket-granting ticket is now updated when OpenID Connect access tokens are exchanged for a user profile.
- Activation of [Remember-Me functionality](../authentication/Configuring-SSO-Cookie.html) now explicitly looks for the `rememberMe` parameter in the request with a truthy value.
- [Puppeteer tests](../../developer/Test-Process.html) now have the ability to verify CAS functionality using an external Apache Tomcat server.
- The entire CAS configuration catalog is indexed and published online to offer [search functionality](../configuration/Configuration-Properties.html).
- The [BlackDot IP Intelligence](../mfa/Adaptive-Authentication-IP-Intelligence.html) functionality is corrected to create the correct component instance.
- Webflow transitions for multifactor device registration requests are re-organized to allow for this functionality in the [user account profile](../registration/Account-Management-Overview.html).
- [User account profile](../registration/Account-Management-Overview.html) gains the ability to delete registered multifactor authentication devices.
