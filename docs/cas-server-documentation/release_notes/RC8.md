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

With JDK `21` as the baseline platform, frameworks such as Spring and Spring Boot start to support virtual threads. 
To use virtual threads, CAS automatically sets the property `spring.threads.virtual.enabled` to `true`. When virtual threads are enabled, 
Tomcat and Jetty will use virtual threads for request processing. This means that when CAS is handling a web request, 
that request will run on a virtual thread.
 
Furthermore, the CAS codebase has made modest and small efforts where possible to either use virtual threads, or to
adjust parts of the codebase that would assist with request execution and handling inside virtual threads. These efforts
mainly include the following:

- Removing `synchronized` blocks and constructs and replacing with the locking that is more appropriate for virtual threads without thread pinning.
- Replacing `@Synchronized` annotations with locking constructs, more appropriate for virtual threads.
- Removing references to `ThreadLocal` as much as possible.

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

### Graal VM Native Images

A CAS server installation and deployment process can be tuned to build and run
as a [Graal VM native image](../installation/GraalVM-NativeImage-Installation.html).
The collection of end-to-end [browser tests based on Puppeteer](../../developer/Test-Process.html) have selectively switched
to build and verify Graal VM native images and we plan to extend the coverage to all such scenarios in the coming releases.

[CAS Initializr](../installation/WAR-Overlay-Initializr.html) is also modified to support Graal VM native images.
 
### Weak Password Detection

[Password Management facilities](../password_management/Password-Management.html) in CAS are able to intercept the user's password after a successful authentication attempt
to evaluate its strength. Passwords detected as weak or insure then force the flow and the user to update and reset their password.

### Testing Strategy

The collection of end-to-end [browser tests based on Puppeteer](../../developer/Test-Process.html) continue to grow to cover more use cases
and scenarios. At the moment, total number of jobs stands at approximately `436` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`. Furthermore, a large number of test categories that group internal unit tests
are now configured to run with parallelism enabled.

## Other Stuff
                          
- U2F functionality is removed from the CAS codebase and is no longer supported. The [underlying library](https://github.com/Yubico/java-u2flib-server) provided by Yubico has been deprecated and archived since 2022.
- Likewise, Authy multifactor authentication support is removed from the CAS codebase and is no longer supported. 
- Authentication throttling support in CAS is now extended to SAML2 identity provider endpoints and functionality.
- OpenID Connect claims can now be [optionally decorated](../authentication/OIDC-Attribute-Definitions.html) to mark an attribute as a structured claim.
- Following the recommendations of the [OAuth Security Workshop](https://oauth.secworkshop.events/osw2023), the validation rules of `redirect_uri` parameters are now tightened to ensure the parameter value does not have URL fragments, invalid schemes such as `javascript` or `data` or suspicious parameters such as `code`, `state`, etc.
- [Risk-based authentication](../authentication/Configuring-RiskBased-Authentication.html) now supports a *Risk Confirmation* flow via a special link sent to the user via email, sms, etc.
- Multifactor provider selection is now skipped when a valid [multifactor-enabled trusted device](../mfa/Multifactor-TrustedDevice-Authentication.html) is found for the user record.
- The WebSDK variation of [Duo Security Multifactor Authentication](../mfa/DuoSecurity-Authentication.html) is now removed.
- Deployments that run on top of Jetty can now switch to Jetty `12` and the CAS-supplied Jetty container has also made the switch to Jetty `12`.

## Library Upgrades
   
- Spring Boot
- Nimbus OIDC
- Spring Boot Admin Server
- SnakeYAML
- Spring Cloud
- Spring Security
- Spring Integration
- Spring Framework
- Spring Shell
- Spring AMQP
- Apache Tomcat
- Gradle
- Hazelcast
- Oshi
- Apache Ignite
- Pac4j
- OpenSAML
- Hibernate
- Jetty
