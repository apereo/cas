---
layout: default
title: CAS - Release Notes
category: Planning
---

# 7.2.0-RC3 Release Notes

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

The migration of the entire codebase to Spring Boot `3.4.x` is complete, and most if not all libraries and
supporting frameworks have shown to be compatible. There may be slight glitches here and there but for the most part,
CAS is now ready to take advantage of the latest and greatest features of Spring Boot `3.4.x`.

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
and scenarios. At the moment, the total number of jobs stands at approximately `506` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`. 

### Java 23

CAS is now able to build and run using Java `23`. As described, the JDK baseline requirement for this CAS release is and **MUST** be JDK `21`. 
This is just a preparatory step to ensure CAS is ready for the next versions of Java.
       
### Delegate Authentication Providers
                 
The configuration of [delegated authentication providers](../integration/Delegate-Authentication-Provider-Registration.html) 
is now extended to support SQL databases.

### SAML2 IdP Metadata via AWS S3

[SAML2 metadata via AWS S3](../installation/Configuring-SAML2-DynamicMetadata-AmazonS3.html) has changed its strategy 
for metadata storage to no longer track certificate and keys for signing and encryption operations as part of 
the *S3 object's metadata*, which may cause failures due to size limitations. Instead, all SAML2 metadata elements are put 
inside the S3 object's content as a JSON document.

<div class="alert alert-warning">:warning: <strong>Breaking Change</strong><p>
This may be a breaking change. Consult the documentation to learn more.</p></div>

## Other Stuff
     
- Performance tests are now available based on the [Artillery](../high_availability/Performance-Testing-Artillery.html) framework.
- A dedicated metric, `slow.requests.timer`, is now available once system health monitoring is enabled to track slow requests that take longer than 5 seconds.
- Additional [theme property](../ux/User-Interface-Customization-Themes-Static.html) to determine whether CAS version details should be displayed in the page footer.
- [Multifactor provider selection](../mfa/Configuring-Multifactor-Authentication-Triggers.html) is set to utilize ranking strategies when multiple competing MFA triggers vote for different MFA providers.
- A new configuration option to control whether [JWT access tokens](../authentication/OAuth-Authentication.html) should include additional attributes and claims beyond the standard claims.
- [DynamoDb ticket registry](../ticketing/DynamoDb-Ticket-Registry.html) is adjusted to not track attributes with empty values when storing tickets in string sets.
- CAS may not generate refresh tokens if the expiration policy for refresh tokens is set to zero.
- Redirecting to a destination URL after [CAS logout](../installation/Logout-Single-Signout.html) is now remembered as a `TST` prior to sending SAML2 logout requests to external identity providers.
- Actuator endpoints can be secured using a static JSON file that may contain user details and roles.
- Support for [Redis modules via LettuceMod](../ticketing/Redis-Ticket-Registry-RediSearch.html) is now extracted into a dedicated module.
- Scratch codes may also be used to verify an account during the [Google Authenticator](../mfa/GoogleAuthenticator-Authentication.html) registration flow.
- Tickets captured via [JPA ticket registry](../ticketing/JPA-Ticket-Registry.html) will also track the ticket expiration time and the last-used time.
- [Passwordless accounts](../authentication/Passwordless-Authentication.html) can be now customized and post-processed once fetched from the passwordless account store.  
- [MongoDb Ticket Registry](../ticketing/MongoDb-Ticket-Registry.html) has now removed a "text" index on the full json contents of the ticket to improve performance.
- Managing tokens for [Simple Multifactor Authentication via REST](../mfa/Simple-Multifactor-Authentication-TokenManagement-REST.html) has switched the HTTP method to `POST` in some cases to better align with RESTful principles. 
- Various small performance improvements and documentation updates. 

## Library Upgrades
       
- Gradle
- Spring Boot
- Spring
- Spring Security
- Spring Integration
- Spring Shell
- JaVers
- Groovy
- Swagger
- JGit
- Spring Cloud
- Spring Cloud GCP
- Apache Kafka
- MongoDb Driver
- Oracle Driver
- Hibernate
- Apache Tomcat
- Spring Data
- Jetty
- SpringDoc
- Quartz
- Hikari
- Lombok
- Pac4j
- Spring Boot Admin
- Micrometer
- Twilio
- Sentry
- Elastic APM
- Google Cloud Logging
