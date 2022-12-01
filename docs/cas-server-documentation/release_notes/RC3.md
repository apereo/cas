---
layout: default
title: CAS - Release Notes
category: Planning
---

# 7.0.0-RC3 Release Notes

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

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.

### JDK Requirement

The JDK baseline requirement for this CAS release is and **MUST** be JDK `17`. All compatible distributions
such as Amazon Corretto, Zulu, Eclipse Temurin, etc should work and are implicitly supported.

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.

### Spring Boot 3

The migration of the entire codebase to Spring Boot `3.0.0` and Jakarta APIs is now complete. This is a major change and upgrade across the board
that affects almost every CAS module and dependency. As a result, a very large number of internal libraries are 
also upgraded to remain compatible These include Spring Data, Spring Security, Spring Cloud, Spring Shell, Pac4j and many many more. 

Switching to Spring Boot also means that CAS has now switched to support [Jakarta EE 10](https://jakarta.ee/release/10/) and 
Servlet specification `6.0.0`. This change does impact supported servlet containers such as Apache Tomcat and Undertow, where 
the minimum supported version is now required to be `10.1.x` and `2.3.x`, accordingly.

Note that Jetty does not support Servlet specification `6.0.0` yet. Deployments that use an embedded Jetty 
servlet container may need to downgrade the version of the Servlet specification manually to `5.0.0`. It is likely that this might 
be sorted out prior to the final GA release by the time Jetty `12` is released.

<div class="alert alert-info"><strong>Usage Warning</strong><p>Remember that this is a major upgrade and may possibly
be somewhat disruptive in the beginning. While most if not all CAS-specific configuration should remain exactly the same, 
you may encounter unexpected hiccups and mishaps along the way. We recommend that you start early by experimenting with 
release candidates and/or follow-up snapshots.</p></div>

## Other Stuff
        
- Support for OpenID Connect `unmet_authentication_requirements` error code is now available.
- Email templates and SMS notification messages for [Simple Multifactor Authentication](../mfa/Simple-Multifactor-Authentication.html) now have access to both 
  `token` and `tokenWithoutPrefix` variables. 
- Negative skew values are now supported for SAML2 responses when skew values are defined for SAML2 registered services.

## Library Upgrades

- Spring
- Spring Boot
- Spring Security
- Spring Data
- Apache Tomcat
- Spring Integration
- Jetty
- Undertow
- Jakarta Servlet API
- Pac4j

