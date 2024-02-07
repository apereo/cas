---
layout: default
title: CAS - Release Notes
category: Planning
---

# 7.1.0-RC1 Release Notes

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

### Spring Boot 3.3

The migration of the entire codebase to Spring Boot `3.3` is ongoing, and at the 
moment is waiting for the wider ecosystem of supporting frameworks and libraries to catch up to 
changes. We anticipate the work to finalize in the next few release candidates and certainly prior to the final release.

### OpenRewrite Upgrade Recipes

A carry-over item from the previous release and while not exactly new, CAS has started to produce 
and publish [OpenRewrite](https://docs.openrewrite.org/) recipes that allow the project to upgrade installations 
in place from one version to the next. [See this guide](../installation/OpenRewrite-Upgrade-Recipes.html) to learn more.

### Graal VM Native Images

A CAS server installation and deployment process can be tuned to build and run
as a [Graal VM native image](../installation/GraalVM-NativeImage-Installation.html).
The collection of end-to-end [browser tests based on Puppeteer](../../developer/Test-Process.html) have selectively switched
to build and verify Graal VM native images and we plan to extend the coverage to all such scenarios in the coming releases.

### Testing Strategy

The collection of end-to-end [browser tests based on Puppeteer](../../developer/Test-Process.html) continue to grow to cover more use cases
and scenarios. At the moment, total number of jobs stands at approximately `473` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`. Furthermore, a large number of test categories that group internal unit tests
are now configured to run with parallelism enabled.

### Attribute Release Activation Criteria

Attribute release policies can now be [activated conditionally](../integration/Attribute-Release-Policy-Activation.html). 
This feature allows one to define conditions that must be met before the policy is chosen and activated to start processing attribute release rules.

### Deprecations & Removals

- Dedicated GoogleApps support, deprecated since CAS `6.2.x`, is now removed from the CAS codebase.
- The `RegexRegisteredService` service type, deprecated since CAS `6.6.x`, is now removed from the CAS codebase.

### Cerbos Access Strategy

Application access requests and authorization decisions can now be submitted to [Cerbos](../services/Service-Access-Strategy-Cerbos.html) for evaluation.
         
### Stateless Ticket Registry

A new [stateless ticket registry](../ticketing/Stateless-Ticket-Registry.html) option is now available that 
does not track or store tickets in a persistent manner via a backend storage technology. 
  
### Gradle & Devlocity 

Throughout a series of experiments and while working with the Devlocity team, several changes are now made to the CAS Gradle build 
to allow more efficient caching of configuration and build artifacts and reduce overall build times. The Gradle remote build cache
that was previously set up and run on Heroku is now retired and replaced with a dedicated Devlocity server and is now publicly available.

You can see the [build scan results here](https://develocity.apereo.org/). 
 
### OAuth & OpenID Connect
     
A number of notable changes are listed here:

- CAS may not generate and track access tokens if the expiration policy of tokens is set to be `0`. This is useful in scenarios where the relying party does only care about ID tokens (in the case of OpenID Connect) and has no need for access tokens.
- CAS will not create and track an access token for `id_token` grant types.
- CAS will not generate an ID token for OpenID connect authentication request that do not specify the `openid` scope.
- CAS will not create access and refresh tokens if the total number of current access/refresh tokens issued for a service exceeds the limit specified in the application's expiration policy. This limit at the moment is exercised for authorization code flows and will eventually cover other grant types.
- Initial basic support for [OAuth Token Exchange](../authentication/OAuth-ProtocolFlow-TokenExchange.html) protocol is now available.

## Other Stuff

- Internal cleanup and refactoring efforts to remove duplicate code, when it comes to grouping `@AutoConfiguration` components.
- Internal cleanup and refactoring efforts to remove duplicate code for [Puppeteer integration tests](../../developer/Test-Process.html).
- Proxy ticket validation should now correctly resolve and determine the authenticated principal id.
- Cleaning of [throttled authentication attempts](../authentication/Configuring-Authentication-Throttling.html) should now take submission expirations dates into account.
- CAS user interface is now instructed to remove the ["Forgot Your Username?"](../password_management/Password-Management-ForgotUsername.html) feature when the feature is disabled.
- [External/delegated authentication](../integration/Delegate-Authentication.html) flows are improved to better handle throttled authentication requests.
- A new `serviceAccess` [actuator endpoint](../services/Configuring-Service-Access-Strategy.html), allowing one to check CAS authorization decisions for a given service and user.
- [JDBC](../integration/Attribute-Resolution-JDBC.html) and [LDAP](../integration/Attribute-Resolution-LDAP.html) attribute repositories are relocated to their own respective modules and moved away from the person directory module. 

## Library Upgrades

- Spring Framework
- Gradle
- ErrorProne
- Spring Security
- Spring Boot
- Spring Data
- Spring Session
- Spring Cloud
- Twilio
- Pac4j
- Grouper
- Micrometer
- Apache Tomcat
- Sentry
- InfluxDb
- Slack
- Ldaptive
- Node
- Slf4j
- Amazon SDK
- Jetty
- Apache Groovy

