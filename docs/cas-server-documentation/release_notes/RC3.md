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

## System Requirements

The JDK baseline requirement for this CAS release is and **MUST** be JDK `17`. All compatible distributions
such as Amazon Corretto, Zulu, Eclipse Temurin, etc should work and are implicitly supported.

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.

### Spring Boot 3

The migration of the entire codebase to Spring Boot `3.0.0` and Jakarta APIs is now complete. This is a major change and upgrade across the board
that affects almost every CAS module and dependency. As a result, a very large number of internal libraries are 
also upgraded to remain compatible. These include Spring Data, Spring Security, Spring Cloud, Spring Shell, Pac4j and many many more. 

Switching to Spring Boot `3.0.0` also means that CAS has now switched to support [Jakarta EE 10](https://jakarta.ee/release/10/) and 
Servlet specification `6.0.0`. This change does impact supported servlet containers such as Apache Tomcat and Undertow, where 
the minimum supported version is now required to be `10.1.x` and `2.3.x`, accordingly.

- Jetty does not support the servlet specification `6.0.0` yet. Deployments that use an embedded Jetty 
servlet container may need to downgrade the version of the Servlet specification manually to `5.0.0`. It is likely that this might 
be sorted out prior to the final GA release by the time Jetty `12` is released.
- A handful of dependencies and libraries (i.e. OpenSAML, Pac4j, Spring Retry,) have yet to provide a final release version compatible with Spring Boot `3` 
  and/or Jakarta APIs. These should hopefully finalize and publish a GA release in the next few release candidates.  
- Apache BVal has been replaced with Hibernate Validator as the primary library for bean validation. The former provides no support for Jakarta APIs, yet.
- Support for Spring Cloud Sleuth has been removed, and will later on be replaced with Micrometer Tracing.
- The [SCIM 2](https://github.com/pingidentity/scim2) library is replaced with an alternative that supports Jakarta APIs.  

<div class="alert alert-info">:information_source: <strong>Usage Warning</strong><p>Remember that this is a major upgrade and may possibly
be somewhat disruptive in the beginning. While most if not all CAS-specific configuration should remain exactly the same, 
you may encounter unexpected hiccups and mishaps along the way. We recommend that you start early by experimenting with 
release candidates and/or follow-up snapshots. For additional warranties, please see the project license.</p></div>

### Testing Strategy

The collection of end-to-end browser tests based on Puppeteer continue to grow to cover more use cases
and scenarios. At the moment, total number of jobs stands at approximately `377` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`.

### Apache Http Client 5

The migration of the entire codebase to Apache Http Client `5.2.x` is now complete. While this upgrade should remain largely invisible,
it is a major change that affects all CAS components that deal with the HTTP layer or support and interact with REST APIs. 

### Docker Integration Tests

Several Docker images that are used for integration tests are now updated to use more recent versions. Those are:

- MongoDb
- Apache Cassandra
- AWS Localstack
- DynamoDb
- InfluxDb
- MariaDb
- MySQL
- CAS Server
- SCIM Server

### Twilio SendGrid

You may also instruct CAS to use [Twilio SendGrid](../notifications/Sending-Email-Configuration.html) for sending emails.

### CAS Intializr UI

The [CAS Initializr](https://getcas.apereo.org/ui) project now provides a user interface:

<img width="1600" alt="image" src="https://user-images.githubusercontent.com/1205228/205378501-a0e6ad1b-bf81-42ea-8a69-5ad13c0dde39.png">

Note that CAS Initializr is now updated to support generating projects based on JDK `17` and CAS `7`.
     
### Passwordless Authentication

A significant amount of work has been done to allow the [passwordless authentication](../authentication/Passwordless-Authentication.html)
to support impersonation, via its own token-based traditional flow as well as delegation authentication flows. Puppeteer tests are made available
to cover quite a number of scenarios and use cases.
  
### OpenID Connect JWKS

The ability to manage and store [OpenID Connect JWKS](../authentication/OIDC-Authentication-JWKS-Storage.html) resources is now split
to support relational databases and MongoDb separately via dedicated extension modules.

### SAML2 Delegated Authentication Metadata

SAML2 service provider metadata used and managed during [delegated authentication](../integration/Delegate-Authentication-SAML.html) 
can now be stored in MongoDb.

## Other Stuff
   
- The session cookie (i.e. `DISSESSION`) typically used for distributed session management when CAS is acting as a SAML2 identity provider can now be signed 
  and encrypted. 
- The [Groovy username provider](../integration/Attribute-Release-PrincipalId-Groovy.html) is now able to support better caching techniques. 
- Support for OpenID Connect `unmet_authentication_requirements` error code is now available.
- Email templates and SMS notification messages for [Simple Multifactor Authentication](../mfa/Simple-Multifactor-Authentication.html) now have access to both 
  `token` and `tokenWithoutPrefix` variables. 
- Additional options to control [logging stacktraces in summary mode](../logging/Logging.html) are now available.
- Negative skew values are now supported for SAML2 responses when skew values are defined for SAML2 registered services.
- Multiple email attribute names can now be specified in the configuration, when locating email addresses for principals.
- The [Return Allowed](../integration/Attribute-Release-Policy-ReturnAllowed.html) attribute release policy is now able to support inline Groovy scripts.
- Dependency and module versions used by CAS [are now documented](../installation/Dependency-Management-Versions.html). 
- Small documentation improvements to ensure default values for settings are picked up dynamically and correctly as much as possible.

## Library Upgrades

- Spring
- Spring Boot
- Spring Security
- Spring Data
- Apache Tomcat
- Spring Integration
- Spring Shell
- Spring Retry
- Spring Cloud
- Jetty
- Undertow
- Slf4j
- Logback
- Jakarta Servlet API
- Pac4j
- Apache Http Client
- Apache CXF
- Classgraph
- Hibernate
- Hibernate Validator
- OpenSAML
- Apache CXF
- Nimbus
- Person Directory
- Spring Webflow
- Inspektr
- Amazon SDK
