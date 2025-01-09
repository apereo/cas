---
layout: default
title: CAS - Release Notes
category: Planning
---

# 7.2.0-RC4 Release Notes

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
and scenarios. At the moment, the total number of jobs stands at approximately `508` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`.
   
### WebAuthN via QR Codes

CAS can be configured to support FIDO2 WebAuthn authentication using QR codes. Once 
enabled, [this feature](../mfa/FIDO2-WebAuthn-Authentication-QRCode.html) allows users to authenticate
using a secondary FIDO2-enabled secondary device by scanning a QR code presented by CAS.
  
### Passwordless Authentication Selection

[Passwordless Authentication](../authentication/Passwordless-Authentication-UserSelectionMenu.html) at the direction of the
account store can now be instructed to allow the user to select from a menu of available authentication options.
   
### Mailgun Integration

Support for [Mailgun](../notifications/Sending-Email-Configuration-Mailgun.html) is now available for sending email messages.

### SAML2 Metadata via DynamoDb

SAML2 metadata for service providers and CAS as the identity provider 
can now be stored and fetched from [Amazon DynamoDb](../installation/Configuring-SAML2-DynamicMetadata-DynamoDb.html).

### Google Cloud Storage Service Registry

CAS can now store service and application definitions in [Google Cloud Storage](../services/GCP-Storage-Service-Management.html).

### Database Authentication via Stored Procedures

CAS can now authenticate users by invoking [stored procedures](../authentication/Database-Authentication-StoredProcedure.html) in SQL databases.

## Other Stuff
   
- [OAuth Refresh Tokens](../authentication/OAuth-Authentication-Clients.html) can optionally be generated as JWTs. 
- [Email notifications](../notifications/Sending-Email-Configuration.html) now test the connection to the email server before sending the actual message.

## Library Upgrades

- Spring Boot
- Spring
- Apache Tomcat
- Java Melody
- Logback
- Spring Integration
- Apache CXF
- Spring Retry
- Zipkin Brave
- Spring Shell
- Micrometer
- Thymeleaf
- Amazon SDK
- Jetty
- Gradle
- Spring Data
- Apache Log4j
- Apache CXF
- Hibernate
- Sentry
- Spring Session
- GCP Logging
