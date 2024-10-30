---
layout: default
title: CAS - Release Notes
category: Planning
---

# 7.2.0-RC2 Release Notes

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
 
### Heimdall Authorization Engine

[Heimdall](../authorization/Heimdall-Authorization-Overview.html) is a simple rule-based authorization 
engine whose main responsibility is to accept an authorization request
in form of an HTTP payload and return a decision whether the request is allowed or denied in form of an HTTP response code.

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
and scenarios. At the moment, total number of jobs stands at approximately `497` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`. Furthermore, a large number of test categories that group internal unit tests
are now configured to run with parallelism enabled.

### Java 23

As described, the JDK baseline requirement for this CAS release is and **MUST** be JDK `21`. We are still waiting for the
wider ecosystem of supporting frameworks and libraries to catch up to Java `23`. We anticipate the work to finalize in the next few
release candidates and certainly prior to the final release. Remember that the baseline requirement will remain unchanged
and this is just a preparatory step to ensure CAS is ready for the next version of Java.
 
### Actuator Endpoints

As part of upgrading to Spring Boot `3.4`, the security configuration of actuator endpoints have been reworked with the following
changes from Spring Boot itself:

Support for enabling and disabling endpoints is replacing the on/off support that it provided with a 
finer-grained access model that supports only allowing read-only access to endpoint operations in addition to 
disabling an endpoint (access of `NONE`) and fully enabling it (access of `UNRESTRICTED`).

The following properties have been deprecated:
                                       
```properties
management.endpoints.enabled-by-default
management.endpoint.<id>.enabled
```

Their replacements are:
                                   
```properties
management.endpoints.access.default
management.endpoint.<id>.access
```

## Other Stuff
     
- Failures to write to session/local storage via Javascript are now reported back to the CAS user interface.
- `log4j-spring-boot` designed to support Spring Boot `2.x` is now removed from CAS.
- In the event that the SAML2 authentication request cannot be retrieved and restored, an appropriate error message is now produced in the CAS user interface. 
- There is now a [dedicated configuration source](../configuration/Configuration-Properties-Security-DockerSecrets.html) to pull CAS properties from Docker secrets. 
- Storing trusted multifactor devices [using MongoDb](../mfa/Multifactor-TrustedDevice-Authentication-Storage-MongoDb.html) can now assign a correct ID to the device record.
- Additional protections around proxy tickets and proxy-granting tickets are now in place to prevent validation errors under high load.
- [Authentication interrupt tracking cookies](../webflow/Webflow-Customization-Interrupt-Tracking.html) are now removed from the browser when the user logs out of CAS. 
- SMS messages sent by [passwordless authentication](../authentication/Passwordless-Authentication-Notifications.html) now correctly record the generated token id.
- A dedicated [actuator endpoint](../password_management/Password-Management.html) to allow CAS to reset the user's password and kickstart the password reset flow. 
- The auto configuration for the embedded Apache Tomcat can now enable the [Session Initialization Filter](https://tomcat.apache.org/tomcat-10.1-doc/api/org/apache/catalina/filters/SessionInitializerFilter.html)
- Small modifications to the `GoogleCloudAppender` to capture location information and encode JSON objects better. 
- The login flow now has access to the http request headers via the `httpRequestHeaders` context variable. 

## Library Upgrades
          
- Twilio
- Google Cloud Logging
- Google Cloud SDK
- Spring Boot Admin
- Spring
- Spring Retry
- Spring Data
- Grouper
- Apache Tomcat
- Logback
- LettuceMod
- Kryo
- Mockito
- MySQL Driver
- Nimbus
- Spring Boot
- Gradle
- Sentry
- ZooKeeper
- MariaDb
- Lombok
