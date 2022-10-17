---
layout: default
title: CAS - Release Notes
category: Planning
---

# 7.0.0-RC2 Release Notes

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

### Testing Strategy

The collection of end-to-end browser tests based on Puppeteer continue to grow to cover more use cases
and scenarios. At the moment, total number of jobs stands at approximately `347` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`.

### Account Registration

The [account registration functionality](../registration/Account-Registration-Overview.html) now allows user accounts
to be provisioned to [Apache Syncope](../registration/Account-Registration-Provisioning-Syncope.html).

## Other Stuff
     
- Client secrets for [OpenID Connect Services](../authentication/OIDC-Authentication-Clients.html) are now URL-decoded before validations.
- A [DynamoDb-based health indicator](../monitoring/Configuring-Monitoring-DynamoDb.html) is available to report back on the health status of
  DynamoDb tables and connections.
- [Git service registry](../services/Git-Service-Management.html) is now able to support rebase operations.
- SSO sessions under [account profile](../registration/Account-Management-Overview.html) can now be selectively removed.
- Authentication attributes can now optionally be included in OpenID Connect ID token or user profile payloads. 

## Library Upgrades

- Spring Boot   
- Apache Tomcat
- Twilio
- Jose4jX
- Apache Ignite
- Apache Shiro
- Netty
- Errorprone
- Jackson
- Hazelcast
- Lettuce
- Micrometer
- Nimbus
- WSS4j
- Hibernate
- Groovy
- HAL Explorer
- Swagger
- Jodatime
- Spring Data
- Azure CosmosDb
