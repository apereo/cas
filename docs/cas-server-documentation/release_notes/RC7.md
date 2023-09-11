---
layout: default
title: CAS - Release Notes
category: Planning
---

# 7.0.0-RC7 Release Notes

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

The JDK baseline requirement for this CAS release is and **MUST** be JDK `17`. All compatible distributions
such as Amazon Corretto, Zulu, Eclipse Temurin, etc should work and are implicitly supported.

Please note that CAS CI builds via GitHub Actions are now switched to build against latest EA releases of JDK `21`, in addition to JDK `17`.
Given the release schedule for JDK `21` in September 2023, it is likely that CAS `v7` would switch to using JDK `21` as its baseline
in the next few release candidates and likely for the final GA release later in the year.

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.

### Testing Strategy

The collection of end-to-end [browser tests based on Puppeteer](../../developer/Test-Process.html) continue to grow to cover more use cases
and scenarios. At the moment, total number of jobs stands at approximately `422` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`.

### Graal VM Native Images

A CAS server installation and deployment process can be tuned to build and run 
as a [Graal VM native image](../installation/GraalVM-NativeImage-Installation.html). 
The collection of end-to-end [browser tests based on Puppeteer](../../developer/Test-Process.html) have selectively switched
to build and verify Graal VM native images and we plan to extend the coverage to all such scenarios in the coming releases.

[CAS Initializr](../installation/WAR-Overlay-Initializr.html) is also modified to support Graal VM native images.

### AWS Verified Permissions

A new service access strategy is now available to authorize application access based on 
[AWS Verified Permissions](../services/Service-Access-Strategy-AWS-VerifiedPermissions.html).

### Google Authenticator Device Registration

When using Google Authenticator as a multifactor authentication provider, the workflow for device registration
is slightly altered to now require a successful authentication event before additional devices can be registered
by the user. This change is only applicable if CAS is configured to allow multiple device registrations.

### Password Reset with Multifactor Authentication

Password reset operations will require and activate multifactor authentication before
password reset instructions can be shared with the end-user. This behavior is configurable 
and the setting is turned on by default.

### Azure Maps & Geotracking

Initial support is now available to support Azure Maps to geolocate authentication requests.
[See this](../authentication/GeoTracking-Authentication-AzureMaps.html) for more info.

### Docker-based Integration Tests

The following docker containers, used for integration tests, are now upgraded:

- AWS LocalStack
- Apereo CAS
- Apache Cassandra
- AWS DynamoDb
- Elastic APM
- Internet2 Grouper
- MariaDb
- MongoDb
- Redis
- SCIM
- Apache Syncope
      
### Attribute-based SSO Participation

A new [single sign-on participation policy](../services/Configuring-Service-SSO-Policy.html) is now available
that can control SSO participation on a per-application basis based on presence or absence of user attributes. 

### Google Authenticator w/ Redis

Support for [Google Authenticator backed by Redis](../mfa/GoogleAuthenticator-Authentication-Registration-Redis.html) is now reworked
to remove a number of performance issues, all of which have had to do with redis `SCAN` operations and the use of wildcard in query patterns.
Performance fixes alter the structure of records and keys and as a result are not backwards compatible. You will need to export existing records
from redis and then import them back via CAS-provided facilities and dedicated endpoints.
                                                                                                                                                    
### Releasing OpenID Connect Claims

The claim release rules for specific grant types, namely `client_credentials` and `password`, are revisited to ensure claims, whether custom or standard,
can be correctly released based on the scopes authorized by the service definition as well as those requested by the relying party in authorization and token requests.

### JWT Response for OAuth Token Introspection

OAuth Token Introspection responses can now be [produced as JWTs](../authentication/OAuth-Authentication-TokenIntrospection.html).

## Other Stuff

- LDAP direct binds are now able to resolve person attributes after authentication.
- Logout events are now sent to the CAS audit log.
- [Renovate](https://docs.renovatebot.com/) is now turned on to scan the codebase for dependency updates. Renovate support is also available in [CAS Initializr](../installation/WAR-Overlay-Initializr.html).
- OpenID Connect JWKS are now able to support an arbitrary number of keys for signing operations.
- OpenID Connect Standard claims can be individually filtered on a per relying party.
- New CAS actuator endpoints are available to simulate [CAS Protocol responses](../protocol/CAS-Protocol.html).
- CAS documentation has switched to use a Dark Theme by default to make life slightly easier on the eyes.
- Many small improvements around auditing facilities, making it easier internally to capture and/or tweak audited fields recorded by the audit log.
- [JWT Authentication](../authentication/JWT-Authentication.html) is now able to work with and support RSA public/private keypairs.
- CAS is now able to conditionally geolocate client sessions when building and pinning [SSO Cookies](../authentication/Configuring-SSO-Cookie.html).
- [Memcached functionality and overall support](../ticketing/Memcached-Ticket-Registry.html) is now deprecated, and is scheduled to be removed in future releases.
- CAS server is now able to act as a [Spring Boot Admin Server](../monitoring/Configuring-SpringBootAdmin.html) itself. 

## Library Upgrades

- Twilio
- Hazelcast
- Gradle
- Apache Tomcat
- Apache Shiro
- Sentry
- Apache CXF
- Oshi
- Logback
- MongoDb
- Nimbus OIDC
- Swagger
- Apache Kafka
- Micrometer
- Elastic APM
- Graal VM
- Hibernate
- BouncyCastle
- Google Cloud
- Spring Boot
- Spring WS
- Spring Framework
- Spring Kafka
- Spring Integration
- Spring RabbitMQ
- Lettuce
- jQuery
- Apache ZooKeeper
